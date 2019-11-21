package net.intelie.tinymap;

import net.intelie.tinymap.util.Preconditions;

import java.util.Arrays;
import java.util.Objects;

public class MutableMapBuilder<K, V> implements CacheableBuilder<MutableMapBuilder<K, V>, TinyMap<K, V>> {
    private final Adapter<K, V> adapter = new Adapter<>(new KeysAdapter<>());

    private Object[] keys = new Object[4];
    private Object[] values = new Object[4];
    //we keep an inverse table to make clear proportional to size even if the builder table has grown way too big
    private int[] inverse = new int[4];
    private int[] table;
    private int size = 0;
    private int realSize = 0;

    public MutableMapBuilder() {
        rehashTo(newTable(16));
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
        for (int i = 0; i < size; i++) {
            if (keys[i] == TinyMap.TOMBSTONE) continue;
            keys[index] = keys[i];
            values[index] = values[i];

            int hash = ~findIndex(table, keys[index]);
            Preconditions.checkState(hash >= 0, "duplicate key, this is a bug");
            table[hash] = index;
            inverse[index] = hash;
            index++;
        }
        for (int i = index; i < size; i++) {
            keys[i] = values[i] = null;
        }

        this.table = table;
        this.size = this.realSize = index;
    }

    private void softClearTable() {
        for (int i = 0; i < size; i++)
            table[inverse[i]] = -1;
    }

    public V put(K key, V value) {
        int index = findIndex(table, key);
        if (index >= 0) {
            Object old = values[index];
            values[index] = value;
            return (V) old;
        }

        if (size == keys.length) {
            keys = Arrays.copyOf(keys, keys.length * 2);
            values = Arrays.copyOf(values, values.length * 2);
            inverse = Arrays.copyOf(inverse, inverse.length * 2);
        }
        if (4 * (size + 1) > 3 * table.length)
            rehashTo(newTable(table.length * 2));

        int hash = ~index;
        int newIndex = size++;

        values[newIndex] = value;
        keys[newIndex] = key;
        table[hash] = newIndex;
        inverse[newIndex] = hash;
        realSize++;
        return null;
    }

    public boolean containsKey(K key) {
        return findIndex(table, key) >= 0;
    }

    public V get(K key) {
        int index = findIndex(table, key);
        if (index < 0) return null;

        return (V) values[index];
    }

    public V remove(K key) {
        int index = findIndex(table, key);
        if (index < 0) return null;
        keys[index] = ListMap.TOMBSTONE;
        values[index] = ListMap.TOMBSTONE;
        realSize--;
        return (V) values[index];

    }

    @Override
    public Adapter<K, V> adapter() {
        return adapter;
    }

    public int size() {
        return realSize;
    }

    @Override
    public TinyMap<K, V> build() {
        TinyMap<K, V> built = innerBuild();
        softClearTable();
        size = realSize = built.size();
        rehashTo(table);
        return built;
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

    public void clear() {
        Arrays.fill(keys, 0, size, null);
        Arrays.fill(values, 0, size, null);
        softClearTable();
        realSize = size = 0;
    }

    public TinyMap<K, V> buildAndClear() {
        TinyMap<K, V> answer = innerBuild();
        clear();
        return answer;
    }


    public static class Adapter<K, V> implements CacheAdapter<MutableMapBuilder<K, V>, TinyMap<K, V>> {
        private final CacheAdapter<MutableMapBuilder<K, V>, TinyMap<K, V>> keysAdapter;

        public Adapter(CacheAdapter<MutableMapBuilder<K, V>, TinyMap<K, V>> keysAdapter) {
            this.keysAdapter = keysAdapter;
        }

        @Override
        public int contentHashCode(MutableMapBuilder<K, V> builder) {
            int hash = 1;
            for (int i = 0; i < builder.size; i++) {
                hash = (hash * 31) + System.identityHashCode(builder.keys[i]);
                hash = (hash * 31) + System.identityHashCode(builder.values[i]);
            }
            return hash;
        }

        @Override
        public TinyMap<K, V> contentEquals(MutableMapBuilder<K, V> builder, Object cached) {
            if (!(cached instanceof TinyMap<?, ?>) || builder.size != ((TinyMap) cached).size())
                return null;
            TinyMap<?, ?> map = (TinyMap<?, ?>) cached;
            for (int i = 0; i < builder.size; i++)
                if (builder.keys[i] != map.keys[i] || builder.values[i] != map.values[i])
                    return null;
            return (TinyMap<K, V>) cached;
        }

        @Override
        public TinyMap<K, V> build(MutableMapBuilder<K, V> builder, ObjectCache cache) {
            return cache.get(builder, keysAdapter);
        }

        @Override
        public TinyMap<K, V> reuse(MutableMapBuilder<K, V> builder, TinyMap<K, V> old, ObjectCache cache) {
            return old;
        }
    }

    public static class KeysAdapter<K, V> implements CacheAdapter<MutableMapBuilder<K, V>, TinyMap<K, V>> {
        @Override
        public int contentHashCode(MutableMapBuilder<K, V> builder) {
            int hash = 1;
            for (int i = 0; i < builder.size; i++) {
                hash = (hash * 31) + System.identityHashCode(builder.keys[i]);
            }
            return hash;
        }

        @Override
        public TinyMap<K, V> contentEquals(MutableMapBuilder<K, V> builder, Object cached) {
            if (!(cached instanceof TinyMap<?, ?>) || builder.size != ((TinyMap) cached).size())
                return null;
            TinyMap<?, ?> map = (TinyMap<?, ?>) cached;
            for (int i = 0; i < builder.size; i++)
                if (builder.keys[i] != map.keys[i])
                    return null;
            return (TinyMap<K, V>) cached;
        }

        @Override
        public TinyMap<K, V> build(MutableMapBuilder<K, V> builder, ObjectCache cache) {
            return builder.build();
        }

        @Override
        public TinyMap<K, V> reuse(MutableMapBuilder<K, V> builder, TinyMap<K, V> old, ObjectCache cache) {
            return old.withValues(builder.values, builder.size);
        }
    }
}
