package net.intelie.tinymap;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.intelie.tinymap.json.JsonToken;
import net.intelie.tinymap.json.TinyJsonDecoder;
import net.intelie.tinymap.support.JavaOptimizer;
import net.intelie.tinymap.support.TestSizeUtils;
import net.intelie.tinymap.util.ObjectOptimizer;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import static java.lang.System.exit;
import static java.lang.System.out;

@Ignore
public class Playground {
    @Test
    public void gson() throws IOException {
        List<Object> objs = new ArrayList<>();
        List<Object> objs2 = new ArrayList<>();


        ObjectCache cache = new ObjectCache(1 << 20);

        Gson gson = new Gson();

        long start = System.nanoTime();
        String fileName = "/home/juanplopes/Downloads/dumps/everything50k.json";
        try (TinyJsonDecoder reader = new TinyJsonDecoder(cache, new BufferedReader(new FileReader(fileName)));
             JsonReader reader2 = new JsonReader(new BufferedReader(new FileReader(fileName)))) {
            reader2.setLenient(true);
            while (true) {
                if (reader.peek() == JsonToken.END_DOCUMENT) break;
                objs.addAll(reader.nextList());

                if (reader2.peek() == com.google.gson.stream.JsonToken.END_DOCUMENT) break;
                List obj = gson.fromJson(reader2, List.class);
                objs2.addAll(obj);
            }
        }

        System.out.println((System.nanoTime() - start) / 1e9);
        System.out.println(objs.equals(objs2));
        //System.out.println(TestSizeUtils.formattedSize(objs2));
        TestSizeUtils.dump(objs2);
    }

    @Test
    public void name() throws IOException {
        ArrayList<Object> list = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("key1", "value" + i);
            map.put("key2", i);
            map.put("key3", (double) (i / 100));
            list.add(map);
        }

        ObjectOptimizer optimizer = new ObjectOptimizer(new ObjectCache());
        TinyList<Object> tinyList = optimizer.optimizeList(list);


//        TestSizeUtils.dump(tinyList);
        TestSizeUtils.dump(new JavaOptimizer(null).optimizeList(tinyList));

    }
}
