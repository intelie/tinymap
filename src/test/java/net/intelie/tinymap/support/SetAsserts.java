package net.intelie.tinymap.support;

import net.intelie.tinymap.base.IndexedSet;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SetAsserts {
    public static void assertSet(Set<String> expected, IndexedSet<String> actual, int removeFrom, int removeTo) throws Exception {
        assertSizes(expected, actual);
        assertElements(expected, actual, removeFrom, removeTo);
        assertElementsInverse(expected, actual, removeFrom, removeTo);

        assertInvalidIndex(actual, -1);
        assertInvalidIndex(actual, actual.rawSize());
        assertForEach(expected, actual);

        assertThat(actual.contains("bbb")).isFalse();
        assertThat(actual.getIndex("bbb")).isLessThan(0);

        assertCommonProperties(expected, actual);

        assertSerialization(actual);
    }

    private static void assertSerialization(IndexedSet<String> actual) throws IOException, ClassNotFoundException {
        byte[] serialized = SerializationHelper.testSerialize(actual);
//        byte[] serializedExpected = SerializationHelper.testSerialize(expected);
//        if (expected.size() > 10 && removeTo - removeFrom == 0)
//            assertThat(serialized.length).isLessThan(2 * serializedExpected.length);

        Set<String> deserialized = SerializationHelper.testDeserialize(serialized);
        assertThat(deserialized).isEqualTo(actual);
    }

    private static void assertCommonProperties(Set<String> expected, IndexedSet<String> actual) {
        assertThat(actual.isEmpty()).isEqualTo(expected.isEmpty());

        assertThat(expected).isEqualTo(actual);
        assertThat(actual.toString()).isEqualTo(expected.toString());

        assertThat(actual).isEqualTo(expected);
        assertThat(actual.hashCode()).isEqualTo(expected.hashCode());

        HashSet<String> unordered = new HashSet<>(expected);
        assertThat(actual).isEqualTo(unordered);
        assertThat(actual.hashCode()).isEqualTo(unordered.hashCode());

        unordered.remove("aaa0");
        unordered.add("bbb0");
        assertThat(actual).isNotEqualTo(unordered);
        assertThat(actual.hashCode()).isNotEqualTo(unordered.hashCode());
    }

    private static void assertForEach(Set<String> expected, IndexedSet<String> actual) {
        Iterator<String> expectedIterator = expected.iterator();
        actual.forEach(obj -> {
            assertThat(expectedIterator).hasNext();
            String expectedEntry = expectedIterator.next();
            assertThat(obj).isEqualTo(expectedEntry);
        });
    }

    private static void assertInvalidIndex(IndexedSet<String> actual, int index) {
        assertThatThrownBy(() -> actual.getEntryAt(index)).isInstanceOfAny(UnsupportedOperationException.class, IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> actual.removeAt(index)).isInstanceOfAny(UnsupportedOperationException.class, IndexOutOfBoundsException.class);
    }

    private static void assertElements(Set<String> expected, IndexedSet<String> actual, int removeFrom, int removeTo) {
        Iterator<String> keysIterator = actual.iterator();

        int index = 0;
        for (String entry : expected) {
            if (index == removeFrom) index = removeTo;
            assertThat(actual.getIndex(entry)).isEqualTo(index);
            assertThat(actual.getEntryAt(index)).isEqualTo(entry);

            assertThat(keysIterator.hasNext()).isTrue();
            assertThat(keysIterator.next()).isEqualTo(entry);

            index++;
        }

        assertThat(keysIterator.hasNext()).isFalse();
    }

    private static void assertElementsInverse(Set<?> expectedSet, IndexedSet<?> actual, int removeFrom, int removeTo) {
        ListIterator<?> keysIterator = actual.iterator(actual.rawSize());
        ListIterator<?> revIterator = new ArrayList<>(expectedSet).listIterator(actual.size());
        while (keysIterator.hasNext()) keysIterator.next();
        while (revIterator.hasNext()) revIterator.next();

        assertThat(keysIterator.nextIndex()).isEqualTo(translateIndex(revIterator.nextIndex(), removeFrom, removeTo));
        assertThat(keysIterator.previousIndex()).isEqualTo(translateIndex(revIterator.previousIndex(), removeFrom, removeTo));

        int index = expectedSet.size() - 1;
        while (revIterator.hasPrevious()) {
            Object entry = revIterator.previous();

            assertThat(actual.getIndex(entry)).isEqualTo(translateIndex(index, removeFrom, removeTo));
            assertThat(actual.getEntryAt(translateIndex(index, removeFrom, removeTo))).isEqualTo(entry);

            assertThat(keysIterator.hasPrevious()).isTrue();
            assertThat(keysIterator.previous()).isEqualTo(entry);

            assertThat(keysIterator.nextIndex()).isEqualTo(translateIndex(revIterator.nextIndex(), removeFrom, removeTo));
            assertThat(keysIterator.previousIndex()).isEqualTo(translateIndex(revIterator.previousIndex(), removeFrom, removeTo));

            index--;
        }

        assertThat(keysIterator.hasPrevious()).isFalse();
        assertThatThrownBy(keysIterator::previous).isInstanceOf(NoSuchElementException.class);
    }

    private static void assertSizes(Set<String> expected, IndexedSet<String> actual) {
        assertThat(actual.size()).isEqualTo(expected.size());
    }

    private static int translateIndex(int index, int removeFrom, int removeTo) {
        if (index >= removeFrom)
            index += (removeTo - removeFrom);
        return index;
    }
}
