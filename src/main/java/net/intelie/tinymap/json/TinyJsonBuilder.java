package net.intelie.tinymap.json;

import net.intelie.tinymap.ObjectCache;
import net.intelie.tinymap.TinyList;
import net.intelie.tinymap.TinyMap;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class TinyJsonBuilder {
    private final ObjectCache cache;
    private final Deque<TinyMap.Builder<String, Object>> maps = new ArrayDeque<>();
    private final Deque<TinyList.Builder<Object>> lists = new ArrayDeque<>();

    public TinyJsonBuilder(ObjectCache cache) {
        this.cache = cache;
    }


    public Object build(TinyJsonReader reader) throws IOException {
        JsonToken peeked = reader.peek();
        switch (peeked) {
            case BEGIN_ARRAY:
                return buildList(reader);
            case BEGIN_OBJECT:
                return buildMap(reader);
            case STRING:
                return reader.nextString();
            case NUMBER:
                return cache.get(reader.nextDouble());
            case BOOLEAN:
                return reader.nextBoolean();
            case NULL:
                return null;
            default:
                throw new IllegalStateException("Illegal token: " + peeked);
        }
    }

    public TinyMap<String, Object> buildMap(TinyJsonReader reader) throws IOException {
        reader.beginObject();
        TinyMap.Builder<String, Object> map = maps.poll();
        if (map == null) map = TinyMap.builder();
        try {
            while (reader.hasNext()) {
                String name = reader.nextName();
                map.put(name, build(reader));
            }
            reader.endObject();
            return cache.get(map);
        } finally {
            map.clear();
            maps.add(map);
        }
    }

    public TinyList<Object> buildList(TinyJsonReader reader) throws IOException {
        reader.beginArray();
        TinyList.Builder<Object> list = lists.poll();
        if (list == null) list = TinyList.builder();
        try {
            while (reader.hasNext())
                list.add(build(reader));
            reader.endArray();
            return cache.get(list);
        } finally {
            list.clear();
            lists.add(list);
        }
    }

}
