package net.intelie.tinymap;

import net.intelie.tinymap.util.Preconditions;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public abstract class TinySet<T> extends TinySetBase<T> implements Serializable {
    public static int tableSize(int length) {
        return Integer.highestOneBit((int) Math.ceil(length * 4.0 / 3) - 1) * 2;
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

    public abstract int debugCollisions(Object key);

    public static class Empty<T> extends TinySet<T> {
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
        public T getAt(int index) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    private static abstract class ArrayTableSet<T, A> extends TinySet<T> {
        private final Object[] keys;
        private final A table;

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

        @Override
        public int debugCollisions(Object key) {
            A table = this.table;
            int mask = tableLength(table) - 1;
            int hash = hash(key) & mask;
            int collisions = 0;
            for (int i = tableGet(table, hash); tableValueUsed(i); i = tableGet(table, hash = (hash + ++collisions) & mask))
                if (Objects.equals(keys[i], key))
                    return collisions;

            return collisions;
        }

        @Override
        public int getIndex(Object key) {
            A table = this.table;
            int mask = tableLength(table) - 1;
            int hash = hash(key) & mask;
            int collisions = 0;
            for (int i = tableGet(table, hash); tableValueUsed(i); i = tableGet(table, hash = (hash + ++collisions) & mask))
                if (Objects.equals(keys[i], key))
                    return i;
            return ~hash;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T getAt(int index) {
            return (T) keys[index];
        }

        @Override
        public int size() {
            return keys.length;
        }

        protected abstract A newTable(int size);

        protected abstract int tableLength(A table);

        protected abstract int tableGet(A table, int index);

        protected abstract void tableSet(A table, int index, int value);

        protected abstract boolean tableValueUsed(int value);
    }

    public static class Small<T> extends ArrayTableSet<T, byte[]> {
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
        protected int tableLength(byte[] table) {
            return table.length;
        }

        @Override
        protected int tableGet(byte[] table, int index) {
            return table[index] & 0xFF;
        }

        @Override
        protected void tableSet(byte[] table, int index, int value) {
            table[index] = (byte) value;
        }

        @Override
        protected boolean tableValueUsed(int value) {
            return value < 0xFF;
        }
    }

    public static class Medium<T> extends ArrayTableSet<T, short[]> {
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
        protected int tableLength(short[] table) {
            return table.length;
        }

        @Override
        protected int tableGet(short[] table, int index) {
            return table[index] & 0xFFFF;
        }

        @Override
        protected void tableSet(short[] table, int index, int value) {
            table[index] = (short) value;
        }

        @Override
        protected boolean tableValueUsed(int value) {
            return value < 0xFFFF;
        }
    }

    public static class Large<T> extends ArrayTableSet<T, int[]> {
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
        protected int tableLength(int[] table) {
            return table.length;
        }

        @Override
        protected int tableGet(int[] table, int index) {
            return table[index];
        }

        @Override
        protected void tableSet(int[] table, int index, int value) {
            table[index] = value;
        }

        @Override
        protected boolean tableValueUsed(int value) {
            return value >= 0;
        }
    }


}
