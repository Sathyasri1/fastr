/*
 * Copyright (c) 2014, 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 3 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.r.test.library.stats;

import org.junit.Test;

import com.oracle.truffle.r.test.TestBase;

/**
 * Tests the updateform external function called from R function update.formula, in FastR this
 * function is implemented in R code. See file modelTests.R in the same directory for pure R tests
 * testing only the FastR R code without the R wrappers from GnuR. When adding new test cases here,
 * consider adding them to modelTests.R too.
 */
public class TestUpdateForm extends TestBase {
    private static final String[] OLD_FORMULAE = new String[]{"x ~ y", ". ~ u+v", "x ~ u+v", "z ~ x + y "};
    private static final String[] NEW_FORMULAE = new String[]{"~ . + x2", "log(.) ~ .:q", "x + . ~ y:. + log(.)", "NULL ~ ."};

    @Test
    public void basicTests() {
        assertEval(Ignored.NewRVersionMigration, template("update.formula(%0, %1)", OLD_FORMULAE, NEW_FORMULAE));
    }
}
