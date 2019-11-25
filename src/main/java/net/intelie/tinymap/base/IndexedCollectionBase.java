package net.intelie.tinymap.base;

import net.intelie.tinymap.util.Preconditions;

import java.util.AbstractCollection;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class IndexedCollectionBase<T> extends AbstractCollection<T> implements IndexedCollection<T> {
    @Override
    public int getIndex(Object key) {
        for (int i = 0; i < rawSize(); i++)
            if (!isRemoved(i) && Objects.equals(getEntryAt(i), key))
                return i;
        return -1;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("modification not supported: " + this);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return getIndex(o) >= 0;
    }

    @Override
    public ListIterator<T> iterator() {
        return iterator(0);
    }

    @Override
    public ListIterator<T> iterator(int fromIndex) {
        return new CollectionIterator(fromIndex);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        for (int i = 0; i < rawSize(); i++) {
            if (!isRemoved(i))
                action.accept(getEntryAt(i));
        }
    }

    @Override
    public boolean add(T obj) {
        return addOrGetIndex(obj) < 0;
    }

    @Override
    public boolean remove(Object o) {
        int index = getIndex(o);
        if (index < 0) return false;
        removeAt(index);
        return true;
    }


    public interface NoAdditiveChange<T> extends IndexedCollection<T> {
        @Override
        default int addOrGetIndex(T obj) {
            throw new UnsupportedOperationException("modification not supported: " + this);
        }

        @Override
        default void add(int index, T obj) {
            throw new UnsupportedOperationException("modification not supported: " + this);
        }

        @Override
        default T set(int index, T obj) {
            throw new UnsupportedOperationException("modification not supported: " + this);
        }
    }

    public interface Immutable<T> extends NoAdditiveChange<T> {
        @Override
        default boolean removeAt(int index) {
            throw new UnsupportedOperationException("modification not supported: " + this);
        }


        @Override
        default boolean isRemoved(int index) {
            return false;
        }

        @Override
        default int rawSize() {
            return size();
        }

    }

    private class CollectionIterator implements ListIterator<T> {
        private int current;
        private int next = 0;
        private int prev;

        public CollectionIterator(int fromIndex) {
            this.current = findNext(fromIndex);
            this.next = findNext(fromIndex);
            this.prev = findPrev(fromIndex - 1);
        }

        private int findNext(int index) {
            while (index < rawSize() && isRemoved(index))
                index++;
            return index;
        }

        private int findPrev(int index) {
            while (index >= 0 && isRemoved(index))
                index--;
            return index;
        }

        @Override
        public boolean hasNext() {
            return next < rawSize();
        }

        @Override
        public boolean hasPrevious() {
            return prev >= 0;
        }

        @Override
        public int nextIndex() {
            return next;
        }

        @Override
        public int previousIndex() {
            return prev;
        }

        @Override
        public void set(T obj) {
            IndexedCollectionBase.this.set(current, obj);
        }

        @Override
        public void add(T obj) {
            IndexedCollectionBase.this.add(current, obj);
        }

        @Override
        public void remove() {
            Preconditions.checkState(current >= 0, "no iteration occurred");
            if (removeAt(current))
                next--;
        }

        @Override
        public T previous() {
            if (!hasPrevious())
                throw new NoSuchElementException();
            next = current = prev;
            prev = findPrev(prev - 1);
            return getEntryAt(current);
        }

        @Override
        public T next() {
            if (!hasNext())
                throw new NoSuchElementException();
            prev = current = next;
            next = findNext(next + 1);
            return getEntryAt(current);
        }
    }
}
