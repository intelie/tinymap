package net.intelie.tinymap.json;

import net.intelie.introspective.ThreadResources;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.fail;

public class FastDoubleTest {
    @Test
    public void testBasics() {
        assertParseFast("1");
        assertParseFast("0.1");
        assertParseFast("  -0.1  ");
        assertParseFast("  +0.1  ");
        assertParseFast("1E2");
        assertParseFast("1E+2");
        assertParseFast("1E-2");
        assertParseFast("1e-2");
        assertParseFast(".1");
        assertParseFast("-0");
        assertParseFast("-1.1");
        assertParseFast("0.00000000000001");
        assertParseFast("0.3");
        assertParseFast("1.14");
        assertParseFast("123456789");
        assertParseFast("999999999999999");
        assertParseFast("-999999999999999");
    }

    @Test
    public void testFallbacks() {
        assertParseFallback("3.3e-256");
        assertParseFallback("0.3e-256");
        assertParseFallback("0.0000000000000000000000000001");
        assertParseFallback("NaN");
        assertParseFallback("Infinity");
        assertParseFallback("-Infinity");
        assertParseFallback("4.198562687463195E38");
        assertParseFallback("1.0999999999999998E38");
        assertParseFallback("123456789123456789");
        assertParseFallback("123456789123456789123");
    }

    @Test
    public void testErrors() {
        assertException("-");
        assertException(".");
        assertException("");
        assertException("aaa");
        assertException("-e");
        assertException("1e");
        assertException("1e1.0");
        assertException("1a2");
    }

    @Test
    public void lab() {
        assertParseFallback("5.0000000000000004E36");
    }

    @Test
    public void testRanges() {
        for (int k = -40; k < 40; k++) {
            for (int i = 0; i < 1000; i++) {
                assertParseFallback(Double.toString(i / Math.pow(10, k)));
            }
        }
    }

    @Test
    public void testLargeRanges() {
        for (int i = -100000; i < 100000; i++) {
            assertParseFallback(Double.toString(i * 100000000000000L));
        }
    }

    private void assertParseFast(String s) {
        for (int i = 0; i < 1000; i++)
            FastDouble.parseDouble(s);

        long startMem = ThreadResources.allocatedBytes();
        for (int i = 0; i < 1000; i++)
            FastDouble.parseDouble(s);
        assertThat(ThreadResources.allocatedBytes() - startMem).describedAs(s).isZero();

        assertParseFallback(s);
    }

    private void assertException(String s) {
        try {
            Double.parseDouble(s);
            fail("must throw");
        } catch (Exception e) {
            assertThatThrownBy(() -> FastDouble.parseDouble(s))
                    .isInstanceOf(e.getClass())
                    .hasMessage(e.getMessage());
            assertThatThrownBy(() -> FastDouble.parseDouble(new StringBuilder().append("xxx").append(s).append("xxx"), 3, s.length() + 3))
                    .isInstanceOf(e.getClass())
                    .hasMessage(e.getMessage());
        }
    }

    private void assertParseFallback(String s) {
        Double d1 = Double.parseDouble(s);
        Double d2 = FastDouble.parseDouble(s);
        Double d3 = FastDouble.parseDouble(new StringBuilder().append("xxx").append(s).append("xxx"), 3, s.length() + 3);

        assertThat(d2).describedAs(s).isEqualTo(d1);
        assertThat(d3).describedAs(s).isEqualTo(d1);
    }
}
