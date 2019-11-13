package net.intelie.tinymap;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.intelie.introspective.ThreadResources;
import net.intelie.tinymap.json.JsonToken;
import net.intelie.tinymap.json.TinyJsonDecoder;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Ignore
public class Playground {

    @Test
    public void gson() throws IOException {
        List<Object> objs = new ArrayList<>();


        ObjectCache cache = new ObjectCache();

        Gson gson = new Gson();

        long startMem = ThreadResources.allocatedBytes();
        try (TinyJsonDecoder reader = new TinyJsonDecoder(cache, new BufferedReader(new FileReader("/home/juanplopes/Downloads/raw_star.json")))) {
            while (true) {
                if (reader.peek() == JsonToken.END_DOCUMENT) break;
                objs.addAll(reader.nextList());
                //objs.add(reader.nextMap());
            }
        }
//        try (JsonReader reader = new JsonReader(new BufferedReader(new FileReader("/home/juanplopes/Downloads/raw_star.json")))) {
//            reader.setLenient(true);
//            while (true) {
//                if (reader.peek() == com.google.gson.stream.JsonToken.END_DOCUMENT) break;
//                objs.addAll(gson.fromJson(reader, List.class));
//                //objs.add(reader.nextMap());
//            }
//        }

//        System.out.println("allocated\t" + TestSizeUtils.formatBytes(ThreadResources.allocatedBytes() - startMem));
//
//        System.out.println("cache size\t" + TestSizeUtils.formattedSize(cache));


        TestSizeUtils.dump(objs);
    }
}
