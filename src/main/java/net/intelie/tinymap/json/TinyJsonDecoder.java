package net.intelie.tinymap.json;

import net.intelie.tinymap.*;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;

public class TinyJsonDecoder extends TinyJsonReader {
    private final ObjectCache cache;
    private final Deque<TinyMapBuilder<String, Object>> maps = new ArrayDeque<>();
    private final Deque<TinyListBuilder<Object>> lists = new ArrayDeque<>();

    public TinyJsonDecoder(ObjectCache cache) {
        this.cache = cache;
        clear();
        setLenient(true);
    }

    public TinyJsonDecoder(ObjectCache cache, Reader reader) {
        this(cache);
        setReader(reader);
    }

    public Object nextObject() throws IOException {
        JsonToken peeked = peek();
        switch (peeked) {
            case BEGIN_ARRAY:
                return nextList();
            case BEGIN_OBJECT:
                return nextMap();
            case NUMBER:
                return cache.get(nextDouble());
            case BOOLEAN:
                return nextBoolean();
            case NULL:
                nextNull();
                return null;
            default:
                return cache.get(nextString());
        }
    }

    public TinyMap<String, Object> nextMap() throws IOException {
        beginObject();
        TinyMapBuilder<String, Object> map = maps.poll();
        if (map == null) map = TinyMap.builder();
        try {
            while (hasNext()) {
                String name = cache.get(nextName());
                map.put(name, nextObject());
            }
            endObject();
            return cache.get(map);
        } finally {
            map.clear();
            maps.push(map);
        }
    }

    public TinyList<Object> nextList() throws IOException {
        beginArray();
        TinyListBuilder<Object> list = lists.poll();
        if (list == null) list = TinyList.builder();
        try {
            while (hasNext())
                list.add(nextObject());
            endArray();
            return cache.get(list);
        } finally {
            list.clear();
            lists.push(list);
        }
    }

}
