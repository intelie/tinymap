package net.intelie.tinymap.lazytests;

import net.intelie.tinymap.TinyList;
import net.intelie.tinymap.TinyListBuilder;
import net.intelie.tinymap.base.IndexedList;
import net.intelie.tinymap.support.ListAsserts;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ListLazyTests {
    @Test
    public void testALotOfOperations() throws Exception {
        LazyTester<List<Object>, TinyListBuilder<Object>, TinyList<Object>> tester = new LazyTester<>(
                TinyList.builder(), new ArrayList<>(), (a, b) -> ListAsserts.assertList(b, a));

        tester.it(x -> x.addAll(range(50, 100)));
        tester.it(x -> x.addAll(0, range(0, 20)));
        tester.it(x -> x.addAll(20, range(20, 50)));
        tester.it(x -> x.removeAll(range(30, 70)));
        tester.it(x -> x.addAll(30, range(30, 70)));
        tester.itv(x -> {
            ListIterator<Object> it = x.listIterator();
            for (int i = 0; i < 30; i++)
                it.next();

            it.next();
            it.remove();
            assertThatThrownBy(it::remove).isInstanceOf(IllegalStateException.class);
            it.next();
            it.remove();
            assertThat(it.next()).isEqualTo("aaa32");
            assertThat(it.previous()).isEqualTo("aaa32");
            assertThat(it.previous()).isEqualTo("aaa29");
        });
        tester.itv(x -> {
            ListIterator<Object> it = x.listIterator(30);
            it.add("bbb30");
            it.add("bbb31");
        });
        tester.itv(x -> {
            ListIterator<Object> it = x.listIterator();
            int i = 0;
            while (it.hasNext()) {
                it.next();
                it.set("aaa" + i++);
            }
        });

        assertThat(tester.getBuilder()).containsExactlyElementsOf(
                range(0, 100)
        );
    }

    @Test
    public void testInsertInSubList() throws Exception {
        LazyTester<List<Object>, TinyListBuilder<Object>, TinyList<Object>> tester = new LazyTester<>(
                TinyList.builder(), new ArrayList<>(), (a, b) -> ListAsserts.assertList(b, a));

        tester.it(x -> x.addAll(range(0, 10)));
        tester.it(x -> x.addAll(range(90, 100)));
        tester.itv(x -> {
            List<Object> list = x.subList(5, 15);
            assertThat(list.get(0)).isEqualTo("aaa5");

            list.addAll(5, range(10, 80));
        });
        tester.it(x -> {
            List<Object> list = x.subList(10, 80);
            if (x instanceof IndexedList)
                return ((IndexedList<Object>) list).removeLast();
            else
                return list.remove(69);
        });
        tester.itv(x -> x.subList(0, 79).addAll(range(79, 90)));

        assertThat(tester.getBuilder()).containsExactlyElementsOf(
                range(0, 100)
        );
    }

    @Test
    public void testIndexOf() throws Exception {
        LazyTester<List<Object>, TinyListBuilder<Object>, TinyList<Object>> tester = new LazyTester<>(
                TinyList.builder(), new ArrayList<>(), (a, b) -> ListAsserts.assertList(b, a));

        tester.it(x -> x.addAll(range(0, 100)));
        tester.it(x -> x.indexOf("aaa30"));
        tester.it(x -> x.lastIndexOf("aaa30"));

    }

    private List<String> range(int start, int end) {
        return IntStream.range(start, end).mapToObj(i -> "aaa" + i).collect(Collectors.toList());
    }
}
