package net.intelie.tinymap;

import net.intelie.tinymap.util.Preconditions;

import java.io.Serializable;

public class TinyMap<K, V> extends TinyMapBase<K, V> implements Serializable {
    private final TinySet<K> keys;
    private final Object[] values;

    private TinyMap(TinySet<K> keys, Object[] values) {
        Preconditions.checkArgument(keys.size() == values.length, "keys and values must have same size");
        this.keys = keys;
        this.values = values;
    }

    public static <K, V> TinyMap<K, V> createUnsafe(TinySet<K> keys, Object[] values) {
        return new TinyMap<>(keys, values);
    }

    public static <K, V> TinyMapBuilder<K, V> builder() {
        return new TinyMapBuilder<>();
    }

    public boolean sharesKeysWith(TinyMap<K, V> other) {
        return keys == other.keys;
    }

    public long debugCollisions(V key) {
        return keys.debugCollisions(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public K getKeyAt(int index) {
        return keys.getAt(index);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V getValueAt(int index) {
        return (V) values[index];
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public int getIndex(Object key) {
        return keys.getIndex(key);
    }

    @Override
    public TinySet<K> keySet() {
        return keys;
    }
}
