/*
 * This material is distributed under the GNU General Public License
 * Version 2. You may review the terms of this license at
 * http://www.gnu.org/licenses/gpl-2.0.html
 *
 * Copyright (c) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (c) 2000-2008, The R Core Team
 * Copyright (c) 2004, The R Foundation
 * Copyright (c) 2016, Oracle and/or its affiliates
 *
 * All rights reserved.
 */
package com.oracle.truffle.r.library.stats;

import static com.oracle.truffle.r.library.stats.MathConstants.DBL_EPSILON;
import static com.oracle.truffle.r.library.stats.MathConstants.DBL_MAX;
import static com.oracle.truffle.r.library.stats.MathConstants.DBL_MIN;

import com.oracle.truffle.r.library.stats.Chisq.QChisq;
import com.oracle.truffle.r.library.stats.DPQ.EarlyReturn;
import com.oracle.truffle.r.library.stats.RMathError.MLError;
import com.oracle.truffle.r.library.stats.StatsFunctions.Function3_2;

public class QNChisq implements Function3_2 {
    private static final double accu = 1e-13;
    private static final double racc = 4 * DBL_EPSILON;
    /* these two are for the "search" loops, can have less accuracy: */
    private static final double Eps = 1e-11; /* must be > accu */
    private static final double rEps = 1e-10; /* relative tolerance ... */

    private final QChisq qchisq = new QChisq();
    private final PNChisq pnchisq = new PNChisq();

    @Override
    public double evaluate(double p, double df, double ncp, boolean lowerTail, boolean logP) {
        if (Double.isNaN(p) || Double.isNaN(df) || Double.isNaN(ncp)) {
            return p + df + ncp;
        }
        if (!Double.isFinite(df) || df < 0 || ncp < 0) {
            return RMathError.defaultError();
        }

        try {
            DPQ.rqp01boundaries(p, 0, Double.POSITIVE_INFINITY, lowerTail, logP);
        } catch (EarlyReturn e) {
            return e.result;
        }

        double pp = DPQ.rdqiv(p, logP);
        if (pp > 1 - DBL_EPSILON) {
            return lowerTail ? Double.POSITIVE_INFINITY : 0.0;
        }

        /*
         * Invert pnchisq(.) : 1. finding an upper and lower bound
         */

        /*
         * This is Pearson's (1959) approximation, which is usually good to 4 figs or so.
         */
        double b = (ncp * ncp) / (df + 3 * ncp);
        double c = (df + 3 * ncp) / (df + 2 * ncp);
        double ff = (df + 2 * ncp) / (c * c);
        double ux = b + c * qchisq.evaluate(p, ff, lowerTail, logP);
        if (ux < 0) {
            ux = 1;
        }
        double ux0 = ux;

        if (!lowerTail && ncp >= 80) {
            /* in this case, pnchisq() works via lower_tail = true */
            if (pp < 1e-10) {
                RMathError.error(MLError.PRECISION, "qnchisq");
            }
            p = DPQ.rdtqiv(p, lowerTail, logP);
            lowerTail = true;
        } else {
            p = pp;
        }

        pp = RMath.fmin2(1 - DBL_EPSILON, p * (1 + Eps));
        while (ux < DBL_MAX && isLower(lowerTail, pnchisq.pnchisqRaw(ux, df, ncp, Eps, rEps, 10000, lowerTail, false), pp)) {
            ux *= 2;
        }
        pp = p * (1 - Eps);
        double lx = RMath.fmin2(ux0, DBL_MAX);
        while (lx > DBL_MIN && isGreater(lowerTail, pnchisq.pnchisqRaw(lx, df, ncp, Eps, rEps, 10000, lowerTail, false), pp)) {
            lx *= 0.5;
        }

        /* 2. interval (lx,ux) halving : */
        double nx;
        do {
            nx = 0.5 * (lx + ux);
            double raw = pnchisq.pnchisqRaw(nx, df, ncp, accu, racc, 100000, lowerTail, false);
            if (isGreater(lowerTail, raw, p)) {
                ux = nx;
            } else {
                lx = nx;
            }
        } while ((ux - lx) / nx > accu);

        return 0.5 * (ux + lx);
    }

    /**
     * Is greater that changes to is lower if {@code lowerTail} is {@code false}.
     */
    private boolean isGreater(boolean lowerTail, double raw, double p) {
        return (lowerTail && raw > p) || (!lowerTail && raw < p);
    }

    private boolean isLower(boolean lowerTail, double raw, double p) {
        return (lowerTail && raw < p) || (!lowerTail && raw > p);
    }
}
