package net.intelie.tinymap.json;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;
import net.intelie.tinymap.ObjectCache;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.*;

public class TinyJsonDecoderTest {

    private Gson gson;
    private ObjectCache cache;

    @Before
    public void setUp() throws Exception {
        gson = new Gson();
        cache = new ObjectCache();
    }

    @Test
    public void testDecoderIsLenientByDefaultButReaderIsNot() {
        TinyJsonDecoder decoder = new TinyJsonDecoder(cache, new StringReader("{a:1}"));
        assertThat(decoder.isLenient()).isTrue();

        TinyJsonReader reader = new TinyJsonReader(cache, new StringReader("{a:1}"));
        assertThat(reader.isLenient()).isFalse();

        assertThat(decoder.toString()).isEqualTo("TinyJsonDecoder at line 1 column 1 path $");
        assertThat(reader.toString()).isEqualTo("TinyJsonReader at line 1 column 1 path $");
    }

    @Test
    public void testInnerInvalidJson() throws IOException {
        TinyJsonDecoder decoder = new TinyJsonDecoder(cache, new StringReader("{a:[{c:/}]}}"));
        assertThatThrownBy(() -> decoder.nextObject())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Expected value");
    }

    @Test
    public void testValid() throws IOException {
        assertValidJson(1, "{a:1}");
        assertValidJson(2, "{a:1}{b:3}");
        assertValidJson(2, "{a:1}\n\t{b:3}");
        assertValidJson(1, "{'a':[1, '2', true, null]}");
    }

    @Test
    public void lab() throws IOException {

    }

    @Test
    public void testInvalid() throws IOException {
        assertInvalidJson(false, "Use JsonReader.setLenient(true)", "1-", JsonReader::peek, TinyJsonReader::peek);
        assertInvalidJson(false, "Use JsonReader.setLenient(true)", "1+", JsonReader::peek, TinyJsonReader::peek);
        assertInvalidJson(false, "Use JsonReader.setLenient(true)", "01", JsonReader::peek, TinyJsonReader::peek);
        assertInvalidJson(false, "Use JsonReader.setLenient(true)", "f", JsonReader::nextBoolean, TinyJsonReader::nextBoolean);
        assertInvalidJson(false, "Use JsonReader.setLenient(true)", "felse", JsonReader::nextBoolean, TinyJsonReader::nextBoolean);

        assertInvalidJson(true, "Expected a double but was BOOLEAN", "true", JsonReader::nextDouble, TinyJsonReader::nextDouble);
        assertInvalidJson(false, "JSON forbids NaN and infinities", "\"NaN\"", JsonReader::nextDouble, TinyJsonReader::nextDouble);
        assertInvalidJson(true, "Unterminated string", "\"NaN", JsonReader::nextString, TinyJsonReader::nextString);
        assertInvalidJson(true, "Unterminated string", "\"NaN", JsonReader::skipValue, TinyJsonReader::skipValue);
        assertInvalidJson(true, "Unterminated string", "'NaN", JsonReader::skipValue, TinyJsonReader::skipValue);
        assertInvalidJson(true, "Use JsonReader.setLenient(true)", "{test;", x -> {
            x.beginObject();
            x.peek();
            x.setLenient(false);
            x.skipValue();
        }, x -> {
            x.beginObject();
            x.peek();
            x.setLenient(false);
            x.skipValue();
        });
        assertInvalidJson(true, "Use JsonReader.setLenient(true)", "{test;", x -> {
            x.beginObject();
            x.peek();
            x.setLenient(false);
            x.nextName();
        }, x -> {
            x.beginObject();
            x.peek();
            x.setLenient(false);
            x.nextName();
        });

        assertInvalidJson(true, "Unterminated array ", "[1[", x -> {
            x.beginArray();
            x.nextDouble();
            x.nextDouble();
        }, x -> {
            x.beginArray();
            x.nextDouble();
            x.nextDouble();
        });

        assertInvalidJson(true, "Unterminated comment", "/*", JsonReader::skipValue, TinyJsonReader::skipValue);
    }

    @Test
    public void testLists() throws Exception {
        assertListOf("[1, 2, 3, 0.3e+5, 0.3e-5]", JsonReader::nextDouble, TinyJsonReader::nextDouble);
        assertListOf("[1, '2', \"3\", 4.5, NaN]", JsonReader::nextDouble, TinyJsonReader::nextDouble);
        assertListOf("[1, '2', \"3\", 4.5, NaN]", JsonReader::nextString, TinyJsonReader::nextString);
        assertListOf("[true, false]", JsonReader::nextBoolean, TinyJsonReader::nextBoolean);
        assertListOf("[null, null]", JsonReader::nextNull, TinyJsonReader::nextNull);
    }

    private void assertInvalidJson(boolean lenient, String exception, String s, VoidCheckedFunction<JsonReader> expectedFn, VoidCheckedFunction<TinyJsonReader> actualFn) throws IOException {
        assertInvalidJson(lenient, exception, s, x -> {
            expectedFn.apply(x);
            return null;
        }, x -> {
            actualFn.apply(x);
            return null;
        });
    }

    private <T> void assertInvalidJson(boolean lenient, String exception, String s, CheckedFunction<JsonReader, T> expectedFn, CheckedFunction<TinyJsonReader, T> actualFn) throws IOException {
        try (JsonReader expectedReader = new JsonReader(new StringReader(s));
             TinyJsonDecoder reader = new TinyJsonDecoder(cache, new StringReader(s))) {
            reader.setLenient(lenient);
            expectedReader.setLenient(lenient);

            try {
                expectedFn.apply(expectedReader);
                fail("must throw");
            } catch (Exception e) {
                if (e instanceof MalformedJsonException)
                    assertThatThrownBy(() -> actualFn.apply(reader))
                            .isInstanceOf(IllegalStateException.class)
                            .hasMessage(e.getMessage())
                            .hasMessageContaining(exception);
                else
                    assertThatThrownBy(() -> actualFn.apply(reader))
                            .isInstanceOf(e.getClass())
                            .hasMessage(e.getMessage())
                            .hasMessageContaining(exception);
            }
        }

    }

    private void assertListOf(String s, VoidCheckedFunction<JsonReader> expectedFn, VoidCheckedFunction<TinyJsonReader> actualFn) throws Exception {
        assertListOf(s, x -> {
            expectedFn.apply(x);
            return null;
        }, x -> {
            actualFn.apply(x);
            return null;
        });
    }

    private <T> void assertListOf(String s, CheckedFunction<JsonReader, T> expectedFn, CheckedFunction<TinyJsonReader, T> actualFn) throws Exception {
        try (JsonReader expectedReader = new JsonReader(new StringReader(s));
             TinyJsonDecoder reader = new TinyJsonDecoder(cache, new StringReader(s))) {
            expectedReader.setLenient(true);

            expectedReader.beginArray();
            reader.beginArray();

            while (expectedReader.hasNext()) {
                T expectedValue = expectedFn.apply(expectedReader);
                assertThat(actualFn.apply(reader)).isEqualTo(expectedValue);
            }
            assertThat(reader.hasNext()).isFalse();

            assertThat(reader.peek()).isEqualTo(net.intelie.tinymap.json.JsonToken.END_ARRAY);

            expectedReader.endArray();
            reader.endArray();

            assertThat(reader.peek().name()).isEqualTo(JsonToken.END_DOCUMENT.name());
            assertThat(expectedReader.peek().name()).isEqualTo(JsonToken.END_DOCUMENT.name());
        }
    }

    private void assertValidJson(int expectedDocs, String s) throws IOException {
        int actualDocs = 0;
        try (JsonReader expectedReader = new JsonReader(new StringReader(s));
             TinyJsonDecoder reader = new TinyJsonDecoder(cache, new StringReader(s))) {
            expectedReader.setLenient(true);
            while (expectedReader.peek() != JsonToken.END_DOCUMENT) {
                Object expectedValue = gson.fromJson(expectedReader, Object.class);
                assertThat(reader.nextObject()).isEqualTo(expectedValue);
                actualDocs++;
            }
        }
        assertThat(actualDocs).isEqualTo(expectedDocs);
    }

    @FunctionalInterface
    public interface CheckedFunction<T, R> {
        R apply(T t) throws Exception;
    }

    @FunctionalInterface
    public interface VoidCheckedFunction<T> {
        void apply(T t) throws Exception;
    }

}