package net.intelie.tinymap;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectCacheTest {
    @Test
    public void testCacheHit() {
        ObjectCache cache = new ObjectCache();

        TinyList.Builder<Object> builder1 = TinyList.builder();
        TinyList.Builder<Object> builder2 = TinyList.builder();

        assertThat(cache.get(builder1)).isSameAs(cache.get(builder2)).isEqualTo(builder1.build());

        builder1.add("aaa");
        builder2.add("aaa");

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
    public void testCacheSecondHitDifferentSize() {
        ObjectCache cache = new ObjectCache();

        TinyList.Builder<Object> builder1 = TinyList.builder();
        TinyList.Builder<Object> builder2 = TinyList.builder();

        builder1.add(0);
        builder1.add(0);

        builder2.add(30*31);

        TinyList<Object> cached1 = cache.get(builder1);
        TinyList<Object> cached2 = cache.get(builder2);
        TinyList<Object> cached1b = cache.get(builder1);

        assertThat(cached1).isNotEqualTo(cached2);
        TinyList.FullCacheAdapter<Object> adapter = builder1.adapter();

        assertThat(adapter.contentHashCode(builder1)).isEqualTo(adapter.contentHashCode(builder2));

        assertThat(cached1).isSameAs(cached1b);
    }

}