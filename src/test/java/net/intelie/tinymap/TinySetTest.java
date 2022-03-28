package net.intelie.tinymap;

import com.google.common.collect.ImmutableSet;
import net.intelie.introspective.reflect.ReflectionCache;
import net.intelie.tinymap.support.SetAsserts;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TinySetTest {
    @Test
    public void testSizes() {
        ReflectionCache reflection = new ReflectionCache();
        assertThat(reflection.get(TinySet.Empty.class).size()).isEqualTo(12);
        assertThat(reflection.get(TinySet.Small.class).size()).isEqualTo(20);
        assertThat(reflection.get(TinySet.Medium.class).size()).isEqualTo(20);
        assertThat(reflection.get(TinySet.Large.class).size()).isEqualTo(20);
    }

    @Test
    public void testBuildAndGet() {
        TinySetBuilder<String> builder = TinySet.builder();
        assertThat(builder.size()).isEqualTo(0);
        builder.add("aaa");
        builder.add("bbb");
        builder.add("aaa");
        assertThat(builder.size()).isEqualTo(2);
        TinySet<String> set = builder.build();

        assertThat(set.getIndex("aaa")).isEqualTo(0);
        assertThat(set.getIndex("bbb")).isEqualTo(1);
        assertThat(set.getIndex("ccc")).isLessThan(0);

        assertThat(set.size()).isEqualTo(2);
        assertThat(set).containsExactly(
                "aaa", "bbb"
        );

    }

    @Test
    public void canBuildWithDuplicateKeys() {
        TinySetBuilder<String> builder = TinySet.builder();
        builder.add("aaa");
        builder.add("aaa");
        builder.add("bbb");

        assertThat(builder.size()).isEqualTo(2);
        assertThat(builder.build()).isEqualTo(ImmutableSet.of("aaa", "bbb"));

        assertThat(builder.size()).isEqualTo(2);
        assertThat(builder.build()).isEqualTo(ImmutableSet.of("aaa", "bbb"));
    }


    @Test
    public void canBuildWithNull() {
        TinySetBuilder<String> builder = TinySet.builder();
        builder.add(null);
        assertThat(builder.build()).isEqualTo(Collections.singleton(null));
    }

    @Test
    public void canBuildMediumWithDuplicateKeys() {
        TinySetBuilder<String> builder = TinySet.builder();
        builder.add("aaa");
        builder.add("aaa");
        for (int i = 0; i < 1000; i++) {
            builder.add("aaa" + i);
        }
        assertThat(builder.build().size()).isEqualTo(1001);
    }

    @Test
    public void canBuildLargeWithDuplicateKeys() {
        TinySetBuilder<String> builder = TinySet.builder();
        builder.add("aaa");
        builder.add("aaa");
        for (int i = 0; i < 0x10000; i++) {
            builder.add("aaa" + i);
        }
        assertThat(builder.build().size()).isEqualTo(65537);
    }


    @Test
    public void testBuildEmpty() throws Exception {
        testCount(0, false);
        testCount(0, true);
    }

    @Test
    public void testBuildMedium() throws Exception {
        testCount(1000, false);
        testCount(1000, true);
    }

    @Test
    public void testBuildLarge() throws Exception {
        testCount(0x10000, true);
    }

    @Test
    public void testBuildAlmostThere() throws Exception {
        testCount(255, false);
        testCount(255, true);
    }

    @Test
    public void testBuildSmall() throws Exception {
        testCount(123, false);
        testCount(123, true);
    }


    @Test
    public void testMaxCollisions() {
        TinySetBuilder<String> builder = TinySet.builder();

        for (int count = 0; count < 1000; count += 20)
            testCollisions(builder, count);
        for (int count = 1000; count < 100000; count += 5000)
            testCollisions(builder, count);
    }

    private void testCollisions(TinySetBuilder<String> builder, int count) {
        while (builder.size() < count)
            builder.add("aaa" + builder.size());

        TinySet<String> map = builder.build();
        map.debugCollisions("abcdef");

        long total = 0;
        for (int i = 0; i < count; i++) {
            total += map.debugCollisions("aaa" + i);
        }
        long totalNonExisting = 0;
        for (int i = 0; i < count; i++) {
            totalNonExisting += map.debugCollisions("bbb" + i);
        }
        //System.out.println(count + "\t" + (total / (double) count) + "\t" + (totalNonExisting / (double) count));
        assertThat(count == 0 ? 0 : total / (double) count).isLessThan(1);
        assertThat(count == 0 ? 0 : totalNonExisting / (double) count).isLessThan(2);
    }

    private void testCount(int count, boolean withNull) throws Exception {
        TinySetBuilder<String> builder = TinySet.builder();
        LinkedHashSet<String> expectedMap = new LinkedHashSet<>();

        for (int i = 0; i < count; i++) {
            if (count < 1000)
                builder.build();

            builder.addAll(Collections.singleton("aaa" + i));
            expectedMap.add("aaa" + i);
        }
        if (withNull) {
            builder.add(null);
            expectedMap.add(null);
            count++;
        }

        TinySet<String> map = builder.build();

        SetAsserts.assertSet(expectedMap, map, 0, 0);
    }

    @Test
    public void immutableIsImmutable() {
        TinySetBuilder<Object> builder = TinySet.builder();
        builder.add("aaa");
        TinySet<Object> map = builder.build();

        assertThatThrownBy(() -> map.clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> map.remove("aaa")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> map.addAll(Collections.singleton("abc"))).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> map.add("abc")).isInstanceOf(UnsupportedOperationException.class);
    }
}
