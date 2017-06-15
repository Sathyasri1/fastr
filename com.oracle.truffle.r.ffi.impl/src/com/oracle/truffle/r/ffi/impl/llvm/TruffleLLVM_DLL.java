/*
 * Copyright (c) 2016, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.r.ffi.impl.llvm;

import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.r.runtime.RInternalError;
import com.oracle.truffle.r.runtime.context.RContext;
import com.oracle.truffle.r.runtime.context.RContext.ContextState;
import com.oracle.truffle.r.runtime.ffi.DLL;
import com.oracle.truffle.r.runtime.ffi.DLL.DLLInfo;
import com.oracle.truffle.r.runtime.ffi.DLL.DotSymbol;
import com.oracle.truffle.r.runtime.ffi.DLL.SymbolHandle;
import com.oracle.truffle.r.runtime.ffi.DLLRFFI;
import com.oracle.truffle.r.runtime.ffi.NativeCallInfo;

/**
 * The Truffle version of {@link DLLRFFI}. {@code dlopen} expects to find the LLVM IR embedded in
 * the shared library.
 *
 * The LLVM bitcode is stored (opaquely) in the shared library file, and access through the
 * {@link LLVM_IR} class. The {@link LLVM_IR#getLLVMIR(String)} method will return an array of
 * {@link LLVM_IR} instances for all the modules in the library. These have to be parsed by the
 * Truffle LLVM system (into ASTs) before they can be interpreted/compiled. Note that there is no
 * support in Truffle LLVM itself for resolving references to functions in other LLVM modules. If
 * the function as an LLVM AST cannot be found it is assumed to be a native function. The upshot of
 * this is that, naively, every module in a library has to be parsed before any modules can be
 * executed, and inter-library dependencies also have to be handled explicitly. Most of the
 * inter-library references are to "libR", which is handled specially. If parsing was fast eager
 * parsing of all modules would not be an issue but, currently, is it not fast and some modules
 * exhibit pathologies that can take as long as a minute to parse, so some effort to support lazy
 * parsing has been implemented. Unfortunately this requires additional metadata. Note also that
 * only functions that are invoked explicitly through on of the RFFI interfaces can be handled
 * lazily; any internal calls must already be resolved (this would change if LLVM had the callback
 * facility alluded to above).
 *
 * This code can operate with lazy or eager parsing, but additional metadata has to be provided on
 * the defined/undefined symbols in a module.
 *
 * There is one major difference between native and LLVM libraries. There is a single global
 * instance of a native library and the symbols are, therefore, accessible from any {@link RContext}
 * instance . However, the (Truffle) function descriptors for an LLVM library are specific to the
 * {@link RContext} they are created (parsed) in. This has two important consequences:
 * <ol>
 * <li>It is theoretically possible to have different versions of libraries in different contexts.
 * </li>
 * <li>The {@code libR} library function descriptors must be made available in every context. At the
 * present time this can only be done by re-parsing the library contents.</li>
 * </ol>
 *
 */
class TruffleLLVM_DLL implements DLLRFFI {
    /**
     * Supports lazy parsing of LLVM modules.
     */
    static class ParseStatus {
        /**
         * Name of associated library.
         */
        final String libName;
        /**
         * The LLVM IR (bitcode).
         */
        final LLVM_IR ir;
        /**
         * {@code true} iff the bitcode has been parsed into a Truffle AST.
         */
        boolean parsed;

        ParseStatus(String libName, LLVM_IR ir, boolean parsed) {
            this.libName = libName;
            this.ir = ir;
            this.parsed = parsed;
        }

        @Override
        public String toString() {
            CompilerAsserts.neverPartOfCompilation();
            return String.format("lib %s, module %s, parsed %b%n", libName, ir.name, parsed);
        }
    }

    static class ContextStateImpl implements RContext.ContextState {
        /**
         * A map from function name to its {@link ParseStatus}, allowing fast determination whether
         * parsing is required in a call, see {@link #ensureParsed}. N.B. parsing happens at the
         * module level, so all exported functions in one module share the same {@link ParseStatus}
         * instance.
         */
        Map<String, ParseStatus> parseStatusMap = new HashMap<>();

        /**
         * When a new {@link RContext} is created we have to re-parse the libR modules,
         * unfortunately, as there is no way to propagate the LLVM state created in the initial
         * context. TODO when do we really need to do this? This is certainly too early for contexts
         * that will not invoke LLVM code (e.g. most unit tests)
         */
        @Override
        public ContextState initialize(RContext context) {
            if (!context.isInitial()) {
                for (LLVM_IR ir : truffleDLL.libRModules) {
                    addExportsToMap(this, "libR", ir, (name) -> name.endsWith("_llvm"));
                }
            }
            if (context.getKind() == RContext.ContextKind.SHARE_PARENT_RW) {
                // must propagate all LLVM library exports
                ArrayList<DLLInfo> loadedDLLs = DLL.getLoadedDLLs();
                for (DLLInfo dllInfo : loadedDLLs) {
                    if (dllInfo.handle instanceof LLVM_Handle) {
                        LLVM_Handle llvmHandle = (LLVM_Handle) dllInfo.handle;
                        for (LLVM_IR ir : llvmHandle.irs) {
                            addExportsToMap(this, llvmHandle.libName, ir, (name) -> true);
                        }
                    }
                }
            }
            return this;
        }

        @Override
        public void beforeDestroy(RContext context) {
            if (!context.isInitial()) {
                parseStatusMap = null;
            }
        }
    }

    private static TruffleLLVM_DLL truffleDLL;

    TruffleLLVM_DLL() {
        assert truffleDLL == null;
        truffleDLL = this;
    }

    static ContextStateImpl newContextState() {
        return new ContextStateImpl();
    }

    static class LLVM_Handle {
        private final String libName;
        private final LLVM_IR[] irs;

        LLVM_Handle(String libName, LLVM_IR[] irs) {
            this.libName = libName;
            this.irs = irs;
        }
    }

    @FunctionalInterface
    interface ModuleNameMatch {
        boolean match(String name);
    }

    private static class TruffleLLVM_DLOpenNode extends DLOpenNode {
        @Child private TruffleLLVM_NativeDLL.TruffleLLVM_NativeDLOpen nativeDLLOpenNode;

        /**
         * If a library is enabled for LLVM, the IR for all the modules is retrieved and analyzed.
         * Every exported symbol in the module added to the parseStatus map for the current
         * {@link RContext}. This allows {@code dlsym} to definitively locate any symbol, even if
         * the IR has not been parsed yet.
         */
        @Override
        public Object execute(String path, boolean local, boolean now) {
            try {
                LLVM_IR[] irs = LLVM_IR.getLLVMIR(path);
                if (irs == null) {
                    return tryOpenNative(path, local, now);
                }
                String libName = getLibName(path);
                // libR can't be parsed at this stage in the startup
                if (libName.equals("libR")) {
                    truffleDLL.libRModules = irs;
                }
                ContextStateImpl contextState = getContextState();
                for (int i = 0; i < irs.length; i++) {
                    LLVM_IR ir = irs[i];
                    addExportsToMap(contextState, libName, ir, libName.equals("libR") ? (name) -> name.endsWith("_llvm") : (name) -> true);
                }
                return new LLVM_Handle(libName, irs);
            } catch (Exception ex) {
                throw new UnsatisfiedLinkError(ex.getMessage());
            }
        }

        private long tryOpenNative(String path, boolean local, boolean now) throws UnsatisfiedLinkError {
            if (nativeDLLOpenNode == null) {
                nativeDLLOpenNode = insert(new TruffleLLVM_NativeDLL.TruffleLLVM_NativeDLOpen());
            }
            return nativeDLLOpenNode.execute(path, local, now);
        }
    }

    private static class TruffleLLVM_DLSymNode extends DLSymNode {
        @Override
        public SymbolHandle execute(Object handle, String symbol) throws UnsatisfiedLinkError {
            assert handle instanceof LLVM_Handle;
            // If the symbol exists it will be in the map
            ParseStatus parseStatus = getContextState().parseStatusMap.get(symbol);
            if (parseStatus != null && parseStatus.libName.equals(((LLVM_Handle) handle).libName)) {
                // force a parse so we have a "value"
                if (!parseStatus.parsed) {
                    ensureParsed(parseStatus.libName, symbol, true);
                }
                Object symValue = RContext.getInstance().getEnv().importSymbol("@" + symbol);
                assert symValue != null;
                return new SymbolHandle(symValue);
            } else {
                // symbol not found (or not in requested library)
                throw new UnsatisfiedLinkError();
            }
        }
    }

    private static class TruffleLLVM_DLCloseNode extends DLCloseNode {
        @Override
        public int execute(Object handle) {
            assert handle instanceof LLVM_Handle;
            return 0;
        }

    }

    @Override
    public DLOpenNode createDLOpenNode() {
        return new TruffleLLVM_DLOpenNode();
    }

    @Override
    public DLSymNode createDLSymNode() {
        return new TruffleLLVM_DLSymNode();
    }

    @Override
    public DLCloseNode createDLCloseNode() {
        return new TruffleLLVM_DLCloseNode();
    }

    private static void addExportsToMap(ContextStateImpl contextState, String libName, LLVM_IR ir, ModuleNameMatch moduleNameMatch) {
        ParseStatus parseStatus = new ParseStatus(libName, ir, false);
        for (String export : ir.exports) {
            if (moduleNameMatch.match(ir.name)) {
                ParseStatus exportParseStatus = contextState.parseStatusMap.get(export);
                // libR and graphics both define the same symbols (which should be fixed)
                if (exportParseStatus == null) {
                    contextState.parseStatusMap.put(export, parseStatus);
                }
            }
        }
    }

    private static String getLibName(String path) {
        String fileName = FileSystems.getDefault().getPath(path).getFileName().toString();
        int ix = fileName.lastIndexOf(".");
        return fileName.substring(0, ix);
    }

    /**
     * Record of the libR modules for subsequent parsing.
     */
    private LLVM_IR[] libRModules;

    private static ContextStateImpl getContextState() {
        return TruffleLLVM_RFFIContextState.getContextState().dllState;
    }

    /**
     * About to invoke the (external) function denoted by {@code nativeCallInfo}. Therefore, it must
     * have been parsed (in {code dlsym}) AND all dependent modules, recursively, must also be
     * parsed. Evidently since the dependencies are expressed at a module level, this may parse more
     * than strictly necessary.
     *
     * @param nativeCallInfo
     */
    static void ensureParsed(NativeCallInfo nativeCallInfo) {
        ensureParsed(nativeCallInfo.dllInfo.name, nativeCallInfo.name, true);
    }

    private static final String[] MustEagerlyParsePackages = new String[]{"grDevices:colors", "graphics:registerBase"};

    private static boolean mustEagerlyParse(String libName, String moduleName) {
        for (String pkg : MustEagerlyParsePackages) {
            String[] parts = pkg.split(":");
            if (parts[0].equals(libName) && parts[1].equals(moduleName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called from {@link TruffleLLVM_PkgInit} to catch internal (aka non-exported) symbols that may
     * nevertheless be invoked.
     */
    static void registerSymbols(DLLInfo dllInfo, DotSymbol[] dotSymbols) {
        ContextStateImpl contextState = getContextState();
        Map<String, ParseStatus> parseStatusMap = contextState.parseStatusMap;
        for (DotSymbol ds : dotSymbols) {
            ParseStatus parseStatus = parseStatusMap.get(ds.name);
            if (parseStatus == null) {
                parseStatusMap.put(ds.name, new ParseStatus(dllInfo.name, null, true));
            }
        }
    }

    /**
     * Similar to {@link #ensureParsed(NativeCallInfo)} but with a function specified as a string
     * (for internal use) and an optional check whether the function must exist.
     */
    @TruffleBoundary
    static void ensureParsed(String libName, String name, boolean fatalIfMissing) {
        ContextStateImpl contextState = getContextState();
        Map<String, ParseStatus> parseStatusMap = contextState.parseStatusMap;
        ParseStatus parseStatus = parseStatusMap.get(name);
        assert parseStatus != null || !fatalIfMissing;
        if (parseStatus != null && !parseStatus.parsed) {
            parseLLVM(libName, parseStatus.ir);
            parseStatus.parsed = true;
            boolean isPackageInit = isPackageInit(libName, name);
            for (String importee : parseStatus.ir.imports) {
                /*
                 * If we are resolving a package init call, we do not want to resolve all the
                 * imports of functions in the same library as this will cause everything in the
                 * library to be parsed eagerly! However, any package that actually invokes an
                 * imported symbol in the R_init_package function must do eager parsing.
                 * Unfortunately, we can't easily tell if that is the case, so we have a backlist of
                 * known problem packages.
                 */
                ParseStatus importeeParseStatus = parseStatusMap.get(importee);
                boolean internal = isPackageInit && importeeParseStatus.libName.equals(libName);
                if (importeeParseStatus != null && (!internal || mustEagerlyParse(libName, importee))) {
                    ensureParsed(importeeParseStatus.libName, importee, false);
                }
            }
        }
    }

    private static boolean isPackageInit(@SuppressWarnings("unused") String libName, String name) {
        if (name.startsWith(DLL.R_INIT_PREFIX)) {
            return true;
        } else {
            return false;
        }
    }

    private static void parseLLVM(String libName, LLVM_IR ir) {
        if (ir instanceof LLVM_IR.Binary) {
            LLVM_IR.Binary bir = (LLVM_IR.Binary) ir;
            parseBinary(libName, bir);
        } else {
            throw RInternalError.unimplemented("LLVM text IR");
        }
    }

    private static CallTarget parseBinary(String libName, LLVM_IR.Binary ir) {
        long start = System.nanoTime();
        RContext context = RContext.getInstance();
        long nanos = 1000 * 1000 * 1000;
        Source source = Source.newBuilder(ir.base64).name(ir.name).mimeType("application/x-llvm-ir-bitcode-base64").build();
        CallTarget result = context.getEnv().parse(source);
        if (System.getenv("LLVM_PARSE_TIME") != null) {
            long end = System.nanoTime();
            System.out.printf("parsed %s:%s in %f secs%n", libName, ir.name, ((double) (end - start)) / (double) nanos);
        }
        return result;
    }

}