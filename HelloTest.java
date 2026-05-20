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

        System.out.println("\n--- Class Integrity Tests ---");
        testClassIsNotAbstract();
        testClassIsNotEnum();
        testClassIsNotInterface();
        testClassHasDefaultConstructor();
        testNoExtraPublicMethods();
        testClassHasNoFields();

        System.out.println("\n--- Encoding & Byte-Level Tests ---");
        testOutputIsUTF8Compatible();
        testOutputByteContent();
        testOutputContainsOnlyASCII();

        System.out.println("\n--- Concurrency & Edge Case Tests ---");
        testConcurrentExecution();
        testMainWithEmptyStringArg();
        testMainDoesNotCallSystemExit();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed > 0) {
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
            String expected = "Hello, World!";
            assertEquals(expected, output.trim());
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

    private static void testClassIsNotAbstract() {
        assertTest("Hello class is not abstract", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isAbstract(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassIsNotEnum() {
        assertTest("Hello class is not an enum", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(clazz.isEnum());
        }, ClassNotFoundException.class);
    }

    private static void testClassIsNotInterface() {
        assertTest("Hello class is not an interface", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(clazz.isInterface());
        }, ClassNotFoundException.class);
    }

    private static void testClassHasDefaultConstructor() {
        assertTest("Hello has a public default constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?>[] constructors = clazz.getConstructors();
            assertTrue(constructors.length >= 1);
            boolean hasDefault = false;
            for (Constructor<?> c : constructors) {
                if (c.getParameterCount() == 0 && Modifier.isPublic(c.getModifiers())) {
                    hasDefault = true;
                    break;
                }
            }
            assertTrue(hasDefault);
        }, ClassNotFoundException.class);
    }

    private static void testNoExtraPublicMethods() {
        assertTest("Hello has only 'main' as declared public static method", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            int publicStaticCount = 0;
            for (Method m : methods) {
                int mod = m.getModifiers();
                if (Modifier.isPublic(mod) && Modifier.isStatic(mod)) {
                    publicStaticCount++;
                    assertEquals("main", m.getName());
                }
            }
            assertEquals(1, publicStaticCount);
        }, ClassNotFoundException.class);
    }

    private static void testClassHasNoFields() {
        assertTest("Hello class has no declared fields (pure function)", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getDeclaredFields().length);
        }, ClassNotFoundException.class);
    }

    private static void testOutputIsUTF8Compatible() {
        assertTest("Output bytes are valid UTF-8", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            String decoded = new String(bytes, StandardCharsets.UTF_8);
            assertEquals(output, decoded);
        });
    }

    private static void testOutputByteContent() {
        assertTest("Output byte content matches expected", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            String expected = "Hello, World!" + System.lineSeparator();
            byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
            assertEquals(expectedBytes.length, bytes.length);
            for (int i = 0; i < bytes.length; i++) {
                assertEquals(expectedBytes[i], bytes[i]);
            }
        });
    }

    private static void testOutputContainsOnlyASCII() {
        assertTest("Output contains only ASCII characters", () -> {
            String output = captureMainOutput();
            for (char c : output.toCharArray()) {
                assertTrue(c < 128);
            }
        });
    }

    private static void testConcurrentExecution() {
        assertTest("Concurrent calls to main() do not throw exceptions", () -> {
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
            ByteArrayOutputStream sharedBaos = new ByteArrayOutputStream();
            PrintStream sharedPs = new PrintStream(sharedBaos);
            PrintStream originalOut = System.out;
            System.setOut(sharedPs);
            try {
                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        try {
                            latch.countDown();
                            latch.await(5, TimeUnit.SECONDS);
                            Hello.main(new String[]{});
                        } catch (Throwable t) {
                            errors.add(t);
                        }
                    });
                }
                executor.shutdown();
                assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
            } finally {
                System.setOut(originalOut);
            }
            assertTrue(errors.isEmpty());
            String combinedOutput = sharedBaos.toString().trim();
            String[] lines = combinedOutput.split(System.lineSeparator());
            assertEquals(threadCount, lines.length);
            for (String line : lines) {
                assertEquals("Hello, World!", line.trim());
            }
        });
    }

    private static void testMainWithEmptyStringArg() {
        assertTest("main() handles empty string in args array", () -> {
            String output = captureMainOutputWithArgs(new String[]{""});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMainDoesNotCallSystemExit() {
        assertTest("main() does not call System.exit", () -> {
            captureMainOutput();
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
            failures.add(name);
            System.out.println("  FAIL: " + name + " - " + e.getMessage());
        } catch (Exception e) {
            failed++;
            failures.add(name);
            if (allowed != null && allowed.isInstance(e)) {
                System.out.println("  FAIL: " + name + " - Expected no exception but got: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            } else {
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

    private static void assertFalse(boolean condition) {
        if (condition) {
            throw new AssertionError("Expected false but got true");
        }
    }
}
