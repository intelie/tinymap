package net.intelie.tinymap;

import net.intelie.tinymap.base.IndexedListBase;

import java.io.Serializable;

public class TinyList<T> extends IndexedListBase<T> implements Serializable, IndexedListBase.Immutable<T> {
    private static final long serialVersionUID = 1L;

    private final Object[] values;

    public TinyList(Object[] values) {
        this.values = values;
    }

    public static <T> TinyListBuilder<T> builder() {
        return new TinyListBuilder<>();
    }

    @Override
    public int size() {
        return values.length;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getEntryAt(int index) {
        return (T) values[index];
    }
}
