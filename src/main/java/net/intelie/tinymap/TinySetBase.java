package net.intelie.tinymap;

import net.intelie.tinymap.util.Preconditions;

import java.util.*;
import java.util.function.Consumer;

public abstract class TinySetBase<T> extends AbstractCollection<T> implements ListSet<T> {
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
        return new SetIterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        for (int i = 0; i < rawSize(); i++) {
            if (!isRemoved(i))
                action.accept(getAt(i));
        }
    }

    public int addOrGetIndex(T obj) {
        throw new UnsupportedOperationException("modification not supported: " + this);
    }

    @Override
    public boolean add(T obj) {
        return addOrGetIndex(obj) >= 0;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Set) || size() != ((Set) o).size()) return false;

        for (Object obj : ((Set) o))
            if (getIndex(obj) < 0)
                return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < rawSize(); i++)
            if (!isRemoved(i))
                hash += Objects.hashCode(getAt(i));
        return hash;
    }

    private class SetIterator implements Iterator<T> {
        private int current = -1;
        private int next = 0;

        @Override
        public boolean hasNext() {
            return next < TinySetBase.this.rawSize();
        }

        @Override
        public void remove() {
            Preconditions.checkState(current >= 0, "no iteration occurred");
            removeAt(current);
        }

        @Override
        public T next() {
            current = next;
            T key = getAt(current);
            do next++; while (isRemoved(next));
            return key;
        }
    }
}
