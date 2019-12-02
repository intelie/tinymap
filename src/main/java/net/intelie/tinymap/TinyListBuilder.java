package net.intelie.tinymap;

import net.intelie.tinymap.base.IndexedListBase;
import net.intelie.tinymap.util.Preconditions;

import java.io.Serializable;
import java.util.Arrays;

public class TinyListBuilder<T> extends IndexedListBase<T> implements CacheableBuilder<TinyListBuilder<T>, TinyList<T>>, Serializable {
    private static final long serialVersionUID = 1L;

    private static final Adapter<?> adapter = new Adapter<>();
    private Object[] values = new Object[4];
    private int size = 0;

    @Override
    public int addOrGetIndex(T obj) {
        int index = size++;
        if (index == values.length)
            values = Arrays.copyOf(values, values.length * 2);
        values[index] = obj;
        return ~index;
    }

    @Override
    public T set(int index, T obj) {
        T old = getEntryAt(index);
        values[index] = obj;
        return old;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getEntryAt(int index) {
        Preconditions.checkElementIndex(index, size);
        return (T) values[index];
    }

    public int size() {
        return size;
    }

    @Override
    public T removeLast() {
        T old = set(size - 1, null);
        size--;
        return old;
    }

    @Override
    public TinyList<T> build() {
        return new TinyList<>(Arrays.copyOf(values, size));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Adapter<T> adapter() {
        return (Adapter<T>) adapter;
    }

    public void clear() {
        Arrays.fill(values, 0, size, null);
        size = 0;
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
            for (int i = 0; i < builder.size; i++)
                if (!builder.isRemoved(i))
                    hash = 31 * hash + System.identityHashCode(builder.values[i]);
            return hash;
        }

        @SuppressWarnings("unchecked")
        @Override
        public TinyList<T> contentEquals(TinyListBuilder<T> builder, Object cached) {
            if (!(cached instanceof TinyList<?>) || builder.size != ((TinyList<?>) cached).size())
                return null;
            TinyList<T> list = (TinyList<T>) cached;
            for (int i = 0; i < builder.size; i++) {
                if (builder.values[i] != list.get(i))
                    return null;
            }
            return list;
        }

        @Override
        public TinyList<T> build(TinyListBuilder<T> builder, ObjectCache cache) {
            return builder.build();
        }
    }
}
