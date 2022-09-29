package net.intelie.tinymap.json;

import junit.framework.TestCase;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

import static net.intelie.tinymap.json.JsonToken.*;

@SuppressWarnings("resource")
public final class TinyJsonReaderTest extends TestCase {
    private TinyJsonReader reader;

    @Override
    public void setUp() {
        reader = new TinyJsonReader();
    }

    public void testReadArray() throws IOException {
        reader.resetTo(reader("[true, true]"));
        reader.beginArray();
        assertEquals(true, reader.nextBoolean());
        assertEquals(true, reader.nextBoolean());
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testReadEmptyArray() throws IOException {
        reader.resetTo(reader("[]"));
        reader.beginArray();
        assertFalse(reader.hasNext());
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testReadObject() throws IOException {
        reader.resetTo(reader(
                "{\"a\": \"android\", \"b\": \"banana\"}"));
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
        assertEquals("android", reader.nextString().toString());
        assertEquals("b", reader.nextName().toString());
        assertEquals("banana", reader.nextString().toString());
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testReadEmptyObject() throws IOException {
        reader.resetTo(reader("{}"));
        reader.beginObject();
        assertFalse(reader.hasNext());
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testSkipArray() throws IOException {
        reader.resetTo(reader(
                "{\"a\": [\"one\", \"two\", \"three\"], \"b\": 123}"));
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
        reader.skipValue();
        assertEquals("b", reader.nextName().toString());
        assertEquals(123.0, reader.nextDouble());
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testSkipArrayAfterPeek() throws Exception {
        reader.resetTo(reader(
                "{\"a\": [\"one\", \"two\", \"three\"], \"b\": 123}"));
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
        assertEquals(BEGIN_ARRAY, reader.peek());
        reader.skipValue();
        assertEquals("b", reader.nextName().toString());
        assertEquals(123.0, reader.nextDouble());
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testSkipTopLevelObject() throws Exception {
        reader.resetTo(reader(
                "{\"a\": [\"one\", \"two\", \"three\"], \"b\": 123}"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testSkipObject() throws IOException {
        reader.resetTo(reader(
                "{\"a\": { \"c\": [], \"d\": [true, true, {}] }, \"b\": \"banana\"}"));
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
        reader.skipValue();
        assertEquals("b", reader.nextName().toString());
        reader.skipValue();
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testSkipObjectAfterPeek() throws Exception {
        String json = "{" + "  \"one\": { \"num\": 1 }"
                + ", \"two\": { \"num\": 2 }" + ", \"three\": { \"num\": 3 }" + "}";
        reader.resetTo(reader(json));
        reader.beginObject();
        assertEquals("one", reader.nextName().toString());
        assertEquals(BEGIN_OBJECT, reader.peek());
        reader.skipValue();
        assertEquals("two", reader.nextName().toString());
        assertEquals(BEGIN_OBJECT, reader.peek());
        reader.skipValue();
        assertEquals("three", reader.nextName().toString());
        reader.skipValue();
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testSkipInteger() throws IOException {
        reader.resetTo(reader(
                "{\"a\":123456789,\"b\":-123456789}"));
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
        reader.skipValue();
        assertEquals("b", reader.nextName().toString());
        reader.skipValue();
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testSkipDouble() throws IOException {
        reader.resetTo(reader(
                "{\"a\":-123.456e-789,\"b\":123456789.0}"));
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
        reader.skipValue();
        assertEquals("b", reader.nextName().toString());
        reader.skipValue();
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testHelloWorld() throws IOException {
        String json = "{\n" +
                "   \"hello\": true,\n" +
                "   \"foo\": [\"world\"]\n" +
                "}";
        reader.resetTo(reader(json));
        reader.beginObject();
        assertEquals("hello", reader.nextName().toString());
        assertEquals(true, reader.nextBoolean());
        assertEquals("foo", reader.nextName().toString());
        reader.beginArray();
        assertEquals("world", reader.nextString().toString());
        reader.endArray();
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testInvalidJsonInput() throws IOException {
        String json = "{\n"
                + "   \"h\\ello\": true,\n"
                + "   \"foo\": [\"world\"]\n"
                + "}";

        reader.resetTo(reader(json));
        reader.beginObject();
        try {
            reader.nextName().toString();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testEmptyString() throws IOException {
        try {
            new TinyJsonReader(reader("")).beginArray();
            fail();
        } catch (EOFException expected) {
        }
        try {
            new TinyJsonReader(reader("")).beginObject();
            fail();
        } catch (EOFException expected) {
        }
    }

    public void testCharacterUnescaping() throws IOException {
        String json = "[\"a\","
                + "\"a\\\"\","
                + "\"\\\"\","
                + "\":\","
                + "\",\","
                + "\"\\b\","
                + "\"\\f\","
                + "\"\\n\","
                + "\"\\r\","
                + "\"\\t\","
                + "\" \","
                + "\"\\\\\","
                + "\"{\","
                + "\"}\","
                + "\"[\","
                + "\"]\","
                + "\"\\u0000\","
                + "\"\\u0019\","
                + "\"\\u20Ac\""
                + "]";
        reader.resetTo(reader(json));
        reader.beginArray();
        assertEquals("a", reader.nextString().toString());
        assertEquals("a\"", reader.nextString().toString());
        assertEquals("\"", reader.nextString().toString());
        assertEquals(":", reader.nextString().toString());
        assertEquals(",", reader.nextString().toString());
        assertEquals("\b", reader.nextString().toString());
        assertEquals("\f", reader.nextString().toString());
        assertEquals("\n", reader.nextString().toString());
        assertEquals("\r", reader.nextString().toString());
        assertEquals("\t", reader.nextString().toString());
        assertEquals(" ", reader.nextString().toString());
        assertEquals("\\", reader.nextString().toString());
        assertEquals("{", reader.nextString().toString());
        assertEquals("}", reader.nextString().toString());
        assertEquals("[", reader.nextString().toString());
        assertEquals("]", reader.nextString().toString());
        assertEquals("\0", reader.nextString().toString());
        assertEquals("\u0019", reader.nextString().toString());
        assertEquals("\u20AC", reader.nextString().toString());
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testUnescapingInvalidCharacters() throws IOException {
        String json = "[\"\\u000g\"]";
        reader.resetTo(reader(json));
        reader.beginArray();
        try {
            reader.nextString().toString();
            fail();
        } catch (NumberFormatException expected) {
        }
    }

    public void testUnescapingTruncatedCharacters() throws IOException {
        String json = "[\"\\u000";
        reader.resetTo(reader(json));
        reader.beginArray();
        try {
            reader.nextString().toString();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testUnescapingTruncatedSequence() throws IOException {
        String json = "[\"\\";
        reader.resetTo(reader(json));
        reader.beginArray();
        try {
            reader.nextString().toString();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testIntegersWithFractionalPartSpecified() throws IOException {
        reader.resetTo(reader("[1.0,1.0,1.0]"));
        reader.beginArray();
        assertEquals(1.0, reader.nextDouble());
        assertEquals(1.0, reader.nextDouble());
        assertEquals(1.0, reader.nextDouble());
    }

    public void testDoubles() throws IOException {
        String json = "[-0.0,"
                + "1.0,"
                + "1.7976931348623157E308,"
                + "4.9E-324,"
                + "0.0,"
                + "-0.5,"
                + "2.2250738585072014E-308,"
                + "3.141592653589793,"
                + "2.718281828459045]";
        reader.resetTo(reader(json));
        reader.beginArray();
        assertEquals(-0.0, reader.nextDouble());
        assertEquals(1.0, reader.nextDouble());
        assertEquals(1.7976931348623157E308, reader.nextDouble());
        assertEquals(4.9E-324, reader.nextDouble());
        assertEquals(0.0, reader.nextDouble());
        assertEquals(-0.5, reader.nextDouble());
        assertEquals(2.2250738585072014E-308, reader.nextDouble());
        assertEquals(3.141592653589793, reader.nextDouble());
        assertEquals(2.718281828459045, reader.nextDouble());
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testStrictNonFiniteDoubles() throws IOException {
        String json = "[NaN]";
        reader.resetTo(reader(json));
        reader.beginArray();
        try {
            reader.nextDouble();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testStrictQuotedNonFiniteDoubles() throws IOException {
        String json = "[\"NaN\"]";
        reader.resetTo(reader(json));
        reader.beginArray();
        try {
            reader.nextDouble();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testLenientNonFiniteDoubles() throws IOException {
        String json = "[NaN, -Infinity, Infinity]";
        reader.resetTo(reader(json));
        reader.setLenient(true);
        reader.beginArray();
        assertTrue(Double.isNaN(reader.nextDouble()));
        assertEquals(Double.NEGATIVE_INFINITY, reader.nextDouble());
        assertEquals(Double.POSITIVE_INFINITY, reader.nextDouble());
        reader.endArray();
    }

    public void testLenientQuotedNonFiniteDoubles() throws IOException {
        String json = "[\"NaN\", \"-Infinity\", \"Infinity\"]";
        reader.resetTo(reader(json));
        reader.setLenient(true);
        reader.beginArray();
        assertTrue(Double.isNaN(reader.nextDouble()));
        assertEquals(Double.NEGATIVE_INFINITY, reader.nextDouble());
        assertEquals(Double.POSITIVE_INFINITY, reader.nextDouble());
        reader.endArray();
    }

    public void testStrictNonFiniteDoublesWithSkipValue() throws IOException {
        String json = "[NaN]";
        reader.resetTo(reader(json));
        reader.beginArray();
        try {
            reader.skipValue();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testLongs() throws IOException {
        String json = "[0,0,0,"
                + "1,1,1,"
                + "-1,-1,-1,"
                + "-9223372036854775808,"
                + "9223372036854775807]";
        reader.resetTo(reader(json));
        reader.beginArray();
        assertEquals(0.0, reader.nextDouble());
        assertEquals(0.0, reader.nextDouble());
        assertEquals(0.0, reader.nextDouble());
        assertEquals(1.0, reader.nextDouble());
        assertEquals(1.0, reader.nextDouble());
        assertEquals(1.0, reader.nextDouble());
        assertEquals(-1.0, reader.nextDouble());
        assertEquals(-1.0, reader.nextDouble());
        assertEquals(-1.0, reader.nextDouble());
        assertEquals(-9223372036854775808.0, reader.nextDouble());
        assertEquals(9223372036854775807.0, reader.nextDouble());
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testBooleans() throws IOException {
        reader.resetTo(reader("[true,false]"));
        reader.beginArray();
        assertEquals(true, reader.nextBoolean());
        assertEquals(false, reader.nextBoolean());
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testPeekingUnquotedStringsPrefixedWithBooleans() throws IOException {
        reader.resetTo(reader("[truey]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(STRING, reader.peek());
        try {
            reader.nextBoolean();
            fail();
        } catch (IOException expected) {
        }
        assertEquals("truey", reader.nextString().toString());
        reader.endArray();
    }

    public void testMalformedNumbers() throws IOException {
        assertNotANumber("-");
        assertNotANumber(".");

        // exponent lacks digit
        assertNotANumber("e");
        assertNotANumber("0e");
        assertNotANumber(".e");
        assertNotANumber("0.e");
        assertNotANumber("-.0e");

        // no integer
        assertNotANumber("e1");
        assertNotANumber(".e1");
        assertNotANumber("-e1");

        // trailing characters
        assertNotANumber("1x");
        assertNotANumber("1.1x");
        assertNotANumber("1e1x");
        assertNotANumber("1ex");
        assertNotANumber("1.1ex");
        assertNotANumber("1.1e1x");

        // fraction has no digit
        assertNotANumber("0.");
        assertNotANumber("-0.");
        assertNotANumber("0.e1");
        assertNotANumber("-0.e1");

        // no leading digit
        assertNotANumber(".0");
        assertNotANumber("-.0");
        assertNotANumber(".0e1");
        assertNotANumber("-.0e1");
    }

    private void assertNotANumber(String s) throws IOException {
        reader.resetTo(reader("[" + s + "]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(STRING, reader.peek());
        assertEquals(s, reader.nextString().toString());
        reader.endArray();
    }

    public void testPeekingUnquotedStringsPrefixedWithIntegers() throws IOException {
        reader.resetTo(reader("[12.34e5x]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(STRING, reader.peek());
        try {
            reader.nextDouble();
            fail();
        } catch (NumberFormatException expected) {
        }
        try {
            reader.nextDouble();
            fail();
        } catch (NumberFormatException expected) {
        }
        assertEquals("12.34e5x", reader.nextString().toString());
    }

    public void testPeekLongMinValue() throws IOException {
        reader.resetTo(reader("[-9223372036854775808]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(NUMBER, reader.peek());
        assertEquals(-9223372036854775808.0, reader.nextDouble());
    }

    public void testPeekLongMaxValue() throws IOException {
        reader.resetTo(reader("[9223372036854775807]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(NUMBER, reader.peek());
        assertEquals(9223372036854775807.0, reader.nextDouble());
    }


    /**
     * Issue 1053, negative zero.
     *
     * @throws Exception
     */
    public void testNegativeZero() throws Exception {
        reader.resetTo(reader("[-0]"));
        reader.setLenient(false);
        reader.beginArray();
        assertEquals(NUMBER, reader.peek());
        assertEquals("-0", reader.nextString().toString());
    }

    /**
     * This test fails because there's no double for 9223372036854775808, and our
     * long parsing uses Double.parseDouble() for fractional values.
     */
    public void disabled_testPeekLargerThanLongMaxValue() throws IOException {
        reader.resetTo(reader("[9223372036854775808]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(NUMBER, reader.peek());
        try {
            reader.nextDouble();
            fail();
        } catch (NumberFormatException e) {
        }
    }

    /**
     * This test fails because there's no double for -9223372036854775809, and our
     * long parsing uses Double.parseDouble() for fractional values.
     */
    public void disabled_testPeekLargerThanLongMinValue() throws IOException {
        reader.resetTo(reader("[-9223372036854775809]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(NUMBER, reader.peek());
        try {
            reader.nextDouble();
            fail();
        } catch (NumberFormatException expected) {
        }
        assertEquals(-9223372036854775809d, reader.nextDouble());
    }

    /**
     * This test fails because there's no double for 9223372036854775806, and
     * our long parsing uses Double.parseDouble() for fractional values.
     */
    public void disabled_testHighPrecisionLong() throws IOException {
        String json = "[9223372036854775806.000]";
        reader.resetTo(reader(json));
        reader.beginArray();
        assertEquals(9223372036854775806L, reader.nextDouble());
        reader.endArray();
    }

    public void testQuotedNumberWithEscape() throws IOException {
        reader.resetTo(reader("[\"12\u00334\"]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(STRING, reader.peek());
        assertEquals(1234.0, reader.nextDouble());
    }

    public void testMixedCaseLiterals() throws IOException {
        reader.resetTo(reader("[True,TruE,False,FALSE,NULL,nulL]"));
        reader.beginArray();
        assertEquals(true, reader.nextBoolean());
        assertEquals(true, reader.nextBoolean());
        assertEquals(false, reader.nextBoolean());
        assertEquals(false, reader.nextBoolean());
        reader.nextNull();
        reader.nextNull();
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testMissingValue() throws IOException {
        reader.resetTo(reader("{\"a\":}"));
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
        try {
            reader.nextString().toString();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testPrematureEndOfInput() throws IOException {
        reader.resetTo(reader("{\"a\":true,"));
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
        assertEquals(true, reader.nextBoolean());
        try {
            reader.nextName().toString();
            fail();
        } catch (EOFException expected) {
        }
    }

    public void testPrematurelyClosed() throws IOException {
        try {
            reader.resetTo(reader("{\"a\":[]}"));
            reader.beginObject();
            reader.close();
            reader.nextName().toString();
            fail();
        } catch (IOException expected) {
        }

        try {
            reader.resetTo(reader("{\"a\":[]}"));
            reader.close();
            reader.beginObject();
            fail();
        } catch (IOException expected) {
        }

        try {
            reader.resetTo(reader("{\"a\":true}"));
            reader.beginObject();
            reader.nextName().toString();
            reader.peek();
            reader.close();
            reader.nextBoolean();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testNextFailuresDoNotAdvance() throws IOException {
        reader.resetTo(reader("{\"a\":true}"));
        reader.beginObject();
        try {
            reader.nextString().toString();
            fail();
        } catch (IOException expected) {
        }
        assertEquals("a", reader.nextName().toString());
        try {
            reader.nextName().toString();
            fail();
        } catch (IOException expected) {
        }
        try {
            reader.beginArray();
            fail();
        } catch (IOException expected) {
        }
        try {
            reader.endArray();
            fail();
        } catch (IOException expected) {
        }
        try {
            reader.beginObject();
            fail();
        } catch (IOException expected) {
        }
        try {
            reader.endObject();
            fail();
        } catch (IOException expected) {
        }
        assertEquals(true, reader.nextBoolean());
        try {
            reader.nextString().toString();
            fail();
        } catch (IOException expected) {
        }
        try {
            reader.nextName().toString();
            fail();
        } catch (IOException expected) {
        }
        try {
            reader.beginArray();
            fail();
        } catch (IOException expected) {
        }
        try {
            reader.endArray();
            fail();
        } catch (IOException expected) {
        }
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        reader.close();
    }

    public void testDoubleMismatchFailuresDoNotAdvance() throws IOException {
        reader.resetTo(reader("[\"1x5\"]"));
        reader.beginArray();
        try {
            reader.nextDouble();
            fail();
        } catch (NumberFormatException expected) {
        }
        assertEquals("1x5", reader.nextString().toString());
        reader.endArray();
    }

    public void testStringNullIsNotNull() throws IOException {
        reader.resetTo(reader("[\"null\"]"));
        reader.beginArray();
        try {
            reader.nextNull();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testNullLiteralIsNotAString() throws IOException {
        reader.resetTo(reader("[null]"));
        reader.beginArray();
        try {
            reader.nextString().toString();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testStrictNameValueSeparator() throws IOException {
        reader.resetTo(reader("{\"a\"=true}"));
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
        try {
            reader.nextBoolean();
            fail();
        } catch (IOException expected) {
        }

        reader = new TinyJsonReader(reader("{\"a\"=>true}"));
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
        try {
            reader.nextBoolean();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testLenientNameValueSeparator() throws IOException {
        reader.resetTo(reader("{\"a\"=true}"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
        assertEquals(true, reader.nextBoolean());

        reader = new TinyJsonReader(reader("{\"a\"=>true}"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
        assertEquals(true, reader.nextBoolean());
    }

    public void testStrictNameValueSeparatorWithSkipValue() throws IOException {
        reader.resetTo(reader("{\"a\"=true}"));
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
        try {
            reader.skipValue();
            fail();
        } catch (IOException expected) {
        }

        reader = new TinyJsonReader(reader("{\"a\"=>true}"));
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
        try {
            reader.skipValue();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testCommentsInStringValue() throws Exception {
        reader.resetTo(reader("[\"// comment\"]"));
        reader.beginArray();
        assertEquals("// comment", reader.nextString().toString());
        reader.endArray();

        reader = new TinyJsonReader(reader("{\"a\":\"#someComment\"}"));
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
        assertEquals("#someComment", reader.nextString().toString());
        reader.endObject();

        reader = new TinyJsonReader(reader("{\"#//a\":\"#some //Comment\"}"));
        reader.beginObject();
        assertEquals("#//a", reader.nextName().toString());
        assertEquals("#some //Comment", reader.nextString().toString());
        reader.endObject();
    }

    public void testStrictComments() throws IOException {
        reader.resetTo(reader("[// comment \n true]"));
        reader.beginArray();
        try {
            reader.nextBoolean();
            fail();
        } catch (IOException expected) {
        }

        reader = new TinyJsonReader(reader("[# comment \n true]"));
        reader.beginArray();
        try {
            reader.nextBoolean();
            fail();
        } catch (IOException expected) {
        }

        reader = new TinyJsonReader(reader("[/* comment */ true]"));
        reader.beginArray();
        try {
            reader.nextBoolean();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testLenientComments() throws IOException {
        reader.resetTo(reader("[// comment \n true]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(true, reader.nextBoolean());

        reader = new TinyJsonReader(reader("[# comment \n true]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(true, reader.nextBoolean());

        reader = new TinyJsonReader(reader("[/* comment */ true]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(true, reader.nextBoolean());
    }

    public void testStrictCommentsWithSkipValue() throws IOException {
        reader.resetTo(reader("[// comment \n true]"));
        reader.beginArray();
        try {
            reader.skipValue();
            fail();
        } catch (IOException expected) {
        }

        reader = new TinyJsonReader(reader("[# comment \n true]"));
        reader.beginArray();
        try {
            reader.skipValue();
            fail();
        } catch (IOException expected) {
        }

        reader = new TinyJsonReader(reader("[/* comment */ true]"));
        reader.beginArray();
        try {
            reader.skipValue();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testStrictUnquotedNames() throws IOException {
        reader.resetTo(reader("{a:true}"));
        reader.beginObject();
        try {
            reader.nextName().toString();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testLenientUnquotedNames() throws IOException {
        reader.resetTo(reader("{a:true}"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
    }

    public void testStrictUnquotedNamesWithSkipValue() throws IOException {
        reader.resetTo(reader("{a:true}"));
        reader.beginObject();
        try {
            reader.skipValue();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testStrictSingleQuotedNames() throws IOException {
        reader.resetTo(reader("{'a':true}"));
        reader.beginObject();
        try {
            reader.nextName().toString();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testLenientSingleQuotedNames() throws IOException {
        reader.resetTo(reader("{'a':true}"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
    }

    public void testStrictSingleQuotedNamesWithSkipValue() throws IOException {
        reader.resetTo(reader("{'a':true}"));
        reader.beginObject();
        try {
            reader.skipValue();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testStrictUnquotedStrings() throws IOException {
        reader.resetTo(reader("[a]"));
        reader.beginArray();
        try {
            reader.nextString().toString();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testStrictUnquotedStringsWithSkipValue() throws IOException {
        reader.resetTo(reader("[a]"));
        reader.beginArray();
        try {
            reader.skipValue();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testLenientUnquotedStrings() throws IOException {
        reader.resetTo(reader("[a]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals("a", reader.nextString().toString());
    }

    public void testStrictSingleQuotedStrings() throws IOException {
        reader.resetTo(reader("['a']"));
        reader.beginArray();
        try {
            reader.nextString().toString();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testLenientSingleQuotedStrings() throws IOException {
        reader.resetTo(reader("['a']"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals("a", reader.nextString().toString());
    }

    public void testStrictSingleQuotedStringsWithSkipValue() throws IOException {
        reader.resetTo(reader("['a']"));
        reader.beginArray();
        try {
            reader.skipValue();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testStrictSemicolonDelimitedArray() throws IOException {
        reader.resetTo(reader("[true;true]"));
        reader.beginArray();
        try {
            reader.nextBoolean();
            reader.nextBoolean();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testLenientSemicolonDelimitedArray() throws IOException {
        reader.resetTo(reader("[true;true]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(true, reader.nextBoolean());
        assertEquals(true, reader.nextBoolean());
    }

    public void testStrictSemicolonDelimitedArrayWithSkipValue() throws IOException {
        reader.resetTo(reader("[true;true]"));
        reader.beginArray();
        try {
            reader.skipValue();
            reader.skipValue();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testStrictSemicolonDelimitedNameValuePair() throws IOException {
        reader.resetTo(reader("{\"a\":true;\"b\":true}"));
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
        try {
            reader.nextBoolean();
            reader.nextName().toString();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testLenientSemicolonDelimitedNameValuePair() throws IOException {
        reader.resetTo(reader("{\"a\":true;\"b\":true}"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
        assertEquals(true, reader.nextBoolean());
        assertEquals("b", reader.nextName().toString());
    }

    public void testStrictSemicolonDelimitedNameValuePairWithSkipValue() throws IOException {
        reader.resetTo(reader("{\"a\":true;\"b\":true}"));
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
        try {
            reader.skipValue();
            reader.skipValue();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testStrictUnnecessaryArraySeparators() throws IOException {
        reader.resetTo(reader("[true,,true]"));
        reader.beginArray();
        assertEquals(true, reader.nextBoolean());
        try {
            reader.nextNull();
            fail();
        } catch (IOException expected) {
        }

        reader = new TinyJsonReader(reader("[,true]"));
        reader.beginArray();
        try {
            reader.nextNull();
            fail();
        } catch (IOException expected) {
        }

        reader = new TinyJsonReader(reader("[true,]"));
        reader.beginArray();
        assertEquals(true, reader.nextBoolean());
        try {
            reader.nextNull();
            fail();
        } catch (IOException expected) {
        }

        reader = new TinyJsonReader(reader("[,]"));
        reader.beginArray();
        try {
            reader.nextNull();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testLenientUnnecessaryArraySeparators() throws IOException {
        reader.resetTo(reader("[true,,true]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(true, reader.nextBoolean());
        reader.nextNull();
        assertEquals(true, reader.nextBoolean());
        reader.endArray();

        reader = new TinyJsonReader(reader("[,true]"));
        reader.setLenient(true);
        reader.beginArray();
        reader.nextNull();
        assertEquals(true, reader.nextBoolean());
        reader.endArray();

        reader = new TinyJsonReader(reader("[true,]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(true, reader.nextBoolean());
        reader.nextNull();
        reader.endArray();

        reader = new TinyJsonReader(reader("[,]"));
        reader.setLenient(true);
        reader.beginArray();
        reader.nextNull();
        reader.nextNull();
        reader.endArray();
    }

    public void testStrictUnnecessaryArraySeparatorsWithSkipValue() throws IOException {
        reader.resetTo(reader("[true,,true]"));
        reader.beginArray();
        assertEquals(true, reader.nextBoolean());
        try {
            reader.skipValue();
            fail();
        } catch (IOException expected) {
        }

        reader = new TinyJsonReader(reader("[,true]"));
        reader.beginArray();
        try {
            reader.skipValue();
            fail();
        } catch (IOException expected) {
        }

        reader = new TinyJsonReader(reader("[true,]"));
        reader.beginArray();
        assertEquals(true, reader.nextBoolean());
        try {
            reader.skipValue();
            fail();
        } catch (IOException expected) {
        }

        reader = new TinyJsonReader(reader("[,]"));
        reader.beginArray();
        try {
            reader.skipValue();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testStrictMultipleTopLevelValues() throws IOException {
        reader.resetTo(reader("[] []"));
        reader.beginArray();
        reader.endArray();
        try {
            reader.peek();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testLenientMultipleTopLevelValues() throws IOException {
        reader.resetTo(reader("[] true {}"));
        reader.setLenient(true);
        reader.beginArray();
        reader.endArray();
        assertEquals(true, reader.nextBoolean());
        reader.beginObject();
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testStrictMultipleTopLevelValuesWithSkipValue() throws IOException {
        reader.resetTo(reader("[] []"));
        reader.beginArray();
        reader.endArray();
        try {
            reader.skipValue();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testTopLevelValueTypes() throws IOException {
        TinyJsonReader reader1 = new TinyJsonReader(reader("true"));
        assertTrue(reader1.nextBoolean());
        assertEquals(JsonToken.END_DOCUMENT, reader1.peek());

        TinyJsonReader reader2 = new TinyJsonReader(reader("false"));
        assertFalse(reader2.nextBoolean());
        assertEquals(JsonToken.END_DOCUMENT, reader2.peek());

        TinyJsonReader reader3 = new TinyJsonReader(reader("null"));
        assertEquals(JsonToken.NULL, reader3.peek());
        reader3.nextNull();
        assertEquals(JsonToken.END_DOCUMENT, reader3.peek());

        TinyJsonReader reader4 = new TinyJsonReader(reader("123"));
        assertEquals(123.0, reader4.nextDouble());
        assertEquals(JsonToken.END_DOCUMENT, reader4.peek());

        TinyJsonReader reader5 = new TinyJsonReader(reader("123.4"));
        assertEquals(123.4, reader5.nextDouble());
        assertEquals(JsonToken.END_DOCUMENT, reader5.peek());

        TinyJsonReader reader6 = new TinyJsonReader(reader("\"a\""));
        assertEquals("a", reader6.nextString().toString());
        assertEquals(JsonToken.END_DOCUMENT, reader6.peek());
    }

    public void testTopLevelValueTypeWithSkipValue() throws IOException {
        reader.resetTo(reader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testStrictNonExecutePrefix() throws IOException {
        reader.resetTo(reader(")]}'\n []"));
        try {
            reader.beginArray();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testStrictNonExecutePrefixWithSkipValue() throws IOException {
        reader.resetTo(reader(")]}'\n []"));
        try {
            reader.skipValue();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testLenientNonExecutePrefix() throws IOException {
        reader.resetTo(reader(")]}'\n []"));
        reader.setLenient(true);
        reader.beginArray();
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testLenientNonExecutePrefixWithLeadingWhitespace() throws IOException {
        reader.resetTo(reader("\r\n \t)]}'\n []"));
        reader.setLenient(true);
        reader.beginArray();
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testLenientPartialNonExecutePrefix() throws IOException {
        reader.resetTo(reader(")]}' []"));
        reader.setLenient(true);
        try {
            assertEquals(")", reader.nextString().toString());
            reader.nextString().toString();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testBomIgnoredAsFirstCharacterOfDocument() throws IOException {
        reader.resetTo(reader("\ufeff[]"));
        reader.beginArray();
        reader.endArray();
    }

    public void testBomForbiddenAsOtherCharacterInDocument() throws IOException {
        reader.resetTo(reader("[\ufeff]"));
        reader.beginArray();
        try {
            reader.endArray();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testFailWithPosition() throws IOException {
        testFailWithPosition("Expected value at line 6 column 5 path $[1]",
                "[\n\n\n\n\n\"a\",}]");
    }

    public void testFailWithPositionGreaterThanBufferSize() throws IOException {
        String spaces = repeat(' ', 8192);
        testFailWithPosition("Expected value at line 6 column 5 path $[1]",
                "[\n\n" + spaces + "\n\n\n\"a\",}]");
    }

    public void testFailWithPositionOverSlashSlashEndOfLineComment() throws IOException {
        testFailWithPosition("Expected value at line 5 column 6 path $[1]",
                "\n// foo\n\n//bar\r\n[\"a\",}");
    }

    public void testFailWithPositionOverHashEndOfLineComment() throws IOException {
        testFailWithPosition("Expected value at line 5 column 6 path $[1]",
                "\n# foo\n\n#bar\r\n[\"a\",}");
    }

    public void testFailWithPositionOverCStyleComment() throws IOException {
        testFailWithPosition("Expected value at line 6 column 12 path $[1]",
                "\n\n/* foo\n*\n*\r\nbar */[\"a\",}");
    }

    public void testFailWithPositionOverQuotedString() throws IOException {
        testFailWithPosition("Expected value at line 5 column 3 path $[1]",
                "[\"foo\nbar\r\nbaz\n\",\n  }");
    }

    public void testFailWithPositionOverUnquotedString() throws IOException {
        testFailWithPosition("Expected value at line 5 column 2 path $[1]", "[\n\nabcd\n\n,}");
    }

    public void testFailWithEscapedNewlineCharacter() throws IOException {
        testFailWithPosition("Expected value at line 5 column 3 path $[1]", "[\n\n\"\\\n\n\",}");
    }

    public void testFailWithPositionIsOffsetByBom() throws IOException {
        testFailWithPosition("Expected value at line 1 column 6 path $[1]",
                "\ufeff[\"a\",}]");
    }

    private void testFailWithPosition(String message, String json) throws IOException {
        // Validate that it works reading the string normally.
        TinyJsonReader reader1 = new TinyJsonReader(reader(json));
        reader1.setLenient(true);
        reader1.beginArray();
        reader1.nextString().toString();
        try {
            reader1.peek();
            fail();
        } catch (IOException expected) {
            assertEquals(message, expected.getMessage());
        }

        // Also validate that it works when skipping.
        TinyJsonReader reader2 = new TinyJsonReader(reader(json));
        reader2.setLenient(true);
        reader2.beginArray();
        reader2.skipValue();
        try {
            reader2.peek();
            fail();
        } catch (IOException expected) {
            assertEquals(message, expected.getMessage());
        }
    }

    public void testFailWithPositionDeepPath() throws IOException {
        reader.resetTo(reader("[1,{\"a\":[2,3,}"));
        reader.beginArray();
        reader.nextDouble();
        reader.beginObject();
        reader.nextName().toString();
        reader.beginArray();
        reader.nextDouble();
        reader.nextDouble();
        try {
            reader.peek();
            fail();
        } catch (IOException expected) {
            assertEquals("Expected value at line 1 column 14 path $[1].a[2]", expected.getMessage());
        }
    }

    public void testStrictVeryLongNumber() throws IOException {
        reader.resetTo(reader("[0." + repeat('9', 8192) + "]"));
        reader.beginArray();
        try {
            assertEquals(1d, reader.nextDouble());
            fail();
        } catch (IOException expected) {
        }
    }

    public void testLenientVeryLongNumber() throws IOException {
        reader.resetTo(reader("[0." + repeat('9', 8192) + "]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(STRING, reader.peek());
        assertEquals(1d, reader.nextDouble());
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testVeryLongUnquotedLiteral() throws IOException {
        String literal = "a" + repeat('b', 8192) + "c";
        reader.resetTo(reader("[" + literal + "]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(literal, reader.nextString().toString());
        reader.endArray();
    }

    public void testDeeplyNestedArrays() throws IOException {
        // this is nested 40 levels deep; Gson is tuned for nesting is 30 levels deep or fewer
        reader.resetTo(reader(
                "[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]"));
        for (int i = 0; i < 40; i++) {
            reader.beginArray();
        }
        assertEquals("$[0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0]"
                + "[0][0][0][0][0][0][0][0][0][0][0][0][0][0]", reader.getPath().toString());
        for (int i = 0; i < 40; i++) {
            reader.endArray();
        }
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testDeeplyNestedObjects() throws IOException {
        // Build a JSON document structured like {"a":{"a":{"a":{"a":true}}}}, but 40 levels deep
        String array = "{\"a\":%s}";
        String json = "true";
        for (int i = 0; i < 40; i++) {
            json = String.format(array, json);
        }

        reader.resetTo(reader(json));
        for (int i = 0; i < 40; i++) {
            reader.beginObject();
            assertEquals("a", reader.nextName().toString());
        }
        assertEquals("$.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a"
                + ".a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a", reader.getPath().toString());
        assertEquals(true, reader.nextBoolean());
        for (int i = 0; i < 40; i++) {
            reader.endObject();
        }
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    // http://code.google.com/p/google-gson/issues/detail?id=409
    public void testStringEndingInSlash() throws IOException {
        reader.resetTo(reader("/"));
        reader.setLenient(true);
        try {
            reader.peek();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testDocumentWithCommentEndingInSlash() throws IOException {
        reader.resetTo(reader("/* foo *//"));
        reader.setLenient(true);
        try {
            reader.peek();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testStringWithLeadingSlash() throws IOException {
        reader.resetTo(reader("/x"));
        reader.setLenient(true);
        try {
            reader.peek();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testUnterminatedObject() throws IOException {
        reader.resetTo(reader("{\"a\":\"android\"x"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
        assertEquals("android", reader.nextString().toString());
        try {
            reader.peek();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testVeryLongQuotedString() throws IOException {
        char[] stringChars = new char[1024 * 16];
        Arrays.fill(stringChars, 'x');
        String string = new String(stringChars);
        String json = "[\"" + string + "\"]";
        reader.resetTo(reader(json));
        reader.beginArray();
        assertEquals(string, reader.nextString().toString());
        reader.endArray();
    }

    public void testVeryLongUnquotedString() throws IOException {
        char[] stringChars = new char[1024 * 16];
        Arrays.fill(stringChars, 'x');
        String string = new String(stringChars);
        String json = "[" + string + "]";
        reader.resetTo(reader(json));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(string, reader.nextString().toString());
        reader.endArray();
    }

    public void testVeryLongUnterminatedString() throws IOException {
        char[] stringChars = new char[1024 * 16];
        Arrays.fill(stringChars, 'x');
        String string = new String(stringChars);
        String json = "[" + string;
        reader.resetTo(reader(json));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(string, reader.nextString().toString());
        try {
            reader.peek();
            fail();
        } catch (EOFException expected) {
        }
    }

    public void testSkipVeryLongUnquotedString() throws IOException {
        reader.resetTo(reader("[" + repeat('x', 8192) + "]"));
        reader.setLenient(true);
        reader.beginArray();
        reader.skipValue();
        reader.endArray();
    }

    public void testSkipTopLevelUnquotedString() throws IOException {
        reader.resetTo(reader(repeat('x', 8192)));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testSkipVeryLongQuotedString() throws IOException {
        reader.resetTo(reader("[\"" + repeat('x', 8192) + "\"]"));
        reader.beginArray();
        reader.skipValue();
        reader.endArray();
    }

    public void testSkipTopLevelQuotedString() throws IOException {
        reader.resetTo(reader("\"" + repeat('x', 8192) + "\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testStringAsNumberWithTruncatedExponent() throws IOException {
        reader.resetTo(reader("[123e]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(STRING, reader.peek());
    }

    public void testStringAsNumberWithDigitAndNonDigitExponent() throws IOException {
        reader.resetTo(reader("[123e4b]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(STRING, reader.peek());
    }

    public void testStringAsNumberWithNonDigitExponent() throws IOException {
        reader.resetTo(reader("[123eb]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(STRING, reader.peek());
    }

    public void testEmptyStringName() throws IOException {
        reader.resetTo(reader("{\"\":true}"));
        reader.setLenient(true);
        assertEquals(BEGIN_OBJECT, reader.peek());
        reader.beginObject();
        assertEquals(NAME, reader.peek());
        assertEquals("", reader.nextName().toString());
        assertEquals(JsonToken.BOOLEAN, reader.peek());
        assertEquals(true, reader.nextBoolean());
        assertEquals(JsonToken.END_OBJECT, reader.peek());
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    public void testStrictExtraCommasInMaps() throws IOException {
        reader.resetTo(reader("{\"a\":\"b\",}"));
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
        assertEquals("b", reader.nextString().toString());
        try {
            reader.peek();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testLenientExtraCommasInMaps() throws IOException {
        reader.resetTo(reader("{\"a\":\"b\",}"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("a", reader.nextName().toString());
        assertEquals("b", reader.nextString().toString());
        try {
            reader.peek();
            fail();
        } catch (IOException expected) {
        }
    }

    private String repeat(char c, int count) {
        char[] array = new char[count];
        Arrays.fill(array, c);
        return new String(array);
    }

    public void testMalformedDocuments() throws IOException {
        assertDocument("{]", BEGIN_OBJECT, IOException.class);
        assertDocument("{,", BEGIN_OBJECT, IOException.class);
        assertDocument("{{", BEGIN_OBJECT, IOException.class);
        assertDocument("{[", BEGIN_OBJECT, IOException.class);
        assertDocument("{:", BEGIN_OBJECT, IOException.class);
        assertDocument("{\"name\",", BEGIN_OBJECT, NAME, IOException.class);
        assertDocument("{\"name\",", BEGIN_OBJECT, NAME, IOException.class);
        assertDocument("{\"name\":}", BEGIN_OBJECT, NAME, IOException.class);
        assertDocument("{\"name\"::", BEGIN_OBJECT, NAME, IOException.class);
        assertDocument("{\"name\":,", BEGIN_OBJECT, NAME, IOException.class);
        assertDocument("{\"name\"=}", BEGIN_OBJECT, NAME, IOException.class);
        assertDocument("{\"name\"=>}", BEGIN_OBJECT, NAME, IOException.class);
        assertDocument("{\"name\"=>\"string\":", BEGIN_OBJECT, NAME, STRING, IOException.class);
        assertDocument("{\"name\"=>\"string\"=", BEGIN_OBJECT, NAME, STRING, IOException.class);
        assertDocument("{\"name\"=>\"string\"=>", BEGIN_OBJECT, NAME, STRING, IOException.class);
        assertDocument("{\"name\"=>\"string\",", BEGIN_OBJECT, NAME, STRING, IOException.class);
        assertDocument("{\"name\"=>\"string\",\"name\"", BEGIN_OBJECT, NAME, STRING, NAME);
        assertDocument("[}", BEGIN_ARRAY, IOException.class);
        assertDocument("[,]", BEGIN_ARRAY, NULL, NULL, END_ARRAY);
        assertDocument("{", BEGIN_OBJECT, IOException.class);
        assertDocument("{\"name\"", BEGIN_OBJECT, NAME, IOException.class);
        assertDocument("{\"name\",", BEGIN_OBJECT, NAME, IOException.class);
        assertDocument("{'name'", BEGIN_OBJECT, NAME, IOException.class);
        assertDocument("{'name',", BEGIN_OBJECT, NAME, IOException.class);
        assertDocument("{name", BEGIN_OBJECT, NAME, IOException.class);
        assertDocument("[", BEGIN_ARRAY, IOException.class);
        assertDocument("[string", BEGIN_ARRAY, STRING, IOException.class);
        assertDocument("[\"string\"", BEGIN_ARRAY, STRING, IOException.class);
        assertDocument("['string'", BEGIN_ARRAY, STRING, IOException.class);
        assertDocument("[123", BEGIN_ARRAY, NUMBER, IOException.class);
        assertDocument("[123,", BEGIN_ARRAY, NUMBER, IOException.class);
        assertDocument("{\"name\":123", BEGIN_OBJECT, NAME, NUMBER, IOException.class);
        assertDocument("{\"name\":123,", BEGIN_OBJECT, NAME, NUMBER, IOException.class);
        assertDocument("{\"name\":\"string\"", BEGIN_OBJECT, NAME, STRING, IOException.class);
        assertDocument("{\"name\":\"string\",", BEGIN_OBJECT, NAME, STRING, IOException.class);
        assertDocument("{\"name\":'string'", BEGIN_OBJECT, NAME, STRING, IOException.class);
        assertDocument("{\"name\":'string',", BEGIN_OBJECT, NAME, STRING, IOException.class);
        assertDocument("{\"name\":false", BEGIN_OBJECT, NAME, BOOLEAN, IOException.class);
        assertDocument("{\"name\":false,,", BEGIN_OBJECT, NAME, BOOLEAN, IOException.class);
    }

    public void testCloseNullReader() throws IOException {
        reader.close();
    }

    /**
     * This test behave slightly differently in Gson 2.2 and earlier. It fails
     * during peek rather than during nextString().
     */
    public void testUnterminatedStringFailure() throws IOException {
        reader.resetTo(reader("[\"string"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(STRING, reader.peek());
        try {
            reader.nextString().toString();
            fail();
        } catch (IOException expected) {
        }
    }

    private void assertDocument(String document, Object... expectations) throws IOException {
        reader.resetTo(reader(document));
        reader.setLenient(true);
        for (Object expectation : expectations) {
            if (expectation == BEGIN_OBJECT) {
                reader.beginObject();
            } else if (expectation == BEGIN_ARRAY) {
                reader.beginArray();
            } else if (expectation == END_OBJECT) {
                reader.endObject();
            } else if (expectation == END_ARRAY) {
                reader.endArray();
            } else if (expectation == NAME) {
                assertEquals("name", reader.nextName().toString());
            } else if (expectation == BOOLEAN) {
                assertEquals(false, reader.nextBoolean());
            } else if (expectation == STRING) {
                assertEquals("string", reader.nextString().toString());
            } else if (expectation == NUMBER) {
                assertEquals(123.0, reader.nextDouble());
            } else if (expectation == NULL) {
                reader.nextNull();
            } else if (expectation == IOException.class) {
                try {
                    reader.peek();
                    fail();
                } catch (IOException expected) {
                }
            } else {
                throw new AssertionError();
            }
        }
    }

    /**
     * Returns a reader that returns one character at a time.
     */
    private Reader reader(final String s) {
        /* if (true) */
        return new StringReader(s);
    /* return new Reader() {
      int position = 0;
      @Override public int read(char[] buffer, int offset, int count) throws IOException {
        if (position == s.length()) {
          return -1;
        } else if (count > 0) {
          buffer[offset] = s.charAt(position++);
          return 1;
        } else {
          throw new IllegalArgumentException();
        }
      }
      @Override public void close() throws IOException {
      }
    }; */
    }
}