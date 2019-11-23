package net.intelie.tinymap;

import net.intelie.tinymap.util.Preconditions;

import java.util.*;
import java.util.function.BiConsumer;

public abstract class TinyMapBase<K, V> implements ListMap<K, V> {
    private static final Object SENTINEL = new Object();

    @SuppressWarnings("unchecked")
    @Override
    public V getOrDefault(Object key, V defaultValue) {
        int index = getIndex(key);
        if (index < 0) return defaultValue;
        return getValueAt(index);
    }

    @SuppressWarnings("unchecked")
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
        for (int i = 0; i < size(); i++)
            if (Objects.equals(getValueAt(i), value))
                return true;
        return false;
    }

    @Override
    public Entry<K, V> getEntryAt(int index) {
        return new ListEntry(index);
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
            if (!Objects.equals(getUnsafe(entry.getKey(), SENTINEL), entry.getValue()))
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

    private abstract class ListIterator<T> implements Iterator<T> {
        private int current = -1;
        private int next = 0;

        @Override
        public boolean hasNext() {
            return next < TinyMapBase.this.rawSize();
        }

        public abstract T makeObject(int index);

        @Override
        public void remove() {
            Preconditions.checkState(current >= 0, "no iteration occurred");
            removeAt(current);
        }

        @Override
        public T next() {
            current = next;
            T key = makeObject(current);
            do next++; while (isRemoved(next));
            return key;
        }
    }

    private class ValuesView extends AbstractCollection<V> {
        @Override
        public Iterator<V> iterator() {
            return new ListIterator<V>() {
                @Override
                public V makeObject(int index) {
                    return getValueAt(index);
                }
            };
        }

        @Override
        public boolean contains(Object o) {
            return containsValue(o);
        }

        @Override
        public int size() {
            return TinyMapBase.this.size();
        }

        @Override
        public void clear() {
            TinyMapBase.this.clear();
        }
    }

    private class KeysView extends AbstractSet<K> {
        @Override
        public boolean contains(Object o) {
            return containsKey(o);
        }

        @Override
        public Iterator<K> iterator() {
            return new ListIterator<K>() {
                @Override
                public K makeObject(int index) {
                    return getKeyAt(index);
                }
            };
        }

        @Override
        public int size() {
            return TinyMapBase.this.size();
        }

        @Override
        public void clear() {
            TinyMapBase.this.clear();
        }
    }

    private class EntriesView extends AbstractSet<Entry<K, V>> {
        @Override
        public boolean contains(Object o) {
            if (o instanceof Entry) {
                Entry entry = (Entry) o;
                return Objects.equals(getUnsafe(entry.getKey(), SENTINEL), entry.getValue());
            }
            return false;
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new ListIterator<Entry<K, V>>() {
                @Override
                public Entry<K, V> makeObject(int index) {
                    return getEntryAt(index);
                }
            };
        }

        @Override
        public int size() {
            return TinyMapBase.this.size();
        }

        @Override
        public void clear() {
            TinyMapBase.this.clear();
        }
    }

    private class ListEntry implements Entry<K, V> {
        private final int index;

        public ListEntry(int index) {
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
            if (!(o instanceof Entry<?, ?>)) return false;
            Entry<?, ?> that = (Entry<?, ?>) o;
            return Objects.equals(getKey(), that.getKey()) && Objects.equals(getValue(), that.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getKeyAt(index)) ^ Objects.hashCode(getValueAt(index));
        }

        @Override
        public String toString() {
            return getKey() + "=" + getValue();
        }
    }
}
