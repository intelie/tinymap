package net.intelie.tinymap.util;

import net.intelie.tinymap.CacheAdapter;
import net.intelie.tinymap.CacheableBuilder;
import net.intelie.tinymap.ObjectCache;

import java.lang.ref.WeakReference;

public class DefaultObjectCache implements ObjectCache {
    private static final StringCacheAdapter STRING_ADAPTER = new StringCacheAdapter();
    private final Bucket[] data;
    private final int mask;
    private final DefaultDoubleCache doubleCache;

    public DefaultObjectCache() {
        this(1 << 16);
    }

    public DefaultObjectCache(int bucketCount) {
        Preconditions.checkArgument(Integer.bitCount(bucketCount) == 1, "Bucket count must be power of two");
        this.data = new Bucket[bucketCount];
        this.doubleCache = new DefaultDoubleCache(bucketCount, 512);
        this.mask = bucketCount - 1;
    }

    @Override
    public Double get(double value) {
        return doubleCache.get(value);
    }

    @Override
    public String get(CharSequence cs) {
        return get(cs, STRING_ADAPTER);
    }

    @Override
    public <B extends CacheableBuilder<B, T>, T> T get(B builder) {
        return get(builder, builder.adapter());
    }

    @Override
    public <B, T> T get(B builder, CacheAdapter<B, T> adapter) {
        if (builder == null)
            return null;
        int hash = adapter.contentHashCode(builder);
        int index = DefaultDoubleCache.mix(hash) & mask;
        Bucket bucket = data[index];
        if (bucket != null && bucket.hash == hash) {
            T cached = adapter.contentEquals(builder, data[index].get());
            if (cached != null)
                return cached;
        }

        T newValue = adapter.build(builder, this);
        data[index] = new Bucket(newValue, hash);
        return newValue;
    }

    private static final class Bucket extends WeakReference<Object> {
        private final int hash;

        private Bucket(Object value, int hash) {
            super(value);
            this.hash = hash;
        }
    }
}
