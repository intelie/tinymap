package net.intelie.tinymap.benchmark;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.intelie.tinymap.JavaOptimizer;
import net.intelie.tinymap.ObjectCache;
import net.intelie.tinymap.TinyMapBuilder;
import net.intelie.tinymap.support.TestSizeUtils;
import net.intelie.tinymap.TinyMap;
import net.intelie.tinymap.util.ObjectOptimizer;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Ignore
public class MapSize {
    @Test
    public void gson() throws IOException {
        List<Object> objs = new ArrayList<>();
        Gson gson = new Gson();
        String fileName = "/home/juanplopes/Downloads/dumps/everything50k.json";
        try (JsonReader reader = new JsonReader(new BufferedReader(new FileReader(fileName)))) {
            reader.setLenient(true);
            while (reader.peek() != com.google.gson.stream.JsonToken.END_DOCUMENT) {
                objs.addAll(gson.<List>fromJson(reader, List.class));
            }
        }

        int step = objs.size() / 100;
        List<Object> gsonList = new ArrayList<>();
        for (int i = 0; i < objs.size(); i += step) {
            for (int j = 0; j < Math.min(step, objs.size() - i); j++) {
                gsonList.add(objs.get(i + j));
            }

            List<Object> javaList = new JavaOptimizer(null).optimizeList(gsonList);
            List<Object> tinyList = new ObjectOptimizer(null).optimizeList(gsonList);

            print(i + step, gsonList, javaList, tinyList);
        }

    }

    private void print(int i, Object... values) {
        System.out.println(i + "\t" + Arrays.stream(values)
                .mapToLong(TestSizeUtils::sizeOnlyStructure)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining("\t")));
    }
}
