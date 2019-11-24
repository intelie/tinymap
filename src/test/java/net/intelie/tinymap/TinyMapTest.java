package net.intelie.tinymap;

import com.google.common.collect.ImmutableMap;
import net.intelie.introspective.reflect.ReflectionCache;
import net.intelie.tinymap.support.MapAsserts;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.*;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class TinyMapTest {
    @Test
    public void testSizes() {
        ReflectionCache reflection = new ReflectionCache();
        assertThat(reflection.get(TinyMap.class).size()).isEqualTo(16);
    }

    @Test
    public void testBuildAndGet() {
        TinyMapBuilder<String, Object> builder = TinyMap.builder();
        assertThat(builder.size()).isEqualTo(0);
        builder.put("aaa", 333);
        builder.put("bbb", 456.0);
        builder.put("aaa", 123);
        assertThat(builder.size()).isEqualTo(2);
        TinyMap<String, Object> map = builder.build();

        assertThat(map.get("aaa")).isEqualTo(123);
        assertThat(map.get("bbb")).isEqualTo(456.0);
        assertThat(map.get("ccc")).isNull();
        assertThat(map.getOrDefault("ccc", "def")).isEqualTo("def");
        assertThat(map.getOrDefault(null, "def")).isEqualTo("def");

        assertThat(map.size()).isEqualTo(2);
        assertThat(map).containsExactly(
                entry("aaa", 123),
                entry("bbb", 456.0)
        );
        assertThat(map.values()).containsExactly(123, 456.0);
        assertThat(map.keySet()).containsExactly("aaa", "bbb");
    }

    @Test
    public void canBuildWithDuplicateKeys() {
        TinyMapBuilder<String, Object> builder = TinyMap.builder();
        builder.put("aaa", 123);
        builder.put("aaa", 456.0);
        builder.put("bbb", 789.0);

        assertThat(builder.size()).isEqualTo(2);
        assertThat(builder.build()).isEqualTo(ImmutableMap.of("aaa", 456.0, "bbb", 789.0));

        assertThat(builder.size()).isEqualTo(2);
        assertThat(builder.build()).isEqualTo(ImmutableMap.of("aaa", 456.0, "bbb", 789.0));
    }


    @Test
    public void canBuildWithNull() {
        TinyMapBuilder<String, Object> builder = TinyMap.builder();
        builder.put(null, 123);
        assertThat(builder.build()).isEqualTo(Collections.singletonMap(null, 123));
    }

    @Test
    public void canBuildMediumWithDuplicateKeys() {
        TinyMapBuilder<String, Object> builder = TinyMap.builder();
        builder.put("aaa", 123);
        builder.put("aaa", 456.0);
        for (int i = 0; i < 1000; i++) {
            builder.put("aaa" + i, i);
        }
        assertThat(builder.build().size()).isEqualTo(1001);
    }

    @Test
    public void canBuildLargeWithDuplicateKeys() {
        TinyMapBuilder<String, Object> builder = TinyMap.builder();
        builder.put("aaa", 123);
        builder.put("aaa", 456.0);
        for (int i = 0; i < 0x10000; i++) {
            builder.put("aaa" + i, i);
        }
        assertThat(builder.build().size()).isEqualTo(65537);
    }

    @Test
    public void testForEach() {
        TinyMapBuilder<String, Object> builder = TinyMap.builder();
        builder.put("aaa", 123);
        builder.put("bbb", 456.0);
        TinyMap<String, Object> map = builder.build();

        BiConsumer consumer = mock(BiConsumer.class);

        map.forEach(consumer);

        InOrder orderly = inOrder(consumer);
        orderly.verify(consumer).accept("aaa", 123);
        orderly.verify(consumer).accept("bbb", 456.0);
        orderly.verifyNoMoreInteractions();
    }

    @Test
    public void testContains() {
        TinyMapBuilder<String, Object> builder = TinyMap.builder();
        builder.put("aaa", null);
        builder.put("bbb", 456.0);

        TinyMap<String, Object> map = builder.build();

        assertThat(map.containsKey("aaa")).isTrue();
        assertThat(map.containsKey("ccc")).isFalse();

        assertThat(map.containsValue(null)).isTrue();
        assertThat(map.containsValue(123.0)).isFalse();

        assertThat(map.keySet().contains("aaa")).isTrue();
        assertThat(map.keySet().contains("ccc")).isFalse();

        assertThat(map.values().contains(null)).isTrue();
        assertThat(map.values().contains(123.0)).isFalse();

        assertThat(map.entrySet().contains(new AbstractMap.SimpleEntry<>("aaa", null))).isTrue();
        assertThat(map.entrySet().contains(new AbstractMap.SimpleEntry<>("aaa", 123.0))).isFalse();
        assertThat(map.entrySet().contains(new AbstractMap.SimpleEntry<>("ccc", null))).isFalse();
        assertThat(map.entrySet().contains(new Object())).isFalse();
    }

    @Test
    public void testBuildSmallEnough() throws Exception {
        testCount(0, true);

        for (int i = 0; i <= 16; i++) {
            testCount(i, false);
        }
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
    public void testValueArrayTwoDifferentMaps() {
        TinyMapBuilder<String, Object> builder1 = TinyMap.builder();
        TinyMapBuilder<String, Object> builder2 = TinyMap.builder();
        for (int i = 0; i < 100; i++) {
            builder1.put("aaa" + i, i);
            builder2.put("aaa" + i, i);
        }

        TinyMap<String, Object> map1 = builder1.build();
        TinyMap<String, Object> map2 = builder2.build();

        assertThat(map1.values()).containsExactlyElementsOf(map2.values());
        assertThat(map1.keySet()).isEqualTo(map2.keySet());
    }

    @Test
    public void testGiantShortProblem() {
        TinyMapBuilder<String, Object> builder = TinyMap.builder();

        for (int i = 0; i < 100000; i++) {
            builder.put("aaa" + i, i);
        }

        TinyMap<String, Object> map = builder.build();

        assertThat(map.get("aaa99999")).isEqualTo(99999);
    }

    @Test
    public void testMaxCollisions() {
        TinyMapBuilder<String, Object> builder = TinyMap.builder();

        for (int count = 0; count < 1000; count += 20)
            testCollisions(builder, count);
        for (int count = 1000; count < 100000; count += 5000)
            testCollisions(builder, count);
    }

    private void testCollisions(TinyMapBuilder<String, Object> builder, int count) {
        while (builder.size() < count)
            builder.put("aaa" + builder.size(), builder.size());

        TinyMap<String, Object> map = builder.build();
        map.debugCollisions("abcdef");

        long total = 0;
        for (int i = 0; i < count; i++) {
            total += map.debugCollisions("aaa" + i);
        }
        long totalNonExisting = 0;
        for (int i = 0; i < count; i++) {
            totalNonExisting += map.debugCollisions("bbb" + i);
        }
        System.out.println(count + "\t" + (total / (double) count) + "\t" + (totalNonExisting / (double) count));
        assertThat(count == 0 ? 0 : total / (double) count).isLessThan(1);
        assertThat(count == 0 ? 0 : totalNonExisting / (double) count).isLessThan(5);
    }

    private void testCount(int count, boolean withNull) throws Exception {
        TinyMapBuilder<String, Object> builder = TinyMap.builder();
        LinkedHashMap<String, Object> expectedMap = new LinkedHashMap<>();

        for (int i = 0; i < count; i++) {
            if (count < 1000)
                builder.build();

            builder.putAll(Collections.singletonMap("aaa" + i, i));
            expectedMap.put("aaa" + i, i);
        }
        if (withNull) {
            builder.put(null, null);
            expectedMap.put(null, null);
            count++;
        }

        TinyMap<String, Object> map = builder.build();

        MapAsserts.assertMap(expectedMap, map, 0, 0);
    }

    @Test
    public void immutableIsImmutable() {
        TinyMapBuilder<Object, Object> builder = TinyMap.builder();
        builder.put("aaa", 111);
        TinyMap<Object, Object> map = builder.build();

        assertThatThrownBy(() -> map.clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> map.remove("aaa")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> map.putAll(Collections.singletonMap("abc", 123))).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> map.put("abc", 123)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> map.setValueAt(0, 123)).isInstanceOf(UnsupportedOperationException.class);
    }
}