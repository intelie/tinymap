package net.intelie.tinymap;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TinySetBuilderAdapterTest {
    @Test
    public void testBuilderHashCode() {
        TinySetBuilder<String> builder1 = new TinySetBuilder<>();
        TinySetBuilder<String> builder2 = new TinySetBuilder<>();

        TinySetBuilder.Adapter<String> adapter = builder1.adapter();

        assertThat(adapter.contentHashCode(builder1)).isEqualTo(adapter.contentHashCode(builder2));
        assertThat(adapter.contentEquals(builder1, builder2.build())).isNotNull();

        builder1.add("aaa");
        builder2.add("aaa");

        assertThat(adapter.contentHashCode(builder1)).isEqualTo(adapter.contentHashCode(builder2));
        assertThat(adapter.contentEquals(builder1, builder2.build())).isNotNull();

        builder1.add("bbb");

        assertThat(adapter.contentHashCode(builder1)).isNotEqualTo(adapter.contentHashCode(builder2));
        assertThat(adapter.contentEquals(builder1, builder2.build())).isNull();

        builder2.add("ccc");

        assertThat(adapter.contentHashCode(builder1)).isNotEqualTo(adapter.contentHashCode(builder2));
        assertThat(adapter.contentEquals(builder1, builder2.build())).isNull();
    }

    @Test
    public void testContentEqualWithDuplicateKeys() {
        TinySetBuilder<String> builder1 = new TinySetBuilder<>();
        TinySetBuilder<String> builder2 = new TinySetBuilder<>();
        TinySetBuilder.Adapter<String> adapter = builder1.adapter();

        assertThat(adapter.contentEquals(builder1, builder2.build())).isNotNull();
        String aaa = "aaa";
        String bbb1 = "bbb";
        String bbb2 = new String("bbb");


        builder1.add(aaa);
        builder2.add(aaa);
        assertThat(adapter.contentEquals(builder1, builder2.build())).isNotNull();

        builder1.add(bbb1);
        builder2.add(bbb2);
        assertThat(adapter.contentEquals(builder1, builder2.build())).isNull();
    }

    @Test
    public void testBuildSmallWithCache() {
        ObjectCache cache = new ObjectCache();

        TinySetBuilder<String> builder1 = new TinySetBuilder<>();
        builder1.add("aaa");
        builder1.add("bbb");
        TinySet<String> map1 = cache.get(builder1);

        TinySetBuilder<String> builder2 = new TinySetBuilder<>();
        builder2.add("aaa");
        builder2.add("bbb");
        TinySet<String> map2 = cache.get(builder2);

        assertThat(map1).isSameAs(map2);
    }

    @Test
    public void testBuildExactlySameWithCache() {
        ObjectCache cache = new ObjectCache();

        TinySetBuilder<String> builder1 = new TinySetBuilder<>();
        for (int i = 0; i < 1000; i++)
            builder1.add(cache.get("aaa" + i));
        TinySet<String> map1 = cache.get(builder1);

        TinySetBuilder<String> builder2 = new TinySetBuilder<>();
        for (int i = 0; i < 1000; i++)
            builder2.add(cache.get("aaa" + i));
        TinySet<String> map2 = cache.get(builder2);

        assertThat(map1).isSameAs(map2);
    }
}