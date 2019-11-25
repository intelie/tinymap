package net.intelie.tinymap;

import net.intelie.tinymap.support.ListAsserts;
import org.junit.Test;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class TinyListBuilderTest {

    @Test
    public void addIndexedOnLast() throws Exception {
        TinyListBuilder<String> builder = new TinyListBuilder<>();
        ArrayList<String> expectedMap = new ArrayList<>();

        expectedMap.add(0, "aaa0");
        builder.add(0, "aaa0");
        expectedMap.add(0, "aaa1");
        builder.add(0, "aaa1");

        ListAsserts.assertList(expectedMap, builder);
    }

    @Test
    public void testRemoveAll() throws Exception {
        TinyListBuilder<String> builder = new TinyListBuilder<>();
        ArrayList<String> expected = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            assertThat(builder.add("aaa" + i)).isTrue();
            assertThat(expected.add("aaa" + i)).isTrue();
        }

        builder.removeAll(IntStream.range(10, 20).mapToObj(x -> "aaa" + x).collect(Collectors.toList()));
        expected.removeAll(IntStream.range(10, 20).mapToObj(x -> "aaa" + x).collect(Collectors.toList()));

        ListAsserts.assertList(expected, builder);
    }

    @Test
    public void testBuildEmpty() throws Exception {
        assertListWithCount(0, false);
        assertListWithCount(0, true);
    }

    @Test
    public void testBuildMedium() throws Exception {
        assertListWithCount(1000, false);
        assertListWithCount(1000, true);
        assertListWithCount(1000, true, 200, 300);
    }

    @Test
    public void testBuildAlmostThere() throws Exception {
        assertListWithCount(255, false);
        assertListWithCount(255, true);
        assertListWithCount(1000, true, 123, 234);
    }


    private void assertListWithCount(int count, boolean withNull) throws Exception {
        assertListWithCount(count, withNull, 0, 0);
    }

    private void assertListWithCount(int count, boolean withNull, int removeFrom, int removeTo) throws Exception {
        TinyListBuilder<String> builder = new TinyListBuilder<>();
        ArrayList<String> expectedMap = new ArrayList<>();

        listIteration(count, withNull, removeFrom, removeTo, builder, expectedMap);
        if (count < 1000) {
            builder.clear();
            expectedMap.clear();
            listIteration(count, withNull, removeFrom, removeTo, builder, expectedMap);
        }
    }

    private void listIteration(int count, boolean withNull, int removeFrom, int removeTo, TinyListBuilder<String> builder, ArrayList<String> expectedMap) throws Exception {
        for (int i = 0; i < count; i++)
            expectedMap.add("aaa" + i);
        builder.addAll(expectedMap);

        if (withNull) {
            builder.add(null);
            expectedMap.add(null);
        }

        for (int i = removeFrom; i < removeTo; i++) {
            builder.remove("aaa" + i);
            expectedMap.remove("aaa" + i);
        }

        ListAsserts.assertList(expectedMap, builder);
        ListAsserts.assertList(expectedMap, builder.build());
    }


}