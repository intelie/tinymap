package net.intelie.tinymap.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StringCacheAdapterTest {
    @Test
    public void testSameHashCodeStringAndStringBuilder() {
        StringBuilder builder = new StringBuilder("abcdef");

        StringCacheAdapter adapter = new StringCacheAdapter();
        assertThat(adapter.contentHashCode(builder)).isEqualTo(adapter.contentHashCode("abcdef"));
    }
}