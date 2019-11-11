package net.intelie.tinymap;

import com.google.common.collect.ImmutableMap;
import net.intelie.introspective.ObjectSizer;
import net.intelie.introspective.ThreadResources;
import net.intelie.introspective.reflect.ReflectionCache;
import net.intelie.introspective.util.IdentityVisitedSet;
import net.intelie.tinymap.json.FastDouble;
import net.intelie.tinymap.json.JsonToken;
import net.intelie.tinymap.json.TinyJsonBuilder;
import net.intelie.tinymap.json.TinyJsonReader;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Ignore
public class Playground {
    @Test
    public void map() {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        Map<String, Object> map = new LinkedHashMap<>();
        TinyMap.Builder<String, Object> builder2 = TinyMap.builder();
        System.out.println(0 + "\t" + SizeUtils.sizeNoStrings(map) + "\t" + SizeUtils.sizeNoStrings(builder.build()) + "\t" + SizeUtils.sizeNoStrings(builder2.build()));
        String[] keys = new String[100];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = "key" + i;
            map.put(keys[i], "value" + i);
            builder.put(keys[i], "value" + i);
            builder2.put(keys[i], "value" + i);
            System.out.println((i + 1) + "\t" + SizeUtils.sizeNoStrings(map) + "\t" + SizeUtils.sizeNoStrings(builder.build()) + "\t" + SizeUtils.sizeNoStrings(builder2.build()));
        }
        //map = builder.build();
        map = builder2.buildAndClear();

        if (map instanceof TinyMap.Small) {
            long total = 0;
            for (String key : keys) {
                total += ((TinyMap<String, Object>) map).debugCollisions(key);
            }
            System.out.println(total / (double) keys.length);
        }

        for (int i = 0; i < 100000; i++)
            for (String key : keys)
                map.get(key);


        long startTime = System.nanoTime();
        long startMem = ThreadResources.allocatedBytes();
        for (int i = 0; i < 10000000; i++)
            for (String key : keys)
                map.get(key);

        System.out.println(ThreadResources.allocatedBytes() - startMem);
        System.out.println((System.nanoTime() - startTime) / 1e9);
    }

    @Test
    public void gson() throws IOException {
        List<Object> objs = new ArrayList<>();

        long startMem = ThreadResources.allocatedBytes();

        ObjectCache cache = new ObjectCache();

        try (BufferedReader reader = new BufferedReader(new FileReader("/home/juanplopes/Downloads/everything50k.json"))) {
            TinyJsonReader jsonReader = new TinyJsonReader(cache, new StringBuilder(), reader);
            TinyJsonBuilder builder = new TinyJsonBuilder(cache);
            while (jsonReader.peek() != JsonToken.END_DOCUMENT) {
                objs.addAll(builder.buildList(jsonReader));
//                String line = reader.readLine();
//                if (line == null) break;
//                Map map = (Map) LiveJson.fromJson(line, List.class).get(0);
//                objs.add(map);
            }
        }
        System.out.println(SizeUtils.formatBytes(ThreadResources.allocatedBytes() - startMem));

        System.out.println(SizeUtils.formattedSize(objs));

        ObjectSizer sizer = new ObjectSizer();
        sizer.resetTo(objs);

        Map<Class, AtomicLong> counts = new HashMap<>();
        Map<Class, AtomicLong> total = new HashMap<>();
        Map<Object, AtomicLong> doubleCount = new HashMap<>();

        while (sizer.moveNext()) {
            counts.computeIfAbsent(sizer.type(), x -> new AtomicLong()).incrementAndGet();
            total.computeIfAbsent(sizer.type(), x -> new AtomicLong()).addAndGet(sizer.bytes());
            doubleCount.computeIfAbsent(sizer.current(), x -> new AtomicLong()).incrementAndGet();
        }
        total.entrySet().stream().sorted(Comparator.comparing(x -> -x.getValue().get())).forEach(entry -> {
            System.out.println(counts.get(entry.getKey()) + "   \t" + SizeUtils.formatBytes(entry.getValue().get()) + "\t" + entry.getKey());
        });

        System.out.println(doubleCount.entrySet().stream()
                .sorted(Comparator.comparingLong(x -> -x.getValue().get()))
                .mapToLong(x -> x.getValue().get() - 1)
                .sum());

        List<Map.Entry<Object, AtomicLong>> list = doubleCount.entrySet().stream()
                .sorted(Comparator.comparingLong(x -> -x.getValue().get()))
                .limit(10)
                .collect(Collectors.toList());

//        System.out.println(list.size());
        for (Map.Entry<Object, AtomicLong> entry : list) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
        System.out.println(SizeUtils.formattedSize(cache));
    }


    @Test
    public void testDoubleParseDouble() throws IOException {
        long[] test = new long[10000];
        long startMem = ThreadResources.allocatedBytes();
        for (int i = 0; i < test.length; i++) {
            test[i] = ThreadResources.allocatedBytes() - startMem;
            FastDouble.getDouble("123", 0, 3);
        }

        for (int i = 0; i < test.length; i++) {
            if ((i - 1) % 100 == 0)
                System.out.println(i + "\t" + test[i]);
        }
    }
}
