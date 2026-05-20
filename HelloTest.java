import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloTest {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;
    private static final List<String> failures = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        System.out.println("=== HelloTest - Unit Tests for Hello.java ===\n");

        System.out.println("--- Basic Structure Tests ---");
        testClassExists();
        testClassName();
        testClassIsPublic();
        testClassHasMainMethod();
        testMainMethodSignature();

        System.out.println("\n--- Output Correctness Tests ---");
        testMainOutput();
        testOutputMatchesPython();
        testOutputEndsWithNewline();
        testNoLeadingWhitespace();
        testNoTrailingSpacesBeforeNewline();
        testExactOutputFormat();

        System.out.println("\n--- Robustness Tests ---");
        testMainWithNullArgs();
        testMainWithNonEmptyArgs();
        testIdempotency();
        testSingleLineOutput();

        System.out.println("\n--- Class Structure Enhancement Tests ---");
        testClassCanBeInstantiated();
        testNoDeclaredFields();
        testOnlyOnePublicMethod();
        testHasDefaultConstructor();
        testClassNotAbstract();
        testClassNotFinal();

        System.out.println("\n--- Encoding & Character Tests ---");
        testOutputIsPureASCII();
        testNoBOMInOutput();
        testOutputLengthExact();

        System.out.println("\n--- Edge Case Tests ---");
        testMainWithEmptyStringArray();
        testMainWithLargeArgArray();
        testMultipleSequentialCalls();
        testOutputConsistencyAcross100Runs();

        System.out.println("\n--- Concurrency Safety Test ---");
        testConcurrentMainCalls();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (!failures.isEmpty()) {
            System.out.println("\nFailed tests:");
            for (String f : failures) {
                System.out.println("  - " + f);
            }
        }
        if (failed == 0) {
            System.out.println("Result: ALL TESTS PASSED");
        } else {
            System.out.println("Result: SOME TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testClassExists() {
        assertTest("Hello class exists and can be loaded", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertNotNull(clazz);
        }, ClassNotFoundException.class);
    }

    private static void testClassName() {
        assertTest("Class is named 'Hello'", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals("Hello", clazz.getSimpleName());
        }, ClassNotFoundException.class);
    }

    private static void testClassIsPublic() {
        assertTest("Hello class is public", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertTrue(Modifier.isPublic(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassHasMainMethod() {
        assertTest("Hello has a main method", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            assertNotNull(main);
        }, NoSuchMethodException.class);
    }

    private static void testMainMethodSignature() {
        assertTest("main method is public static void with String[] param", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            int mod = main.getModifiers();
            assertTrue(Modifier.isPublic(mod));
            assertTrue(Modifier.isStatic(mod));
            assertEquals(void.class, main.getReturnType());
            Class<?>[] params = main.getParameterTypes();
            assertEquals(1, params.length);
            assertEquals(String[].class, params[0]);
        }, NoSuchMethodException.class);
    }

    private static void testMainOutput() {
        assertTest("main() outputs 'Hello, World!'", () -> {
            String output = captureMainOutput();
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testOutputMatchesPython() {
        assertTest("Java output matches Python print('Hello, World!') output", () -> {
            String output = captureMainOutput();
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testOutputEndsWithNewline() {
        assertTest("Output ends with system newline (matches println behavior)", () -> {
            String output = captureMainOutput();
            assertTrue(output.endsWith(System.lineSeparator()));
        });
    }

    private static void testNoLeadingWhitespace() {
        assertTest("Output has no leading whitespace", () -> {
            String output = captureMainOutput();
            String content = output.replace(System.lineSeparator(), "");
            assertEquals(content, content.replaceAll("^\\s+", ""));
        });
    }

    private static void testNoTrailingSpacesBeforeNewline() {
        assertTest("No trailing spaces before newline", () -> {
            String output = captureMainOutput();
            String line = output.split(System.lineSeparator())[0];
            assertEquals(line, line.replaceAll("\\s+$", ""));
        });
    }

    private static void testExactOutputFormat() {
        assertTest("Exact output is 'Hello, World!' + newline", () -> {
            String output = captureMainOutput();
            assertEquals("Hello, World!" + System.lineSeparator(), output);
        });
    }

    private static void testMainWithNullArgs() {
        assertTest("main() handles null args without crashing", () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(baos));
            try {
                Hello.main(null);
            } finally {
                System.setOut(originalOut);
            }
            assertEquals("Hello, World!", baos.toString().trim());
        });
    }

    private static void testMainWithNonEmptyArgs() {
        assertTest("main() ignores extra arguments gracefully", () -> {
            String output = captureMainOutputWithArgs(new String[]{"arg1", "arg2"});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testIdempotency() {
        assertTest("Running main() twice produces identical output", () -> {
            String first = captureMainOutput();
            String second = captureMainOutput();
            assertEquals(first, second);
        });
    }

    private static void testSingleLineOutput() {
        assertTest("Output is exactly one line", () -> {
            String output = captureMainOutput();
            String withoutTrailingNewline = output.endsWith(System.lineSeparator())
                ? output.substring(0, output.length() - System.lineSeparator().length())
                : output;
            assertFalse(withoutTrailingNewline.contains(System.lineSeparator()));
        });
    }

    private static void testClassCanBeInstantiated() {
        assertTest("Hello class can be instantiated via reflection", () -> {
            Class<?> clazz = Class.forName("Hello");
            Object instance = clazz.getDeclaredConstructor().newInstance();
            assertNotNull(instance);
        });
    }

    private static void testNoDeclaredFields() {
        assertTest("Hello class has no declared fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            Field[] fields = clazz.getDeclaredFields();
            assertEquals(0, fields.length);
        });
    }

    private static void testOnlyOnePublicMethod() {
        assertTest("Hello class has exactly one declared public method (main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            int publicCount = 0;
            for (Method m : methods) {
                if (Modifier.isPublic(m.getModifiers())) {
                    publicCount++;
                }
            }
            assertEquals(1, publicCount);
        });
    }

    private static void testHasDefaultConstructor() {
        assertTest("Hello has a default (no-arg) constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            boolean hasDefault = false;
            for (Constructor<?> c : constructors) {
                if (c.getParameterCount() == 0) {
                    hasDefault = true;
                    break;
                }
            }
            assertTrue(hasDefault);
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

    private static void testOutputIsPureASCII() {
        assertTest("Output contains only ASCII characters", () -> {
            String output = captureMainOutput();
            for (char c : output.toCharArray()) {
                assertTrue(c < 128);
            }
        });
    }

    private static void testNoBOMInOutput() {
        assertTest("Output does not start with BOM (Byte Order Mark)", () -> {
            String output = captureMainOutput();
            assertFalse(output.startsWith("\uFEFF"));
        });
    }

    private static void testOutputLengthExact() {
        assertTest("Output length matches expected: 'Hello, World!' + newline", () -> {
            String output = captureMainOutput();
            int expectedLen = "Hello, World!".length() + System.lineSeparator().length();
            assertEquals(expectedLen, output.length());
        });
    }

    private static void testMainWithEmptyStringArray() {
        assertTest("main() with empty String array produces correct output", () -> {
            String output = captureMainOutputWithArgs(new String[]{});
            assertEquals("Hello, World!" + System.lineSeparator(), output);
        });
    }

    private static void testMainWithLargeArgArray() {
        assertTest("main() with 1000-element arg array still produces correct output", () -> {
            String[] largeArgs = new String[1000];
            for (int i = 0; i < 1000; i++) {
                largeArgs[i] = "arg" + i;
            }
            String output = captureMainOutputWithArgs(largeArgs);
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMultipleSequentialCalls() {
        assertTest("5 sequential main() calls all produce identical output", () -> {
            String first = captureMainOutput();
            for (int i = 0; i < 4; i++) {
                assertEquals(first, captureMainOutput());
            }
        });
    }

    private static void testOutputConsistencyAcross100Runs() {
        assertTest("100 consecutive main() calls produce consistent output", () -> {
            String expected = captureMainOutput();
            for (int i = 1; i < 100; i++) {
                assertEquals(expected, captureMainOutput());
            }
        });
    }

    private static void testConcurrentMainCalls() {
        assertTest("Concurrent main() calls (10 threads) all complete without error", () -> {
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<String> errors = Collections.synchronizedList(new ArrayList<>());
            List<String> outputs = Collections.synchronizedList(new ArrayList<>());

            PrintStream originalOut = System.out;
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    System.setOut(new PrintStream(baos));
                    try {
                        Hello.main(new String[]{});
                        outputs.add(baos.toString().trim());
                    } catch (Exception e) {
                        errors.add(e.getClass().getSimpleName() + ": " + e.getMessage());
                    } finally {
                        System.setOut(originalOut);
                        latch.countDown();
                    }
                });
            }
            latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();
            assertTrue(errors.isEmpty());
            for (String out : outputs) {
                assertEquals("Hello, World!", out);
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
            String msg = name + " - " + e.getMessage();
            failures.add(msg);
            System.out.println("  FAIL: " + msg);
        } catch (Exception e) {
            String msg;
            if (allowed != null && allowed.isInstance(e)) {
                msg = name + " - Expected no exception but got: " + e.getClass().getSimpleName() + ": " + e.getMessage();
            } else {
                msg = name + " - Exception: " + e.getClass().getSimpleName() + ": " + e.getMessage();
            }
            failed++;
            failures.add(msg);
            System.out.println("  FAIL: " + msg);
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
