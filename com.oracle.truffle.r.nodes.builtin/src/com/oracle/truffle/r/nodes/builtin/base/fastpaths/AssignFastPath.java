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
import com.oracle.truffle.r.nodes.builtin.base.Assign;
import com.oracle.truffle.r.nodes.builtin.base.AssignNodeGen;
import com.oracle.truffle.r.runtime.RRuntime;
import com.oracle.truffle.r.runtime.data.RMissing;
import com.oracle.truffle.r.runtime.data.model.RAbstractStringVector;
import com.oracle.truffle.r.runtime.env.REnvironment;
import com.oracle.truffle.r.runtime.nodes.RFastPathNode;

public abstract class AssignFastPath extends RFastPathNode {

    @Child private Assign assign = AssignNodeGen.create(true);

    @Specialization
    @SuppressWarnings("unused")
    protected Object assign(VirtualFrame frame, RAbstractStringVector x, Object value, RMissing pos, REnvironment envir, byte inherits, Object immediate) {
        return assign.executeBuiltin(frame, x, value, envir, inherits);
    }

    @Specialization
    @SuppressWarnings("unused")
    protected Object assign(VirtualFrame frame, RAbstractStringVector x, Object value, RMissing pos, REnvironment envir, RMissing inherits, Object immediate) {
        return assign.executeBuiltin(frame, x, value, envir, RRuntime.LOGICAL_FALSE);
    }

    @Specialization
    @SuppressWarnings("unused")
    protected Object assign(VirtualFrame frame, RAbstractStringVector x, Object value, REnvironment pos, RMissing envir, byte inherits, Object immediate) {
        return assign.executeBuiltin(frame, x, value, pos, inherits);
    }

    @Specialization
    @SuppressWarnings("unused")
    protected Object assign(VirtualFrame frame, RAbstractStringVector x, Object value, REnvironment pos, RMissing envir, RMissing inherits, Object immediate) {
        return assign.executeBuiltin(frame, x, value, pos, RRuntime.LOGICAL_FALSE);
    }

    @Fallback
    @SuppressWarnings("unused")
    protected Object fallback(Object xv, Object value, Object pos, Object envir, Object inherits, Object immediate) {
        return null;
    }
}
