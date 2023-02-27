package net.intelie.tinymap.support;

import net.intelie.tinymap.ObjectCache;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class JavaOptimizer {
    private final ObjectCache cache;

    public JavaOptimizer(ObjectCache cache) {
        this.cache = cache;
    }

    public Object optimize(Object object) {
        if (object instanceof CharSequence)
            return cache != null ? cache.get((CharSequence) object) : new StringBuilder().append(object).toString();
        if (object instanceof Double)
            return cache != null ? cache.get(((Double) object)) : Double.valueOf((Double) object);
        if (object instanceof List<?>)
            return optimizeList((Iterable<?>) object);
        if (object instanceof Map<?, ?>)
            return optimizeMap((Map<?, ?>) object);
        return object;
    }

    public <K, V> LinkedHashMap<K, V> optimizeMap(Map<K, V> object) {
        LinkedHashMap<K, V> map = new LinkedHashMap<>();
        object.forEach((k, v) -> map.put((K) optimize(k), (V) optimize(v)));
        return map;
    }

    public <T> ArrayList<T> optimizeList(Iterable<T> object) {
        ArrayList<T> list = new ArrayList<>();
        object.forEach(x -> list.add((T) optimize(x)));
        return list;
    }
}
