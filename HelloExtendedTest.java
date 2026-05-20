import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

public class HelloExtendedTest {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloExtendedTest - Supplementary Unit Tests ===\n");

        System.out.println("--- Class Integrity Tests ---");
        testClassHasExactlyOneDeclaredMethod();
        testClassHasNoDeclaredFields();
        testClassExtendsObject();
        testClassIsNotAbstract();
        testClassIsNotFinal();
        testClassIsNotInterface();
        testClassHasNoAnnotations();
        testClassHasDefaultConstructor();

        System.out.println("\n--- Byte-Level Output Tests ---");
        testExactByteLength();
        testOutputIsPureASCII();
        testOutputHasNoBOM();
        testOutputBytesMatchExpected();

        System.out.println("\n--- Robustness Supplementary Tests ---");
        testSystemOutRestoredAfterMain();
        testMainWithEmptyStringArray();
        testMainWithLargeArgsArray();
        testMainWithSpecialCharArgs();
        testConcurrentMainCalls();
        testMainDoesNotMutateArgs();
        testMainDoesNotReadFromStdin();

        System.out.println("\n=== Extended Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL EXTENDED TESTS PASSED");
        } else {
            System.out.println("Result: SOME EXTENDED TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testClassHasExactlyOneDeclaredMethod() {
        assertTest("Class has exactly 1 declared method (main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            assertEquals(1, methods.length);
        }, ClassNotFoundException.class);
    }

    private static void testClassHasNoDeclaredFields() {
        assertTest("Class has no declared fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            Field[] fields = clazz.getDeclaredFields();
            assertEquals(0, fields.length);
        }, ClassNotFoundException.class);
    }

    private static void testClassExtendsObject() {
        assertTest("Class directly extends Object", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(Object.class, clazz.getSuperclass());
        }, ClassNotFoundException.class);
    }

    private static void testClassIsNotAbstract() {
        assertTest("Class is not abstract", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isAbstract(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassIsNotFinal() {
        assertTest("Class is not final (no unnecessary restriction)", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isFinal(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassIsNotInterface() {
        assertTest("Class is not an interface", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(clazz.isInterface());
        }, ClassNotFoundException.class);
    }

    private static void testClassHasNoAnnotations() {
        assertTest("Class has no runtime annotations", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getAnnotations().length);
        }, ClassNotFoundException.class);
    }

    private static void testClassHasDefaultConstructor() {
        assertTest("Class has a public no-arg constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertNotNull(clazz.getConstructor());
            assertTrue(Modifier.isPublic(clazz.getConstructor().getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testExactByteLength() {
        assertTest("Output byte length equals 'Hello, World!' + newline bytes", () -> {
            String output = captureMainOutput();
            String expected = "Hello, World!" + System.lineSeparator();
            byte[] outputBytes = output.getBytes(StandardCharsets.UTF_8);
            byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
            assertEquals(expectedBytes.length, outputBytes.length);
        });
    }

    private static void testOutputIsPureASCII() {
        assertTest("Output contains only ASCII characters (bytes 0x00-0x7F)", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            for (byte b : bytes) {
                assertTrue((b & 0xFF) <= 0x7F);
            }
        });
    }

    private static void testOutputHasNoBOM() {
        assertTest("Output does not start with UTF-8 BOM (EF BB BF)", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            if (bytes.length >= 3) {
                assertFalse(
                    (bytes[0] & 0xFF) == 0xEF &&
                    (bytes[1] & 0xFF) == 0xBB &&
                    (bytes[2] & 0xFF) == 0xBF
                );
            }
        });
    }

    private static void testOutputBytesMatchExpected() {
        assertTest("Raw output bytes exactly match expected UTF-8 bytes", () -> {
            String output = captureMainOutput();
            String expected = "Hello, World!" + System.lineSeparator();
            byte[] outputBytes = output.getBytes(StandardCharsets.UTF_8);
            byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
            assertArrayEquals(expectedBytes, outputBytes);
        });
    }

    private static void testSystemOutRestoredAfterMain() {
        assertTest("System.out is unchanged after main() execution", () -> {
            PrintStream original = System.out;
            captureMainOutput();
            assertSame(original, System.out);
        });
    }

    private static void testMainWithEmptyStringArray() {
        assertTest("main() works with new String[]{} (explicit empty array)", () -> {
            String output = captureMainOutputWithArgs(new String[]{});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMainWithLargeArgsArray() {
        assertTest("main() handles large args array (10000 elements)", () -> {
            String[] largeArgs = new String[10000];
            for (int i = 0; i < largeArgs.length; i++) {
                largeArgs[i] = "arg" + i;
            }
            String output = captureMainOutputWithArgs(largeArgs);
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMainWithSpecialCharArgs() {
        assertTest("main() ignores args with special characters", () -> {
            String output = captureMainOutputWithArgs(new String[]{
                "--flag", "你好", "🎉", "null", "\t\n", ""
            });
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testConcurrentMainCalls() {
        assertTest("Concurrent main() calls all produce correct output", () -> {
            final int threadCount = 5;
            Thread[] threads = new Thread[threadCount];
            final boolean[] results = new boolean[threadCount];
            Object lock = new Object();

            for (int i = 0; i < threadCount; i++) {
                final int idx = i;
                threads[i] = new Thread(() -> {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PrintStream ps = new PrintStream(baos);
                    synchronized (lock) {
                        PrintStream original = System.out;
                        System.setOut(ps);
                        try {
                            Hello.main(new String[]{});
                        } finally {
                            System.setOut(original);
                        }
                    }
                    results[idx] = "Hello, World!".equals(baos.toString().trim());
                });
            }

            for (Thread t : threads) t.start();
            for (Thread t : threads) t.join();

            for (int i = 0; i < threadCount; i++) {
                assertTrue("Thread " + i + " output mismatch", results[i]);
            }
        });
    }

    private static void testMainDoesNotMutateArgs() {
        assertTest("main() does not mutate the input args array", () -> {
            String[] args = new String[]{"original1", "original2"};
            String arg0Before = args[0];
            String arg1Before = args[1];
            captureMainOutputWithArgs(args);
            assertEquals(arg0Before, args[0]);
            assertEquals(arg1Before, args[1]);
        });
    }

    private static void testMainDoesNotReadFromStdin() {
        assertTest("main() does not consume System.in", () -> {
            java.io.InputStream originalIn = System.in;
            byte[] dummy = "should not be read".getBytes(StandardCharsets.UTF_8);
            java.io.ByteArrayInputStream fakeIn = new java.io.ByteArrayInputStream(dummy);
            System.setIn(fakeIn);
            try {
                captureMainOutput();
                assertEquals(dummy.length, fakeIn.available());
            } finally {
                System.setIn(originalIn);
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

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static void assertTest(String name, ThrowingRunnable test) {
        assertTest(name, test, null);
    }

    private static void assertTest(String name, ThrowingRunnable test, Class<? extends Exception> allowed) {
        total++;
        try {
            test.run();
            passed++;
            System.out.println("  PASS: " + name);
        } catch (AssertionError e) {
            failed++;
            System.out.println("  FAIL: " + name + " - " + e.getMessage());
        } catch (Exception e) {
            if (allowed != null && allowed.isInstance(e)) {
                failed++;
                System.out.println("  FAIL: " + name + " - Expected no exception but got: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            } else {
                failed++;
                System.out.println("  FAIL: " + name + " - Exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
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

    private static void assertEquals(String message, Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " - Expected <" + expected + "> but got <" + actual + ">");
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

    private static void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition) {
        if (condition) {
            throw new AssertionError("Expected false but got true");
        }
    }

    private static void assertSame(Object expected, Object actual) {
        if (expected != actual) {
            throw new AssertionError("Expected same reference but got different objects");
        }
    }

    private static void assertArrayEquals(byte[] expected, byte[] actual) {
        if (expected.length != actual.length) {
            throw new AssertionError("Array length mismatch: expected " + expected.length + " but got " + actual.length);
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                throw new AssertionError("Array mismatch at index " + i + ": expected " + expected[i] + " but got " + actual[i]);
            }
        }
    }
}
