package net.intelie.tinymap;

import net.intelie.tinymap.util.Preconditions;

import java.util.AbstractList;
import java.util.Objects;
import java.util.function.Consumer;

public class TinyList<T> extends AbstractList<T> {
    private final T[] values;

    public TinyList(T[] values) {
        this.values = values;
    }

    public static <T> TinyListBuilder<T> builder() {
        return new TinyListBuilder<>();
    }

    @Override
    public int hashCode() {
        int hash = 1;
        for (T value : values)
            hash = 31 * hash + Objects.hashCode(value);
        return hash;
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
}
