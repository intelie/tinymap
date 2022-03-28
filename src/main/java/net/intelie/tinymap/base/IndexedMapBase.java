package net.intelie.tinymap.base;

import net.intelie.tinymap.util.Preconditions;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public abstract class IndexedMapBase<K, V> implements IndexedMap<K, V> {
    private static final Object SENTINEL = new Object();

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        int index = getIndex(key);
        if (index < 0) return defaultValue;
        return getValueAt(index);
    }

    @Override
    public V get(Object key) {
        int index = getIndex(key);
        if (index < 0) return null;
        return getValueAt(index);
    }

    @Override
    public boolean containsKey(Object key) {
        return getUnsafe(key, SENTINEL) != SENTINEL;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsValue(Object value) {
        for (int i = 0; i < rawSize(); i++)
            if (!isRemoved(i) && Objects.equals(value, getValueAt(i)))
                return true;
        return false;
    }

    @Override
    public Entry<K, V> getEntryAt(int index) {
        Preconditions.checkElementIndex(index, rawSize());
        return new IndexedEntry(index);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        int size = rawSize();
        for (int i = 0; i < size; i++)
            if (!isRemoved(i))
                action.accept(getKeyAt(i), getValueAt(i));
    }

    @Override
    public V removeAt(int index) {
        throw new UnsupportedOperationException("modification not supported: " + this);
    }

    @Override
    public V setValueAt(int index, V value) {
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
    public Object getUnsafe(Object key, Object defaultValue) {
        int index = getIndex(key);
        if (index < 0) return defaultValue;
        return getValueAt(index);
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException("modification not supported: " + this);
    }

    @Override
    public V remove(Object key) {
        int index = getIndex(key);
        if (index < 0) return null;
        return removeAt(index);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("modification not supported: " + this);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public IndexedSet<K> keySet() {
        return new KeysView();
    }

    @Override
    public Collection<V> values() {
        return new ValuesView();
    }

    @Override
    public IndexedSet<Map.Entry<K, V>> entrySet() {
        return new EntriesView();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Map<?, ?>) || size() != ((Map<?, ?>) o).size()) return false;

        for (Map.Entry<?, ?> entry : ((Map<?, ?>) o).entrySet())
            if (!Objects.equals(entry.getValue(), getUnsafe(entry.getKey(), SENTINEL)))
                return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < rawSize(); i++)
            if (!isRemoved(i))
                hash += Objects.hashCode(getKeyAt(i)) ^ Objects.hashCode(getValueAt(i));
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append('{');
        boolean first = true;
        for (int i = 0; i < rawSize(); i++) {
            if (isRemoved(i)) continue;

            if (!first)
                sb.append(", ");
            first = false;
            sb.append(getKeyAt(i)).append('=').append(getValueAt(i));
        }
        return sb.append('}').toString();
    }

    private class ValuesView extends IndexedCollectionBase<V> implements Serializable, IndexedCollectionBase.NoAdditiveChange<V> {
        private static final long serialVersionUID = 1L;

        @Override
        public V getEntryAt(int index) {
            return getValueAt(index);
        }

        @Override
        public void clear() {
            IndexedMapBase.this.clear();
        }

        @Override
        public boolean removeAt(int index) {
            IndexedMapBase.this.removeAt(index);
            return false;
        }

        @Override
        public boolean isRemoved(int index) {
            return IndexedMapBase.this.isRemoved(index);
        }

        @Override
        public int rawSize() {
            return IndexedMapBase.this.rawSize();
        }

        @Override
        public int size() {
            return IndexedMapBase.this.size();
        }
    }

    private class KeysView extends IndexedSetBase<K> implements Serializable, IndexedCollectionBase.NoAdditiveChange<K> {
        private static final long serialVersionUID = 1L;

        @Override
        public int getIndex(Object key) {
            return IndexedMapBase.this.getIndex(key);
        }

        @Override
        public K getEntryAt(int index) {
            return getKeyAt(index);
        }

        @Override
        public void clear() {
            IndexedMapBase.this.clear();
        }

        @Override
        public boolean removeAt(int index) {
            IndexedMapBase.this.removeAt(index);
            return false;
        }

        @Override
        public boolean isRemoved(int index) {
            return IndexedMapBase.this.isRemoved(index);
        }

        @Override
        public int rawSize() {
            return IndexedMapBase.this.rawSize();
        }

        @Override
        public int size() {
            return IndexedMapBase.this.size();
        }
    }

    private class EntriesView extends IndexedSetBase<Map.Entry<K, V>> implements Serializable, IndexedCollectionBase.NoAdditiveChange<Map.Entry<K, V>> {
        private static final long serialVersionUID = 1L;

        @Override
        public int getIndex(Object key) {
            if (!(key instanceof Map.Entry<?, ?>))
                return -1;
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) key;
            int index = IndexedMapBase.this.getIndex(entry.getKey());
            if (index < 0 || Objects.equals(entry.getValue(), getValueAt(index)))
                return index;
            return -1;
        }

        @Override
        public Entry<K, V> getEntryAt(int index) {
            return IndexedMapBase.this.getEntryAt(index);
        }

        @Override
        public void clear() {
            IndexedMapBase.this.clear();
        }

        @Override
        public boolean removeAt(int index) {
            IndexedMapBase.this.removeAt(index);
            return false;
        }

        @Override
        public boolean isRemoved(int index) {
            return IndexedMapBase.this.isRemoved(index);
        }

        @Override
        public int rawSize() {
            return IndexedMapBase.this.rawSize();
        }

        @Override
        public int size() {
            return IndexedMapBase.this.size();
        }
    }

    private class IndexedEntry implements IndexedMap.Entry<K, V>, Serializable {
        private static final long serialVersionUID = 1L;

        private final int index;

        public IndexedEntry(int index) {
            this.index = index;
        }

        @Override
        public K getKey() {
            return getKeyAt(index);
        }

        @Override
        public V getValue() {
            return getValueAt(index);
        }

        @Override
        public V setValue(V value) {
            return setValueAt(index, value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Map.Entry<?, ?>)) return false;
            Map.Entry<?, ?> that = (Map.Entry<?, ?>) o;
            return Objects.equals(that.getKey(), getKey()) && Objects.equals(that.getValue(), getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getKeyAt(index)) ^ Objects.hashCode(getValueAt(index));
        }

        @Override
        public String toString() {
            return getKey() + "=" + getValue();
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public boolean isRemoved() {
            return IndexedMapBase.this.isRemoved(index);
        }
    }
}
