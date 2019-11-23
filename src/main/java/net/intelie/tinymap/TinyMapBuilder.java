package net.intelie.tinymap;

import java.io.Serializable;
import java.util.Arrays;

public class TinyMapBuilder<K, V> extends TinyMapBase<K, V> implements CacheableBuilder<TinyMapBuilder<K, V>, TinyMap<K, V>>, Serializable {
    private static final Object TOMBSTONE = new Serializable() {
    };
    private static final Adapter<?, ?> adapter = new Adapter<>();

    private TinySetBuilder<K> keys;
    private Object[] values;

    public TinyMapBuilder() {
        this(16);
    }

    public TinyMapBuilder(int expectedSize) {
        values = new Object[expectedSize];
        keys = new TinySetBuilder<K>(expectedSize) {
            @Override
            public void compact() {
                int index = 0;
                int rawSize = rawSize();
                for (int i = 0; i < rawSize; i++) {
                    if (values[i] == TOMBSTONE) continue;
                    values[index++] = values[i];
                }
                Arrays.fill(values, index, rawSize, null);
                super.compact();
            }
        };
    }

    public void compact() {
        keys.compact();
    }

    public V put(K key, V value) {
        int index = keys.addOrGetIndex(key);
        if (index >= 0)
            return setValueAt(index, value);

        index = ~index;
        if (index >= values.length)
            values = Arrays.copyOf(values, values.length + (values.length >> 1));

        values[index] = value;
        return null;
    }

    @Override
    public int getIndex(Object key) {
        return keys.getIndex(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public K getKeyAt(int index) {
        return keys.getAt(index);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V getValueAt(int index) {
        return (V) values[index];
    }

    @SuppressWarnings("unchecked")
    @Override
    public V setValueAt(int index, V value) {
        Object old = values[index];
        values[index] = value;
        return (V) old;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V removeAt(int index) {
        keys.removeAt(index);
        Object old = values[index];
        values[index] = TOMBSTONE;
        return (V) old;
    }

    @Override
    public boolean isRemoved(int index) {
        return keys.isRemoved(index);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Adapter<K, V> adapter() {
        return (Adapter<K, V>) adapter;
    }

    public int size() {
        return keys.size();
    }

    @Override
    public int rawSize() {
        return keys.rawSize();
    }

    @Override
    public TinyMap<K, V> build() {
        TinySet<K> keys = this.keys.build();
        return buildWithKeys(keys);
    }

    private TinyMap<K, V> buildWithKeys(TinySet<K> keys) {
        return TinyMap.createUnsafe(keys, Arrays.copyOf(values, keys.size()));
    }

    @Override
    public void clear() {
        Arrays.fill(values, 0, keys.rawSize(), null);
        keys.clear();
    }

    public static class Adapter<K, V> implements CacheAdapter<TinyMapBuilder<K, V>, TinyMap<K, V>> {
        @Override
        public int contentHashCode(TinyMapBuilder<K, V> builder) {
            int hash = 1;
            for (int i = 0; i < builder.rawSize(); i++) {
                if (builder.isRemoved(i)) continue;
                hash = (hash * 31) + System.identityHashCode(builder.getKeyAt(i));
                hash = (hash * 31) + System.identityHashCode(builder.getValueAt(i));
            }
            return hash;
        }

        @SuppressWarnings("unchecked")
        @Override
        public TinyMap<K, V> contentEquals(TinyMapBuilder<K, V> builder, Object cached) {
            if (!(cached instanceof TinyMap<?, ?>) || builder.size() != ((TinyMap) cached).size())
                return null;
            TinyMap<?, ?> map = (TinyMap<?, ?>) cached;
            int j = 0;
            for (int i = 0; i < builder.rawSize(); i++) {
                if (builder.isRemoved(i)) continue;
                if (builder.getKeyAt(i) != map.getKeyAt(j) || builder.getValueAt(i) != map.getValueAt(j))
                    return null;
                j++;
            }
            return (TinyMap<K, V>) cached;
        }

        @Override
        public TinyMap<K, V> build(TinyMapBuilder<K, V> builder, ObjectCache cache) {
            return builder.buildWithKeys(cache.get(builder.keys));
        }
    }
}
