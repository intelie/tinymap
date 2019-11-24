package net.intelie.tinymap;

import java.util.Collection;
import java.util.Map;

public interface ListCollection<T> extends Collection<T> {
    int getIndex(Object key);

    T getEntryAt(int index);

    void removeAt(int index);

    boolean isRemoved(int index);

    int rawSize();
}
