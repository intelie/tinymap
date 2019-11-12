package net.intelie.tinymap;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class TinyOptimizer {
    private final ObjectCache cache;
    private final Deque<TinyMap.Builder<?, ?>> maps = new ArrayDeque<>();
    private final Deque<TinyList.Builder<?>> lists = new ArrayDeque<>();

    public TinyOptimizer(ObjectCache cache) {
        this.cache = cache;
    }

    public Object optimize(Object object) {
        if (object instanceof CharSequence)
            return cache.get((CharSequence) object);
        if (object instanceof Double)
            return cache.get(((Double) object));
        if (object instanceof List<?>)
            return optimizeList((Iterable<?>) object);
        if (object instanceof Map<?, ?>)
            return optimizeMap((Map<?, ?>) object);
        return object;
    }

    public <K, V> TinyMap<K, V> optimizeMap(Map<K, V> object) {
        TinyMap.Builder<K, V> map = makeMapBuilder();
        try {
            object.forEach((k, v) -> {
                map.put((K) optimize(k), (V) optimize(v));
            });
            return cache.get(map);
        } finally {
            map.clear();
            maps.add(map);
        }
    }

    private <K, V> TinyMap.Builder<K, V> makeMapBuilder() {
        TinyMap.Builder<?, ?> map = maps.poll();
        if (map == null) map = TinyMap.builder();
        return (TinyMap.Builder<K, V>) map;
    }

    public <T> TinyList<T> optimizeList(Iterable<T> object) {
        TinyList.Builder<T> list = makeListBuilder();
        try {
            object.forEach(x -> {
                list.add((T) optimize(x));
            });
            return cache.get(list);
        } finally {
            list.clear();
            lists.add(list);
        }
    }

    private <T> TinyList.Builder<T> makeListBuilder() {
        TinyList.Builder<?> list = lists.poll();
        if (list == null) list = TinyList.builder();
        return (TinyList.Builder<T>) list;
    }

}
