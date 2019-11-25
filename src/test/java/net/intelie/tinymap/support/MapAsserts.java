package net.intelie.tinymap.support;

import net.intelie.tinymap.base.IndexedMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MapAsserts {
    public static void assertMap(Map<String, Object> expected, IndexedMap<String, Object> actual, int removeFrom, int removeTo) throws Exception {
        assertSizes(expected, actual);
        assertElements(expected, actual, removeFrom, removeTo);

        assertInvalidIndex(actual, -1);
        assertInvalidIndex(actual, actual.rawSize());
        assertForEach(expected, actual);

        assertThat(actual.get("bbb")).isNull();
        assertThat(actual.getOrDefault("bbb", "xxx")).isEqualTo("xxx");
        assertThat(actual.getIndex("bbb")).isLessThan(0);

        assertCommonProperties(expected, actual);

        assertSerialization(actual);

        SetAsserts.assertSet(expected.keySet(), actual.keySet(), removeFrom, removeTo);
    }

    private static void assertSerialization(IndexedMap<String, Object> actual) throws IOException, ClassNotFoundException {
        byte[] serialized = SerializationHelper.testSerialize(actual);
//        byte[] serializedExpected = SerializationHelper.testSerialize(expected);
//        if (expected.size() > 10 && removeTo - removeFrom == 0)
//            assertThat(serialized.length).isLessThan(2 * serializedExpected.length);

        Map<String, Object> deserialized = SerializationHelper.testDeserialize(serialized);
        assertThat(deserialized).isEqualTo(actual);
    }

    private static void assertCommonProperties(Map<String, Object> expected, IndexedMap<String, Object> actual) {
        assertThat(actual.isEmpty()).isEqualTo(expected.isEmpty());

        assertThat(expected).isEqualTo(actual);
        assertThat(actual.toString()).isEqualTo(expected.toString());

        assertThat(actual).isEqualTo(expected);
        assertThat(actual.hashCode()).isEqualTo(expected.hashCode());

        HashMap<String, Object> unordered = new HashMap<>(expected);
        assertThat(actual).isEqualTo(unordered);
        assertThat(actual.hashCode()).isEqualTo(unordered.hashCode());

        unordered.put("aaa0", "different");
        assertThat(actual).isNotEqualTo(unordered);
        assertThat(actual.hashCode()).isNotEqualTo(unordered.hashCode());
    }

    private static void assertForEach(Map<String, Object> expected, IndexedMap<String, Object> actual) {
        Iterator<Map.Entry<String, Object>> expectedIterator = expected.entrySet().iterator();
        actual.forEach((k, v) -> {
            assertThat(expectedIterator.hasNext());
            Map.Entry<String, Object> expectedEntry = expectedIterator.next();
            assertThat(k).isEqualTo(expectedEntry.getKey());
            assertThat(v).isEqualTo(expectedEntry.getValue());
        });
    }

    private static void assertInvalidIndex(IndexedMap<String, Object> actual, int index) {
        assertThatThrownBy(() -> actual.getEntryAt(index)).isInstanceOfAny(UnsupportedOperationException.class, IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> actual.getKeyAt(index)).isInstanceOfAny(UnsupportedOperationException.class, IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> actual.getValueAt(index)).isInstanceOfAny(UnsupportedOperationException.class, IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> actual.removeAt(index)).isInstanceOfAny(UnsupportedOperationException.class, IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> actual.setValueAt(index, 123)).isInstanceOfAny(UnsupportedOperationException.class, IndexOutOfBoundsException.class);
    }

    private static void assertElements(Map<String, Object> expected, IndexedMap<String, Object> actual, int removeFrom, int removeTo) {
        Iterator<String> keysIterator = actual.keySet().iterator();
        Iterator<Object> valuesIterator = actual.values().iterator();
        Iterator<Map.Entry<String, Object>> entriesIterator = actual.entrySet().iterator();

        int index = 0;
        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            if (index == removeFrom) index = removeTo;
            assertThat(actual.get(entry.getKey())).isEqualTo(entry.getValue());
            assertThat(actual.getOrDefault(entry.getKey(), null)).isEqualTo(entry.getValue());
            assertThat(actual.getIndex(entry.getKey())).isEqualTo(index);
            assertThat(actual.getKeyAt(index)).isEqualTo(entry.getKey());
            assertThat(actual.getValueAt(index)).isEqualTo(entry.getValue());
            assertThat(actual.containsKey(entry.getKey())).isTrue();

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

    private static void assertSizes(Map<String, Object> expected, IndexedMap<String, Object> actual) {
        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual.keySet().size()).isEqualTo(expected.size());
        assertThat(actual.values().size()).isEqualTo(expected.size());
        assertThat(actual.entrySet().size()).isEqualTo(expected.size());
    }
}
