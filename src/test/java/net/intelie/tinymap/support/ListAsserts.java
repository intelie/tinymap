package net.intelie.tinymap.support;

import net.intelie.tinymap.base.IndexedCollection;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ListAsserts {
    public static void assertList(List<String> expected, IndexedCollection<String> actual, int removeFrom, int removeTo) throws Exception {
        assertSizes(expected, actual);
        assertElements(expected, actual, removeFrom, removeTo);

        assertInvalidIndex(actual, -1);
        assertInvalidIndex(actual, actual.rawSize());
        assertForEach(expected, actual);

        assertThat(actual.contains("bbb")).isFalse();
        assertThat(actual.getIndex("bbb")).isLessThan(0);

        assertCommonProperties(expected, actual);

        assertSerialization(actual);
    }

    private static void assertSerialization(IndexedCollection<String> actual) throws IOException, ClassNotFoundException {
        byte[] serialized = SerializationHelper.testSerialize(actual);
//        byte[] serializedExpected = SerializationHelper.testSerialize(expectedSet);
//        if (expectedSet.size() > 10 && removeTo - removeFrom == 0)
//            assertThat(serialized.length).isLessThan(2 * serializedExpected.length);

        List<String> deserialized = SerializationHelper.testDeserialize(serialized);
        assertThat(deserialized).isEqualTo(actual);
    }

    private static void assertCommonProperties(List<String> expectedSet, IndexedCollection<String> actual) {
        assertThat(actual.isEmpty()).isEqualTo(expectedSet.isEmpty());

        assertThat(expectedSet).isEqualTo(actual);
        assertThat(actual.toString()).isEqualTo(expectedSet.toString());

        assertThat(actual).isEqualTo(expectedSet);
        assertThat(actual.hashCode()).isEqualTo(expectedSet.hashCode());

        ArrayList<String> unordered = new ArrayList<>(expectedSet);
        assertThat(actual).isEqualTo(unordered);
        assertThat(actual.hashCode()).isEqualTo(unordered.hashCode());

        unordered.remove("aaa0");
        unordered.add("bbb0");
        assertThat(actual).isNotEqualTo(unordered);
        assertThat(actual.hashCode()).isNotEqualTo(unordered.hashCode());
    }

    private static void assertForEach(List<String> expectedSet, IndexedCollection<String> actual) {
        Iterator<String> expectedIterator = expectedSet.iterator();
        actual.forEach(obj -> {
            assertThat(expectedIterator.hasNext());
            String expectedEntry = expectedIterator.next();
            assertThat(obj).isEqualTo(expectedEntry);
        });
    }

    private static void assertInvalidIndex(IndexedCollection<String> actual, int index) {
        assertThatThrownBy(() -> actual.getEntryAt(index)).isInstanceOfAny(UnsupportedOperationException.class, IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> actual.removeAt(index)).isInstanceOfAny(UnsupportedOperationException.class, IndexOutOfBoundsException.class);
    }

    private static void assertElements(List<String> expectedSet, IndexedCollection<String> actual, int removeFrom, int removeTo) {
        Iterator<String> keysIterator = actual.iterator();

        int index = 0;
        for (String entry : expectedSet) {
            if (index == removeFrom) index = removeTo;
            assertThat(actual.getIndex(entry)).isEqualTo(index);
            assertThat(actual.getEntryAt(index)).isEqualTo(entry);

            assertThat(keysIterator.hasNext()).isTrue();
            assertThat(keysIterator.next()).isEqualTo(entry);

            index++;
        }

        assertThat(keysIterator.hasNext()).isFalse();
    }

    private static void assertSizes(List<String> expectedSet, IndexedCollection<String> actual) {
        assertThat(actual.size()).isEqualTo(expectedSet.size());
    }
}
