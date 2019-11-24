package net.intelie.tinymap;

import net.intelie.tinymap.util.Preconditions;

import java.io.Serializable;
import java.util.Arrays;

public class TinyListBuilder<T> extends ListCollectionBase<T> implements CacheableBuilder<TinyListBuilder<T>, TinyList<T>> {
    private static final Object TOMBSTONE = new Serializable() {
    };
    private final Adapter<T> adapter = new Adapter<>();
    @SuppressWarnings("unchecked")
    private Object[] values = new Object[4];
    private int rawSize = 0;
    private int size = 0;

    @Override
    public int addOrGetIndex(T obj) {
        int index = rawSize++;
        if (index == values.length)
            values = Arrays.copyOf(values, values.length * 2);
        values[index] = obj;
        size++;
        return ~index;
    }

    @Override
    public void removeAt(int index) {
        Preconditions.checkElementIndex(index, rawSize);
        size--;
        values[index] = TOMBSTONE;
    }

    @Override
    public boolean isRemoved(int index) {
        Preconditions.checkElementIndex(index, rawSize);
        return values[index] == TOMBSTONE;
    }

    @Override
    public T getEntryAt(int index) {
        Preconditions.checkElementIndex(index, rawSize);
        return (T) values[index];
    }

    @Override
    public int rawSize() {
        return rawSize;
    }

    public int size() {
        return size;
    }

    @Override
    public void compact() {
        if (size == rawSize) return;
        int index = 0;
        for (int i = 0; i < rawSize; i++) {
            if (isRemoved(i)) continue;
            values[index++] = values[i];
        }
        Arrays.fill(values, index, rawSize, null);
        assert index == size;
        rawSize = index;
    }

    @Override
    public TinyList<T> build() {
        compact();
        return new TinyList<>(Arrays.copyOf(values, size));
    }

    @Override
    public Adapter<T> adapter() {
        return adapter;
    }

    public void clear() {
        Arrays.fill(values, 0, rawSize, null);
        size = rawSize = 0;
    }

    public TinyList<T> buildAndClear() {
        TinyList<T> answer = build();
        clear();
        return answer;
    }

    public static class Adapter<T> implements CacheAdapter<TinyListBuilder<T>, TinyList<T>> {
        @Override
        public int contentHashCode(TinyListBuilder<T> builder) {
            int hash = 1;
            for (int i = 0; i < builder.rawSize; i++)
                if (!builder.isRemoved(i))
                    hash = 31 * hash + System.identityHashCode(builder.values[i]);
            return hash;
        }

        @Override
        public TinyList<T> contentEquals(TinyListBuilder<T> builder, Object cached) {
            if (!(cached instanceof TinyList<?>) || builder.size != ((TinyList) cached).size())
                return null;
            TinyList<T> list = (TinyList<T>) cached;
            int j = 0;
            for (int i = 0; i < builder.rawSize; i++) {
                if (builder.isRemoved(i)) continue;
                if (builder.values[i] != list.get(j))
                    return null;
                j++;
            }
            return list;
        }

        @Override
        public TinyList<T> build(TinyListBuilder<T> builder, ObjectCache cache) {
            return builder.build();
        }
    }
}
