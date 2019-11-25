package net.intelie.tinymap.base;

import net.intelie.tinymap.util.Preconditions;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class IndexedListBase<T> extends IndexedCollectionBase<T> implements IndexedList<T> {
    @Override
    public boolean addAll(int index, Collection<? extends T> collection) {
        for (T obj : collection)
            add(index++, obj);
        return collection.size() > 0;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) return true;
        if (!(that instanceof List) || size() != ((List) that).size()) return false;

        List<?> list = (List<?>) that;
        for (int i = 0; i < rawSize(); i++) {
            if (!Objects.equals(getEntryAt(i), list.get(i)))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        for (int i = 0; i < rawSize(); i++)
            hash = 31 * hash + Objects.hashCode(getEntryAt(i));
        return hash;
    }

    @Override
    public final boolean isRemoved(int index) {
        return false;
    }

    @Override
    public final int rawSize() {
        return size();
    }

    @Override
    public T get(int index) {
        return getEntryAt(index);
    }

    @Override
    public void add(int index, T obj) {
        Preconditions.checkElementIndex(index, size() + 1);
        for (int i = index; i < rawSize(); i++)
            obj = set(i, obj);
        add(obj);
    }

    @Override
    public boolean removeAt(int index) {
        remove(index);
        return true;
    }

    @Override
    public T remove(int index) {
        Preconditions.checkElementIndex(index, rawSize());
        T obj = removeLast();
        for (int i = rawSize() - 1; i >= index; i--)
            obj = set(i, obj);
        return obj;
    }

    @Override
    public int indexOf(Object o) {
        for (int i = 0; i < rawSize(); i++)
            if (Objects.equals(getEntryAt(i), o))
                return i;
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        for (int i = rawSize() - 1; i >= 0; i--)
            if (Objects.equals(getEntryAt(i), o))
                return i;
        return -1;
    }

    @Override
    public ListIterator<T> listIterator() {
        return iterator();
    }

    @Override
    public ListIterator<T> listIterator(int fromIndex) {
        return iterator(fromIndex);
    }

    @Override
    public IndexedList<T> subList(int fromIndex, int toIndex) {
        return new ViewList(fromIndex, toIndex);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        for (int i = 0; i < rawSize(); i++) {
            action.accept(getEntryAt(i));
        }
    }

    public interface Immutable<T> extends IndexedList<T> {
        @Override
        default T set(int index, T obj) {
            throw new UnsupportedOperationException("modification not supported: " + this);
        }

        @Override
        default T removeLast() {
            throw new UnsupportedOperationException("modification not supported: " + this);
        }

        @Override
        default int addOrGetIndex(T obj) {
            throw new UnsupportedOperationException("modification not supported: " + this);
        }
    }

    public class ViewList extends IndexedListBase<T> implements Serializable {
        private final int fromIndex;
        private int toIndex;

        public ViewList(int fromIndex, int toIndex) {
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
        }

        @Override
        public int addOrGetIndex(T obj) {
            IndexedListBase.this.add(toIndex, obj);
            toIndex++;
            return -1;
        }

        @Override
        public int size() {
            return toIndex - fromIndex;
        }

        @Override
        public T set(int index, T obj) {
            Preconditions.checkElementIndex(index, toIndex - fromIndex);
            return IndexedListBase.this.set(index + fromIndex, obj);

        }

        @Override
        public T removeLast() {
            return IndexedListBase.this.remove(toIndex - 1);
        }

        @Override
        public T getEntryAt(int index) {
            Preconditions.checkElementIndex(index, toIndex - fromIndex);
            return IndexedListBase.this.getEntryAt(index + fromIndex);
        }
    }


}
