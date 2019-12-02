package net.intelie.tinymap;

import net.intelie.tinymap.base.IndexedCollectionBase;
import net.intelie.tinymap.base.IndexedSetBase;
import net.intelie.tinymap.util.Preconditions;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class TinySetBuilder<T> extends IndexedSetBase<T> implements
        CacheableBuilder<TinySetBuilder<T>, TinySet<T>>, Serializable, IndexedCollectionBase.NoAdditiveChange<T> {
    private static final long serialVersionUID = 1L;

    private static final Object TOMBSTONE = new Serializable() {
        private static final long serialVersionUID = 1L;

        @Override
        public String toString() {
            return "TOMBSTONE";
        }
    };
    private static final Adapter<?> adapter = new Adapter<>();

    private Object[] keys;
    //we keep an inverse table to make clear proportional to size even if the builder table has grown way too big
    private int[] inverse;
    private int[] table;
    private int rawSize = 0;
    private int size = 0;

    public TinySetBuilder() {
        this(16);
    }

    public TinySetBuilder(int expectedSize) {
        this.keys = new Object[expectedSize];
        this.inverse = new int[expectedSize];
        forceRehash(TinySet.tableSize(expectedSize));
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

    public void compact() {
        if (rawSize == size) return;
        softClearTable();
        int index = 0;
        for (int i = 0; i < rawSize; i++) {
            if (keys[i] == TOMBSTONE) continue;
            keys[index] = keys[i];

            int hash = ~getIndex(keys[index]);
            assert hash >= 0;
            table[hash] = index;
            inverse[index] = hash;
            index++;
        }
        Arrays.fill(keys, index, rawSize, null);

        this.size = index;
        this.rawSize = index;
    }

    private void forceRehash(int newSize) {
        this.table = newTable(newSize);
        this.size = 0;
        compact();
    }

    private void softClearTable() {
        for (int i = 0; i < rawSize; i++)
            table[inverse[i]] = -1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getEntryAt(int index) {
        Preconditions.checkElementIndex(index, rawSize);
        return (T) keys[index];
    }

    @Override
    public int addOrGetIndex(T key) {
        int index = getIndex(key);
        if (index >= 0)
            return index;

        index = checkOverflow(key, index);

        int hash = ~index;
        int newIndex = rawSize++;
        keys[newIndex] = key;
        table[hash] = newIndex;
        inverse[newIndex] = hash;
        size++;

        return ~newIndex;
    }

    private int checkOverflow(T key, int index) {
        if (rawSize == keys.length) {
            int newSize = keys.length + (keys.length >> 1);
            keys = Arrays.copyOf(keys, newSize);
            inverse = Arrays.copyOf(inverse, newSize);
        }
        if (2 * (rawSize + 1) > table.length) {
            forceRehash(table.length * 2);
            index = getIndex(key);
        }
        return index;
    }

    @Override
    public int getIndex(Object key) {
        int collisions = 0;
        int mask = table.length - 1;
        int hash = hash(key) & mask;

        for (int i = table[hash]; i >= 0; i = table[hash = (hash + ++collisions) & mask]) {
            if (Objects.equals(key, keys[i]))
                return i;
        }
        return ~hash;
    }

    @Override
    public boolean removeAt(int index) {
        Preconditions.checkElementIndex(index, rawSize);
        keys[index] = TOMBSTONE;
        size--;
        return false;
    }

    @Override
    public boolean isRemoved(int index) {
        return keys[index] == TOMBSTONE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Adapter<T> adapter() {
        return (Adapter<T>) adapter;
    }

    public int size() {
        return size;
    }

    @Override
    public int rawSize() {
        return rawSize;
    }

    @Override
    public TinySet<T> build() {
        compact();
        return TinySet.createUnsafe(Arrays.copyOf(keys, size));
    }

    @Override
    public void clear() {
        Arrays.fill(keys, 0, rawSize, null);
        softClearTable();
        size = rawSize = 0;
    }

    public static class Adapter<T> implements CacheAdapter<TinySetBuilder<T>, TinySet<T>> {
        @Override
        public int contentHashCode(TinySetBuilder<T> builder) {
            int hash = 1;
            for (int i = 0; i < builder.rawSize(); i++) {
                if (builder.isRemoved(i)) continue;
                hash = (hash * 31) + System.identityHashCode(builder.keys[i]);
            }
            return hash;
        }

        @SuppressWarnings("unchecked")
        @Override
        public TinySet<T> contentEquals(TinySetBuilder<T> builder, Object cached) {
            if (!(cached instanceof TinySet<?>) || builder.size() != ((TinySet<?>) cached).size())
                return null;
            TinySet<?> set = (TinySet<?>) cached;
            int j = 0;
            for (int i = 0; i < builder.rawSize(); i++) {
                if (builder.isRemoved(i)) continue;
                if (builder.getEntryAt(i) != set.getEntryAt(j))
                    return null;
                j++;
            }
            return (TinySet<T>) cached;
        }

        @Override
        public TinySet<T> build(TinySetBuilder<T> builder, ObjectCache cache) {
            return builder.build();
        }
    }
}
