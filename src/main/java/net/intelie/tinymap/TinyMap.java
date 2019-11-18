package net.intelie.tinymap;

import net.intelie.tinymap.util.Preconditions;

import java.util.Arrays;
import java.util.Objects;

public abstract class TinyMap<K, V> extends ListMapBase<K, V> {
    protected final K[] keys;
    protected final V[] values;

    private TinyMap(K[] keys, V[] values) {
        this.keys = keys;
        this.values = values;
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


    protected abstract TinyMap<K, V> withValues(V[] values);

    public abstract int debugCollisions(Object key);

    @Override
    public K getKeyAt(int index) {
        return keys[index];
    }

    @Override
    public V getValueAt(int index) {
        return values[index];
    }

    @Override
    public int size() {
        return keys.length;
    }

    private interface InitInsert {
        int perform(int position, Object key);
    }

    public static class Empty<K, V> extends TinyMap<K, V> {
        private static final Object[] EMPTY = new Object[0];

        @SuppressWarnings("unchecked")
        private Empty() {
            super((K[]) EMPTY, (V[]) EMPTY);
        }

        @Override
        public int debugCollisions(Object key) {
            return 0;
        }

        @Override
        public int getIndex(Object key) {
            return -1;
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

        private Small(K[] keys, V[] values, byte[] table) {
            super(keys, values);
            this.table = table;
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
            int mask = table.length - 1;
            int hash = hash(key) & mask;
            int collisions = 0;
            for (int i = table[hash] & 0xFF; i < 0xFF; i = table[hash = (hash + ++collisions) & mask] & 0xFF)
                if (Objects.equals(keys[i], key))
                    return collisions;

            return collisions;
        }

        @Override
        public int getIndex(Object key) {
            byte[] table = this.table;
            int mask = table.length - 1;
            int hash = hash(key) & mask;
            int collisions = 0;
            for (int i = table[hash] & 0xFF; i < 0xFF; i = table[hash = (hash + ++collisions) & mask] & 0xFF)
                if (Objects.equals(keys[i], key))
                    return i;

            return -1;
        }

        @Override
        public Object getUnsafe(Object key, Object defaultValue) {
            byte[] table = this.table;
            int mask = table.length - 1;
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

        private Medium(K[] keys, V[] values, short[] table) {
            super(keys, values);
            this.table = table;
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
            int mask = table.length - 1;
            int hash = hash(key) & mask;
            int collisions = 0;
            for (int i = table[hash] & 0xFFFF; i < 0xFFFF; i = table[hash = (hash + ++collisions) & mask] & 0xFFFF)
                if (Objects.equals(keys[i], key))
                    return collisions;

            return collisions;
        }

        @Override
        public int getIndex(Object key) {
            short[] table = this.table;
            int mask = table.length - 1;
            int hash = hash(key) & mask;
            int collisions = 0;
            for (int i = table[hash] & 0xFFFF; i < 0xFFFF; i = table[hash = (hash + ++collisions) & mask] & 0xFFFF)
                if (Objects.equals(keys[i], key))
                    return i;

            return -1;
        }

        @Override
        public Object getUnsafe(Object key, Object defaultValue) {
            short[] table = this.table;
            int mask = table.length - 1;
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

        private Large(K[] keys, V[] values, int[] table) {
            super(keys, values);
            this.table = table;
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
            int[] table = this.table;
            int mask = table.length - 1;
            int hash = hash(key) & mask;
            int collisions = 0;
            for (int i = table[hash]; i >= 0; i = table[hash = (hash + ++collisions) & mask])
                if (Objects.equals(keys[i], key))
                    return collisions;
            return collisions;
        }

        @Override
        public int getIndex(Object key) {
            int[] table = this.table;
            int mask = table.length - 1;
            int hash = hash(key) & mask;
            int collisions = 0;
            for (int i = table[hash]; i >= 0; i = table[hash = (hash + ++collisions) & mask])
                if (Objects.equals(keys[i], key))
                    return i;
            return -1;
        }

        @Override
        public Object getUnsafe(Object key, Object defaultValue) {
            int[] table = this.table;
            int mask = table.length - 1;
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
                hash = (hash * 31) + System.identityHashCode(builder.keys[i]);
                hash = (hash * 31) + System.identityHashCode(builder.values[i]);
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
                hash = (hash * 31) + System.identityHashCode(builder.keys[i]);
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
}
