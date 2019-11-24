package net.intelie.tinymap;

import net.intelie.tinymap.support.MapAsserts;
import net.intelie.tinymap.support.SetAsserts;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class TinySetBuilderTest {
    @Test
    public void testAddAndGet() {
        TinySetBuilder<String> builder = TinySet.builder();

        assertThat(builder.add("abc")).isTrue();
        assertThat(builder.add("abc")).isFalse();

        assertThat(builder.size()).isEqualTo(1);
        assertThat(builder.contains("abc")).isTrue();
        assertThat(builder.getIndex("abc")).isEqualTo(0);

        assertThat(builder.build()).isEqualTo(Collections.singleton("abc"));
        assertThat(builder.build()).isEqualTo(Collections.singleton("abc"));
    }

    @Test
    public void testAddAndRemove() {
        TinySetBuilder<String> builder = new TinySetBuilder<>();

        for (int i = 0; i < 100; i++)
            assertThat(builder.add("aaa" + i)).isTrue();
        for (int i = 0; i < 100; i++)
            assertThat(builder.contains("aaa" + i)).isTrue();

        assertThat(builder.size()).isEqualTo(100);

        for (int i = 0; i < 100; i += 2)
            assertThat(builder.remove("aaa" + i)).isTrue();

        for (int i = 0; i < 100; i++) {
            assertThat(builder.contains("aaa" + i)).isEqualTo(i % 2 != 0);
        }

        assertThat(builder.size()).isEqualTo(50);
        assertThat(builder.build().size()).isEqualTo(50);
    }

    @Test
    public void testIteratorChanges() throws Exception {
        TinySetBuilder<String> builder = new TinySetBuilder<>();
        LinkedHashSet<String> expected = new LinkedHashSet<>();

        for (int i = 0; i < 100; i++) {
            assertThat(builder.add("aaa" + i)).isTrue();
            assertThat(expected.add("aaa" + i)).isTrue();
        }

        Iterator<String> it = builder.iterator();
        for (int i = 0; i < 10; i++)
            it.next();
        for (int i = 10; i < 20; i++) {
            it.next();
            it.remove();
            expected.remove("aaa" + i);
        }

        SetAsserts.assertSet(expected, builder, 10, 20);
    }

    @Test
    public void testBuildEmpty() throws Exception {
        assertSetWithCount(0, false);
        assertSetWithCount(0, true);
    }

    @Test
    public void testBuildMedium() throws Exception {
        assertSetWithCount(1000, false);
        assertSetWithCount(1000, true);
        assertSetWithCount(1000, true, 200, 500);
    }

    @Test
    public void testBuildAlmostThere() throws Exception {
        assertSetWithCount(255, false);
        try {
            assertSetWithCount(255, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertSetWithCount(255, true, 100, 200);
    }

    private void assertSetWithCount(int count, boolean withNull) throws Exception {
        assertSetWithCount(count, withNull, 0, 0);
    }

    private void assertSetWithCount(int count, boolean withNull, int removeFrom, int removeTo) throws Exception {
        TinySetBuilder<String> builder = new TinySetBuilder<>();
        LinkedHashSet<String> expectedMap = new LinkedHashSet<>();

        setIteration(count, withNull, removeFrom, removeTo, builder, expectedMap);
        if (count < 1000) {
            builder.clear();
            expectedMap.clear();
            setIteration(count, withNull, removeFrom, removeTo, builder, expectedMap);
        }
    }

    private void setIteration(int count, boolean withNull, int removeFrom, int removeTo, TinySetBuilder<String> builder, LinkedHashSet<String> expectedMap) throws Exception {
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

        SetAsserts.assertSet(expectedMap, builder, removeFrom, removeTo);
        SetAsserts.assertSet(expectedMap, builder.build(), 0, 0);
    }


}