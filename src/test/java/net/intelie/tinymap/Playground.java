package net.intelie.tinymap;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import net.intelie.introspective.ObjectSizer;
import net.intelie.introspective.ThreadResources;
import net.intelie.tinymap.json.TinyJsonDecoder;
import net.intelie.tinymap.json.TinyJsonReader;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

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

        TinyJsonDecoder builder = new TinyJsonDecoder(cache);
        TinyOptimizer optimizer = new TinyOptimizer(cache);
        Gson gson = new Gson();

        try (BufferedReader reader = new BufferedReader(new FileReader("/home/juanplopes/Downloads/everything50k.json"))) {
            TinyJsonReader jsonReader = new TinyJsonReader(cache, new StringBuilder(), reader);
            while (true) {
//                if (jsonReader.peek() == JsonToken.END_DOCUMENT) break;
//                objs.addAll(builder.buildList(jsonReader));
                String line = reader.readLine();
                if (line == null) break;
                Map map = (Map) gson.fromJson(line, List.class).get(0);
                objs.add(map);
            }
        }
        System.out.println("allocated\t" + SizeUtils.formatBytes(ThreadResources.allocatedBytes() - startMem));

        List<Object> optimized = optimizer.optimizeList(objs);

        System.out.println("cache size\t" + SizeUtils.formattedSize(cache));
        System.out.println("regular\t" + SizeUtils.formattedSize(objs));
        System.out.println("optimized\t" + SizeUtils.formattedSize(optimized));
        System.out.println("equals\t" + objs.equals(optimized) + "\t" + optimized.equals(objs));

        for (int i = 0; i < Math.max(objs.size(), optimized.size()); i++) {
            if (!Objects.equals(objs.get(i), optimized.get(i)))
                System.out.println("ne\t" + i + "\t" + objs.get(i) + "\t" + optimized.get(i));
        }

        ObjectSizer sizer = new ObjectSizer();
        sizer.resetTo(optimized);

        Map<Class, AtomicLong> counts = new HashMap<>();
        Map<Class, AtomicLong> total = new HashMap<>();

        while (sizer.moveNext()) {
            counts.computeIfAbsent(sizer.type(), x -> new AtomicLong()).incrementAndGet();
            total.computeIfAbsent(sizer.type(), x -> new AtomicLong()).addAndGet(sizer.bytes());
        }
        total.entrySet().stream().sorted(Comparator.comparing(x -> -x.getValue().get())).forEach(entry -> {
            System.out.println(counts.get(entry.getKey()) + "   \t" + SizeUtils.formatBytes(entry.getValue().get()) + "\t" + entry.getKey());
        });
    }
}
