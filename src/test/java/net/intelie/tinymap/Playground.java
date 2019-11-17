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
import java.util.Collection;
import java.util.List;

@Ignore
public class Playground {

    @Test
    public void gson() throws IOException {
        List<Object> objs = new ArrayList<>();
        List<Object> objs2 = new ArrayList<>();


        ObjectCache cache = new ObjectCache();

        Gson gson = new Gson();

        try (TinyJsonDecoder reader = new TinyJsonDecoder(cache, new BufferedReader(new FileReader("/home/juanplopes/Downloads/rtolive.json")));
             JsonReader reader2 = new JsonReader(new BufferedReader(new FileReader("/home/juanplopes/Downloads/rtolive.json")))) {
            reader2.setLenient(true);
            while (true) {
                if (reader.peek() == JsonToken.END_DOCUMENT) break;
                objs.addAll(reader.nextList());

//                if (reader2.peek() == com.google.gson.stream.JsonToken.END_DOCUMENT) break;
//                Collection<?> obj = gson.fromJson(reader2, List.class);
//                objs2.addAll(obj);

            }
        }

//        System.out.println(objs.equals(objs2));

        TestSizeUtils.dump(objs);
    }
}
