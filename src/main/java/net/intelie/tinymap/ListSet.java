package net.intelie.tinymap;

import java.util.Set;

public interface ListSet<T> extends Set<T> {
    int getIndex(Object key);

    T getAt(int index);

    void removeAt(int index);

    boolean isRemoved(int index);

    int rawSize();
}
