package net.intelie.tinymap;

import net.intelie.tinymap.base.IndexedSetBase;
import net.intelie.tinymap.util.Preconditions;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public abstract class TinySet<T> extends IndexedSetBase<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public static int tableSize(int length) {
        return Integer.highestOneBit(length * 2 - 1) * 2;
    }

    private static int hash(Object key) {
        int h;
        return key == null ? 0 : (h = key.hashCode() * 0x85ebca6b) ^ h >>> 16;
    }

    public static <T> TinySet<T> createUnsafe(Object[] keys) {
        if (keys.length == 0)
            return new TinySet.Empty<>();
        else if (keys.length < 0xFF)
            return new Small<>(keys);
        else if (keys.length < 0xFFFF)
            return new Medium<>(keys);
        else
            return new Large<>(keys);
    }


    public static <T> TinySetBuilder<T> builder() {
        return new TinySetBuilder<>();
    }

    public abstract int debugCollisions(Object key);

    public static class Empty<T> extends TinySet<T> implements TinySet.Immutable<T> {
        private static final long serialVersionUID = 1L;

        @Override
        public int debugCollisions(Object key) {
            return 0;
        }

        @Override
        public int getIndex(Object key) {
            return -1;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public T getEntryAt(int index) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    private static abstract class ArrayTableSet<T, A> extends TinySet<T> implements TinySet.Immutable<T> {
        private static final long serialVersionUID = 1L;

        protected final Object[] keys;
        protected final A table;

        private ArrayTableSet(Object[] keys) {
            this.keys = keys;
            this.table = newTable(tableSize(keys.length));

            for (int j = 0; j < keys.length; j++) {
                Object key = keys[j];
                int hash = ~getIndex(key);
                Preconditions.checkArgument(hash >= 0, "duplicate key: %s", key);
                tableSet(table, hash, j);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public T getEntryAt(int index) {
            return (T) keys[index];
        }

        @Override
        public int size() {
            return keys.length;
        }

        protected abstract A newTable(int size);

        protected abstract void tableSet(A table, int index, int value);
    }

    public static class Small<T> extends ArrayTableSet<T, byte[]> {
        private static final long serialVersionUID = 1L;

        private Small(Object[] keys) {
            super(keys);
        }

        @Override
        protected byte[] newTable(int size) {
            byte[] table = new byte[size];
            Arrays.fill(table, (byte) 0xFF);
            return table;
        }

        @Override
        protected void tableSet(byte[] table, int index, int value) {
            table[index] = (byte) value;
        }

        @Override
        public int debugCollisions(Object key) {
            byte[] table = this.table;
            int mask = table.length - 1;
            int hash = hash(key) & mask;
            int collisions = 0;
            for (int i = table[hash] & 0xFF; i < 0xFF; i = table[hash = (hash + ++collisions) & mask] & 0xFF)
                if (Objects.equals(key, keys[i]))
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
                if (Objects.equals(key, keys[i]))
                    return i;
            return ~hash;
        }
    }

    public static class Medium<T> extends ArrayTableSet<T, short[]> {
        private static final long serialVersionUID = 1L;

        private Medium(Object[] keys) {
            super(keys);
        }

        @Override
        protected short[] newTable(int size) {
            short[] table = new short[size];
            Arrays.fill(table, (short) 0xFFFF);
            return table;
        }

        @Override
        protected void tableSet(short[] table, int index, int value) {
            table[index] = (short) value;
        }

        @Override
        public int debugCollisions(Object key) {
            short[] table = this.table;
            int mask = table.length - 1;
            int hash = hash(key) & mask;
            int collisions = 0;
            for (int i = table[hash] & 0xFFFF; i < 0xFFFF; i = table[hash = (hash + ++collisions) & mask] & 0xFFFF)
                if (Objects.equals(key, keys[i]))
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
                if (Objects.equals(key, keys[i]))
                    return i;
            return ~hash;
        }
    }

    public static class Large<T> extends ArrayTableSet<T, int[]> {
        private static final long serialVersionUID = 1L;

        private Large(Object[] keys) {
            super(keys);
        }

        @Override
        protected int[] newTable(int size) {
            int[] table = new int[size];
            Arrays.fill(table, -1);
            return table;
        }

        @Override
        protected void tableSet(int[] table, int index, int value) {
            table[index] = value;
        }

        @Override
        public int debugCollisions(Object key) {
            int[] table = this.table;
            int mask = table.length - 1;
            int hash = hash(key) & mask;
            int collisions = 0;
            for (int i = table[hash]; i >= 0; i = table[hash = (hash + ++collisions) & mask])
                if (Objects.equals(key, keys[i]))
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
                if (Objects.equals(key, keys[i]))
                    return i;
            return ~hash;
        }
    }


}
