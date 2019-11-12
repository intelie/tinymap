package net.intelie.tinymap;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import net.intelie.introspective.ObjectSizer;
import net.intelie.introspective.ThreadResources;
import net.intelie.tinymap.json.JsonToken;
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
    public void gson() throws IOException {
        List<Object> objs = new ArrayList<>();


        ObjectCache cache = new ObjectCache();

        TinyJsonDecoder builder = new TinyJsonDecoder(cache);
        TinyOptimizer optimizer = new TinyOptimizer(cache);
        //Gson gson = new Gson();

        long startMem;
        try (BufferedReader reader = new BufferedReader(new FileReader("/home/juanplopes/Downloads/raw_star.json"))) {
            TinyJsonReader jsonReader = new TinyJsonReader(cache, new StringBuilder(), reader);
            startMem = ThreadResources.allocatedBytes();
            while (true) {
                if (jsonReader.peek() == JsonToken.END_DOCUMENT) break;
                objs.addAll(builder.buildList(jsonReader));
//                String line = reader.readLine();
//                if (line == null) break;
//                Map map = (Map) gson.fromJson(line, List.class).get(0);
//                objs.add(map);
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
