package net.intelie.tinymap;

import net.intelie.introspective.reflect.ReflectionCache;
import org.junit.Test;
import org.mockito.InOrder;
import org.omg.CORBA.OBJ_ADAPTER;

import java.util.*;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class TinyMapTest {
    @Test
    public void testSizes() {
        ReflectionCache reflection = new ReflectionCache();
        assertThat(reflection.get(TinyMap.Empty.class).size()).isEqualTo(24);
        assertThat(reflection.get(TinyMap.Small.class).size()).isEqualTo(32);
        assertThat(reflection.get(TinyMap.Medium.class).size()).isEqualTo(32);
        assertThat(reflection.get(TinyMap.Large.class).size()).isEqualTo(32);
    }

    @Test
    public void testBuilderHashCode() {
        TinyMap.Builder<String, Object> builder1 = TinyMap.builder();
        TinyMap.Builder<String, Object> builder2 = TinyMap.builder();

        TinyMap.FullCacheAdapter<String, Object> adapter = builder1.adapter();

        assertThat(adapter.contentHashCode(builder1)).isEqualTo(adapter.contentHashCode(builder2));
        assertThat(adapter.contentEquals(builder1, builder2.build())).isNotNull();

        builder1.put("aaa", 111);
        builder2.put("aaa", 111);

        assertThat(adapter.contentHashCode(builder1)).isEqualTo(adapter.contentHashCode(builder2));
        assertThat(adapter.contentEquals(builder1, builder2.build())).isNotNull();

        builder1.put("bbb", 222);

        assertThat(adapter.contentHashCode(builder1)).isNotEqualTo(adapter.contentHashCode(builder2));
        assertThat(adapter.contentEquals(builder1, builder2.build())).isNull();

        builder2.put("bbb", 333);

        assertThat(adapter.contentHashCode(builder1)).isNotEqualTo(adapter.contentHashCode(builder2));
        assertThat(adapter.contentEquals(builder1, builder2.build())).isNull();
    }

    @Test
    public void testContentEqualWithDuplicateKeys() {
        TinyMap.Builder<String, Object> builder1 = TinyMap.builder();
        TinyMap.FullCacheAdapter<String, Object> adapter = builder1.adapter();

        assertThat(adapter.contentEquals(builder1, builder1.build())).isNotNull();
        builder1.put("aaa", 111);
        assertThat(adapter.contentEquals(builder1, builder1.build())).isNotNull();
        builder1.put("bbb", 222);
        assertThat(adapter.contentEquals(builder1, builder1.build())).isNotNull();
        builder1.put("aaa", 333);

        //even if this builder alwyas build the same map, the duplicate keys make it impossible
        //to predict it without actually creating the map
        assertThat(adapter.contentEquals(builder1, builder1.build())).isNull();
    }

    @Test
    public void testContentEqualComparingOnlyKeys() {
        TinyMap.Builder<String, Object> builder1 = TinyMap.builder();
        TinyMap.Builder<String, Object> builder2 = TinyMap.builder();
        TinyMap.KeysCacheAdapter<String, Object> adapter = new TinyMap.KeysCacheAdapter<>();

        builder1.put("aaa", 111);
        builder2.put("aaa", 222);

        assertThat(adapter.contentEquals(builder1, builder2.build())).isNotNull();

        builder1.put("bbb", 333);
        assertThat(adapter.contentEquals(builder1, builder2.build())).isNull();
        builder2.put("ccc", 444);
        assertThat(adapter.contentEquals(builder1, builder2.build())).isNull();
    }

    @Test
    public void testBuildAndGet() {
        TinyMap.Builder<String, Object> builder = TinyMap.builder();
        assertThat(builder.size()).isEqualTo(0);
        builder.put("aaa", 333);
        builder.put("bbb", 456.0);
        builder.put("aaa", 123);
        assertThat(builder.size()).isEqualTo(3);
        TinyMap<String, Object> map = builder.buildAndClear();

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
    public void testReuseEmpty() {
        TinyMap.KeysCacheAdapter<String, Object> adapter = new TinyMap.KeysCacheAdapter<>();

        TinyMap.Builder<String, Object> builder2 = TinyMap.builder();

        TinyMap.Builder<String, Object> builder1 = TinyMap.builder();
        TinyMap<String, Object> map1 = builder1.build();

        TinyMap<String, Object> map2 = adapter.reuse(builder2, map1, null);

        assertThat(map1).isSameAs(map2);
        assertThat(map1.sharesKeysWith(map2)).isTrue();
    }

    @Test
    public void testBuildSmallWithCache() {
        ObjectCache cache = new ObjectCache();

        TinyMap.Builder<String, Object> builder1 = TinyMap.builder();
        builder1.put("aaa", 111);
        builder1.put("bbb", 222);
        TinyMap<String, Object> map1 = cache.get(builder1);

        TinyMap.Builder<String, Object> builder2 = TinyMap.builder();
        builder2.put("aaa", 333);
        builder2.put("bbb", 444);
        TinyMap<String, Object> map2 = cache.get(builder2);

        assertThat(map1.sharesKeysWith(map2)).isTrue();
    }

    @Test
    public void testBuildMediumWithCache() {
        ObjectCache cache = new ObjectCache();

        TinyMap.Builder<String, Object> builder1 = TinyMap.builder();
        for (int i = 0; i < 1000; i++)
            builder1.put(cache.get("aaa" + i), 1000 * i);
        TinyMap<String, Object> map1 = cache.get(builder1);

        TinyMap.Builder<String, Object> builder2 = TinyMap.builder();
        for (int i = 0; i < 1000; i++)
            builder2.put(cache.get("aaa" + i), 2000 * i);
        TinyMap<String, Object> map2 = cache.get(builder2);

        assertThat(map1.sharesKeysWith(map2)).isTrue();
    }

    @Test
    public void testBuildLargeWithCache() {
        ObjectCache cache = new ObjectCache(1 << 20);

        TinyMap.Builder<String, Object> builder1 = TinyMap.builder();
        for (int i = 0; i < 100000; i++)
            builder1.put(cache.get("aaa" + i), 100000 * i);
        TinyMap<String, Object> map1 = cache.get(builder1);

        TinyMap.Builder<String, Object> builder2 = TinyMap.builder();
        for (int i = 0; i < 100000; i++)
            builder2.put(cache.get("aaa" + i), 100000 * i);
        TinyMap<String, Object> map2 = cache.get(builder2);

        assertThat(map1.sharesKeysWith(map2)).isTrue();
    }

    @Test
    public void canBuildWithDuplicateKeys() {
        TinyMap.Builder<String, Object> builder = TinyMap.builder();
        builder.put("aaa", 123);
        builder.put("aaa", 456.0);
        assertThat(builder.buildAndClear()).isEqualTo(Collections.singletonMap("aaa", 456.0));
    }

    @Test
    public void canBuildWithNull() {
        TinyMap.Builder<String, Object> builder = TinyMap.builder();
        builder.put(null, 123);
        assertThat(builder.buildAndClear()).isEqualTo(Collections.singletonMap(null, 123));
    }

    @Test
    public void canBuildMediumWithDuplicateKeys() {
        TinyMap.Builder<String, Object> builder = TinyMap.builder();
        builder.put("aaa", 123);
        builder.put("aaa", 456.0);
        for (int i = 0; i < 1000; i++) {
            builder.put("aaa" + i, i);
        }
        assertThat(builder.buildAndClear().size()).isEqualTo(1001);
    }

    @Test
    public void canBuildLargeWithDuplicateKeys() {
        TinyMap.Builder<String, Object> builder = TinyMap.builder();
        builder.put("aaa", 123);
        builder.put("aaa", 456.0);
        for (int i = 0; i < 0x10000; i++) {
            builder.put("aaa" + i, i);
        }
        assertThat(builder.buildAndClear().size()).isEqualTo(65537);
    }

    @Test
    public void testForEach() {
        TinyMap.Builder<String, Object> builder = TinyMap.builder();
        builder.put("aaa", 123);
        builder.put("bbb", 456.0);
        TinyMap<String, Object> map = builder.buildAndClear();

        BiConsumer consumer = mock(BiConsumer.class);

        map.forEach(consumer);

        InOrder orderly = inOrder(consumer);
        orderly.verify(consumer).accept("aaa", 123);
        orderly.verify(consumer).accept("bbb", 456.0);
        orderly.verifyNoMoreInteractions();
    }

    @Test
    public void testContains() {
        TinyMap.Builder<String, Object> builder = TinyMap.builder();
        builder.put("aaa", null);
        builder.put("bbb", 456.0);

        TinyMap<String, Object> map = builder.buildAndClear();

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
    public void testBuildEmpty() {
        testCount(0, false);
        testCount(0, true);
    }

    @Test
    public void testBuildMedium() {
        testCount(1000, false);
        testCount(1000, true);
    }

    @Test
    public void testBuildLarge() {
        testCount(0x10000, true);
    }

    @Test
    public void testBuildAlmostThere() {
        testCount(255, false);
        testCount(255, true);
    }

    @Test
    public void testValueArrayTwoDifferentMaps() {
        TinyMap.Builder<String, Object> builder1 = TinyMap.builder();
        TinyMap.Builder<String, Object> builder2 = TinyMap.builder();
        for (int i = 0; i < 100; i++) {
            builder1.put("aaa" + i, i);
            builder2.put("aaa" + i, i);
        }

        TinyMap<String, Object> map1 = builder1.build();
        TinyMap<String, Object> map2 = builder2.build();

        assertThat(map1.values()).isEqualTo(map2.values());
        assertThat(map1.keySet()).isEqualTo(map2.keySet());
    }

    @Test
    public void testGiantShortProblem() {
        TinyMap.Builder<String, Object> builder = TinyMap.builder();

        for (int i = 0; i < 100000; i++) {
            builder.put("aaa" + i, i);
        }

        TinyMap<String, Object> map = builder.build();

        assertThat(map.get("aaa99999")).isEqualTo(99999);
    }

    @Test
    public void testMaxCollisions() {
        TinyMap.Builder<String, Object> builder = TinyMap.builder();

        for (int count = 0; count < 1000; count += 20)
            testCollisions(builder, count);
        for (int count = 1000; count < 100000; count += 5000)
            testCollisions(builder, count);
    }

    private void testCollisions(TinyMap.Builder<String, Object> builder, int count) {
        while (builder.size() < count)
            builder.put("aaa" + builder.size(), builder.size());

        TinyMap<String, Object> map = builder.buildAndClear();
        map.debugCollisions("abcdef");

        long total = 0;
        for (int i = 0; i < count; i++) {
            total += map.debugCollisions("aaa" + i);
        }
        //System.out.println(count + "\t" + (total / (double) count));
        assertThat(count == 0 ? 0 : total / (double) count).isLessThan(1);
    }

    private void testCount(int count, boolean withNull) {
        TinyMap.Builder<String, Object> builder = TinyMap.builder();
        LinkedHashMap<String, Object> expectedMap = new LinkedHashMap<>();

        for (int i = 0; i < count; i++) {
            if (count < 1000)
                builder.build();

            builder.put("aaa" + i, i);
            expectedMap.put("aaa" + i, i);
        }
        if (withNull) {
            builder.put(null, "null");
            expectedMap.put(null, "null");
            count++;
        }

        TinyMap<String, Object> map = builder.buildAndClear();

        assertThat(map.keySet().size()).isEqualTo(count);
        assertThat(map.values().size()).isEqualTo(count);
        assertThat(map.entrySet().size()).isEqualTo(count);

        Iterator<String> keysIterator = map.keySet().iterator();
        Iterator<Object> valuesIterator = map.values().iterator();
        Iterator<Map.Entry<String, Object>> entriesIterator = map.entrySet().iterator();

        for (Map.Entry<String, Object> entry : expectedMap.entrySet()) {
            assertThat(map.get(entry.getKey())).isEqualTo(entry.getValue());

            assertThat(keysIterator.hasNext()).isTrue();
            assertThat(keysIterator.next()).isEqualTo(entry.getKey());

            assertThat(valuesIterator.hasNext()).isTrue();
            assertThat(valuesIterator.next()).isEqualTo(entry.getValue());

            assertThat(entriesIterator.hasNext()).isTrue();
            assertThat(entriesIterator.next()).isEqualTo(entry);
        }
        assertThat(keysIterator.hasNext()).isFalse();
        assertThat(valuesIterator.hasNext()).isFalse();
        assertThat(entriesIterator.hasNext()).isFalse();

        assertThat(map.get("bbb")).isNull();
        assertThat(map.isEmpty()).isEqualTo(count == 0);
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

    @Test
    public void immutableIsImmutable() {
        TinyMap<Object, Object> map = TinyMap.builder().build();

        assertThatThrownBy(() -> map.clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> map.remove("abc")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> map.putAll(Collections.singletonMap("abc", 123))).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> map.put("abc", 123)).isInstanceOf(UnsupportedOperationException.class);

    }
}