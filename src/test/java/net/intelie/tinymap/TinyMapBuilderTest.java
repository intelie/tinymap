package net.intelie.tinymap;

import net.intelie.tinymap.support.MapAsserts;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class TinyMapBuilderTest {
    @Test
    public void testAddAndGet() {
        TinyMapBuilder<String, Object> builder = TinyMap.builder();

        assertThat(builder.put("abc", 123)).isNull();
        assertThat(builder.put("abc", 456)).isEqualTo(123);

        assertThat(builder.size()).isEqualTo(1);
        assertThat(builder.containsKey("abc")).isTrue();
        assertThat(builder.get("abc")).isEqualTo(456);

        assertThat(builder.build()).isEqualTo(Collections.singletonMap("abc", 456));
        assertThat(builder.build()).isEqualTo(Collections.singletonMap("abc", 456));
    }

    @Test
    public void testAddAndRemove() {
        TinyMapBuilder<String, Object> builder = new TinyMapBuilder<>();

        for (int i = 0; i < 100; i++)
            assertThat(builder.put("aaa" + i, i)).isNull();
        for (int i = 0; i < 100; i++)
            assertThat(builder.containsKey("aaa" + i)).isTrue();

        assertThat(builder.size()).isEqualTo(100);

        for (int i = 0; i < 100; i += 2)
            assertThat(builder.remove("aaa" + i)).isEqualTo(i);

        for (int i = 0; i < 100; i++) {
            assertThat(builder.containsKey("aaa" + i)).isEqualTo(i % 2 != 0);
        }

        assertThat(builder.size()).isEqualTo(50);
        assertThat(builder.build().size()).isEqualTo(50);
    }

    @Test
    public void testIteratorChanges() throws Exception {
        TinyMapBuilder<String, Object> builder = new TinyMapBuilder<>();
        LinkedHashMap<String, Object> expected = new LinkedHashMap<>();

        for (int i = 0; i < 100; i++) {
            assertThat(builder.put("aaa" + i, i)).isNull();
            assertThat(expected.put("aaa" + i, i)).isNull();
        }

        Iterator<Map.Entry<String, Object>> it = builder.entrySet().iterator();
        for (int i = 0; i < 10; i++)
            it.next();
        for (int i = 10; i < 20; i++) {
            it.next();
            it.remove();
            expected.remove("aaa" + i);
        }
        for (int i = 20; i < 30; i++) {
            it.next().setValue("x" + i);
            expected.put("aaa" + i, "x" + i);
        }

        MapAsserts.assertMap(expected, builder, 10, 20);
    }

    @Test
    public void testBuildEmpty() throws Exception {
        assertMapWithCount(0, false);
        assertMapWithCount(0, true);
    }

    @Test
    public void testBuildMedium() throws Exception {
        assertMapWithCount(1000, false);
        assertMapWithCount(1000, true);
        assertMapWithCount(1000, true, 200, 500);
    }

    @Test
    public void testBuildAlmostThere() throws Exception {
        assertMapWithCount(255, false);
        try {
            assertMapWithCount(255, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertMapWithCount(255, true, 100, 200);
    }

    @Test
    public void testBuildLarge() throws Exception {
        assertMapWithCount(0x10000, true);
    }

    private void assertMapWithCount(int count, boolean withNull) throws Exception {
        assertMapWithCount(count, withNull, 0, 0);
    }

    private void assertMapWithCount(int count, boolean withNull, int removeFrom, int removeTo) throws Exception {
        TinyMapBuilder<String, Object> builder = new TinyMapBuilder<>();
        LinkedHashMap<String, Object> expectedMap = new LinkedHashMap<>();

        mapIteration(count, withNull, removeFrom, removeTo, builder, expectedMap);
        if (count < 1000) {
            builder.clear();
            expectedMap.clear();
            mapIteration(count, withNull, removeFrom, removeTo, builder, expectedMap);

            builder.entrySet().clear();
            expectedMap.entrySet().clear();
            mapIteration(count, withNull, removeFrom, removeTo, builder, expectedMap);

            builder.values().clear();
            expectedMap.values().clear();
            mapIteration(count, withNull, removeFrom, removeTo, builder, expectedMap);

            builder.keySet().clear();
            expectedMap.keySet().clear();
            mapIteration(count, withNull, removeFrom, removeTo, builder, expectedMap);
        }
    }

    private void mapIteration(int count, boolean withNull, int removeFrom, int removeTo, TinyMapBuilder<String, Object> builder, LinkedHashMap<String, Object> expectedMap) throws Exception {
        for (int i = 0; i < count; i++)
            expectedMap.put("aaa" + i, i);
        builder.putAll(expectedMap);

        if (withNull) {
            builder.put(null, null);
            expectedMap.put(null, null);
        }

        for (int i = removeFrom; i < removeTo; i++) {
            builder.remove("aaa" + i);
            expectedMap.remove("aaa" + i);
        }

        MapAsserts.assertMap(expectedMap, builder, removeFrom, removeTo);
        MapAsserts.assertMap(expectedMap, builder.build(), 0, 0);
        builder.compact();
        MapAsserts.assertMap(expectedMap, builder, 0, 0);
    }


}