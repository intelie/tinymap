package net.intelie.tinymap.json;

public class FastDouble {
    private final static boolean USE_POW_TABLE = true;

    // Precompute Math.pow(10, n) as table:
    private final static int POW_RANGE = (USE_POW_TABLE) ? 256 : 0;
    private final static double[] POS_EXPS = new double[POW_RANGE];
    private final static double[] NEG_EXPS = new double[POW_RANGE];

    static {
        for (int i = 0; i < POW_RANGE; i++) {
            POS_EXPS[i] = Math.pow(10., i);
            NEG_EXPS[i] = Math.pow(10., -i);
        }
    }

    // Calculate the value of the specified exponent - reuse a precalculated value if possible
    private final static double getPow10(final int exp) {
        if (USE_POW_TABLE) {
            if (exp > -POW_RANGE) {
                if (exp <= 0) {
                    return NEG_EXPS[-exp];
                } else if (exp < POW_RANGE) {
                    return POS_EXPS[exp];
                }
            }
        }
        return Math.pow(10., exp);
    }

    public static double getDouble(final CharSequence csq,
                                   final int offset, final int end) throws NumberFormatException {
        if (true)
            return Double.parseDouble(new StringBuilder().append(csq, offset, end).toString());

        int off = offset;
        int len = end - offset;

        if (len == 0) {
            return Double.NaN;
        }

        char ch;
        boolean numSign = true;

        ch = csq.charAt(off);
        if (ch == '+') {
            off++;
            len--;
        } else if (ch == '-') {
            numSign = false;
            off++;
            len--;
        }

        double number;

        // Look for the special csqings NaN, Inf,
        if (len >= 3
                && ((ch = csq.charAt(off)) == 'n' || ch == 'N')
                && ((ch = csq.charAt(off + 1)) == 'a' || ch == 'A')
                && ((ch = csq.charAt(off + 2)) == 'n' || ch == 'N')) {

            number = Double.NaN;

            // Look for the longer csqing first then try the shorter.
        } else if (len >= 8
                && ((ch = csq.charAt(off)) == 'i' || ch == 'I')
                && ((ch = csq.charAt(off + 1)) == 'n' || ch == 'N')
                && ((ch = csq.charAt(off + 2)) == 'f' || ch == 'F')
                && ((ch = csq.charAt(off + 3)) == 'i' || ch == 'I')
                && ((ch = csq.charAt(off + 4)) == 'n' || ch == 'N')
                && ((ch = csq.charAt(off + 5)) == 'i' || ch == 'I')
                && ((ch = csq.charAt(off + 6)) == 't' || ch == 'T')
                && ((ch = csq.charAt(off + 7)) == 'y' || ch == 'Y')) {

            number = Double.POSITIVE_INFINITY;

        } else if (len >= 3
                && ((ch = csq.charAt(off)) == 'i' || ch == 'I')
                && ((ch = csq.charAt(off + 1)) == 'n' || ch == 'N')
                && ((ch = csq.charAt(off + 2)) == 'f' || ch == 'F')) {

            number = Double.POSITIVE_INFINITY;

        } else {

            boolean error = true;

            int startOffset = off;
            double dval;

            // TODO: check too many digits (overflow)
            for (dval = 0d; (len > 0) && ((ch = csq.charAt(off)) >= '0') && (ch <= '9'); ) {
                dval *= 10d;
                dval += ch - '0';
                off++;
                len--;
            }
            int numberLength = off - startOffset;

            number = dval;

            if (numberLength > 0) {
                error = false;
            }

            // Check for fractional values after decimal
            if ((len > 0) && (csq.charAt(off) == '.')) {

                off++;
                len--;

                startOffset = off;

                // TODO: check too many digits (overflow)
                for (dval = 0d; (len > 0) && ((ch = csq.charAt(off)) >= '0') && (ch <= '9'); ) {
                    dval *= 10d;
                    dval += ch - '0';
                    off++;
                    len--;
                }
                numberLength = off - startOffset;

                if (numberLength > 0) {
                    // TODO: try factorizing pow10 with exponent below: only 1 long + operation
                    number += getPow10(-numberLength) * dval;
                    error = false;
                }
            }

            if (error) {
                throw new NumberFormatException("Invalid Double : " + csq);
            }

            // Look for an exponent
            if (len > 0) {
                // note: ignore any non-digit character at end:

                if ((ch = csq.charAt(off)) == 'e' || ch == 'E') {

                    off++;
                    len--;

                    if (len > 0) {
                        boolean expSign = true;

                        ch = csq.charAt(off);
                        if (ch == '+') {
                            off++;
                            len--;
                        } else if (ch == '-') {
                            expSign = false;
                            off++;
                            len--;
                        }

                        int exponent = 0;

                        // note: ignore any non-digit character at end:
                        for (exponent = 0; (len > 0) && ((ch = csq.charAt(off)) >= '0') && (ch <= '9'); ) {
                            exponent *= 10;
                            exponent += ch - '0';
                            off++;
                            len--;
                        }

                        // TODO: check exponent < 1024 (overflow)
                        if (!expSign) {
                            exponent = -exponent;
                        }

                        // For very small numbers we try to miminize
                        // effects of denormalization.
                        if (exponent > -300) {
                            // TODO: cache Math.pow ?? see web page
                            number *= getPow10(exponent);
                        } else {
                            number = 1.0E-300 * (number * getPow10(exponent + 300));
                        }
                    }
                }
            }
            // check other characters:
            if (len > 0) {
                throw new NumberFormatException("Invalid Double : " + csq);
            }
        }

        return (numSign) ? number : -number;
    }


}
