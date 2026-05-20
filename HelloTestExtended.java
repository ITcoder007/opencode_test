import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

public class HelloTestExtended {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloTestExtended - Supplementary Tests ===\n");

        System.out.println("--- Extended Structure Tests ---");
        testOnlyOnePublicMethod();
        testNoDeclaredFields();
        testClassNotAbstract();
        testClassNotFinal();
        testDefaultConstructorOnly();

        System.out.println("\n--- Extended Output Tests ---");
        testOutputByteLength();
        testOutputUTF8Compatible();
        testOutputAsciiOnly();
        testOutputLengthConsistency();

        System.out.println("\n--- Extended Robustness Tests ---");
        testSystemOutRestoredAfterCall();
        testLargeArgsArray();
        testConcurrentExecution();
        testMainExitCodeZero();
        testEmptyStringArgs();
        testUnicodeArgsIgnored();

        System.out.println("\n=== Extended Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL EXTENDED TESTS PASSED");
        } else {
            System.out.println("Result: SOME EXTENDED TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testOnlyOnePublicMethod() {
        assertTest("Hello has exactly 1 public method (main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] publicMethods = clazz.getDeclaredMethods();
            List<Method> publicMethodList = new ArrayList<>();
            for (Method m : publicMethodList) {
                publicMethodList.add(m);
            }
            int publicCount = 0;
            for (Method m : clazz.getDeclaredMethods()) {
                if (Modifier.isPublic(m.getModifiers())) {
                    publicCount++;
                }
            }
            assertEquals(1, publicCount);
        });
    }

    private static void testNoDeclaredFields() {
        assertTest("Hello has no declared fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            Field[] fields = clazz.getDeclaredFields();
            assertEquals(0, fields.length);
        });
    }

    private static void testClassNotAbstract() {
        assertTest("Hello class is not abstract", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isAbstract(clazz.getModifiers()));
        });
    }

    private static void testClassNotFinal() {
        assertTest("Hello class is not final", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isFinal(clazz.getModifiers()));
        });
    }

    private static void testDefaultConstructorOnly() {
        assertTest("Hello has only default no-arg constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            assertEquals(1, constructors.length);
            assertEquals(0, constructors[0].getParameterCount());
        });
    }

    private static void testOutputByteLength() {
        assertTest("Output byte length matches expected", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            String expected = "Hello, World!" + System.lineSeparator();
            byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
            assertEquals(expectedBytes.length, bytes.length);
        });
    }

    private static void testOutputUTF8Compatible() {
        assertTest("Output is valid UTF-8", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            String reconstructed = new String(bytes, StandardCharsets.UTF_8);
            assertEquals(output, reconstructed);
        });
    }

    private static void testOutputAsciiOnly() {
        assertTest("Output contains only ASCII characters", () -> {
            String output = captureMainOutput();
            String content = output.replace(System.lineSeparator(), "");
            for (char c : content.toCharArray()) {
                assertTrue(c < 128);
            }
        });
    }

    private static void testOutputLengthConsistency() {
        assertTest("Output length is consistent across 10 invocations", () -> {
            int firstLen = captureMainOutput().length();
            for (int i = 0; i < 9; i++) {
                assertEquals(firstLen, captureMainOutput().length());
            }
        });
    }

    private static void testSystemOutRestoredAfterCall() {
        assertTest("System.out is usable after main() call", () -> {
            PrintStream original = System.out;
            captureMainOutput();
            assertSame(original, System.out);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));
            System.out.print("probe");
            System.setOut(original);
            assertEquals("probe", baos.toString());
        });
    }

    private static void testLargeArgsArray() {
        assertTest("main() handles 10000-element args array", () -> {
            String[] largeArgs = new String[10000];
            for (int i = 0; i < largeArgs.length; i++) {
                largeArgs[i] = "arg" + i;
            }
            String output = captureMainOutputWithArgs(largeArgs);
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testConcurrentExecution() {
        assertTest("main() runs without error under concurrent execution (10 threads)", () -> {
            int threadCount = 10;
            ByteArrayOutputStream sharedBaos = new ByteArrayOutputStream();
            PrintStream sharedPs = new PrintStream(sharedBaos);
            PrintStream originalOut = System.out;
            System.setOut(sharedPs);

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            AtomicInteger errorCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        Hello.main(new String[]{});
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
            System.setOut(originalOut);
            executor.shutdown();

            assertTrue(completed);
            assertEquals(0, errorCount.get());
            String combined = sharedBaos.toString().trim();
            for (String line : combined.split(System.lineSeparator())) {
                assertEquals("Hello, World!", line);
            }
        });
    }

    private static void testMainExitCodeZero() {
        assertTest("Hello.main() completes without throwing exception", () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(baos));
            try {
                Hello.main(new String[]{});
            } finally {
                System.setOut(originalOut);
            }
            assertTrue(baos.toString().contains("Hello, World!"));
        });
    }

    private static void testEmptyStringArgs() {
        assertTest("main() handles args with empty strings", () -> {
            String output = captureMainOutputWithArgs(new String[]{"", "", ""});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testUnicodeArgsIgnored() {
        assertTest("main() ignores Unicode arguments", () -> {
            String output = captureMainOutputWithArgs(new String[]{"你好", "世界", "🎉"});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static String getTmpClasspath() {
        return System.getProperty("user.dir") + java.io.File.separator + "tmp";
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

    private static void assertSame(Object expected, Object actual) {
        if (expected != actual) {
            throw new AssertionError("Expected same reference but got different objects");
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
