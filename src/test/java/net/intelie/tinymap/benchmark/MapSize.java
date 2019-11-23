package net.intelie.tinymap.benchmark;

import com.google.common.collect.ImmutableMap;
import net.intelie.tinymap.TinyMapBuilder;
import net.intelie.tinymap.support.TestSizeUtils;
import net.intelie.tinymap.TinyMap;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Ignore
public class MapSize {
    @Test
    public void main() {
        ImmutableMap.Builder<String, Object> guava = ImmutableMap.builder();
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> linked = new LinkedHashMap<>();
        TinyMapBuilder<String, Object> tiny = TinyMap.builder();

        String[] keys = new String[10000];

        print(0, linked, map, guava.build(), tiny, tiny.build());
        for (int i = 0; i < keys.length; i++) {
            keys[i] = "key" + i;
            map.put(keys[i], "value" + i);
            linked.put(keys[i], "value" + i);
            guava.put(keys[i], "value" + i);
            tiny.put(keys[i], "value" + i);
            if (i % (keys.length / 100) == 0)
                print(i + 1, linked, map, guava.build(), tiny, tiny.build());
        }
    }

    private void print(int i, Object... values) {
        System.out.println(i + "\t" + Arrays.stream(values)
                .mapToLong(TestSizeUtils::sizeNoStrings)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining("\t")));
    }
}
