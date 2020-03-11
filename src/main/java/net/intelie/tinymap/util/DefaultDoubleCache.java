package net.intelie.tinymap.util;

public class DefaultDoubleCache {
    private static final Double NEG_ZERO = -0.0;
    private final int smallCacheAmplitude;
    private final Double[] smallCache;
    private final Double[] data;
    private final int mask;

    public DefaultDoubleCache() {
        this((1 << 14), 512);
    }

    public DefaultDoubleCache(int bucketCount, int smallCacheAmplitude) {
        Preconditions.checkArgument(Integer.bitCount(bucketCount) == 1, "Bucket count must be power of two");
        this.smallCacheAmplitude = smallCacheAmplitude;
        this.data = new Double[bucketCount];
        this.mask = bucketCount - 1;
        this.smallCache = new Double[smallCacheAmplitude * 2];
        for (int i = 0; i < smallCache.length; i++) {
            smallCache[i] = (double) (i - smallCacheAmplitude);
        }
    }

    public Double get(double value) {
        return getCached(value, null);

    }

    public Double get(Double value) {
        if (value == null) return null;
        return getCached(value, value);
    }


    public static int mix(int h) {
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;
        return h;
    }

    private Double getCached(double value, Double boxed) {
        if (value >= -smallCacheAmplitude && value < smallCacheAmplitude && value == (int) value) {
            if (Double.doubleToLongBits(value) == 0x8000000000000000L)
                return NEG_ZERO;
            return smallCache[(int) value + smallCacheAmplitude];
        }
        int hash = Double.hashCode(value);
        int index = mix(hash) & mask;
        Double cached = data[index];
        if (cached != null && Double.doubleToLongBits(cached) == Double.doubleToLongBits(value))
            return cached;

        return data[index] = boxed != null ? boxed : Double.valueOf(value);
    }
}