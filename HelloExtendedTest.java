import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

    public static void main(String[] args) {
        System.out.println("=== HelloExtendedTest - Supplementary Tests ===\n");

        System.out.println("--- Class Properties Tests ---");
        testClassIsNotAbstract();
        testClassIsNotInterface();
        testClassIsNotEnum();
        testClassHasDefaultConstructor();
        testClassHasNoDeclaredFields();
        testClassHasExactlyOneMethod();

        System.out.println("\n--- Content Validation Tests ---");
        testOutputStartsWithHello();
        testOutputEndsWithExclamation();
        testOutputContainsComma();
        testOutputIsPureASCII();
        testOutputByteLength();
        testCaseSensitiveExactMatch();
        testOutputContainsNoTab();
        testOutputContainsNoCarriageReturn();
        testOutputIsExactlyHelloWorld();

        System.out.println("\n--- Concurrency & Edge Case Tests ---");
        testConcurrentExecution();
        testCanInstantiateClass();
        testMultipleRapidCalls();

        System.out.println("\n=== Extended Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL TESTS PASSED");
        } else {
            System.out.println("Result: SOME TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testClassIsNotAbstract() {
        assertTest("Hello class is not abstract", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isAbstract(clazz.getModifiers()));
        });
    }

    private static void testClassIsNotInterface() {
        assertTest("Hello class is not an interface", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isInterface(clazz.getModifiers()));
        });
    }

    private static void testClassIsNotEnum() {
        assertTest("Hello class is not an enum", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(clazz.isEnum());
        });
    }

    private static void testClassHasDefaultConstructor() {
        assertTest("Hello has a public default constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?>[] constructors = clazz.getConstructors();
            assertTrue(constructors.length >= 1);
            for (Constructor<?> c : constructors) {
                if (c.getParameterCount() == 0) {
                    assertTrue(Modifier.isPublic(c.getModifiers()));
                    return;
                }
            }
            throw new AssertionError("No default constructor found");
        });
    }

    private static void testClassHasNoDeclaredFields() {
        assertTest("Hello class has no declared fields (pure behavior class)", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getDeclaredFields().length);
        });
    }

    private static void testClassHasExactlyOneMethod() {
        assertTest("Hello class declares exactly one method (main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            assertEquals(1, methods.length);
            assertEquals("main", methods[0].getName());
        });
    }

    private static void testOutputStartsWithHello() {
        assertTest("Output starts with 'Hello'", () -> {
            String output = captureMainOutput();
            assertTrue(output.startsWith("Hello"));
        });
    }

    private static void testOutputEndsWithExclamation() {
        assertTest("Content ends with '!'", () -> {
            String output = captureMainOutput().trim();
            assertTrue(output.endsWith("!"));
        });
    }

    private static void testOutputContainsComma() {
        assertTest("Output contains a comma after 'Hello'", () -> {
            String output = captureMainOutput().trim();
            assertTrue(output.contains("Hello,"));
        });
    }

    private static void testOutputIsPureASCII() {
        assertTest("Output contains only ASCII characters", () -> {
            String output = captureMainOutput();
            for (char c : output.toCharArray()) {
                assertTrue(c < 128);
            }
        });
    }

    private static void testOutputByteLength() {
        assertTest("Output byte length matches expected (13 bytes + newline)", () -> {
            String output = captureMainOutput();
            String content = output.trim();
            assertEquals(13, content.length());
        });
    }

    private static void testCaseSensitiveExactMatch() {
        assertTest("Output is exactly 'Hello, World!' (case-sensitive)", () -> {
            String output = captureMainOutput().trim();
            assertEquals("Hello, World!", output);
            assertFalse(output.equals("hello, world!"));
            assertFalse(output.equals("HELLO, WORLD!"));
        });
    }

    private static void testOutputContainsNoTab() {
        assertTest("Output contains no tab characters", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("\t"));
        });
    }

    private static void testOutputContainsNoCarriageReturn() {
        assertTest("Output contains no stray carriage return characters", () -> {
            String output = captureMainOutput();
            String normalized = output.replace(System.lineSeparator(), "");
            assertFalse(normalized.contains("\r"));
        });
    }

    private static void testOutputIsExactlyHelloWorld() {
        assertTest("Output has exactly two words: 'Hello' and 'World!'", () -> {
            String output = captureMainOutput().trim();
            String[] parts = output.split(",\\s*");
            assertEquals(2, parts.length);
            assertEquals("Hello", parts[0]);
            assertEquals("World!", parts[1]);
        });
    }

    private static void testConcurrentExecution() {
        assertTest("Concurrent calls to main() all produce correct output", () -> {
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        String output = captureMainOutput();
                        if ("Hello, World!".equals(output.trim())) {
                            successCount.incrementAndGet();
                        } else {
                            failCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
            executor.shutdown();
            assertEquals(threadCount, successCount.get());
            assertEquals(0, failCount.get());
        });
    }

    private static void testCanInstantiateClass() {
        assertTest("Hello class can be instantiated", () -> {
            Class<?> clazz = Class.forName("Hello");
            Object instance = clazz.getDeclaredConstructor().newInstance();
            assertNotNull(instance);
            assertTrue(clazz.isInstance(instance));
        });
    }

    private static void testMultipleRapidCalls() {
        assertTest("100 rapid sequential calls all produce correct output", () -> {
            for (int i = 0; i < 100; i++) {
                String output = captureMainOutput();
                if (!"Hello, World!".equals(output.trim())) {
                    throw new AssertionError("Failed at iteration " + i + ": got '" + output.trim() + "'");
                }
            }
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
