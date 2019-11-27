package net.intelie.tinymap;

import net.intelie.tinymap.util.CacheData;
import net.intelie.tinymap.util.DoubleCache;
import net.intelie.tinymap.util.StringCacheAdapter;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicLong;

public class ObjectCache {
    private static final StringCacheAdapter STRING_ADAPTER = new StringCacheAdapter();
    private final CacheData<Bucket> data;
    private final DoubleCache doubleCache;

    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();
    private final boolean useSoftReference;

    public ObjectCache() {
        this(1 << 14);
    }

    public ObjectCache(int bucketCount) {
        this(bucketCount, 4);
    }

    public ObjectCache(int bucketCount, int bucketSize) {
        this(bucketCount, bucketSize, true);
    }

    public ObjectCache(int bucketCount, int bucketSize, boolean useSoftReference) {
        this.data = new CacheData<>(bucketCount, bucketSize);
        this.doubleCache = new DoubleCache(bucketCount, bucketSize, 512);
        this.useSoftReference = useSoftReference;
    }

    private <B, T> T eq(Bucket bucket, CacheAdapter<B, T> adapter, B builder, int hash) {
        if (bucket == null || bucket.hash() != hash)
            return null;
        return adapter.contentEquals(builder, bucket.get());
    }

    public long objectHits() {
        return hits.get();
    }

    public long objectMisses() {
        return misses.get();
    }

    public long doubleHits() {
        return doubleCache.hits();
    }

    public long doubleMisses() {
        return doubleCache.misses();
    }

    public Double get(double value) {
        return doubleCache.get(value);
    }

    public String get(CharSequence cs) {
        return get(cs, STRING_ADAPTER);
    }

    public <B extends CacheableBuilder<B, T>, T> T get(B builder) {
        return get(builder, builder.adapter());
    }

    public <B, T> T get(B builder, CacheAdapter<B, T> adapter) {
        if (builder == null)
            return null;
        int hash = adapter.contentHashCode(builder);
        int n = data.makeIndex(hash);
        T cached = eq(data.get(n), adapter, builder, hash);
        if (cached != null) {
            hits.incrementAndGet();
            return cached;
        }
        for (int k = 1; k < data.bucketSize(); k++) {
            Bucket bucket = data.get(n + k);
            cached = eq(bucket, adapter, builder, hash);
            if (cached != null) {
                hits.incrementAndGet();
                return data.finishCached(bucket, cached, n, k);
            }
        }
        misses.incrementAndGet();
        cached = adapter.build(builder, this);
        return data.finishCached(makeBucket(hash, cached), cached, n);
    }

    private Bucket makeBucket(int hash, Object cached) {
        return useSoftReference ? new SoftBucket(cached, hash) : new WeakBucket(cached, hash);
    }

    private interface Bucket {
        int hash();

        Object get();
    }

    private static class WeakBucket extends WeakReference<Object> implements Bucket {
        private final int hash;

        private WeakBucket(Object value, int hash) {
            super(value);
            this.hash = hash;
        }

        @Override
        public int hash() {
            return hash;
        }
    }

    private static class SoftBucket extends SoftReference<Object> implements Bucket {
        private final int hash;

        private SoftBucket(Object value, int hash) {
            super(value);
            this.hash = hash;
        }

        @Override
        public int hash() {
            return hash;
        }
    }
}
