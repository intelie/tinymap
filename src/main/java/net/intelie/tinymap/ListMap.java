package net.intelie.tinymap;

import java.util.Map;

public interface ListMap<K, V> extends Map<K, V> {
    int getIndex(Object key);

    K getKeyAt(int index);

    V getValueAt(int index);

    Entry<K, V> getEntryAt(int index);

    Object getUnsafe(Object key, Object defaultValue);
}
