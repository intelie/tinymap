package net.intelie.tinymap.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class ObjectOptimizerTest {
    @Test
    public void testOptimizeSimpleMap() {
        LinkedHashMap<String, Object> obj = new LinkedHashMap<>();
        obj.put("aaa", Arrays.asList(123, "456"));
        obj.put("bbb", Collections.singletonMap("ccc", 111));
        obj.put("ddd", ImmutableMap.of("eee", 222, "fff", 333.0));
        obj.put("ggg", ImmutableSet.of("eee", 222, "fff", 333.0));

        ObjectOptimizer optimizer = new ObjectOptimizer(new DefaultObjectCache());
        Object optimized = optimizer.optimize(obj);

        assertThat(optimized).isEqualTo(obj);
    }

    @Test
    public void testOptimizeSimpleMapNoCache() {
        LinkedHashMap<String, Object> obj = new LinkedHashMap<>();
        obj.put("aaa", Arrays.asList(123, "456"));
        obj.put("bbb", Collections.singletonMap("ccc", 111));
        obj.put("ddd", ImmutableMap.of("eee", 222, "fff", 333.0));
        obj.put("ggg", ImmutableSet.of("eee", 222, "fff", 333.0));

        ObjectOptimizer optimizer = new ObjectOptimizer(null);
        Object optimized = optimizer.optimize(obj);

        assertThat(optimized).isEqualTo(obj);
    }

    @Test
    public void testOptimizeSimpleMapWithException() {
        RuntimeException ex = new RuntimeException("abc");
        Map map = mock(Map.class);
        doThrow(ex).when(map).forEach(any());

        LinkedHashMap<String, Object> obj = new LinkedHashMap<>();
        obj.put("aaa", Arrays.asList(123, "456", Collections.singleton(map)));
        obj.put("bbb", Collections.singletonMap("ccc", "ddd"));
        obj.put("ddd", ImmutableMap.of("eee", 222, "fff", 333.0));


        ObjectOptimizer optimizer = new ObjectOptimizer(new DefaultObjectCache());
        assertThatThrownBy(() -> optimizer.optimize(obj)).isSameAs(ex);
    }
}