package net.intelie.tinymap.util;

import java.util.concurrent.atomic.AtomicLong;

public class DoubleCache {
    private final Double NEG_ZERO = -0.0;
    private final int smallCacheAmplitude;
    private final Double[] smallCache;
    private final CacheData<Double> data;

    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();


    public DoubleCache() {
        this((1 << 14), 4, 512);
    }

    public DoubleCache(int bucketCount, int bucketSize, int smallCacheAmplitude) {
        Preconditions.checkArgument(Integer.bitCount(bucketCount) == 1, "Bucket count must be power of two");
        this.smallCacheAmplitude = smallCacheAmplitude;
        this.data = new CacheData<>(bucketCount, bucketSize);
        this.smallCache = new Double[smallCacheAmplitude * 2];
        for (int i = 0; i < smallCache.length; i++) {
            smallCache[i] = (double) (i - smallCacheAmplitude);
        }
    }

    private static boolean eq(Double cached, double value) {
        return cached != null && Double.doubleToLongBits(cached) == Double.doubleToLongBits(value);
    }

    public Double get(double value) {
        return getCached(value, null);

    }

    public Double get(Double value) {
        if (value == null) return null;
        return getCached(value, value);
    }

    public long hits() {
        return hits.get();
    }

    public long misses() {
        return misses.get();
    }

    private Double getCached(double value, Double boxed) {
        if (value >= -smallCacheAmplitude && value < smallCacheAmplitude && value == (int) value) {
            if (Double.doubleToLongBits(value) == 0x8000000000000000L)
                return NEG_ZERO;
            return smallCache[(int) value + smallCacheAmplitude];
        }
        int hash = Double.hashCode(value);
        int n = data.makeIndex(hash);
        Double cached = data.get(n);
        if (eq(cached, value)) {
            hits.incrementAndGet();
            return cached;
        }
        for (int k = 1; k < data.bucketSize(); k++)
            if (eq(cached = data.get(n + k), value)) {
                hits.incrementAndGet();
                return data.finish(cached, n, k);
            }

        misses.incrementAndGet();
        return data.finish(boxed != null ? boxed : value, n);
    }
}