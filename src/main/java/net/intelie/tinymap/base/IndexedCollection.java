package net.intelie.tinymap.base;

import java.util.Collection;
import java.util.ListIterator;

public interface IndexedCollection<T> extends Collection<T> {
    int addOrGetIndex(T obj);

    void add(int index, T obj);

    T set(int index, T obj);

    int getIndex(Object key);

    T getEntryAt(int index);

    void removeAt(int index);

    boolean isRemoved(int index);

    int rawSize();

    @Override
    ListIterator<T> iterator();

    ListIterator<T> iterator(int fromIndex);

    ListIterator<T> iterator(int fromIndex, int toIndex);
}
