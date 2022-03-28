package net.intelie.tinymap.util;

import net.intelie.tinymap.*;

import java.util.*;

@SuppressWarnings("unchecked")
public class ObjectOptimizer {
    private final ObjectCache cache;
    private final Deque<TinyMapBuilder<?, ?>> maps = new ArrayDeque<>();
    private final Deque<TinyListBuilder<?>> lists = new ArrayDeque<>();
    private final Deque<TinySetBuilder<?>> sets = new ArrayDeque<>();

    public ObjectOptimizer(ObjectCache cache) {
        this.cache = cache;
    }

    public Object optimize(Object object) {
        if (object instanceof CharSequence)
            return cache != null ? cache.get((CharSequence) object) : object.toString();
        if (object instanceof Double)
            return cache != null ? cache.get(((Double) object)) : (Double) object;
        if (object instanceof Set<?>)
            return optimizeSet((Iterable<?>) object);
        if (object instanceof List<?>)
            return optimizeList((Iterable<?>) object);
        if (object instanceof Map<?, ?>)
            return optimizeMap((Map<?, ?>) object);
        return object;
    }

    public <K, V> TinyMap<K, V> optimizeMap(Map<K, V> object) {
        TinyMapBuilder<K, V> map = makeMapBuilder();
        try {
            object.forEach((k, v) -> map.put((K) optimize(k), (V) optimize(v)));
            return cache != null ? cache.get(map) : map.build();
        } finally {
            map.clear();
            maps.add(map);
        }
    }

    private <K, V> TinyMapBuilder<K, V> makeMapBuilder() {
        TinyMapBuilder<?, ?> map = maps.poll();
        if (map == null) map = TinyMap.builder();
        return (TinyMapBuilder<K, V>) map;
    }

    public <T> TinyList<T> optimizeList(Iterable<T> object) {
        TinyListBuilder<T> list = makeListBuilder();
        try {
            object.forEach(x -> list.add((T) optimize(x)));
            return cache != null ? cache.get(list) : list.build();
        } finally {
            list.clear();
            lists.add(list);
        }
    }

    private <T> TinyListBuilder<T> makeListBuilder() {
        TinyListBuilder<?> list = lists.poll();
        if (list == null) list = TinyList.builder();
        return (TinyListBuilder<T>) list;

    }

    public <T> TinySet<T> optimizeSet(Iterable<T> object) {
        TinySetBuilder<T> set = makeSetBuilder();
        try {
            object.forEach(x -> set.add((T) optimize(x)));
            return cache != null ? cache.get(set) : set.build();
        } finally {
            set.clear();
            sets.add(set);
        }
    }

    private <T> TinySetBuilder<T> makeSetBuilder() {
        TinySetBuilder<?> set = sets.poll();
        if (set == null) set = TinySet.builder();
        return (TinySetBuilder<T>) set;
    }

}
