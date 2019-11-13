package net.intelie.tinymap;

import net.intelie.tinymap.util.Preconditions;

import java.util.*;
import java.util.function.BiConsumer;

public abstract class TinyMap<K, V> implements Map<K, V> {
    private static final Object[] EMPTY = new Object[0];
    private static final Object SENTINEL = new Object();
    protected final K[] keys;
    protected final V[] values;
    private final int cachedHash;

    private TinyMap(K[] keys, V[] values) {
        this.keys = keys;
        this.values = values;

        int hash = 0;
        for (int i = 0; i < keys.length; i++)
            hash += Objects.hashCode(keys[i]) ^ Objects.hashCode(values[i]);
        this.cachedHash = hash;
    }

    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    public static int makeTableSize(int length) {
        return Integer.highestOneBit((int) Math.ceil(length * 4.0 / 3) - 1) * 2;
    }

    private static int hash(Object key) {
        int h;
        return key == null ? 0 : (h = key.hashCode() * 0x85ebca6b) ^ h >>> 16;
    }

    protected static <K, V> int initTable(K[] keys, V[] values, int size, InitInsert insert) {
        int newSize = 0;
        for (int j = 0; j < size; j++) {
            int position = insert.perform(newSize, keys[j]);
            keys[position] = keys[j];
            values[position] = values[j];
            if (position == newSize)
                newSize++;
        }
        return newSize;
    }

    public boolean sharesKeysWith(TinyMap<K, V> other) {
        return keys == other.keys;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsValue(Object value) {
        for (V v : values)
            if (Objects.equals(v, value))
                return true;
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return getUnsafe(key, SENTINEL) != SENTINEL;
    }

    protected abstract TinyMap<K, V> withValues(V[] values);

    public abstract int debugCollisions(Object key);

    public abstract Object getUnsafe(Object key, Object defaultValue);

    @SuppressWarnings("unchecked")
    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return (V) getUnsafe(key, defaultValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        return (V) getUnsafe(key, null);
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException("immutable map: " + this);
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException("immutable map: " + this);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("immutable map: " + this);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("immutable map: " + this);
    }

    @Override
    public int size() {
        return keys.length;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        int size = size();
        for (int i = 0; i < size; i++)
            action.accept(keys[i], values[i]);
    }

    @Override
    public Set<K> keySet() {
        return new KeysView();
    }

    @Override
    public Collection<V> values() {
        return new ValuesView();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new EntriesView();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Map) || size() != ((Map) o).size()) return false;

        for (Entry<?, ?> entry : ((Map<?, ?>) o).entrySet())
            if (!Objects.equals(getUnsafe(entry.getKey(), SENTINEL), entry.getValue()))
                return false;
        return true;
    }

    @Override
    public int hashCode() {
        return cachedHash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append('{');
        boolean first = true;
        for (int i = 0; i < keys.length; i++) {
            if (!first)
                sb.append(", ");
            first = false;
            sb.append(keys[i]).append('=').append(values[i]);
        }
        return sb.append('}').toString();
    }

    private interface InitInsert {
        int perform(int position, Object key);
    }

    public static class Empty<K, V> extends TinyMap<K, V> {
        @SuppressWarnings("unchecked")
        private Empty() {
            super((K[]) EMPTY, (V[]) EMPTY);
        }

        @Override
        public int debugCollisions(Object key) {
            return 0;
        }

        @Override
        protected TinyMap<K, V> withValues(Object[] values) {
            Preconditions.checkArgument(values.length == 0, "must be empty");
            return this;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Object getUnsafe(Object key, Object defaultValue) {
            return defaultValue;
        }
    }

    public static class Small<K, V> extends TinyMap<K, V> {
        private final byte[] table;
        private final int mask;

        private Small(K[] keys, V[] values, byte[] table) {
            super(keys, values);
            this.table = table;
            this.mask = table.length - 1;
        }

        public static <K, V> Small<K, V> create(K[] keys, V[] values, int size) {
            byte[] table = new byte[makeTableSize(size)];
            Arrays.fill(table, (byte) 0xFF);

            int newSize = initTable(keys, values, size, (position, key) -> {
                int collisions = 0;
                int mask = table.length - 1;
                int hash = hash(key) & mask;

                for (int i = table[hash] & 0xFF; i < 0xFF; i = table[hash = (hash + ++collisions) & mask] & 0xFF)
                    if (Objects.equals(keys[i], key)) {
                        position = i;
                        break;
                    }
                table[hash] = (byte) position;
                return position;
            });
            return new Small<>(Arrays.copyOf(keys, newSize), Arrays.copyOf(values, newSize), table);
        }

        @Override
        protected Small<K, V> withValues(V[] values) {
            Preconditions.checkArgument(values.length == keys.length, "must have same length");
            return new Small<>(keys, values, table);
        }

        @Override
        public int debugCollisions(Object key) {
            byte[] table = this.table;
            int mask = this.mask;
            int hash = hash(key) & mask;
            int collisions = 0;
            for (int i = table[hash] & 0xFF; i < 0xFF; i = table[hash = (hash + ++collisions) & mask] & 0xFF)
                if (Objects.equals(keys[i], key))
                    return collisions;

            return collisions;
        }

        @Override
        public Object getUnsafe(Object key, Object defaultValue) {
            byte[] table = this.table;
            int mask = this.mask;
            int hash = hash(key) & mask;
            int collisions = 0;
            for (int i = table[hash] & 0xFF; i < 0xFF; i = table[hash = (hash + ++collisions) & mask] & 0xFF)
                if (Objects.equals(keys[i], key))
                    return values[i];

            return defaultValue;
        }

    }

    public static class Medium<K, V> extends TinyMap<K, V> {
        private final short[] table;
        private final int mask;

        private Medium(K[] keys, V[] values, short[] table) {
            super(keys, values);
            this.table = table;
            this.mask = table.length - 1;
        }

        public static <K, V> Medium<K, V> create(K[] keys, V[] values, int size) {
            short[] table = new short[makeTableSize(size)];
            Arrays.fill(table, (short) 0xFFFF);

            int newSize = initTable(keys, values, size, (position, key) -> {
                int collisions = 0;
                int mask = table.length - 1;
                int hash = hash(key) & mask;

                for (int i = table[hash] & 0xFFFF; i < 0xFFFF; i = table[hash = (hash + ++collisions) & mask] & 0xFFFF)
                    if (Objects.equals(keys[i], key)) {
                        position = i;
                        break;
                    }
                table[hash] = (short) position;
                return position;
            });
            return new Medium<>(Arrays.copyOf(keys, newSize), Arrays.copyOf(values, newSize), table);
        }

        @Override
        protected Medium<K, V> withValues(V[] values) {
            Preconditions.checkArgument(values.length == keys.length, "must have same length");
            return new Medium<>(keys, values, table);
        }

        @Override
        public int debugCollisions(Object key) {
            short[] table = this.table;
            int mask = this.mask;
            int hash = hash(key) & mask;
            int collisions = 0;
            for (int i = table[hash] & 0xFFFF; i < 0xFFFF; i = table[hash = (hash + ++collisions) & mask] & 0xFFFF)
                if (Objects.equals(keys[i], key))
                    return collisions;

            return collisions;
        }

        @Override
        public Object getUnsafe(Object key, Object defaultValue) {
            short[] table = this.table;
            int mask = this.mask;
            int hash = hash(key) & mask;
            int collisions = 0;
            for (int i = table[hash] & 0xFFFF; i < 0xFFFF; i = table[hash = (hash + ++collisions) & mask] & 0xFFFF)
                if (Objects.equals(keys[i], key))
                    return values[i];

            return defaultValue;
        }
    }

    public static class Large<K, V> extends TinyMap<K, V> {
        private final int[] table;
        private final int mask;

        private Large(K[] keys, V[] values, int[] table) {
            super(keys, values);
            this.table = table;
            this.mask = table.length - 1;
        }

        private static <K, V> Large<K, V> create(K[] keys, V[] values, int size) {
            int[] table = new int[makeTableSize(size)];
            Arrays.fill(table, -1);

            int newSize = initTable(keys, values, size, (position, key) -> {
                int collisions = 0;
                int mask = table.length - 1;
                int hash = hash(key) & mask;

                for (int i = table[hash]; i >= 0; i = table[hash = (hash + ++collisions) & mask])
                    if (Objects.equals(keys[i], key)) {
                        position = i;
                        break;
                    }
                table[hash] = position;
                return position;
            });
            return new Large<>(Arrays.copyOf(keys, newSize), Arrays.copyOf(values, newSize), table);

        }

        @Override
        protected Large<K, V> withValues(V[] values) {
            Preconditions.checkArgument(values.length == keys.length, "must have same length");
            return new Large<>(keys, values, table);
        }

        @Override
        public int debugCollisions(Object key) {
            int mask = this.mask;
            int hash = hash(key) & mask;
            int collisions = 0;
            for (int i = table[hash]; i >= 0; i = table[hash = (hash + ++collisions) & mask])
                if (Objects.equals(keys[i], key))
                    return collisions;
            return collisions;
        }

        @Override
        public Object getUnsafe(Object key, Object defaultValue) {
            int[] table = this.table;
            int mask = this.mask;
            int hash = hash(key) & mask;
            int collisions = 0;
            for (int i = table[hash]; i >= 0; i = table[hash = (hash + ++collisions) & mask])
                if (Objects.equals(keys[i], key))
                    return values[i];
            return defaultValue;
        }
    }

    public static class Builder<K, V> {
        private final FullCacheAdapter<K, V> adapter = new FullCacheAdapter<>(new KeysCacheAdapter<>());

        private K[] keys = (K[]) new Object[4];
        private V[] values = (V[]) new Object[4];
        private int size = 0;

        public Builder<K, V> put(K key, V value) {
            if (size == keys.length) {
                keys = Arrays.copyOf(keys, keys.length * 2);
                values = Arrays.copyOf(values, values.length * 2);
            }
            keys[size] = key;
            values[size] = value;
            size++;
            return this;
        }

        public FullCacheAdapter<K, V> adapter() {
            return adapter;
        }

        public int size() {
            return size;
        }

        public TinyMap<K, V> build() {
            if (size == 0)
                return new Empty<>();
            else if (size < 0xFF)
                return Small.create(keys, values, size);
            else if (size < 0xFFFF)
                return Medium.create(keys, values, size);
            else
                return Large.create(keys, values, size);
        }

        public void clear() {
            Arrays.fill(keys, 0, size, null);
            Arrays.fill(values, 0, size, null);
            size = 0;
        }

        public TinyMap<K, V> buildAndClear() {
            TinyMap<K, V> answer = build();
            clear();
            return answer;
        }
    }

    public static class FullCacheAdapter<K, V> implements CacheAdapter<Builder<K, V>, TinyMap<K, V>> {
        private final CacheAdapter<Builder<K, V>, TinyMap<K, V>> keysAdapter;

        public FullCacheAdapter(CacheAdapter<Builder<K, V>, TinyMap<K, V>> keysAdapter) {
            this.keysAdapter = keysAdapter;
        }

        @Override
        public int contentHashCode(Builder<K, V> builder) {
            int hash = 1;
            for (int i = 0; i < builder.size; i++) {
                hash = (hash * 31) + Objects.hashCode(builder.keys[i]);
                hash = (hash * 31) + Objects.hashCode(builder.values[i]);
            }
            return hash;
        }

        @Override
        public TinyMap<K, V> contentEquals(Builder<K, V> builder, Object cached) {
            if (!(cached instanceof TinyMap<?, ?>) || builder.size != ((TinyMap) cached).size())
                return null;
            TinyMap<?, ?> map = (TinyMap<?, ?>) cached;
            for (int i = 0; i < builder.size; i++)
                if (builder.keys[i] != map.keys[i] || builder.values[i] != map.values[i])
                    return null;
            return (TinyMap<K, V>) cached;
        }

        @Override
        public TinyMap<K, V> build(Builder<K, V> builder, ObjectCache cache) {
            return cache.get(builder, keysAdapter);
        }

        @Override
        public TinyMap<K, V> reuse(Builder<K, V> builder, TinyMap<K, V> old, ObjectCache cache) {
            return old;
        }
    }

    public static class KeysCacheAdapter<K, V> implements CacheAdapter<Builder<K, V>, TinyMap<K, V>> {
        @Override
        public int contentHashCode(Builder<K, V> builder) {
            int hash = 1;
            for (int i = 0; i < builder.size; i++) {
                hash = (hash * 31) + Objects.hashCode(builder.keys[i]);
            }
            return hash;
        }

        @Override
        public TinyMap<K, V> contentEquals(Builder<K, V> builder, Object cached) {
            if (!(cached instanceof TinyMap<?, ?>) || builder.size != ((TinyMap) cached).size())
                return null;
            TinyMap<?, ?> map = (TinyMap<?, ?>) cached;
            for (int i = 0; i < builder.size; i++)
                if (builder.keys[i] != map.keys[i])
                    return null;
            return (TinyMap<K, V>) cached;
        }

        @Override
        public TinyMap<K, V> build(Builder<K, V> builder, ObjectCache cache) {
            return builder.build();
        }

        @Override
        public TinyMap<K, V> reuse(Builder<K, V> builder, TinyMap<K, V> old, ObjectCache cache) {
            return old.withValues(Arrays.copyOf(builder.values, builder.size));
        }
    }

    private class ValuesView extends AbstractList<V> {
        @Override
        public Iterator<V> iterator() {
            return new SimpleIterator<V>() {
                @Override
                public V makeObject(int index) {
                    return values[index];
                }
            };
        }

        @Override
        public V get(int index) {
            return values[index];
        }

        @Override
        public boolean contains(Object o) {
            return containsValue(o);
        }

        @Override
        public int size() {
            return TinyMap.this.size();
        }
    }

    private class KeysView extends AbstractSet<K> {
        @Override
        public boolean contains(Object o) {
            return containsKey(o);
        }

        @Override
        public Iterator<K> iterator() {
            return new SimpleIterator<K>() {
                @Override
                public K makeObject(int index) {
                    return keys[index];
                }
            };
        }

        @Override
        public int size() {
            return TinyMap.this.size();
        }
    }

    private abstract class SimpleIterator<T> implements Iterator<T> {
        private int next = 0;

        @Override
        public boolean hasNext() {
            return next < TinyMap.this.size();
        }

        public abstract T makeObject(int index);

        @Override
        public T next() {
            T key = makeObject(next);
            next++;
            return key;
        }
    }

    private class EntriesView extends AbstractSet<Entry<K, V>> {
        @Override
        public boolean contains(Object o) {
            if (o instanceof Entry) {
                Entry entry = (Entry) o;
                return Objects.equals(getUnsafe(entry.getKey(), SENTINEL), entry.getValue());
            }
            return false;
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new SimpleIterator<Entry<K, V>>() {
                @Override
                public Entry<K, V> makeObject(int index) {
                    return new AbstractMap.SimpleImmutableEntry<>(keys[index], values[index]);
                }
            };
        }

        @Override
        public int size() {
            return TinyMap.this.size();
        }
    }

}
