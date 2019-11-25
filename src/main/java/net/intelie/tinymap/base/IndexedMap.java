package net.intelie.tinymap.base;

import java.util.Map;

public interface IndexedMap<K, V> extends Map<K, V> {
    int getIndex(Object key);

    K getKeyAt(int index);

    V getValueAt(int index);

    Entry<K, V> getEntryAt(int index);

    V removeAt(int index);

    V setValueAt(int index, V value);

    boolean isRemoved(int index);

    int rawSize();

    Object getUnsafe(Object key, Object defaultValue);

    @Override
    IndexedSet<K> keySet();

    @Override
    IndexedSet<Entry<K, V>> entrySet();
}
