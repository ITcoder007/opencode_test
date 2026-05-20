import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloExtendedTest {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloExtendedTest - Supplementary Unit Tests ===\n");

        System.out.println("--- Instantiation & Constructor Tests ---");
        testDefaultConstructorExists();
        testCanInstantiateHello();
        testInstanceIsNotNull();

        System.out.println("\n--- Encoding & Byte-Level Tests ---");
        testOutputIsUTF8Compatible();
        testOutputByteLength();
        testOutputCharacterCount();
        testNoBOMInOutput();
        testAllASCIICharacters();

        System.out.println("\n--- Concurrency & Thread Safety Tests ---");
        testConcurrentExecution();
        testConcurrentOutputConsistency();

        System.out.println("\n--- Class Hierarchy & Method Count Tests ---");
        testExtendsObject();
        testHasOnlyMainMethod();
        testNoAdditionalFields();

        System.out.println("\n--- Edge Case Tests ---");
        testMainWithEmptyStringArray();
        testMainWithLargeArgsArray();
        testOutputDoesNotContainTab();
        testOutputDoesNotContainCarriageReturn();
        testMultipleRapidInvocations();

        System.out.println("\n=== Extended Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL TESTS PASSED");
        } else {
            System.out.println("Result: SOME TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testDefaultConstructorExists() {
        assertTest("Hello has a default constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?>[] constructors = clazz.getConstructors();
            assertTrue(constructors.length > 0);
        }, ClassNotFoundException.class);
    }

    private static void testCanInstantiateHello() {
        assertTest("Hello can be instantiated via default constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?> ctor = clazz.getConstructor();
            Object instance = ctor.newInstance();
            assertNotNull(instance);
        }, ReflectiveOperationException.class);
    }

    private static void testInstanceIsNotNull() {
        assertTest("Hello instance is not null", () -> {
            Hello instance = new Hello();
            assertNotNull(instance);
        });
    }

    private static void testOutputIsUTF8Compatible() {
        assertTest("Output bytes are valid UTF-8", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            String decoded = new String(bytes, StandardCharsets.UTF_8);
            assertEquals(output, decoded);
        });
    }

    private static void testOutputByteLength() {
        assertTest("Output byte length is exactly 14 bytes (content) + newline", () -> {
            String output = captureMainOutput();
            String content = output.replace(System.lineSeparator(), "");
            assertEquals(13, content.getBytes(StandardCharsets.UTF_8).length);
        });
    }

    private static void testOutputCharacterCount() {
        assertTest("Output content is exactly 13 characters", () -> {
            String output = captureMainOutput();
            String content = output.replace(System.lineSeparator(), "");
            assertEquals(13, content.length());
        });
    }

    private static void testNoBOMInOutput() {
        assertTest("Output does not start with UTF-8 BOM", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            if (bytes.length >= 3) {
                assertFalse(bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF);
            }
        });
    }

    private static void testAllASCIICharacters() {
        assertTest("All output characters are ASCII (code point < 128)", () -> {
            String output = captureMainOutput();
            for (char c : output.toCharArray()) {
                assertTrue(c < 128);
            }
        });
    }

    private static void testConcurrentExecution() {
        assertTest("main() can be called from 10 threads concurrently without error", () -> {
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            List<Throwable> errors = new ArrayList<>();

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        Hello.main(new String[]{});
                    } catch (Throwable t) {
                        synchronized (errors) {
                            errors.add(t);
                        }
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            assertTrue(doneLatch.await(5, TimeUnit.SECONDS));
            assertTrue("Expected no errors, got " + errors.size(), errors.isEmpty());
            executor.shutdown();
        }, InterruptedException.class);
    }

    private static void testConcurrentOutputConsistency() {
        assertTest("Concurrent calls produce consistent output", () -> {
            String expected = captureMainOutput();
            int runs = 50;
            for (int i = 0; i < runs; i++) {
                String output = captureMainOutput();
                assertEquals(expected, output);
            }
        });
    }

    private static void testExtendsObject() {
        assertTest("Hello directly extends java.lang.Object", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(Object.class, clazz.getSuperclass());
        }, ClassNotFoundException.class);
    }

    private static void testHasOnlyMainMethod() {
        assertTest("Hello declares only the main method", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            assertEquals(1, methods.length);
            assertEquals("main", methods[0].getName());
        }, ClassNotFoundException.class);
    }

    private static void testNoAdditionalFields() {
        assertTest("Hello has no declared fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getDeclaredFields().length);
        }, ClassNotFoundException.class);
    }

    private static void testMainWithEmptyStringArray() {
        assertTest("main() works with new String[]{''}", () -> {
            String output = captureMainOutputWithArgs(new String[]{""});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMainWithLargeArgsArray() {
        assertTest("main() works with 1000-element args array", () -> {
            String[] largeArgs = new String[1000];
            for (int i = 0; i < 1000; i++) {
                largeArgs[i] = "arg" + i;
            }
            String output = captureMainOutputWithArgs(largeArgs);
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testOutputDoesNotContainTab() {
        assertTest("Output does not contain tab character", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("\t"));
        });
    }

    private static void testOutputDoesNotContainCarriageReturn() {
        assertTest("Output does not contain standalone CR (\\r)", () -> {
            String output = captureMainOutput();
            if (System.lineSeparator().equals("\n")) {
                assertFalse(output.contains("\r"));
            }
        });
    }

    private static void testMultipleRapidInvocations() {
        assertTest("100 rapid sequential invocations all produce correct output", () -> {
            for (int i = 0; i < 100; i++) {
                String output = captureMainOutput();
                assertEquals("Hello, World!" + System.lineSeparator(), output);
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
}
