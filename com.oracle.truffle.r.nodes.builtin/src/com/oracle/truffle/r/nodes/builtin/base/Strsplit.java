/*
 * Copyright (c) 2013, 2015, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.nodes.builtin.base;

import static com.oracle.truffle.r.runtime.RBuiltinKind.*;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.utilities.*;
import com.oracle.truffle.r.nodes.*;
import com.oracle.truffle.r.nodes.access.*;
import com.oracle.truffle.r.nodes.builtin.*;
import com.oracle.truffle.r.runtime.*;
import com.oracle.truffle.r.runtime.data.*;
import com.oracle.truffle.r.runtime.data.model.*;
import com.oracle.truffle.r.runtime.ops.na.*;

@RBuiltin(name = "strsplit", kind = INTERNAL, parameterNames = {"x", "split", "fixed", "perl", "useBytes"})
public abstract class Strsplit extends RBuiltinNode {

    private final NACheck na = NACheck.create();
    private final ConditionProfile emptySplitProfile = ConditionProfile.createBinaryProfile();

    @Override
    public RNode[] getParameterValues() {
        // x, split, fixed = FALSE, perl = FALSE, useBytes = FALSE
        return new RNode[]{ConstantNode.create(RMissing.instance), ConstantNode.create(RMissing.instance), ConstantNode.create(RRuntime.LOGICAL_FALSE), ConstantNode.create(RRuntime.LOGICAL_FALSE),
                        ConstantNode.create(RRuntime.LOGICAL_FALSE)};
    }

    @SuppressWarnings("unused")
    @Specialization
    protected RList split(RAbstractStringVector x, String split, byte fixed, byte perl, byte useBytes) {
        controlVisibility();
        RStringVector[] result = new RStringVector[x.getLength()];
        na.enable(x);
        if (emptySplitProfile.profile(split.isEmpty())) {
            for (int i = 0; i < x.getLength(); ++i) {
                String data = x.getDataAt(i);
                result[i] = na.check(data) ? RDataFactory.createNAStringVector() : emptySplitIntl(data);
            }
        } else {
            for (int i = 0; i < x.getLength(); ++i) {
                String data = x.getDataAt(i);
                result[i] = na.check(data) ? RDataFactory.createNAStringVector() : splitIntl(data, split);
            }
        }
        return RDataFactory.createList(result);
    }

    @SuppressWarnings("unused")
    @Specialization
    protected RList split(RAbstractStringVector x, RAbstractStringVector split, byte fixed, byte perl, byte useBytes) {
        controlVisibility();
        RStringVector[] result = new RStringVector[x.getLength()];
        na.enable(x);
        for (int i = 0; i < x.getLength(); ++i) {
            String data = x.getDataAt(i);
            String currentSplit = getSplit(split, i);
            if (emptySplitProfile.profile(currentSplit.isEmpty())) {
                result[i] = na.check(data) ? RDataFactory.createNAStringVector() : emptySplitIntl(data);
            } else {
                result[i] = na.check(data) ? RDataFactory.createNAStringVector() : splitIntl(data, currentSplit);
            }
        }
        return RDataFactory.createList(result);
    }

    private static String getSplit(RAbstractStringVector split, int i) {
        return split.getDataAt(i % split.getLength());
    }

    @TruffleBoundary
    private static RStringVector splitIntl(String input, String separator) {
        assert !RRuntime.isNA(input);
        return RDataFactory.createStringVector(input.split(separator), true);
    }

    private static RStringVector emptySplitIntl(String input) {
        assert !RRuntime.isNA(input);
        String[] result = new String[input.length()];
        for (int i = 0; i < input.length(); i++) {
            result[i] = new String(new char[]{input.charAt(i)});
        }
        return RDataFactory.createStringVector(result, true);
    }
}
