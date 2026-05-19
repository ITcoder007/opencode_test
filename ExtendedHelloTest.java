import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public class ExtendedHelloTest {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== ExtendedHelloTest - Extended Unit Tests for Hello.java ===\n");

        System.out.println("--- Encoding & Byte-Level Tests ---");
        testOutputIsPureASCII();
        testOutputUTF8Compatible();
        testOutputByteLength();
        testNoBOMInOutput();
        testOutputContainsExactString();

        System.out.println("\n--- Character-Level Precision Tests ---");
        testOutputCharByChar();
        testOutputContainsComma();
        testOutputContainsExclamation();
        testStringLength();
        testSubstringWorld();

        System.out.println("\n--- Multi-Invocation Stability Tests ---");
        testTenInvocations();
        testHundredInvocationsAllIdentical();
        testOutputConsistencyAfterGC();

        System.out.println("\n--- Thread Safety Basic Tests ---");
        testConcurrentMainCalls();

        System.out.println("\n--- Reflection & Invocation Tests ---");
        testReflectiveMainInvocation();
        testMainWithEmptyStringArray();
        testMainWithLargeArgsArray();

        System.out.println("\n--- Python Equivalence Tests ---");
        testOutputEqualsPythonPrint();
        testNewlineCount();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL TESTS PASSED");
        } else {
            System.out.println("Result: SOME TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testOutputIsPureASCII() {
        assertTest("Output contains only ASCII characters", () -> {
            String output = captureMainOutput();
            for (char c : output.toCharArray()) {
                assertTrue(c < 128);
            }
        });
    }

    private static void testOutputUTF8Compatible() {
        assertTest("Output is valid UTF-8", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            String decoded = new String(bytes, StandardCharsets.UTF_8);
            assertEquals(output, decoded);
        });
    }

    private static void testOutputByteLength() {
        assertTest("Output byte length is correct (13 chars + newline)", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            int expectedLen = "Hello, World!".length() + System.lineSeparator().length();
            assertEquals(expectedLen, bytes.length);
        });
    }

    private static void testNoBOMInOutput() {
        assertTest("Output has no UTF-8 BOM", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            assertFalse(bytes.length >= 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF);
        });
    }

    private static void testOutputContainsExactString() {
        assertTest("Output contains 'Hello, World!' as substring", () -> {
            String output = captureMainOutput();
            assertTrue(output.contains("Hello, World!"));
        });
    }

    private static void testOutputCharByChar() {
        assertTest("Output matches 'Hello, World!' char by char", () -> {
            String output = captureMainOutput().trim();
            String expected = "Hello, World!";
            assertEquals(expected.length(), output.length());
            for (int i = 0; i < expected.length(); i++) {
                assertEquals(expected.charAt(i), output.charAt(i));
            }
        });
    }

    private static void testOutputContainsComma() {
        assertTest("Output contains comma at position 5", () -> {
            String output = captureMainOutput().trim();
            assertEquals(',', output.charAt(5));
        });
    }

    private static void testOutputContainsExclamation() {
        assertTest("Output contains exclamation mark at last position", () -> {
            String output = captureMainOutput().trim();
            assertEquals('!', output.charAt(output.length() - 1));
        });
    }

    private static void testStringLength() {
        assertTest("Trimmed output length is exactly 13", () -> {
            String output = captureMainOutput().trim();
            assertEquals(13, output.length());
        });
    }

    private static void testSubstringWorld() {
        assertTest("Output contains 'World' substring at correct position", () -> {
            String output = captureMainOutput().trim();
            assertEquals("World", output.substring(7, 12));
        });
    }

    private static void testTenInvocations() {
        assertTest("10 consecutive invocations produce identical output", () -> {
            String first = captureMainOutput();
            for (int i = 0; i < 9; i++) {
                assertEquals(first, captureMainOutput());
            }
        });
    }

    private static void testHundredInvocationsAllIdentical() {
        assertTest("100 consecutive invocations all produce identical output", () -> {
            String first = captureMainOutput();
            for (int i = 1; i < 100; i++) {
                assertEquals(first, captureMainOutput());
            }
        });
    }

    private static void testOutputConsistencyAfterGC() {
        assertTest("Output consistent after garbage collection", () -> {
            String before = captureMainOutput();
            System.gc();
            Thread.sleep(50);
            String after = captureMainOutput();
            assertEquals(before, after);
        });
    }

    private static void testConcurrentMainCalls() {
        assertTest("Concurrent main() calls complete without exception", () -> {
            int threadCount = 10;
            Throwable[] errors = new Throwable[threadCount];
            Thread[] threads = new Thread[threadCount];
            PrintStream original = System.out;
            PrintStream devNull = new PrintStream(new ByteArrayOutputStream());
            System.setOut(devNull);
            try {
                for (int i = 0; i < threadCount; i++) {
                    final int idx = i;
                    threads[i] = new Thread(() -> {
                        try {
                            Hello.main(new String[]{});
                        } catch (Throwable t) {
                            errors[idx] = t;
                        }
                    });
                }
                for (Thread t : threads) t.start();
                for (Thread t : threads) t.join(5000);
            } finally {
                System.setOut(original);
            }
            for (int i = 0; i < threadCount; i++) {
                if (errors[i] != null) {
                    throw new AssertionError("Thread " + i + " threw: " + errors[i].getMessage());
                }
            }
        });
    }

    private static void testReflectiveMainInvocation() {
        assertTest("Reflective invocation of main produces correct output", () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream original = System.out;
            System.setOut(new PrintStream(baos));
            try {
                Method main = Hello.class.getMethod("main", String[].class);
                main.invoke(null, (Object) new String[]{});
            } finally {
                System.setOut(original);
            }
            assertEquals("Hello, World!", baos.toString().trim());
        });
    }

    private static void testMainWithEmptyStringArray() {
        assertTest("main() with empty String array produces correct output", () -> {
            String output = captureMainOutputWithArgs(new String[]{""});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMainWithLargeArgsArray() {
        assertTest("main() with 1000-element args array produces correct output", () -> {
            String[] bigArgs = new String[1000];
            for (int i = 0; i < 1000; i++) bigArgs[i] = "arg" + i;
            String output = captureMainOutputWithArgs(bigArgs);
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testOutputEqualsPythonPrint() {
        assertTest("Java output equals Python print('Hello, World!') output", () -> {
            String javaOutput = captureMainOutput().trim();
            assertEquals("Hello, World!", javaOutput);
        });
    }

    private static void testNewlineCount() {
        assertTest("Output contains exactly one newline (at end)", () -> {
            String output = captureMainOutput();
            String nl = System.lineSeparator();
            int count = 0;
            int idx = 0;
            while ((idx = output.indexOf(nl, idx)) != -1) {
                count++;
                idx += nl.length();
            }
            assertEquals(1, count);
        });
    }

    private static String captureMainOutput() {
        return captureMainOutputWithArgs(new String[]{});
    }

    private static String captureMainOutputWithArgs(String[] args) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(baos));
        try {
            Hello.main(args);
        } finally {
            System.setOut(original);
        }
        return baos.toString();
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static void assertTest(String name, ThrowingRunnable test) {
        total++;
        try {
            test.run();
            passed++;
            System.out.println("  PASS: " + name);
        } catch (AssertionError e) {
            failed++;
            System.out.println("  FAIL: " + name + " - " + e.getMessage());
        } catch (Exception e) {
            failed++;
            System.out.println("  FAIL: " + name + " - Exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected <" + expected + "> but got <" + actual + ">");
        }
    }

    private static void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError("Expected <" + expected + "> but got <" + actual + ">");
        }
    }

    private static void assertEquals(char expected, char actual) {
        if (expected != actual) {
            throw new AssertionError("Expected <" + expected + "> but got <" + actual + ">");
        }
    }

    private static void assertNotNull(Object obj) {
        if (obj == null) {
            throw new AssertionError("Expected non-null value");
        }
    }

    private static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected true but got false");
        }
    }

    private static void assertFalse(boolean condition) {
        if (condition) {
            throw new AssertionError("Expected false but got true");
        }
    }
}
