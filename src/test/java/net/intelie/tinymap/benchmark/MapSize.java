package net.intelie.tinymap.benchmark;

import com.google.common.collect.ImmutableMap;
import net.intelie.introspective.ThreadResources;
import net.intelie.tinymap.SizeUtils;
import net.intelie.tinymap.TinyMap;
import org.junit.Ignore;
import org.junit.Test;
import vlsi.utils.CompactHashMap;

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
        Map<String, Object> compact = new CompactHashMap<>();
        TinyMap.Builder<String, Object> tiny = TinyMap.builder();

        String[] keys = new String[100000];

        print(0, linked, map, guava.build(), tiny.build());
        for (int i = 0; i < keys.length; i++) {
            keys[i] = "key" + i;
            map.put(keys[i], "value" + i);
            linked.put(keys[i], "value" + i);
            compact.put(keys[i], "value" + i);
            guava.put(keys[i], "value" + i);
            tiny.put(keys[i], "value" + i);
            if (i % (keys.length / 100) == 0)
                print(i + 1, linked, map, guava.build(), tiny.build());
        }
    }

    private void print(int i, Object... values) {
        System.out.println(i + "\t" + Arrays.stream(values)
                .mapToLong(SizeUtils::sizeNoStrings)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining("\t")));
    }
}
