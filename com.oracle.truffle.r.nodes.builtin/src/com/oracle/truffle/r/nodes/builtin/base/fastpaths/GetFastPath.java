/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.nodes.builtin.base.fastpaths;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.r.nodes.builtin.base.GetFunctions.Get;
import com.oracle.truffle.r.nodes.builtin.base.GetFunctionsFactory.GetNodeGen;
import com.oracle.truffle.r.runtime.RRuntime;
import com.oracle.truffle.r.runtime.data.RMissing;
import com.oracle.truffle.r.runtime.env.REnvironment;
import com.oracle.truffle.r.runtime.nodes.RFastPathNode;

public abstract class GetFastPath extends RFastPathNode {

    @Child private Get get = GetNodeGen.create();

    @Specialization
    @SuppressWarnings("unused")
    protected Object getNonInherit(VirtualFrame frame, String x, RMissing pos, REnvironment envir, RMissing mode, byte inherits) {
        return get.execute(frame, x, envir, "any", RRuntime.fromLogical(inherits));
    }

    @Specialization
    @SuppressWarnings("unused")
    protected Object getNonInherit(VirtualFrame frame, String x, RMissing pos, REnvironment envir, RMissing mode, RMissing inherits) {
        return get.execute(frame, x, envir, "any", true);
    }

    @Fallback
    @SuppressWarnings("unused")
    protected Object fallback(Object xv, Object pos, Object envir, Object mode, Object inherits) {
        return null;
    }
}
