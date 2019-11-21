package net.intelie.tinymap;

import java.util.Arrays;

public class TinyListBuilder<T> implements CacheableBuilder<TinyListBuilder<T>, TinyList<T>> {
    private final Adapter<T> adapter = new Adapter<>();
    @SuppressWarnings("unchecked")
    private T[] values = (T[]) new Object[4];
    private int size = 0;

    public TinyListBuilder<T> add(T obj) {
        if (size == values.length) {
            values = Arrays.copyOf(values, values.length * 2);
        }
        values[size++] = obj;
        return this;
    }

    public int size() {
        return size;
    }

    @Override
    public TinyList<T> build() {
        return new TinyList<>(Arrays.copyOf(values, size));
    }

    @Override
    public Adapter<T> adapter() {
        return adapter;
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
                hash = 31 * hash + System.identityHashCode(builder.values[i]);
            return hash;
        }

        @Override
        public TinyList<T> contentEquals(TinyListBuilder<T> builder, Object cached) {
            if (!(cached instanceof TinyList<?>) || builder.size != ((TinyList) cached).size())
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
