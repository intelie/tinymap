package net.intelie.tinymap;

import net.intelie.introspective.reflect.ReflectionCache;
import net.intelie.tinymap.support.SerializationHelper;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class TinyListTest {
    @Test
    public void testSizes() {
        ReflectionCache reflection = new ReflectionCache();
        assertThat(reflection.get(TinyList.class).size()).isEqualTo(20);
    }

    @Test
    public void testBuilderHashCode() {
        TinyListBuilder<String> builder1 = TinyList.builder();
        TinyListBuilder<String> builder2 = TinyList.builder();

        TinyListBuilder.Adapter<String> adapter = builder1.adapter();

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
    public void testBuildAndGet() {
        TinyListBuilder<Object> builder = TinyList.builder();
        assertThat(builder.size()).isEqualTo(0);
        builder.add("aaa");
        builder.add(123);
        assertThat(builder.size()).isEqualTo(2);
        List<Object> list = builder.buildAndClear();

        assertThat(list.get(0)).isEqualTo("aaa");
        assertThat(list.get(1)).isEqualTo(123);
        assertThatThrownBy(() -> list.get(2)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThat(list.size()).isEqualTo(2);

        assertThat(list).containsExactly("aaa", 123);
    }

    @Test
    public void testForEach() {
        TinyListBuilder<Object> builder = TinyList.builder();
        builder.add("aaa");
        builder.add(123);
        List<Object> map = builder.buildAndClear();

        Consumer consumer = mock(Consumer.class);

        map.forEach(consumer);

        InOrder orderly = inOrder(consumer);
        orderly.verify(consumer).accept("aaa");
        orderly.verify(consumer).accept(123);
        orderly.verifyNoMoreInteractions();
    }

    @Test
    public void testBuildEmpty() throws IOException, ClassNotFoundException {
        testCount(0);
    }

    @Test
    public void testBuildGiant() throws IOException, ClassNotFoundException {
        testCount(1000);
    }

    @Test
    public void testBuildAlmostThere() throws IOException, ClassNotFoundException {
        testCount(255);
    }

    private void testCount(int count) throws IOException, ClassNotFoundException {
        TinyListBuilder<Object> builder = TinyList.builder();
        ArrayList<String> expected = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            builder.add("aaa" + i);
            expected.add("aaa" + i);
        }
        TinyList<Object> list = builder.buildAndClear();


        assertThat(list.size()).isEqualTo(expected.size());
        for (int i = 0; i < expected.size(); i++) {
            assertThat(list.get(i)).isEqualTo(expected.get(i));
        }
        assertThat(list.toString()).isEqualTo(expected.toString());

        assertThat(list).isEqualTo(expected);
        assertThat(expected).isEqualTo(list);
        assertThat(list.hashCode()).isEqualTo(expected.hashCode());

        byte[] serialized = SerializationHelper.testSerialize(list);
        byte[] serializedExpected = SerializationHelper.testSerialize(expected);
        if (expected.size() > 0)
            assertThat(serialized.length).isLessThan(2 * serializedExpected.length);

        List<Object> deserialized = SerializationHelper.testDeserialize(serialized);
        assertThat(deserialized).isEqualTo(list);
    }
}