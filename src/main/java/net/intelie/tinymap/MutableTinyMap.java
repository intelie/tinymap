package net.intelie.tinymap;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class MutableTinyMap<K, V> extends ListMapBase<K, V> implements CacheableBuilder<MutableTinyMap<K, V>, TinyMap<K, V>>, Serializable {
    private static final Object TOMBSTONE = new Serializable() {
    };
    private static final Adapter<?, ?> adapter = new Adapter<>(new KeysAdapter<>());

    private Object[] keys;
    private Object[] values;
    //we keep an inverse table to make clear proportional to size even if the builder table has grown way too big
    private int[] inverse;
    private int[] table;
    private int rawSize = 0;
    private int size = 0;

    public MutableTinyMap() {
        this(16);
    }

    public MutableTinyMap(int expectedSize) {
        keys = new Object[expectedSize];
        values = new Object[expectedSize];
        inverse = new int[expectedSize];
        rehashTo(newTable(TinyMap.tableSize(expectedSize)));
    }

    private static int hash(Object key) {
        int h;
        return key == null ? 0 : (h = key.hashCode() * 0x85ebca6b) ^ h >>> 16;
    }

    private static int[] newTable(int size) {
        int[] table = new int[size];
        Arrays.fill(table, -1);
        return table;
    }

    private int findIndex(int[] table, Object key) {
        int collisions = 0;
        int mask = table.length - 1;
        int hash = hash(key) & mask;

        for (int i = table[hash]; i >= 0; i = table[hash = (hash + ++collisions) & mask]) {
            if (Objects.equals(keys[i], key))
                return i;
        }
        return ~hash;
    }

    private void rehashTo(int[] table) {
        int index = 0;
        for (int i = 0; i < rawSize; i++) {
            if (keys[i] == TOMBSTONE) continue;
            keys[index] = keys[i];
            values[index] = values[i];

            int hash = ~findIndex(table, keys[index]);
            assert hash >= 0;
            table[hash] = index;
            inverse[index] = hash;
            index++;
        }
        Arrays.fill(keys, index, rawSize, null);
        Arrays.fill(values, index, rawSize, null);

        assert size == index;

        this.table = table;
        this.rawSize = index;
    }

    private void softClearTable() {
        for (int i = 0; i < rawSize; i++)
            table[inverse[i]] = -1;
    }

    public V put(K key, V value) {
        int index = findIndex(table, key);
        if (index >= 0)
            return setValueAt(index, value);

        index = checkOverflow(key, index);

        int hash = ~index;
        int newIndex = rawSize++;

        values[newIndex] = value;
        keys[newIndex] = key;
        table[hash] = newIndex;
        inverse[newIndex] = hash;
        size++;
        return null;
    }

    private int checkOverflow(K key, int index) {
        if (rawSize == keys.length) {
            int newSize = keys.length + (keys.length >> 1);
            keys = Arrays.copyOf(keys, newSize);
            values = Arrays.copyOf(values, newSize);
            inverse = Arrays.copyOf(inverse, newSize);
        }
        if (4 * (rawSize + 1) > 3 * table.length) {
            rehashTo(newTable(table.length * 2));
            index = findIndex(table, key);
        }
        return index;
    }

    @Override
    public int getIndex(Object key) {
        return findIndex(table, key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public K getKeyAt(int index) {
        return (K) keys[index];
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
        Object old = values[index];
        keys[index] = TOMBSTONE;
        values[index] = null;
        size--;
        return (V) old;
    }

    @Override
    public boolean isRemoved(int index) {
        return keys[index] == TOMBSTONE;
    }

    public void rehash() {
        if (rawSize == size) return;
        softClearTable();
        rehashTo(table);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Adapter<K, V> adapter() {
        return (Adapter<K, V>) adapter;
    }

    public int size() {
        return size;
    }

    @Override
    public int rawSize() {
        return rawSize;
    }

    @Override
    public TinyMap<K, V> build() {
        rehash();
        return TinyMap.create(keys, values, rawSize);
    }

    @Override
    public void clear() {
        Arrays.fill(keys, 0, rawSize, null);
        Arrays.fill(values, 0, rawSize, null);
        softClearTable();
        size = rawSize = 0;
    }

    public TinyMap<K, V> buildAndClear() {
        TinyMap<K, V> answer = build();
        clear();
        return answer;
    }

    public static class Adapter<K, V> implements CacheAdapter<MutableTinyMap<K, V>, TinyMap<K, V>> {
        private final CacheAdapter<MutableTinyMap<K, V>, TinyMap<K, V>> keysAdapter;

        public Adapter(CacheAdapter<MutableTinyMap<K, V>, TinyMap<K, V>> keysAdapter) {
            this.keysAdapter = keysAdapter;
        }

        @Override
        public int contentHashCode(MutableTinyMap<K, V> builder) {
            int hash = 1;
            for (int i = 0; i < builder.rawSize(); i++) {
                if (builder.isRemoved(i)) continue;
                hash = (hash * 31) + System.identityHashCode(builder.keys[i]);
                hash = (hash * 31) + System.identityHashCode(builder.values[i]);
            }
            return hash;
        }

        @SuppressWarnings("unchecked")
        @Override
        public TinyMap<K, V> contentEquals(MutableTinyMap<K, V> builder, Object cached) {
            if (!(cached instanceof TinyMap<?, ?>) || builder.rawSize != ((TinyMap) cached).size())
                return null;
            TinyMap<?, ?> map = (TinyMap<?, ?>) cached;
            int j = 0;
            for (int i = 0; i < builder.rawSize(); i++) {
                if (builder.isRemoved(j)) continue;
                if (builder.keys[i] != map.keys[j] || builder.values[i] != map.values[j])
                    return null;
                j++;
            }
            return (TinyMap<K, V>) cached;
        }

        @Override
        public TinyMap<K, V> build(MutableTinyMap<K, V> builder, ObjectCache cache) {
            return cache.get(builder, keysAdapter);
        }

        @Override
        public TinyMap<K, V> reuse(MutableTinyMap<K, V> builder, TinyMap<K, V> old, ObjectCache cache) {
            return old;
        }
    }

    public static class KeysAdapter<K, V> implements CacheAdapter<MutableTinyMap<K, V>, TinyMap<K, V>> {
        @Override
        public int contentHashCode(MutableTinyMap<K, V> builder) {
            builder.rehash();
            int hash = 1;
            for (int i = 0; i < builder.rawSize(); i++) {
                if (builder.isRemoved(i)) continue;
                hash = (hash * 31) + System.identityHashCode(builder.keys[i]);
            }
            return hash;
        }

        @SuppressWarnings("unchecked")
        @Override
        public TinyMap<K, V> contentEquals(MutableTinyMap<K, V> builder, Object cached) {
            builder.rehash();
            if (!(cached instanceof TinyMap<?, ?>) || builder.rawSize != ((TinyMap) cached).size())
                return null;
            TinyMap<?, ?> map = (TinyMap<?, ?>) cached;
            int j = 0;
            for (int i = 0; i < builder.rawSize(); i++) {
                if (builder.isRemoved(i)) continue;
                if (builder.keys[i] != map.keys[j])
                    return null;
                j++;
            }
            return (TinyMap<K, V>) cached;
        }

        @Override
        public TinyMap<K, V> build(MutableTinyMap<K, V> builder, ObjectCache cache) {
            return builder.build();
        }

        @Override
        public TinyMap<K, V> reuse(MutableTinyMap<K, V> builder, TinyMap<K, V> old, ObjectCache cache) {
            builder.rehash();
            return old.withValues(builder.values, builder.rawSize);
        }
    }
}
