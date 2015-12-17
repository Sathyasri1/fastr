/*
 * Copyright (c) 2015, 2015, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.profiles.*;
import com.oracle.truffle.r.runtime.*;
import com.oracle.truffle.r.runtime.data.model.*;
import com.oracle.truffle.r.runtime.nodes.*;
import com.oracle.truffle.r.runtime.ops.na.*;

public abstract class IsElementFastPath extends RFastPathNode {

    @Specialization(guards = "el.getLength() == 1")
    protected Byte iselementOne(RAbstractStringVector el, RAbstractStringVector set, //
                    @Cached("create()") NAProfile na, //
                    @Cached("create()") BranchProfile trueProfile, //
                    @Cached("create()") BranchProfile falseProfile) {
        String element = el.getDataAt(0);
        if (!na.isNA(element)) {
            int length = set.getLength();
            for (int i = 0; i < length; i++) {
                if (element.equals(set.getDataAt(i))) {
                    trueProfile.enter();
                    return RRuntime.LOGICAL_TRUE;
                }
            }
            falseProfile.enter();
            return RRuntime.LOGICAL_FALSE;
        }
        return null;
    }

    @Fallback
    @SuppressWarnings("unused")
    protected Object fallback(Object el, Object set) {
        return null;
    }
}
