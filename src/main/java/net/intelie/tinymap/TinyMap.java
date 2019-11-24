package net.intelie.tinymap;

import net.intelie.tinymap.util.Preconditions;

import java.io.Serializable;

public abstract class TinyMap<K, V> extends TinyMapBase<K, V> implements Serializable {
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

    @SuppressWarnings("unchecked")
    @Override
    public K getKeyAt(int index) {
        return keys.getAt(index);
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

    public static class Op8<K, V> extends TinyMap<K, V> implements Serializable {
        private final Object v1;
        private final Object v2;
        private final Object v3;
        private final Object v4;
        private final Object v5;
        private final Object v6;
        private final Object v7;
        private final Object v8;

        public Op8(TinySet<K> keys, Object v1, Object v2, Object v3, Object v4, Object v5, Object v6, Object v7, Object v8) {
            super(keys);
            Preconditions.checkArgument(keys.size() == 8, "keys and values must have same size");
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
            this.v7 = v7;
            this.v8 = v8;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V getValueAt(int index) {
            switch (index) {
                case 0:
                    return (V) v1;
                case 1:
                    return (V) v2;
                case 2:
                    return (V) v3;
                case 3:
                    return (V) v4;
                case 4:
                    return (V) v5;
                case 5:
                    return (V) v6;
                case 6:
                    return (V) v7;
                case 7:
                    return (V) v8;
                default:
                    throw new ArrayIndexOutOfBoundsException(index);
            }
        }
    }
}
