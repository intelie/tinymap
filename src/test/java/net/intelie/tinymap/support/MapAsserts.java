package net.intelie.tinymap.support;

import net.intelie.tinymap.ListMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MapAsserts {
    public static void assertMap(Map<String, Object> expectedMap, ListMap<String, Object> map, int removeFrom, int removeTo) throws Exception {
        assertSizes(expectedMap, map);
        assertElements(expectedMap, map, removeFrom, removeTo);

        assertInvalidIndex(map, -1);
        assertInvalidIndex(map, map.rawSize());
        assertForEach(expectedMap, map);

        assertThat(map.get("bbb")).isNull();
        assertThat(map.getOrDefault("bbb", "xxx")).isEqualTo("xxx");
        assertThat(map.getIndex("bbb")).isLessThan(0);

        assertCommonProperties(expectedMap, map);

        assertSerialization(map);

        SetAsserts.assertSet(expectedMap.keySet(), map.keySet(), removeFrom, removeTo);
    }

    private static void assertSerialization(ListMap<String, Object> map) throws IOException, ClassNotFoundException {
        byte[] serialized = SerializationHelper.testSerialize(map);
//        byte[] serializedExpected = SerializationHelper.testSerialize(expectedMap);
//        if (expectedMap.size() > 10 && removeTo - removeFrom == 0)
//            assertThat(serialized.length).isLessThan(2 * serializedExpected.length);

        Map<String, Object> deserialized = SerializationHelper.testDeserialize(serialized);
        assertThat(deserialized).isEqualTo(map);
    }

    private static void assertCommonProperties(Map<String, Object> expectedMap, ListMap<String, Object> map) {
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

    private static void assertForEach(Map<String, Object> expectedMap, ListMap<String, Object> map) {
        Iterator<Map.Entry<String, Object>> expectedIterator = expectedMap.entrySet().iterator();
        map.forEach((k, v) -> {
            assertThat(expectedIterator.hasNext());
            Map.Entry<String, Object> expectedEntry = expectedIterator.next();
            assertThat(k).isEqualTo(expectedEntry.getKey());
            assertThat(v).isEqualTo(expectedEntry.getValue());
        });
    }

    private static void assertInvalidIndex(ListMap<String, Object> map, int index) {
        assertThatThrownBy(() -> map.getEntryAt(index)).isInstanceOfAny(UnsupportedOperationException.class, IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> map.getKeyAt(index)).isInstanceOfAny(UnsupportedOperationException.class, IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> map.getValueAt(index)).isInstanceOfAny(UnsupportedOperationException.class, IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> map.removeAt(index)).isInstanceOfAny(UnsupportedOperationException.class, IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> map.setValueAt(index, 123)).isInstanceOfAny(UnsupportedOperationException.class, IndexOutOfBoundsException.class);
    }

    private static void assertElements(Map<String, Object> expectedMap, ListMap<String, Object> map, int removeFrom, int removeTo) {
        Iterator<String> keysIterator = map.keySet().iterator();
        Iterator<Object> valuesIterator = map.values().iterator();
        Iterator<Map.Entry<String, Object>> entriesIterator = map.entrySet().iterator();

        int index = 0;
        for (Map.Entry<String, Object> entry : expectedMap.entrySet()) {
            if (index == removeFrom) index = removeTo;
            assertThat(map.get(entry.getKey())).isEqualTo(entry.getValue());
            assertThat(map.getOrDefault(entry.getKey(), null)).isEqualTo(entry.getValue());
            assertThat(map.getIndex(entry.getKey())).isEqualTo(index);
            assertThat(map.getKeyAt(index)).isEqualTo(entry.getKey());
            assertThat(map.getValueAt(index)).isEqualTo(entry.getValue());
            assertThat(map.containsKey(entry.getKey())).isTrue();

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
    }

    private static void assertSizes(Map<String, Object> expectedMap, ListMap<String, Object> map) {
        assertThat(map.size()).isEqualTo(expectedMap.size());
        assertThat(map.keySet().size()).isEqualTo(expectedMap.size());
        assertThat(map.values().size()).isEqualTo(expectedMap.size());
        assertThat(map.entrySet().size()).isEqualTo(expectedMap.size());
    }
}
