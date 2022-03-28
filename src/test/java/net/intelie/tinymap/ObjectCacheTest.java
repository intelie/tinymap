package net.intelie.tinymap;

import net.intelie.tinymap.util.DefaultObjectCache;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectCacheTest {
    @Test
    public void testListCacheHit() {
        DefaultObjectCache cache = new DefaultObjectCache();

        TinyListBuilder<Object> builder1 = TinyList.builder();
        TinyListBuilder<Object> builder2 = TinyList.builder();

        assertThat(cache.get(builder1)).isSameAs(cache.get(builder2)).isEqualTo(builder1.build());

        builder1.add("aaa");
        builder2.add("aaa");

        assertThat(cache.get(builder1)).isSameAs(cache.get(builder2)).isEqualTo(builder1.build());
    }

    @Test
    public void testMapCacheHit() {
        ObjectCache cache = new DefaultObjectCache();

        TinyMapBuilder<Object, Object> builder1 = TinyMap.builder();
        TinyMapBuilder<Object, Object> builder2 = TinyMap.builder();

        assertThat(cache.get(builder1)).isSameAs(cache.get(builder2)).isEqualTo(builder1.build());

        builder1.put("aaa", 123);
        builder2.put("aaa", 123);

        assertThat(cache.get(builder1)).isSameAs(cache.get(builder2)).isEqualTo(builder1.build());
    }

    @Test
    public void testDoubleCacheHit() {
        DefaultObjectCache cache = new DefaultObjectCache();
        Double cached1 = cache.get(Double.parseDouble("123.456"));
        Double cached2 = cache.get(Double.parseDouble("123.456"));

        assertThat(cached1).isSameAs(cached2);
    }
}
