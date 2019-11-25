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
    public void removeAt(int index) {
        throw new UnsupportedOperationException("modification not supported: " + this);
    }

    @Override
    public boolean isRemoved(int index) {
        return false;
    }

    @Override
    public int rawSize() {
        return size();
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
        return new CollectionIterator(0, rawSize());
    }

    @Override
    public ListIterator<T> iterator(int fromIndex) {
        return new CollectionIterator(fromIndex, rawSize());
    }

    @Override
    public ListIterator<T> iterator(int fromIndex, int toIndex) {
        return new CollectionIterator(fromIndex, toIndex);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        for (int i = 0; i < rawSize(); i++) {
            if (!isRemoved(i))
                action.accept(getEntryAt(i));
        }
    }

    public int addOrGetIndex(T obj) {
        throw new UnsupportedOperationException("modification not supported: " + this);
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

    @Override
    public void clear() {
        throw new UnsupportedOperationException("modification not supported: " + this);
    }

    private class CollectionIterator implements ListIterator<T> {
        private final int toIndex;
        private final int fromIndex;
        private int current;
        private int next = 0;
        private int prev;

        public CollectionIterator(int fromIndex, int toIndex) {
            this.next = fromIndex;
            do next++; while (isRemoved(next) && next < toIndex);

            this.prev = fromIndex - 1;
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
        }

        @Override
        public boolean hasNext() {
            return next < toIndex;
        }

        @Override
        public boolean hasPrevious() {
            return prev >= fromIndex;
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
        public void set(T t) {
            throw new UnsupportedOperationException("not implemented for ListIterator");

        }

        @Override
        public void add(T obj) {
            throw new UnsupportedOperationException("not implemented for ListIterator");
        }

        @Override
        public void remove() {
            Preconditions.checkState(current >= 0, "no iteration occurred");
            removeAt(current);
        }

        @Override
        public T previous() {
            if (!hasPrevious())
                throw new NoSuchElementException();
            next = current;
            current = prev;
            T key = getEntryAt(current);
            do prev--; while (isRemoved(prev) && prev >= fromIndex);
            return key;
        }

        @Override
        public T next() {
            if (!hasNext())
                throw new NoSuchElementException();
            prev = current;
            current = next;
            T key = getEntryAt(current);
            do next++; while (isRemoved(next) && next < toIndex);
            return key;
        }
    }
}
