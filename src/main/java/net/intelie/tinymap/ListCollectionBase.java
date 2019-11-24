package net.intelie.tinymap;

import net.intelie.tinymap.util.Preconditions;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class ListCollectionBase<T> extends AbstractCollection<T> implements ListCollection<T> {
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
    public Iterator<T> iterator() {
        return new CollectionIterator();
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

    private class CollectionIterator implements Iterator<T> {
        private int current = -1;
        private int next = 0;

        @Override
        public boolean hasNext() {
            return next < ListCollectionBase.this.rawSize();
        }

        @Override
        public void remove() {
            Preconditions.checkState(current >= 0, "no iteration occurred");
            removeAt(current);
        }

        @Override
        public T next() {
            current = next;
            T key = getEntryAt(current);
            do next++; while (isRemoved(next));
            return key;
        }
    }
}
