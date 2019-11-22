package net.intelie.tinymap;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MutableTinyMapAdapterTest {
    @Test
    public void testBuilderHashCode() {
        MutableTinyMap<String, Object> builder1 = new MutableTinyMap<>();
        MutableTinyMap<String, Object> builder2 = new MutableTinyMap<>();

        MutableTinyMap.Adapter<String, Object> adapter = builder1.adapter();

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
        MutableTinyMap<String, Object> builder1 = new MutableTinyMap<>();
        MutableTinyMap<String, Object> builder2 = new MutableTinyMap<>();
        MutableTinyMap.Adapter<String, Object> adapter = builder1.adapter();

        assertThat(adapter.contentEquals(builder1, builder2.build())).isNotNull();
        String aaa1 = "aaa";
        String aaa2 = "aaa";
        String bbb = "bbb";

        Integer v111 = 111;
        Integer v222 = 222;
        Integer v333 = 333;

        builder1.put(aaa1, v111);
        builder2.put(aaa1, v111);
        assertThat(adapter.contentEquals(builder1, builder2.build())).isNotNull();

        builder1.put(bbb, v222);
        builder2.put(bbb, v222);
        assertThat(adapter.contentEquals(builder1, builder2.build())).isNotNull();

        builder1.put(aaa2, v333);
        builder2.put(aaa2, v333);

        assertThat(adapter.contentEquals(builder1, builder2.build())).isNotNull();
    }

    @Test
    public void testContentEqualComparingOnlyKeys() {
        MutableTinyMap<String, Object> builder1 = new MutableTinyMap<>();
        MutableTinyMap<String, Object> builder2 = new MutableTinyMap<>();
        MutableTinyMap.KeysAdapter<String, Object> adapter = new MutableTinyMap.KeysAdapter<>();

        builder1.put("aaa", 111);
        builder2.put("aaa", 222);

        assertThat(adapter.contentEquals(builder1, builder2.build())).isNotNull();

        builder1.put("bbb", 333);
        assertThat(adapter.contentEquals(builder1, builder2.build())).isNull();
        builder2.put("ccc", 444);
        assertThat(adapter.contentEquals(builder1, builder2.build())).isNull();
    }


    @Test
    public void testReuseEmpty() {
        MutableTinyMap.KeysAdapter<String, Object> adapter = new MutableTinyMap.KeysAdapter<>();

        MutableTinyMap<String, Object> builder2 = new MutableTinyMap<>();

        MutableTinyMap<String, Object> builder1 = new MutableTinyMap<>();
        TinyMap<String, Object> map1 = builder1.build();

        TinyMap<String, Object> map2 = adapter.reuse(builder2, map1, null);

        assertThat(map1).isSameAs(map2);
        assertThat(map1.sharesKeysWith(map2)).isTrue();
    }


    @Test
    public void testBuildSmallWithCache() {
        ObjectCache cache = new ObjectCache();

        MutableTinyMap<String, Object> builder1 = new MutableTinyMap<>();
        builder1.put("aaa", 111);
        builder1.put("bbb", 222);
        TinyMap<String, Object> map1 = cache.get(builder1);

        MutableTinyMap<String, Object> builder2 = new MutableTinyMap<>();
        builder2.put("aaa", 333);
        builder2.put("bbb", 444);
        TinyMap<String, Object> map2 = cache.get(builder2);

        assertThat(map1.sharesKeysWith(map2)).isTrue();
    }

    @Test
    public void testBuildExactlySameWithCache() {
        ObjectCache cache = new ObjectCache();

        MutableTinyMap<String, Object> builder1 = new MutableTinyMap<>();
        for (int i = 0; i < 1000; i++)
            builder1.put(cache.get("aaa" + i), cache.get(1000 * i));
        TinyMap<String, Object> map1 = cache.get(builder1);

        MutableTinyMap<String, Object> builder2 = new MutableTinyMap<>();
        for (int i = 0; i < 1000; i++)
            builder2.put(cache.get("aaa" + i), cache.get(1000 * i));
        TinyMap<String, Object> map2 = cache.get(builder2);

        assertThat(map1).isSameAs(map2);
    }

    @Test
    public void testBuildMediumWithCache() {
        ObjectCache cache = new ObjectCache();

        MutableTinyMap<String, Object> builder1 = new MutableTinyMap<>();
        for (int i = 0; i < 1000; i++)
            builder1.put(cache.get("aaa" + i), 1000 * i);
        TinyMap<String, Object> map1 = cache.get(builder1);

        MutableTinyMap<String, Object> builder2 = new MutableTinyMap<>();
        for (int i = 0; i < 1000; i++)
            builder2.put(cache.get("aaa" + i), 2000 * i);
        TinyMap<String, Object> map2 = cache.get(builder2);

        assertThat(map1.sharesKeysWith(map2)).isTrue();
    }

    @Test
    public void testBuildLargeWithCache() {
        ObjectCache cache = new ObjectCache(1 << 20);

        MutableTinyMap<String, Object> builder1 = new MutableTinyMap<>();
        for (int i = 0; i < 100000; i++)
            builder1.put(cache.get("aaa" + i), 100000 * i);
        TinyMap<String, Object> map1 = cache.get(builder1);

        MutableTinyMap<String, Object> builder2 = new MutableTinyMap<>();
        for (int i = 0; i < 100000; i++)
            builder2.put(cache.get("aaa" + i), 100000 * i);
        TinyMap<String, Object> map2 = cache.get(builder2);

        assertThat(map1.sharesKeysWith(map2)).isTrue();
    }

}