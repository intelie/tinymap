package net.intelie.tinymap.support;

import net.intelie.tinymap.ListSet;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SetAsserts {
    public static void assertSet(Set<String> expectedSet, ListSet<String> set, int removeFrom, int removeTo) throws Exception {
        assertSizes(expectedSet, set);
        assertElements(expectedSet, set, removeFrom, removeTo);

        assertInvalidIndex(set, -1);
        assertInvalidIndex(set, set.rawSize());
        assertForEach(expectedSet, set);

        assertThat(set.contains("bbb")).isFalse();
        assertThat(set.getIndex("bbb")).isLessThan(0);

        assertCommonProperties(expectedSet, set);

        assertSerialization(set);
    }

    private static void assertSerialization(ListSet<String> set) throws IOException, ClassNotFoundException {
        byte[] serialized = SerializationHelper.testSerialize(set);
//        byte[] serializedExpected = SerializationHelper.testSerialize(expectedSet);
//        if (expectedSet.size() > 10 && removeTo - removeFrom == 0)
//            assertThat(serialized.length).isLessThan(2 * serializedExpected.length);

        Set<String> deserialized = SerializationHelper.testDeserialize(serialized);
        assertThat(deserialized).isEqualTo(set);
    }

    private static void assertCommonProperties(Set<String> expectedSet, ListSet<String> set) {
        assertThat(set.isEmpty()).isEqualTo(expectedSet.isEmpty());

        assertThat(expectedSet).isEqualTo(set);
        assertThat(set.toString()).isEqualTo(expectedSet.toString());

        assertThat(set).isEqualTo(expectedSet);
        assertThat(set.hashCode()).isEqualTo(expectedSet.hashCode());

        HashSet<String> unordered = new HashSet<>(expectedSet);
        assertThat(set).isEqualTo(unordered);
        assertThat(set.hashCode()).isEqualTo(unordered.hashCode());

        unordered.remove("aaa0");
        unordered.add("bbb0");
        assertThat(set).isNotEqualTo(unordered);
        assertThat(set.hashCode()).isNotEqualTo(unordered.hashCode());
    }

    private static void assertForEach(Set<String> expectedSet, ListSet<String> set) {
        Iterator<String> expectedIterator = expectedSet.iterator();
        set.forEach(obj -> {
            assertThat(expectedIterator.hasNext());
            String expectedEntry = expectedIterator.next();
            assertThat(obj).isEqualTo(expectedEntry);
        });
    }

    private static void assertInvalidIndex(ListSet<String> set, int index) {
        assertThatThrownBy(() -> set.getEntryAt(index)).isInstanceOfAny(UnsupportedOperationException.class, IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> set.removeAt(index)).isInstanceOfAny(UnsupportedOperationException.class, IndexOutOfBoundsException.class);
    }

    private static void assertElements(Set<String> expectedSet, ListSet<String> set, int removeFrom, int removeTo) {
        Iterator<String> keysIterator = set.iterator();

        int index = 0;
        for (String entry : expectedSet) {
            if (index == removeFrom) index = removeTo;
            assertThat(set.getIndex(entry)).isEqualTo(index);
            assertThat(set.getEntryAt(index)).isEqualTo(entry);

            assertThat(keysIterator.hasNext()).isTrue();
            assertThat(keysIterator.next()).isEqualTo(entry);

            index++;
        }

        assertThat(keysIterator.hasNext()).isFalse();
    }

    private static void assertSizes(Set<String> expectedSet, ListSet<String> set) {
        assertThat(set.size()).isEqualTo(expectedSet.size());
    }
}
