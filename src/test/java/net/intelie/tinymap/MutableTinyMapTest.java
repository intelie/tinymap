package net.intelie.tinymap;

import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class MutableTinyMapTest {
    @Test
    public void testAddAndGet() {
        MutableTinyMap<String, Object> builder = new MutableTinyMap<>();

        builder.put("abc", 123);
        builder.put("abc", 456);

        assertThat(builder.size()).isEqualTo(1);
        assertThat(builder.containsKey("abc")).isTrue();
        assertThat(builder.get("abc")).isEqualTo(456);

        assertThat(builder.build()).isEqualTo(Collections.singletonMap("abc", 456));
        assertThat(builder.build()).isEqualTo(Collections.singletonMap("abc", 456));
    }

    @Test
    public void testAddAndRemove() {
        MutableTinyMap<String, Object> builder = new MutableTinyMap<>();

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
    public void testIteratorChanges() {
        MutableTinyMap<String, Object> builder = new MutableTinyMap<>();
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

        assertMap(expected, builder, 10, 20);
    }

    @Test
    public void testBuildEmpty() {
        assertMapWithCount(0, false);
        assertMapWithCount(0, true);
    }

    @Test
    public void testBuildMedium() {
        assertMapWithCount(1000, false);
        assertMapWithCount(1000, true);
        assertMapWithCount(1000, true, 200, 500);
    }

    @Test
    public void testBuildAlmostThere() {
        assertMapWithCount(255, false);
        assertMapWithCount(255, true);
        assertMapWithCount(255, true, 100, 200);
    }

    @Test
    public void testBuildLarge() {
        assertMapWithCount(0x10000, true);
    }

    private void assertMapWithCount(int count, boolean withNull) {
        assertMapWithCount(count, withNull, 0, 0);
    }

    private void assertMapWithCount(int count, boolean withNull, int removeFrom, int removeTo) {
        MutableTinyMap<String, Object> builder = new MutableTinyMap<>();
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
            assertMap(expectedMap, builder.buildAndClear(), 0, 0);
        }
    }

    private void mapIteration(int count, boolean withNull, int removeFrom, int removeTo, MutableTinyMap<String, Object> builder, LinkedHashMap<String, Object> expectedMap) {
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

        assertMap(expectedMap, builder, removeFrom, removeTo);
        assertMap(expectedMap, builder.build(), 0, 0);
    }

    private void assertMap(LinkedHashMap<String, Object> expectedMap, ListMap<String, Object> map, int removeFrom, int removeTo) {
        assertThat(map.keySet().size()).isEqualTo(expectedMap.size());
        assertThat(map.values().size()).isEqualTo(expectedMap.size());
        assertThat(map.entrySet().size()).isEqualTo(expectedMap.size());

        Iterator<String> keysIterator = map.keySet().iterator();
        Iterator<Object> valuesIterator = map.values().iterator();
        Iterator<Map.Entry<String, Object>> entriesIterator = map.entrySet().iterator();

        int index = 0;
        for (Map.Entry<String, Object> entry : expectedMap.entrySet()) {
            if (index == removeFrom) index = removeTo;
            assertThat(map.get(entry.getKey())).isEqualTo(entry.getValue());
            assertThat(map.getIndex(entry.getKey())).isEqualTo(index);
            assertThat(map.getKeyAt(index)).isEqualTo(entry.getKey());
            assertThat(map.getValueAt(index)).isEqualTo(entry.getValue());

            assertThat(keysIterator.hasNext()).isTrue();
            assertThat(keysIterator.next()).isEqualTo(entry.getKey());

            assertThat(valuesIterator.hasNext()).isTrue();
            assertThat(valuesIterator.next()).isEqualTo(entry.getValue());

            assertThat(entriesIterator.hasNext()).isTrue();
            Map.Entry<String, Object> nextEntry = entriesIterator.next();
            assertThat(nextEntry).isEqualTo(entry);
            assertThat(nextEntry.hashCode()).isEqualTo(entry.hashCode());
            assertThat(nextEntry.toString()).isEqualTo(entry.toString());
            index++;
        }

        assertThat(keysIterator.hasNext()).isFalse();
        assertThat(valuesIterator.hasNext()).isFalse();
        assertThat(entriesIterator.hasNext()).isFalse();

        Iterator<Map.Entry<String, Object>> expectedIterator = expectedMap.entrySet().iterator();

        map.forEach((k, v) -> {
            assertThat(expectedIterator.hasNext());
            Map.Entry<String, Object> expectedEntry = expectedIterator.next();
            assertThat(k).isEqualTo(expectedEntry.getKey());
            assertThat(v).isEqualTo(expectedEntry.getValue());
        });

        assertThat(map.get("bbb")).isNull();
        assertThat(map.getIndex("bbb")).isLessThan(0);
        assertThat(map.isEmpty()).isEqualTo(expectedMap.isEmpty());
        assertThat(expectedMap).isEqualTo(map);
        assertThat(map.toString()).isEqualTo(expectedMap.toString());

        assertThat(map).isEqualTo(expectedMap);
        assertThat(map.hashCode()).isEqualTo(expectedMap.hashCode());

        HashMap<String, Object> unordered = new HashMap<>(expectedMap);
        assertThat(map).isEqualTo(unordered);
        assertThat(map.hashCode()).isEqualTo(unordered.hashCode());

        unordered.put("aaa0", "different");
        assertThat(map).isNotEqualTo(unordered);
        assertThat(map.hashCode()).isNotEqualTo(unordered.hashCode());
    }


}