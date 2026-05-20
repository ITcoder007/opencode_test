import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HelloExtendedTest {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;
    private static final List<String> failures = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        System.out.println("=== HelloExtendedTest - Supplementary Test Suite ===\n");

        System.out.println("--- Class Modifier & Instantiation Tests ---");
        testClassNotAbstract();
        testClassNotInterface();
        testClassNotFinal();
        testClassNotEnum();
        testDefaultPackage();
        testHasDefaultConstructor();
        testCanInstantiate();
        testOnlyOnePublicMethod();

        System.out.println("\n--- Encoding & Stream Tests ---");
        testOutputIsUTF8();
        testStderrRemainsClean();
        testOutputByteContent();

        System.out.println("\n--- Performance & Concurrency Tests ---");
        testExecutionTimeBounded();
        testConcurrentExecution();
        testRepeatedExecution100Times();

        System.out.println("\n--- Semantic Equivalence Tests ---");
        testOutputCaseSensitive();
        testOutputContainsComma();
        testOutputContainsExclamation();
        testOutputLength();
        testNoExtraPublicFields();

        System.out.println("\n=== HelloExtendedTest Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed > 0) {
            System.out.println("\nFailed tests:");
            for (String f : failures) {
                System.out.println("  - " + f);
            }
        }
        if (failed == 0) {
            System.out.println("Result: ALL EXTENDED TESTS PASSED");
        } else {
            System.out.println("Result: SOME EXTENDED TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testClassNotAbstract() {
        assertTest("Hello is not abstract", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isAbstract(clazz.getModifiers()));
        });
    }

    private static void testClassNotInterface() {
        assertTest("Hello is not an interface", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isInterface(clazz.getModifiers()));
        });
    }

    private static void testClassNotFinal() {
        assertTest("Hello is not final (extendable)", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isFinal(clazz.getModifiers()));
        });
    }

    private static void testClassNotEnum() {
        assertTest("Hello is not an enum", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(clazz.isEnum());
        });
    }

    private static void testDefaultPackage() {
        assertTest("Hello is in default (unnamed) package", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals("", clazz.getPackage().getName());
        });
    }

    private static void testHasDefaultConstructor() {
        assertTest("Hello has a public no-arg constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?> ctor = clazz.getConstructor();
            assertNotNull(ctor);
            assertTrue(Modifier.isPublic(ctor.getModifiers()));
        });
    }

    private static void testCanInstantiate() {
        assertTest("Hello can be instantiated", () -> {
            Class<?> clazz = Class.forName("Hello");
            Object instance = clazz.getConstructor().newInstance();
            assertNotNull(instance);
            assertEquals("Hello", instance.getClass().getSimpleName());
        });
    }

    private static void testOnlyOnePublicMethod() {
        assertTest("Hello has exactly one declared public method (main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] publicMethods = clazz.getDeclaredMethods();
            int publicCount = 0;
            for (Method m : publicMethods) {
                if (Modifier.isPublic(m.getModifiers())) {
                    publicCount++;
                }
            }
            assertEquals(1, publicCount);
        });
    }

    private static void testOutputIsUTF8() {
        assertTest("Output bytes are valid UTF-8", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            String decoded = new String(bytes, StandardCharsets.UTF_8);
            assertEquals(output, decoded);
        });
    }

    private static void testStderrRemainsClean() {
        assertTest("main() writes nothing to stderr", () -> {
            PrintStream originalErr = System.err;
            ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            System.setErr(new PrintStream(errContent));
            System.setOut(new PrintStream(outContent));
            try {
                Hello.main(new String[]{});
            } finally {
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
            assertEquals("", errContent.toString());
        });
    }

    private static void testOutputByteContent() {
        assertTest("Output byte content matches expected ASCII", () -> {
            String output = captureMainOutput();
            byte[] expected = ("Hello, World!" + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
            byte[] actual = output.getBytes(StandardCharsets.UTF_8);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < expected.length; i++) {
                assertEquals(expected[i], actual[i]);
            }
        });
    }

    private static void testExecutionTimeBounded() {
        assertTest("main() completes within 500ms", () -> {
            PrintStream original = System.out;
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            long start;
            try {
                start = System.nanoTime();
                for (int i = 0; i < 100; i++) {
                    Hello.main(new String[]{});
                }
            } finally {
                System.setOut(original);
            }
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            assertTrue(elapsed < 500);
        });
    }

    private static void testConcurrentExecution() {
        assertTest("Concurrent calls to main() do not throw exceptions", () -> {
            final int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger errorCount = new AtomicInteger(0);
            PrintStream original = System.out;
            ByteArrayOutputStream collective = new ByteArrayOutputStream();
            System.setOut(new PrintStream(collective));

            try {
                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        try {
                            latch.countDown();
                            latch.await();
                            Hello.main(new String[]{});
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        }
                    });
                }
                executor.shutdown();
                assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
            } finally {
                System.setOut(original);
            }

            assertEquals(0, errorCount.get());
            String output = collective.toString();
            int lineCount = 0;
            for (String line : output.split(System.lineSeparator())) {
                if (line.equals("Hello, World!")) {
                    lineCount++;
                }
            }
            assertEquals(threadCount, lineCount);
        });
    }

    private static void testRepeatedExecution100Times() {
        assertTest("100 repeated executions produce consistent output", () -> {
            String expected = "Hello, World!" + System.lineSeparator();
            for (int i = 0; i < 100; i++) {
                String output = captureMainOutput();
                if (!expected.equals(output)) {
                    throw new AssertionError("Iteration " + i + ": expected <" + escape(expected) + "> but got <" + escape(output) + ">");
                }
            }
        });
    }

    private static void testOutputCaseSensitive() {
        assertTest("Output has correct capitalization: 'H' upper, 'W' upper", () -> {
            String output = captureMainOutput().trim();
            assertTrue(output.startsWith("H"));
            assertTrue(output.contains("W"));
            assertEquals('H', output.charAt(0));
            int wIdx = output.indexOf('W');
            assertTrue(wIdx > 0);
        });
    }

    private static void testOutputContainsComma() {
        assertTest("Output contains a comma after 'Hello'", () -> {
            String output = captureMainOutput().trim();
            assertTrue(output.contains("Hello,"));
        });
    }

    private static void testOutputContainsExclamation() {
        assertTest("Output ends with exclamation mark before newline", () -> {
            String output = captureMainOutput().trim();
            assertTrue(output.endsWith("!"));
        });
    }

    private static void testOutputLength() {
        assertTest("Output content is exactly 13 characters", () -> {
            String output = captureMainOutput().trim();
            assertEquals(13, output.length());
        });
    }

    private static void testNoExtraPublicFields() {
        assertTest("Hello has no declared public fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
            int publicFieldCount = 0;
            for (java.lang.reflect.Field f : fields) {
                if (Modifier.isPublic(f.getModifiers())) {
                    publicFieldCount++;
                }
            }
            assertEquals(0, publicFieldCount);
        });
    }

    private static String captureMainOutput() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(baos));
        try {
            Hello.main(new String[]{});
        } finally {
            System.setOut(originalOut);
        }
        return baos.toString();
    }

    private static String escape(String s) {
        return s.replace("\n", "\\n").replace("\r", "\\r");
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
            failures.add(name + " - " + e.getMessage());
            System.out.println("  FAIL: " + name + " - " + e.getMessage());
        } catch (Exception e) {
            failed++;
            failures.add(name + " - " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.out.println("  FAIL: " + name + " - " + e.getClass().getSimpleName() + ": " + e.getMessage());
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
