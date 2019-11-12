package net.intelie.tinymap.json;

public class FastDouble {
    private final static long MAX_DECIMAL = 999999999999999L;
    private final static int POW_RANGE = 23;
    private final static double[] POW10 = new double[POW_RANGE];

    static {
        for (int i = 0; i < POW_RANGE; i++)
            POW10[i] = Math.pow(10., i);
    }

    public static double parseDouble(String s) {
        return parseDouble(s, 0, s.length());
    }

    private static double finishDouble(boolean negative, long value, int exp) {
        double d = exp < 0 ? value / POW10[-exp] : value * POW10[exp];
        return negative ? -d : d;
    }

    private static double fallback(CharSequence csq, int offset, int end) {
        return Double.parseDouble(csq.subSequence(offset, end).toString());
    }

    public static double parseDouble(CharSequence csq, int offset, int end) {
        int i = offset;
        i = trim(csq, end, i);

        boolean negative = false;
        long value = 0;
        int exp = 0;
        int digits = 0;

        if (i < end && csq.charAt(i) == '-') {
            negative = true;
            i++;
        }

        while (i < end && value < MAX_DECIMAL) {
            char c = csq.charAt(i);
            if (c < '0' || c > '9') break;
            value = (value * 10) + (c - '0');
            i++;
            digits++;
        }

        if (i < end && csq.charAt(i) == '.') {
            i++;
            while (i < end && value < MAX_DECIMAL) {
                char c = csq.charAt(i);
                if (c < '0' || c > '9') break;
                value = (value * 10) + (c - '0');
                i++;
                digits++;
                exp--;
            }
        }

        if (digits == 0)
            return fallback(csq, offset, end);


        if (i < end && (csq.charAt(i) == 'E' || csq.charAt(i) == 'e')) {
            i++;
            boolean expNegative = false;
            int expExp = 0;
            int expDigits = 0;
            if (i < end && csq.charAt(i) == '-') {
                expNegative = true;
                i++;
            }
            while (i < end) {
                char c = csq.charAt(i);
                if (c < '0' || c > '9') break;
                expExp = (expExp * 10) + (c - '0');
                expDigits++;
                i++;
            }

            if (expDigits == 0)
                return fallback(csq, offset, end);

            exp += (expNegative ? -expExp : expExp);

        }

        i = trim(csq, end, i);

        if (Math.abs(exp) >= POW_RANGE || digits == 0 || i < end || value > MAX_DECIMAL)
            return fallback(csq, offset, end);

        return finishDouble(negative, value, exp);
    }

    private static int trim(CharSequence csq, int end, int i) {
        while (i < end && csq.charAt(i) == ' ')
            i++;
        return i;
    }


}
