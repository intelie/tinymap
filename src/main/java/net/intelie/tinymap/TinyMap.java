package net.intelie.tinymap;

import net.intelie.tinymap.base.IndexedMapBase;
import net.intelie.tinymap.util.TinyMapGenerated;

import java.io.Serializable;

public abstract class TinyMap<K, V> extends IndexedMapBase<K, V> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final TinySet<K> keys;

    protected TinyMap(TinySet<K> keys) {
        this.keys = keys;
    }

    public static <K, V> TinyMap<K, V> createUnsafe(TinySet<K> keys, Object[] values) {
        //return new TinyMapGenerated.SizeAny<>(keys, values);
        return TinyMapGenerated.createUnsafe(keys, values);
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

    @Override
    public K getKeyAt(int index) {
        return keys.getEntryAt(index);
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
