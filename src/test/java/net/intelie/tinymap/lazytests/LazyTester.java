package net.intelie.tinymap.lazytests;

import static org.assertj.core.api.Assertions.assertThat;

public class LazyTester<T, B extends T, Q> {
    private final B builder;
    private final T expected;
    private final Assert<B, T> assertion;

    public LazyTester(B builder, T expected, Assert<B, T> assertion) {
        this.builder = builder;
        this.expected = expected;
        this.assertion = assertion;
    }

    public void itv(VoidFn<T> fn) throws Exception {
        fn.apply(expected);
        fn.apply(builder);
        assertion.apply(builder, expected);
    }

    public <V> V it(Fn<T, V> fn) throws Exception {
        V expectedResult = fn.apply(expected);
        V builderResult = fn.apply(builder);
        assertThat(builderResult).isEqualTo(expectedResult);
        assertion.apply(builder, expected);
        return builderResult;
    }

    public B getBuilder() {
        return builder;
    }

    public T getExpected() {
        return expected;
    }

    interface VoidFn<T> {
        void apply(T obj) throws Exception;
    }

    interface Fn<T, Q> {
        Q apply(T obj) throws Exception;
    }

    interface Assert<T, B> {
        void apply(T expected, B builder) throws Exception;
    }
}
