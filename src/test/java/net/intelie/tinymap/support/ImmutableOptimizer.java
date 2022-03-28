package net.intelie.tinymap.support;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.intelie.tinymap.ObjectCache;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ImmutableOptimizer {
    private final ObjectCache cache;

    public ImmutableOptimizer(ObjectCache cache) {
        this.cache = cache;
    }

    public Object optimize(Object object) {
        if (object instanceof CharSequence)
            return cache != null ? cache.get((CharSequence) object) : object.toString();
        if (object instanceof Double)
            return cache != null ? cache.get(((Double) object)) : (Double) object;
        if (object instanceof List<?>)
            return optimizeList((Iterable<?>) object);
        if (object instanceof Map<?, ?>)
            return optimizeMap((Map<?, ?>) object);
        return object;
    }

    public <K, V> Map<K, V> optimizeMap(Map<K, V> object) {
        ImmutableMap.Builder<K, V> map = ImmutableMap.builder();
        object.forEach((k, v) -> map.put((K) optimize(k), (V) optimize(v)));
        return map.build();
    }

    public <T> List<T> optimizeList(Iterable<T> object) {
        ImmutableList.Builder<T> list = ImmutableList.builder();
        object.forEach(x -> list.add((T) optimize(x)));
        return list.build();
    }
}
