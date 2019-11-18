package net.intelie.tinymap;

import net.intelie.tinymap.util.DoubleCache;
import net.intelie.tinymap.util.StringCacheAdapter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectCacheTest {
    @Test
    public void testListCacheHit() {
        ObjectCache cache = new ObjectCache();

        TinyList.Builder<Object> builder1 = TinyList.builder();
        TinyList.Builder<Object> builder2 = TinyList.builder();

        assertThat(cache.get(builder1)).isSameAs(cache.get(builder2)).isEqualTo(builder1.build());

        builder1.add("aaa");
        builder2.add("aaa");

        assertThat(cache.get(builder1)).isSameAs(cache.get(builder2)).isEqualTo(builder1.build());
    }

    @Test
    public void testMapCacheHit() {
        ObjectCache cache = new ObjectCache();

        TinyMap.Builder<Object, Object> builder1 = TinyMap.builder();
        TinyMap.Builder<Object, Object> builder2 = TinyMap.builder();

        assertThat(cache.get(builder1)).isSameAs(cache.get(builder2)).isEqualTo(builder1.build());

        builder1.put("aaa", 123);
        builder2.put("aaa", 123);

        assertThat(cache.get(builder1)).isSameAs(cache.get(builder2)).isEqualTo(builder1.build());
    }

    @Test
    public void testCacheSecondHit() {
        StringCacheAdapter stringAdapter = new StringCacheAdapter();

        ObjectCache cache = new ObjectCache();
        StringBuilder original1 = new StringBuilder("FB");
        StringBuilder original2 = new StringBuilder("Ea");

        String cached1 = cache.get(original1);
        String cached2 = cache.get(original2);
        String cached1b = cache.get(original1);

        assertThat(original1.toString()).isEqualTo(cached1).isNotSameAs(cached1);
        assertThat(original2.toString()).isEqualTo(cached2).isNotSameAs(cached2);

        assertThat(cached1).isNotEqualTo(cached2);
        assertThat(stringAdapter.contentHashCode(original1)).isEqualTo(stringAdapter.contentHashCode(cached2));

        assertThat(cached1).isSameAs(cached1b);
    }

    @Test
    public void testCacheSecondHitDifferentSize() {
        StringCacheAdapter stringAdapter = new StringCacheAdapter();

        ObjectCache cache = new ObjectCache();
        StringBuilder original1 = new StringBuilder("\001!");
        StringBuilder original2 = new StringBuilder("@");

        String cached1 = cache.get(original1);
        String cached2 = cache.get(original2);
        String cached1b = cache.get(original1);

        assertThat(original1.toString()).isEqualTo(cached1).isNotSameAs(cached1);
        assertThat(original2.toString()).isEqualTo(cached2).isNotSameAs(cached2);

        assertThat(cached1).isNotEqualTo(cached2);
        assertThat(stringAdapter.contentHashCode(original1)).isEqualTo(stringAdapter.contentHashCode(cached2));

        assertThat(cached1).isSameAs(cached1b);
    }

    @Test
    public void testDoubleCacheHit() {
        ObjectCache cache = new ObjectCache();
        Double cached1 = cache.get(Double.parseDouble("123.456"));
        Double cached2 = cache.get(Double.parseDouble("123.456"));

        assertThat(cached1).isSameAs(cached2);
    }
}