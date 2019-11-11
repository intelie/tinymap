package net.intelie.tinymap.util;

import net.intelie.tinymap.util.Preconditions;

public class CacheData<T> {
    private final Object[] cache;
    private final int bucketSize;
    private final int mask;

    public CacheData(int bucketCount, int bucketSize) {
        Preconditions.checkArgument(Integer.bitCount(bucketCount) == 1, "Bucket count must be power of two");
        this.bucketSize = bucketSize;
        this.cache = new Object[bucketCount * bucketSize];
        this.mask = bucketCount - 1;
    }

    public static int mix(int h) {
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;
        return h;
    }

    public int makeIndex(int hash) {
        return (mix(hash) & mask) * bucketSize;
    }

    public int bucketSize() {
        return bucketSize;
    }

    public T get(int index) {
        return (T) cache[index];
    }

    public <V> V finishCached(T cached, V cachedValue, int n) {
        return finishCached(cached, cachedValue, n, bucketSize - 1);
    }

    public <V> V finishCached(T cached, V cachedValue, int n, int k) {
        for (int i = n + k; i > n; i--)
            cache[i] = cache[i - 1];
        cache[n] = cached;
        return cachedValue;
    }

    public T finish(T cached, int n) {
        return finishCached(cached, cached, n, bucketSize - 1);
    }

    public T finish(T cached, int n, int k) {
        return finishCached(cached, cached, n, k);
    }
}
