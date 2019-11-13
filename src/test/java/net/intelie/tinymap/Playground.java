package net.intelie.tinymap;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.intelie.introspective.ThreadResources;
import net.intelie.tinymap.json.JsonToken;
import net.intelie.tinymap.json.TinyJsonDecoder;
import net.intelie.tinymap.json.TinyJsonReader;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Ignore
public class Playground {

    @Test
    public void gson() throws IOException {
        List<Object> objs = new ArrayList<>();


        ObjectCache cache = new ObjectCache();

        TinyOptimizer optimizer = new TinyOptimizer(cache);
        Gson gson = new Gson();

        long startMem= ThreadResources.allocatedBytes();;
//        try (TinyJsonDecoder reader = new TinyJsonDecoder(cache, new BufferedReader(new FileReader("/home/juanplopes/Downloads/rows.json")))) {
//            startMem = ThreadResources.allocatedBytes();
//            while (true) {
//                if (reader.peek() == JsonToken.END_DOCUMENT) break;
//                //objs.addAll(reader.nextList());
//                objs.add(reader.nextMap());
//            }
//        }
        try (JsonReader reader = new JsonReader(new BufferedReader(new FileReader("/home/juanplopes/Downloads/repositories.json")))) {
            objs.addAll(gson.fromJson(reader, List.class));
        }
        try (JsonReader reader = new JsonReader(new BufferedReader(new FileReader("/home/juanplopes/Downloads/repositories2.json")))) {
            objs.addAll(gson.fromJson(reader, List.class));
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


        SizeUtils.dump(optimized);
        System.out.println(ObjectCache.CREATED.get());
    }
}
