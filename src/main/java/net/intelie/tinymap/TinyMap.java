package net.intelie.tinymap;

import net.intelie.tinymap.util.Preconditions;

import java.util.Arrays;
import java.util.Objects;

public abstract class TinyMap<K, V> extends ListMapBase<K, V> {
    public static final Object TOMBSTONE = new Object();

    protected final Object[] keys;
    protected final Object[] values;

    private TinyMap(Object[] keys, Object[] values) {
        this.keys = keys;
        this.values = values;
    }

    public static <K, V> TinyMapBuilder<K, V> builder() {
        return new TinyMapBuilder<>();
    }

    public static int makeTableSize(int length) {
        return Integer.highestOneBit((int) Math.ceil(length * 4.0 / 3) - 1) * 2;
    }

    private static int hash(Object key) {
        int h;
        return key == null ? 0 : (h = key.hashCode() * 0x85ebca6b) ^ h >>> 16;
    }

    public static int initTable(Object[] keys, Object[] values, int size, InitInsert insert) {
        int newSize = 0;
        for (int j = 0; j < size; j++) {
            if (keys[j] == TinyMap.TOMBSTONE) continue;
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

    public abstract TinyMap<K, V> withValues(Object[] values, int size);

    public abstract int debugCollisions(Object key);

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

    @Override
    public int size() {
        return keys.length;
    }

    private interface InitInsert {
        int perform(int position, Object key);
    }

    public static class Empty<K, V> extends TinyMap<K, V> {
        private static final Object[] EMPTY = new Object[0];

        public Empty() {
            super(EMPTY, EMPTY);
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
        public TinyMap<K, V> withValues(Object[] values, int size) {
            Preconditions.checkArgument(size == 0, "must be empty");
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

        private Small(Object[] keys, Object[] values, byte[] table) {
            super(keys, values);
            this.table = table;
        }

        public static <K, V> Small<K, V> create(Object[] keys, Object[] values, int size) {
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
        public Small<K, V> withValues(Object[] values, int size) {
            Preconditions.checkArgument(size == keys.length, "must have same length");
            return new Small<>(keys, Arrays.copyOf(values, size), table);
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

        private Medium(Object[] keys, Object[] values, short[] table) {
            super(keys, values);
            this.table = table;
        }

        public static <K, V> Medium<K, V> create(Object[] keys, Object[] values, int size) {
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
        public Medium<K, V> withValues(Object[] values, int size) {
            Preconditions.checkArgument(size == keys.length, "must have same length");
            return new Medium<>(keys, Arrays.copyOf(values, size), table);
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

        private Large(Object[] keys, Object[] values, int[] table) {
            super(keys, values);
            this.table = table;
        }

        public static <K, V> Large<K, V> create(Object[] keys, Object[] values, int size) {
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
        public Large<K, V> withValues(Object[] values, int size) {
            Preconditions.checkArgument(size == keys.length, "must have same length");
            return new Large<>(keys, Arrays.copyOf(values, size), table);
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
}
