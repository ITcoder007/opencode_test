import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class HelloExtendedTest {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloExtendedTest - Extended Unit Tests for Hello.java ===\n");

        System.out.println("--- Character & Encoding Tests ---");
        testOutputIsASCII();
        testOutputContainsNoNullBytes();
        testOutputStringHasExpectedLength();

        System.out.println("\n--- Reflection & Instantiation Tests ---");
        testClassCanBeInstantiated();
        testClassHasExactlyOneMethod();
        testClassHasDefaultConstructor();
        testClassModifierIsNotFinal();
        testClassModifierIsNotAbstract();

        System.out.println("\n--- Concurrency & Repeated Execution Tests ---");
        testConcurrentExecutionProducesCorrectOutput();
        testRapidRepeatedExecution();
        testOutputConsistencyAcross10Runs();

        System.out.println("\n--- System.out Restoration Tests ---");
        testSystemOutRestoredAfterCall();
        testSystemOutRestoredAfterException();

        System.out.println("\n--- Edge Case Tests ---");
        testMainWithEmptyStringArray();
        testMainWithLargeArgsArray();
        testOutputDoesNotContainExtraWhitespace();
        testOutputIsDeterministic();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL TESTS PASSED");
        } else {
            System.out.println("Result: SOME TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testOutputIsASCII() {
        assertTest("Output contains only ASCII characters", () -> {
            String output = captureMainOutput();
            for (char c : output.toCharArray()) {
                assertTrue(c < 128);
            }
        });
    }

    private static void testOutputContainsNoNullBytes() {
        assertTest("Output contains no null bytes", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("\0"));
        });
    }

    private static void testOutputStringHasExpectedLength() {
        assertTest("Output string length matches expected", () -> {
            String output = captureMainOutput();
            String expected = "Hello, World!" + System.lineSeparator();
            assertEquals(expected.length(), output.length());
        });
    }

    private static void testClassCanBeInstantiated() {
        assertTest("Hello class can be instantiated via reflection", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?> ctor = clazz.getDeclaredConstructor();
            Object instance = ctor.newInstance();
            assertNotNull(instance);
            assertEquals("Hello", instance.getClass().getSimpleName());
        });
    }

    private static void testClassHasExactlyOneMethod() {
        assertTest("Hello class has exactly one declared method (main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            assertEquals(1, methods.length);
            assertEquals("main", methods[0].getName());
        });
    }

    private static void testClassHasDefaultConstructor() {
        assertTest("Hello class has a default (no-arg) constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?>[] ctors = clazz.getDeclaredConstructors();
            assertTrue(ctors.length >= 1);
            boolean hasDefault = false;
            for (Constructor<?> c : ctors) {
                if (c.getParameterCount() == 0) {
                    hasDefault = true;
                    break;
                }
            }
            assertTrue(hasDefault);
        });
    }

    private static void testClassModifierIsNotFinal() {
        assertTest("Hello class is not final", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isFinal(clazz.getModifiers()));
        });
    }

    private static void testClassModifierIsNotAbstract() {
        assertTest("Hello class is not abstract", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isAbstract(clazz.getModifiers()));
        });
    }

    private static void testConcurrentExecutionProducesCorrectOutput() {
        assertTest("Concurrent execution of main() produces correct output", () -> {
            Thread[] threads = new Thread[5];
            String[] results = new String[5];
            ByteArrayOutputStream[] buffers = new ByteArrayOutputStream[5];

            for (int i = 0; i < 5; i++) {
                final int idx = i;
                buffers[idx] = new ByteArrayOutputStream();
                threads[i] = new Thread(() -> {
                    PrintStream originalOut = System.out;
                    System.setOut(new PrintStream(buffers[idx]));
                    try {
                        Hello.main(new String[]{});
                    } finally {
                        System.setOut(originalOut);
                    }
                    results[idx] = buffers[idx].toString();
                });
            }

            for (Thread t : threads) t.start();
            for (Thread t : threads) t.join(5000);

            String expected = "Hello, World!" + System.lineSeparator();
            for (int i = 0; i < 5; i++) {
                assertNotNull(results[i]);
                assertEquals(expected, results[i]);
            }
        });
    }

    private static void testRapidRepeatedExecution() {
        assertTest("Rapid 100x repeated execution produces consistent output", () -> {
            String expected = "Hello, World!" + System.lineSeparator();
            for (int i = 0; i < 100; i++) {
                String output = captureMainOutput();
                assertEquals(expected, output);
            }
        });
    }

    private static void testOutputConsistencyAcross10Runs() {
        assertTest("Output hash is consistent across 10 runs", () -> {
            String first = captureMainOutput();
            for (int i = 0; i < 9; i++) {
                assertEquals(first, captureMainOutput());
            }
        });
    }

    private static void testSystemOutRestoredAfterCall() {
        assertTest("System.out is properly restored after captureMainOutput", () -> {
            PrintStream original = System.out;
            captureMainOutput();
            assertTrue(original == System.out);
        });
    }

    private static void testSystemOutRestoredAfterException() {
        assertTest("System.out is restored even when exception occurs", () -> {
            PrintStream original = System.out;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));
            try {
                Hello.main(null);
            } finally {
                System.setOut(original);
            }
            assertTrue(original == System.out);
        });
    }

    private static void testMainWithEmptyStringArray() {
        assertTest("main() with empty String array produces correct output", () -> {
            String output = captureMainOutputWithArgs(new String[]{});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMainWithLargeArgsArray() {
        assertTest("main() with 1000-element args array still works", () -> {
            String[] largeArgs = new String[1000];
            for (int i = 0; i < 1000; i++) {
                largeArgs[i] = "arg" + i;
            }
            String output = captureMainOutputWithArgs(largeArgs);
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testOutputDoesNotContainExtraWhitespace() {
        assertTest("Output contains no tabs or double spaces", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("\t"));
            assertFalse(output.contains("  "));
        });
    }

    private static void testOutputIsDeterministic() {
        assertTest("Output is byte-for-byte deterministic", () -> {
            byte[] first = captureMainOutputBytes();
            for (int i = 0; i < 10; i++) {
                byte[] current = captureMainOutputBytes();
                assertEquals(first.length, current.length);
                for (int j = 0; j < first.length; j++) {
                    assertEquals(first[j], current[j]);
                }
            }
        });
    }

    private static String captureMainOutput() {
        return captureMainOutputWithArgs(new String[]{});
    }

    private static String captureMainOutputWithArgs(String[] args) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(baos));
        try {
            Hello.main(args);
        } finally {
            System.setOut(originalOut);
        }
        return baos.toString();
    }

    private static byte[] captureMainOutputBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(baos));
        try {
            Hello.main(new String[]{});
        } finally {
            System.setOut(originalOut);
        }
        return baos.toByteArray();
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

    private static void assertEquals(byte expected, byte actual) {
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
