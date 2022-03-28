package net.intelie.tinymap.support;

import net.intelie.tinymap.base.IndexedCollection;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ListAsserts {
    public static void assertList(List<?> expected, IndexedCollection<?> actual) throws Exception {
        assertSizes(expected, actual);
        assertElements(expected, actual);
        assertElementsInverse(expected, actual);

        assertInvalidIndex(actual, -1);
        assertInvalidIndex(actual, actual.rawSize());
        assertForEach(expected, actual);

        assertThat(actual.contains("bbb")).isFalse();
        assertThat(actual.getIndex("bbb")).isLessThan(0);

        assertCommonProperties(expected, actual);

        assertSerialization(actual);
    }

    private static void assertSerialization(IndexedCollection<?> actual) throws IOException, ClassNotFoundException {
        byte[] serialized = SerializationHelper.testSerialize(actual);
//        byte[] serializedExpected = SerializationHelper.testSerialize(expectedSet);
//        if (expectedSet.size() > 10 && removeTo - removeFrom == 0)
//            assertThat(serialized.length).isLessThan(2 * serializedExpected.length);

        List<?> deserialized = SerializationHelper.testDeserialize(serialized);
        assertThat(deserialized).isEqualTo(actual);
    }

    private static void assertCommonProperties(List<?> expectedSet, IndexedCollection<?> actual) {
        assertThat(actual.isEmpty()).isEqualTo(expectedSet.isEmpty());

        assertThat(expectedSet).isEqualTo(actual);
        assertThat(actual.toString()).isEqualTo(expectedSet.toString());

        assertThat(actual).isEqualTo(expectedSet);
        assertThat(actual.hashCode()).isEqualTo(expectedSet.hashCode());

        ArrayList<?> unordered = new ArrayList<>(expectedSet);
        assertThat(actual).isEqualTo(unordered);
        assertThat(actual.hashCode()).isEqualTo(unordered.hashCode());

        unordered.remove("aaa0");
        ((List<Object>) unordered).add("bbb0");
        assertThat(actual).isNotEqualTo(unordered);
        assertThat(actual.hashCode()).isNotEqualTo(unordered.hashCode());
    }

    private static void assertForEach(List<?> expectedSet, IndexedCollection<?> actual) {
        Iterator<?> expectedIterator = expectedSet.iterator();
        actual.forEach(obj -> {
            assertThat(expectedIterator.hasNext());
            Object expectedEntry = expectedIterator.next();
            assertThat(obj).isEqualTo(expectedEntry);
        });
    }

    private static void assertInvalidIndex(IndexedCollection<?> actual, int index) {
        assertThatThrownBy(() -> actual.getEntryAt(index)).isInstanceOfAny(UnsupportedOperationException.class, IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> actual.removeAt(index)).isInstanceOfAny(UnsupportedOperationException.class, IndexOutOfBoundsException.class);
    }

    private static void assertElements(List<?> expectedSet, IndexedCollection<?> actual) {
        ListIterator<?> keysIterator = actual.iterator();

        int index = 0;
        for (ListIterator<?> iterator = expectedSet.listIterator(); iterator.hasNext(); ) {
            Object entry = iterator.next();


            assertThat(keysIterator.hasNext()).isTrue();
            assertThat(keysIterator.next()).isEqualTo(entry);

            assertThat(actual.getIndex(entry)).isEqualTo(index);
            assertThat(actual.getEntryAt(index)).isEqualTo(entry);


            assertThat(keysIterator.nextIndex()).isEqualTo(iterator.nextIndex());
            assertThat(keysIterator.previousIndex()).isEqualTo(iterator.previousIndex());


            index++;
        }

        assertThat(keysIterator.hasNext()).isFalse();
        assertThatThrownBy(keysIterator::next).isInstanceOf(NoSuchElementException.class);
    }

    private static void assertElementsInverse(List<?> expectedSet, IndexedCollection<?> actual) {
        ListIterator<?> keysIterator = actual.iterator(actual.rawSize());
        ListIterator<?> expectedIterator = expectedSet.listIterator(actual.size());
        while (keysIterator.hasNext()) keysIterator.next();
        while (expectedIterator.hasNext()) expectedIterator.next();

        assertThat(keysIterator.nextIndex()).isEqualTo(expectedIterator.nextIndex());
        assertThat(keysIterator.previousIndex()).isEqualTo(expectedIterator.previousIndex());

        int index = expectedSet.size() - 1;
        while (expectedIterator.hasPrevious()) {
            Object entry = expectedIterator.previous();
            assertThat(keysIterator.hasPrevious()).isTrue();
            assertThat(keysIterator.previous()).isEqualTo(entry);

            assertThat(actual.getIndex(entry)).isEqualTo(index);
            assertThat(actual.getEntryAt(index)).isEqualTo(entry);

            assertThat(keysIterator.nextIndex()).isEqualTo(expectedIterator.nextIndex());
            assertThat(keysIterator.previousIndex()).isEqualTo(expectedIterator.previousIndex());

            index--;
        }

        assertThat(keysIterator.hasPrevious()).isFalse();
        assertThatThrownBy(keysIterator::previous).isInstanceOf(NoSuchElementException.class);
    }

    private static void assertSizes(List<?> expectedSet, IndexedCollection<?> actual) {
        assertThat(actual.size()).isEqualTo(expectedSet.size());
    }
}
