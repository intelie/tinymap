package net.intelie.tinymap;

import java.util.*;
import java.util.function.BiConsumer;

public abstract class ListMapBase<K, V> implements ListMap<K, V> {
    @SuppressWarnings("unchecked")
    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return (V) getUnsafe(key, defaultValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        return (V) getUnsafe(key, null);
    }

    @Override
    public boolean containsKey(Object key) {
        return getUnsafe(key, TOMBSTONE) != TOMBSTONE;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsValue(Object value) {
        for (int i = 0; i < size(); i++)
            if (Objects.equals(getValueAt(i), value))
                return true;
        return false;
    }

    @Override
    public Entry<K, V> getEntryAt(int index) {
        return new AbstractMap.SimpleImmutableEntry<>(getKeyAt(index), getValueAt(index));
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        int size = size();
        for (int i = 0; i < size; i++)
            action.accept(getKeyAt(i), getValueAt(i));
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException("modification not supported: " + this);
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException("modification not supported: " + this);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("modification not supported: " + this);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("modification not supported: " + this);
    }


    @Override
    public Set<K> keySet() {
        return new KeysView();
    }

    @Override
    public Collection<V> values() {
        return new ValuesView();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new EntriesView();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Map) || size() != ((Map) o).size()) return false;

        for (Entry<?, ?> entry : ((Map<?, ?>) o).entrySet())
            if (!Objects.equals(getUnsafe(entry.getKey(), TOMBSTONE), entry.getValue()))
                return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < size(); i++)
            hash += Objects.hashCode(getKeyAt(i)) ^ Objects.hashCode(getValueAt(i));
        return hash;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append('{');
        boolean first = true;
        for (int i = 0; i < size(); i++) {
            if (!first)
                sb.append(", ");
            first = false;
            sb.append(getKeyAt(i)).append('=').append(getValueAt(i));
        }
        return sb.append('}').toString();
    }


    private abstract class SimpleIterator<T> implements Iterator<T> {
        private int next = 0;

        @Override
        public boolean hasNext() {
            return next < ListMapBase.this.size();
        }

        public abstract T makeObject(int index);

        @Override
        public T next() {
            T key = makeObject(next);
            next++;
            return key;
        }
    }

    private class ValuesView extends AbstractList<V> {
        @Override
        public Iterator<V> iterator() {
            return new SimpleIterator<V>() {
                @Override
                public V makeObject(int index) {
                    return getValueAt(index);
                }
            };
        }

        @Override
        public V get(int index) {
            return getValueAt(index);
        }

        @Override
        public boolean contains(Object o) {
            return containsValue(o);
        }

        @Override
        public int size() {
            return ListMapBase.this.size();
        }
    }

    private class KeysView extends AbstractSet<K> {
        @Override
        public boolean contains(Object o) {
            return containsKey(o);
        }

        @Override
        public Iterator<K> iterator() {
            return new SimpleIterator<K>() {
                @Override
                public K makeObject(int index) {
                    return getKeyAt(index);
                }
            };
        }

        @Override
        public int size() {
            return ListMapBase.this.size();
        }
    }

    private class EntriesView extends AbstractSet<Entry<K, V>> {
        @Override
        public boolean contains(Object o) {
            if (o instanceof Entry) {
                Entry entry = (Entry) o;
                return Objects.equals(getUnsafe(entry.getKey(), TOMBSTONE), entry.getValue());
            }
            return false;
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new SimpleIterator<Entry<K, V>>() {
                @Override
                public Entry<K, V> makeObject(int index) {
                    return getEntryAt(index);
                }
            };
        }

        @Override
        public int size() {
            return ListMapBase.this.size();
        }
    }
}
