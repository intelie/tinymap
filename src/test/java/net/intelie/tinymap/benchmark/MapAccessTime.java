package net.intelie.tinymap.benchmark;

import com.google.common.collect.ImmutableMap;
import net.intelie.introspective.ThreadResources;
import net.intelie.tinymap.TinyMapBuilder;
import net.intelie.tinymap.support.TestSizeUtils;
import net.intelie.tinymap.TinyMap;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Ignore
public class MapAccessTime {
    @Test
    public void main() {
        ImmutableMap.Builder<String, Object> guava = ImmutableMap.builder();
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> linked = new LinkedHashMap<>();
        TinyMapBuilder<String, Object> tiny = TinyMap.builder();
        TinyMapBuilder<String, Object> mutable = new TinyMapBuilder<>();

        String[] keys = new String[100];
        String[] nkeys = new String[100];

        //Random random = new Random();

        for (int i = 0; i < keys.length; i++) {
            keys[i] = "key" + i;
            nkeys[i] = "non" + i;
            map.put(keys[i], "value" + i);
            linked.put(keys[i], "value" + i);
            guava.put(keys[i], "value" + i);
            tiny.put(keys[i], "value" + i);
            mutable.put(keys[i], "value" + i);
        }
//        keys = nkeys;

//        test("Tiny", keys, tiny.build());
//        test("LinkedHashMap", keys, linked);
//        test("MutableTiny", keys, mutable);
//        test("HashMap", keys, map);
//        test("Guava", keys, guava.build());
    }

    private void test(String name, String[] keys, Map<String, Object> map) {
        for (int i = 0; i < 100000; i++)
            for (String key : keys)
                map.get(key);


        long startTime = System.nanoTime();
        long startMem = ThreadResources.allocatedBytes();
        for (int i = 0; i < 10000000; i++)
            for (String key : keys)
                map.get(key);

        long endMem = ThreadResources.allocatedBytes() - startMem;
        long endTime = System.nanoTime() - startTime;

        System.out.println(name);
        System.out.println("  alloc: " + TestSizeUtils.formatBytes(endMem));
        System.out.println("  time: " + endTime / 1e9);

        if (map instanceof TinyMap) {
            long total = 0;
            for (String key : keys) {
                total += ((TinyMap<String, Object>) map).debugCollisions(key);
            }
            System.out.println("  collisions: " + (total / (double) keys.length));
        }
    }
}
