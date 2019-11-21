package net.intelie.tinymap;

import java.util.Arrays;

public class TinyMapBuilder<K, V> implements CacheableBuilder<TinyMapBuilder<K, V>, TinyMap<K, V>> {
    private final Adapter<K, V> adapter = new Adapter<>(new KeysAdapter<>());

    private Object[] keys = new Object[4];
    private Object[] values = new Object[4];
    private int size = 0;

    public TinyMapBuilder<K, V> put(K key, V value) {
        if (size == keys.length) {
            keys = Arrays.copyOf(keys, keys.length * 2);
            values = Arrays.copyOf(values, values.length * 2);
        }
        keys[size] = key;
        values[size] = value;
        size++;
        return this;
    }

    @Override
    public Adapter<K, V> adapter() {
        return adapter;
    }

    public int size() {
        return size;
    }

    @Override
    public TinyMap<K, V> build() {
        TinyMap<K, V> built = innerBuild();
        this.size = built.size();
        return built;
    }

    public void clear() {
        Arrays.fill(keys, 0, size, null);
        Arrays.fill(values, 0, size, null);
        size = 0;
    }

    private TinyMap<K, V> innerBuild() {
        if (size == 0)
            return new TinyMap.Empty<>();
        else if (size < 0xFF)
            return TinyMap.Small.create(keys, values, size);
        else if (size < 0xFFFF)
            return TinyMap.Medium.create(keys, values, size);
        else
            return TinyMap.Large.create(keys, values, size);
    }

    public TinyMap<K, V> buildAndClear() {
        TinyMap<K, V> answer = innerBuild();
        clear();
        return answer;
    }


    public static class Adapter<K, V> implements CacheAdapter<TinyMapBuilder<K, V>, TinyMap<K, V>> {
        private final CacheAdapter<TinyMapBuilder<K, V>, TinyMap<K, V>> keysAdapter;

        public Adapter(CacheAdapter<TinyMapBuilder<K, V>, TinyMap<K, V>> keysAdapter) {
            this.keysAdapter = keysAdapter;
        }

        @Override
        public int contentHashCode(TinyMapBuilder<K, V> builder) {
            int hash = 1;
            for (int i = 0; i < builder.size; i++) {
                hash = (hash * 31) + System.identityHashCode(builder.keys[i]);
                hash = (hash * 31) + System.identityHashCode(builder.values[i]);
            }
            return hash;
        }

        @Override
        public TinyMap<K, V> contentEquals(TinyMapBuilder<K, V> builder, Object cached) {
            if (!(cached instanceof TinyMap<?, ?>) || builder.size != ((TinyMap) cached).size())
                return null;
            TinyMap<?, ?> map = (TinyMap<?, ?>) cached;
            for (int i = 0; i < builder.size; i++)
                if (builder.keys[i] != map.keys[i] || builder.values[i] != map.values[i])
                    return null;
            return (TinyMap<K, V>) cached;
        }

        @Override
        public TinyMap<K, V> build(TinyMapBuilder<K, V> builder, ObjectCache cache) {
            return cache.get(builder, keysAdapter);
        }

        @Override
        public TinyMap<K, V> reuse(TinyMapBuilder<K, V> builder, TinyMap<K, V> old, ObjectCache cache) {
            return old;
        }
    }

    public static class KeysAdapter<K, V> implements CacheAdapter<TinyMapBuilder<K, V>, TinyMap<K, V>> {
        @Override
        public int contentHashCode(TinyMapBuilder<K, V> builder) {
            int hash = 1;
            for (int i = 0; i < builder.size; i++) {
                hash = (hash * 31) + System.identityHashCode(builder.keys[i]);
            }
            return hash;
        }

        @Override
        public TinyMap<K, V> contentEquals(TinyMapBuilder<K, V> builder, Object cached) {
            if (!(cached instanceof TinyMap<?, ?>) || builder.size != ((TinyMap) cached).size())
                return null;
            TinyMap<?, ?> map = (TinyMap<?, ?>) cached;
            for (int i = 0; i < builder.size; i++)
                if (builder.keys[i] != map.keys[i])
                    return null;
            return (TinyMap<K, V>) cached;
        }

        @Override
        public TinyMap<K, V> build(TinyMapBuilder<K, V> builder, ObjectCache cache) {
            return builder.build();
        }

        @Override
        public TinyMap<K, V> reuse(TinyMapBuilder<K, V> builder, TinyMap<K, V> old, ObjectCache cache) {
            return old.withValues(builder.values, builder.size);
        }
    }
}
