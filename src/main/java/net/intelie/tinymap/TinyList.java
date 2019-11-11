package net.intelie.tinymap;

import net.intelie.tinymap.util.Preconditions;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

public class TinyList<T> extends AbstractList<T> {
    private final T[] values;
    private final int cachedHash;

    public TinyList(T[] values) {
        this.values = values;
        int hash = 1;
        for (T value : values)
            hash = 31 * hash + Objects.hashCode(value);
        this.cachedHash = hash;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    @Override
    public int hashCode() {
        return cachedHash;
    }

    @Override
    public T get(int index) {
        Preconditions.checkElementIndex(index, values.length);
        return values[index];
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        T[] values = this.values;
        int length = values.length;
        for (int i = 0; i < length; i++) {
            action.accept(values[i]);
        }
    }

    @Override
    public int size() {
        return values.length;
    }

    public static class FullCacheAdapter<T> implements CacheAdapter<Builder<T>, TinyList<T>> {

        @Override
        public int contentHashCode(Builder<T> builder) {
            int hash = 1;
            for (int i = 0; i < builder.size; i++)
                hash = 31 * hash + Objects.hashCode(builder.values[i]);
            return hash;
        }

        @Override
        public TinyList<T> contentEquals(Builder<T> builder, Object cached) {
            if (!(cached instanceof TinyList<?>) || builder.size != ((TinyList) cached).size())
                return null;
            for (int i = 0; i < builder.size; i++)
                if (builder.values[i] != ((TinyList<?>) cached).values[i])
                    return null;
            return (TinyList<T>) cached;
        }

        @Override
        public TinyList<T> build(Builder<T> builder, ObjectCache cache) {
            return builder.build();
        }
    }

    public static class Builder<T> {
        private final FullCacheAdapter<T> adapter = new FullCacheAdapter<>();
        @SuppressWarnings("unchecked")
        private T[] values = (T[]) new Object[4];
        private int size = 0;

        public Builder<T> add(T obj) {
            if (size == values.length) {
                values = Arrays.copyOf(values, values.length * 2);
            }
            values[size++] = obj;
            return this;
        }

        public int size() {
            return size;
        }

        public TinyList<T> build() {
            return new TinyList<>(Arrays.copyOf(values, size));
        }

        public FullCacheAdapter<T> adapter() {
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
    }
}
