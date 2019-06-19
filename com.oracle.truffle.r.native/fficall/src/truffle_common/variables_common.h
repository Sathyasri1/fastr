/*
 * Copyright (c) 2018, 2019, Oracle and/or its affiliates. All rights reserved.
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
#define NO_FASTR_REDEFINE
#include <stdlib.h>
#include <string.h>
#include <Rinterface.h>
#include "../common/rffi_variablesindex.h"

Rboolean R_interrupts_suspended;
int R_interrupts_pending;

// various ignored flags and variables nevertheless needed to resolve symbols
Rboolean R_Visible;
Rboolean mbcslocale;
Rboolean useaqua;
char* OutDec = ".";
Rboolean utf8locale = FALSE;
Rboolean mbcslocale = FALSE;
Rboolean latin1locale = FALSE;
int R_dec_min_exponent = -308;
int max_contour_segments = 25000;

// from sys-std.c
#include <R_ext/eventloop.h>

char *copystring(char *value) {
	char *result = malloc(strlen(value) + 1);
	strcpy(result, value);
	return result;
}

// Generated by FFIVariablesCodeGen

char* R_Home;
char* R_TempDir;
char* Sys_TempDir;
SEXP R_GlobalEnv;
SEXP R_BaseEnv;
SEXP R_BaseNamespace;
SEXP R_NamespaceRegistry;
Rboolean R_Interactive;
SEXP R_NilValue;
SEXP R_UnboundValue;
SEXP R_MissingArg; /* "" */
SEXP R_EmptyEnv;
SEXP R_Srcref;
SEXP R_Bracket2Symbol; /* "[[" */
SEXP R_BracketSymbol; /* "[" */
SEXP R_BraceSymbol; /* "{" */
SEXP R_DoubleColonSymbol; /* "::" */
SEXP R_ClassSymbol; /* "class" */
SEXP R_DeviceSymbol; /* ".Device" */
SEXP R_DevicesSymbol; /* ".Devices" */
SEXP R_DimNamesSymbol; /* "dimnames" */
SEXP R_DimSymbol; /* "dim" */
SEXP R_DollarSymbol; /* "$" */
SEXP R_DotsSymbol; /* "..." */
SEXP R_DropSymbol; /* "drop" */
SEXP R_LastvalueSymbol; /* ".Last.value" */
SEXP R_LevelsSymbol; /* "levels" */
SEXP R_ModeSymbol; /* "mode" */
SEXP R_NameSymbol; /* "name" */
SEXP R_NamesSymbol; /* "names" */
SEXP R_NaRmSymbol; /* "na.rm" */
SEXP R_PackageSymbol; /* "package" */
SEXP R_QuoteSymbol; /* "quote" */
SEXP R_RowNamesSymbol; /* "row.names" */
SEXP R_SeedsSymbol; /* ".Random.seed" */
SEXP R_SourceSymbol; /* "source" */
SEXP R_TspSymbol; /* "tsp" */
SEXP R_dot_defined; /* ".defined" */
SEXP R_dot_Method; /* ".Method" */
SEXP R_dot_target; /* ".target" */
SEXP R_dot_packageName; /* ".packageName" */
SEXP R_dot_Generic; /* ".Generic" */
SEXP R_SrcrefSymbol; /* "srcref" */
SEXP R_SrcfileSymbol; /* "srcfile" */
SEXP R_NaString;
double R_NaN;
double R_PosInf;
double R_NegInf;
double R_NaReal;
int R_NaInt;
SEXP R_TrueValue;
SEXP R_FalseValue;
SEXP R_LogicalNAValue;
SEXP R_BlankString;
SEXP R_BlankScalarString;
SEXP R_BaseSymbol; /* "base" */
SEXP R_NamespaceEnvSymbol; /* ".__NAMESPACE__." */
SEXP R_RestartToken; /* "" */
SEXP R_SortListSymbol; /* "sort.list" */
SEXP R_SpecSymbol; /* "spec" */
SEXP R_TripleColonSymbol; /* ":::" */
SEXP R_PreviousSymbol; /* "previous" */

void Call_initvar_double(int index, double value) {
    switch (index) {
        case R_NaN_x: R_NaN = value; break;
        case R_PosInf_x: R_PosInf = value; break;
        case R_NegInf_x: R_NegInf = value; break;
        case R_NaReal_x: R_NaReal = value; break;
        default:
            printf("Call_initvar_double: unimplemented index %d\n", index);
            exit(1);
    }
}

void Call_initvar_int(int index, int value) {
    switch (index) {
        case R_Interactive_x: R_Interactive = value; break;
        case R_NaInt_x: R_NaInt = value; break;
        default:
            printf("Call_initvar_int: unimplemented index %d\n", index);
            exit(1);
    }
}

void Call_initvar_string(int index, char* value) {
    switch (index) {
        case R_Home_x: R_Home = copystring(value); break;
        case R_TempDir_x: R_TempDir = copystring(value); break;
        case Sys_TempDir_x: Sys_TempDir = copystring(value); break;
        default:
            printf("Call_initvar_string: unimplemented index %d\n", index);
            exit(1);
    }
}

void Call_initvar_obj_common(int index, void* value) {
    switch (index) {
        case R_GlobalEnv_x: R_GlobalEnv = value; break;
        case R_BaseEnv_x: R_BaseEnv = value; break;
        case R_BaseNamespace_x: R_BaseNamespace = value; break;
        case R_NamespaceRegistry_x: R_NamespaceRegistry = value; break;
        case R_NilValue_x: R_NilValue = value; break;
        case R_UnboundValue_x: R_UnboundValue = value; break;
        case R_MissingArg_x: R_MissingArg = value; break;
        case R_EmptyEnv_x: R_EmptyEnv = value; break;
        case R_Srcref_x: R_Srcref = value; break;
        case R_Bracket2Symbol_x: R_Bracket2Symbol = value; break;
        case R_BracketSymbol_x: R_BracketSymbol = value; break;
        case R_BraceSymbol_x: R_BraceSymbol = value; break;
        case R_DoubleColonSymbol_x: R_DoubleColonSymbol = value; break;
        case R_ClassSymbol_x: R_ClassSymbol = value; break;
        case R_DeviceSymbol_x: R_DeviceSymbol = value; break;
        case R_DevicesSymbol_x: R_DevicesSymbol = value; break;
        case R_DimNamesSymbol_x: R_DimNamesSymbol = value; break;
        case R_DimSymbol_x: R_DimSymbol = value; break;
        case R_DollarSymbol_x: R_DollarSymbol = value; break;
        case R_DotsSymbol_x: R_DotsSymbol = value; break;
        case R_DropSymbol_x: R_DropSymbol = value; break;
        case R_LastvalueSymbol_x: R_LastvalueSymbol = value; break;
        case R_LevelsSymbol_x: R_LevelsSymbol = value; break;
        case R_ModeSymbol_x: R_ModeSymbol = value; break;
        case R_NameSymbol_x: R_NameSymbol = value; break;
        case R_NamesSymbol_x: R_NamesSymbol = value; break;
        case R_NaRmSymbol_x: R_NaRmSymbol = value; break;
        case R_PackageSymbol_x: R_PackageSymbol = value; break;
        case R_QuoteSymbol_x: R_QuoteSymbol = value; break;
        case R_RowNamesSymbol_x: R_RowNamesSymbol = value; break;
        case R_SeedsSymbol_x: R_SeedsSymbol = value; break;
        case R_SourceSymbol_x: R_SourceSymbol = value; break;
        case R_TspSymbol_x: R_TspSymbol = value; break;
        case R_dot_defined_x: R_dot_defined = value; break;
        case R_dot_Method_x: R_dot_Method = value; break;
        case R_dot_target_x: R_dot_target = value; break;
        case R_dot_packageName_x: R_dot_packageName = value; break;
        case R_dot_Generic_x: R_dot_Generic = value; break;
        case R_SrcrefSymbol_x: R_SrcrefSymbol = value; break;
        case R_SrcfileSymbol_x: R_SrcfileSymbol = value; break;
        case R_NaString_x: R_NaString = value; break;
        case R_TrueValue_x: R_TrueValue = value; break;
        case R_FalseValue_x: R_FalseValue = value; break;
        case R_LogicalNAValue_x: R_LogicalNAValue = value; break;
        case R_BlankString_x: R_BlankString = value; break;
        case R_BlankScalarString_x: R_BlankScalarString = value; break;
        case R_BaseSymbol_x: R_BaseSymbol = value; break;
        case R_NamespaceEnvSymbol_x: R_NamespaceEnvSymbol = value; break;
        case R_RestartToken_x: R_RestartToken = value; break;
        case R_SortListSymbol_x: R_SortListSymbol = value; break;
        case R_SpecSymbol_x: R_SpecSymbol = value; break;
        case R_TripleColonSymbol_x: R_TripleColonSymbol = value; break;
        case R_PreviousSymbol_x: R_PreviousSymbol = value; break;
        default:
            printf("Call_initvar_obj_common: unimplemented index %d\n", index);
            exit(1);
    }
}
