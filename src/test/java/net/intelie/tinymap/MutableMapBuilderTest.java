package net.intelie.tinymap;

import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class MutableMapBuilderTest {
    @Test
    public void testAddAndGet() {
        MutableMapBuilder<String, Object> builder = new MutableMapBuilder<>();

        builder.put("abc", 123);
        builder.put("abc", 456);

        assertThat(builder.size()).isEqualTo(1);
        assertThat(builder.containsKey("abc")).isTrue();
        assertThat(builder.get("abc")).isEqualTo(456);

        assertThat(builder.build()).isEqualTo(Collections.singletonMap("abc", 456));
        assertThat(builder.build()).isEqualTo(Collections.singletonMap("abc", 456));
    }

    @Test
    public void testAddAndRemove() {
        MutableMapBuilder<String, Object> builder = new MutableMapBuilder<>();

        for (int i = 0; i < 100; i++)
            assertThat(builder.put("aaa" + i, i)).isNull();
        for (int i = 0; i < 100; i++)
            assertThat(builder.containsKey("aaa" + i)).isTrue();

        assertThat(builder.size()).isEqualTo(100);

        for (int i = 0; i < 100; i += 2)
            assertThat(builder.remove("aaa" + i)).isEqualTo(i);

        for (int i = 0; i < 100; i++) {
            assertThat(builder.containsKey("aaa" + i)).isEqualTo(i % 2 != 0);
        }

        assertThat(builder.size()).isEqualTo(50);
        assertThat(builder.build().size()).isEqualTo(50);
    }

    @Test
    public void testBuildEmpty() {
        testInsertCount(0, false);
        testInsertCount(0, true);
    }

    @Test
    public void testBuildMedium() {
        testInsertCount(1000, false);
        testInsertCount(1000, true);
    }

    @Test
    public void testBuildLarge() {
        testInsertCount(0x10000, true);
    }

    @Test
    public void testBuildAlmostThere() {
        testInsertCount(255, false);
        testInsertCount(255, true);
    }


    private void testInsertCount(int count, boolean withNull) {
        MutableMapBuilder<String, Object> builder = new MutableMapBuilder<>();
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

        int index = 0;
        for (Map.Entry<String, Object> entry : expectedMap.entrySet()) {
            assertThat(map.get(entry.getKey())).isEqualTo(entry.getValue());
            assertThat(map.getIndex(entry.getKey())).isEqualTo(index);
            assertThat(map.getKeyAt(index)).isEqualTo(entry.getKey());
            assertThat(map.getValueAt(index)).isEqualTo(entry.getValue());

            assertThat(keysIterator.hasNext()).isTrue();
            assertThat(keysIterator.next()).isEqualTo(entry.getKey());

            assertThat(valuesIterator.hasNext()).isTrue();
            assertThat(valuesIterator.next()).isEqualTo(entry.getValue());

            assertThat(entriesIterator.hasNext()).isTrue();
            assertThat(entriesIterator.next()).isEqualTo(entry);
            index++;
        }
        assertThat(keysIterator.hasNext()).isFalse();
        assertThat(valuesIterator.hasNext()).isFalse();
        assertThat(entriesIterator.hasNext()).isFalse();

        assertThat(map.get("bbb")).isNull();
        assertThat(map.getIndex("bbb")).isEqualTo(-1);
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


}