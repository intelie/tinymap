package net.intelie.tinymap.json;

import net.intelie.tinymap.ObjectCache;
import net.intelie.tinymap.TinyList;
import net.intelie.tinymap.TinyMap;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;

public class TinyJsonDecoder extends TinyJsonReader {
    private final ObjectCache cache;
    private final Deque<TinyMap.Builder<String, Object>> maps = new ArrayDeque<>();
    private final Deque<TinyList.Builder<Object>> lists = new ArrayDeque<>();

    public TinyJsonDecoder(ObjectCache cache, Reader reader) {
        super(cache, reader);
        setLenient(true);
        this.cache = cache;
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
                return nextString();
        }
    }

    public TinyMap<String, Object> nextMap() throws IOException {
        beginObject();
        TinyMap.Builder<String, Object> map = maps.poll();
        if (map == null) map = TinyMap.builder();
        try {
            while (hasNext()) {
                String name = nextName();
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
        TinyList.Builder<Object> list = lists.poll();
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
