package net.intelie.tinymap;

import net.intelie.tinymap.util.DoubleCache;
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
        ObjectCache cache = new ObjectCache();

        TinyList.Builder<Object> builder1 = TinyList.builder();
        TinyList.Builder<Object> builder2 = TinyList.builder();

        builder1.add(1);
        builder1.add(31);

        builder2.add(2);
        builder2.add(0);

        TinyList<Object> cached1 = cache.get(builder1);
        TinyList<Object> cached2 = cache.get(builder2);
        TinyList<Object> cached1b = cache.get(builder1);

        assertThat(cached1).isNotEqualTo(cached2);
        TinyList.FullCacheAdapter<Object> adapter = builder1.adapter();
        assertThat(adapter.contentHashCode(builder1)).isEqualTo(adapter.contentHashCode(builder2));

        assertThat(cached1).isSameAs(cached1b);
    }

    @Test
    public void testDoubleCacheHit() {
        ObjectCache cache = new ObjectCache();
        Double cached1 = cache.get(Double.parseDouble("123.456"));
        Double cached2 = cache.get(Double.parseDouble("123.456"));

        assertThat(cached1).isSameAs(cached2);
    }

    @Test
    public void testCacheSecondHitDifferentSize() {
        ObjectCache cache = new ObjectCache();

        TinyList.Builder<Object> builder1 = TinyList.builder();
        TinyList.Builder<Object> builder2 = TinyList.builder();

        builder1.add(0);
        builder1.add(0);

        builder2.add(30 * 31);

        TinyList<Object> cached1 = cache.get(builder1);
        TinyList<Object> cached2 = cache.get(builder2);
        TinyList<Object> cached1b = cache.get(builder1);

        assertThat(cached1).isNotEqualTo(cached2);
        TinyList.FullCacheAdapter<Object> adapter = builder1.adapter();

        assertThat(adapter.contentHashCode(builder1)).isEqualTo(adapter.contentHashCode(builder2));

        assertThat(cached1).isSameAs(cached1b);
    }

}