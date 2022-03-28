package net.intelie.tinymap.benchmark;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.intelie.tinymap.support.ImmutableOptimizer;
import net.intelie.tinymap.support.JavaOptimizer;
import net.intelie.tinymap.support.TestSizeUtils;
import net.intelie.tinymap.util.DefaultObjectCache;
import net.intelie.tinymap.util.ObjectOptimizer;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Ignore
public class MapSizeReal {
    @Test
    public void gson() throws IOException {
        List<Object> objs = new ArrayList<>();
        Gson gson = new Gson();
        String fileName = "/home/juanplopes/Downloads/rtolive.json";
        try (JsonReader reader = new JsonReader(new BufferedReader(new FileReader(fileName)))) {
            reader.setLenient(true);
            while (reader.peek() != com.google.gson.stream.JsonToken.END_DOCUMENT) {
                objs.addAll(gson.<List<?>>fromJson(reader, List.class));
            }
        }

        int step = objs.size() / 100;
        List<Object> gsonList = new ArrayList<>();

        for (int i = 0; i < objs.size(); i += step) {
            for (int j = 0; j < Math.min(step, objs.size() - i); j++) {
                gsonList.add(objs.get(i + j));
            }

            List<Object> javaList = new JavaOptimizer(null).optimizeList(gsonList);
            List<Object> javaOptList = new JavaOptimizer(new DefaultObjectCache()).optimizeList(gsonList);

            List<Object> immutableList = new ImmutableOptimizer(null).optimizeList(gsonList);
            List<Object> immutableOptList = new ImmutableOptimizer(new DefaultObjectCache()).optimizeList(gsonList);

            List<Object> tinyList = new ObjectOptimizer(null).optimizeList(gsonList);
            List<Object> tinyOptList = new ObjectOptimizer(new DefaultObjectCache()).optimizeList(gsonList);

            print(i + step,
                    javaList,
                    immutableList,
                    tinyList,
                    javaOptList,
                    immutableOptList,
                    tinyOptList);
        }

    }

    private void print(int i, Object... values) {
        System.out.println(i + "\t" + Arrays.stream(values)
                .mapToLong(TestSizeUtils::size)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining("\t")));
    }
}
