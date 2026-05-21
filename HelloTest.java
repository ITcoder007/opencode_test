import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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

        System.out.println("\n--- Error Stream Cleanliness Tests ---");
        testNoStderrOutput();
        testNoStderrWithArgs();

        System.out.println("\n--- Output Metrics Tests ---");
        testOutputCharacterCount();
        testOutputByteCount();
        testOutputContainsSubstrings();
        testOutputDoesNotContainUnexpectedChars();
        testOutputIsPlainAscii();

        System.out.println("\n--- Class API Minimality Tests ---");
        testOnlyMainIsPublicStatic();
        testClassHasNoPublicFields();

        System.out.println("\n--- Exception Safety Tests ---");
        testMainCompletesWithoutException();
        testMainCompletesWithoutExceptionNullArgs();

        System.out.println("\n--- Concurrency Safety Tests ---");
        testConcurrentExecution();
        testConcurrentExecutionProducesSameOutput();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
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

    private static void testNoStderrOutput() {
        assertTest("main() produces no output to stderr", () -> {
            PrintStream realOut = System.out;
            PrintStream realErr = System.err;
            ByteArrayOutputStream outBaos = new ByteArrayOutputStream();
            ByteArrayOutputStream errBaos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outBaos));
            System.setErr(new PrintStream(errBaos));
            try {
                Hello.main(new String[]{});
            } finally {
                System.setOut(realOut);
                System.setErr(realErr);
            }
            assertEquals("", errBaos.toString());
        });
    }

    private static void testNoStderrWithArgs() {
        assertTest("main() produces no stderr output even with args", () -> {
            PrintStream realOut = System.out;
            PrintStream realErr = System.err;
            ByteArrayOutputStream outBaos = new ByteArrayOutputStream();
            ByteArrayOutputStream errBaos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outBaos));
            System.setErr(new PrintStream(errBaos));
            try {
                Hello.main(new String[]{"unexpected", "args"});
            } finally {
                System.setOut(realOut);
                System.setErr(realErr);
            }
            assertEquals("", errBaos.toString());
        });
    }

    private static void testOutputCharacterCount() {
        assertTest("Output content is exactly 13 characters", () -> {
            String output = captureMainOutput();
            String content = output.trim();
            assertEquals(13, content.length());
        });
    }

    private static void testOutputByteCount() {
        assertTest("Output is 13 bytes in UTF-8 (pure ASCII)", () -> {
            String output = captureMainOutput();
            String content = output.trim();
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            assertEquals(13, bytes.length);
        });
    }

    private static void testOutputContainsSubstrings() {
        assertTest("Output contains expected substrings", () -> {
            String output = captureMainOutput().trim();
            assertTrue(output.contains("Hello"));
            assertTrue(output.contains("World"));
            assertTrue(output.contains(", "));
            assertTrue(output.contains("!"));
        });
    }

    private static void testOutputDoesNotContainUnexpectedChars() {
        assertTest("Output does not contain digits or unexpected special chars", () -> {
            String output = captureMainOutput().trim();
            for (char c : output.toCharArray()) {
                assertFalse(Character.isDigit(c));
            }
            assertFalse(output.contains("@"));
            assertFalse(output.contains("#"));
            assertFalse(output.contains("\t"));
        });
    }

    private static void testOutputIsPlainAscii() {
        assertTest("Output contains only ASCII printable characters", () -> {
            String output = captureMainOutput().trim();
            for (char c : output.toCharArray()) {
                assertTrue(c >= 32 && c <= 126);
            }
        });
    }

    private static void testOnlyMainIsPublicStatic() {
        assertTest("Only 'main' is a public static method in Hello", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            List<String> publicStatic = new ArrayList<>();
            for (Method m : methods) {
                int mod = m.getModifiers();
                if (Modifier.isPublic(mod) && Modifier.isStatic(mod)) {
                    publicStatic.add(m.getName());
                }
            }
            assertEquals(1, publicStatic.size());
            assertEquals("main", publicStatic.get(0));
        }, ClassNotFoundException.class);
    }

    private static void testClassHasNoPublicFields() {
        assertTest("Hello class has no public fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getFields().length);
        }, ClassNotFoundException.class);
    }

    private static void testMainCompletesWithoutException() {
        assertTest("main() completes without throwing any exception", () -> {
            captureMainOutput();
        });
    }

    private static void testMainCompletesWithoutExceptionNullArgs() {
        assertTest("main(null) completes without throwing any exception", () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(baos));
            try {
                Hello.main(null);
            } finally {
                System.setOut(originalOut);
            }
        });
    }

    private static void testConcurrentExecution() {
        assertTest("main() can be called concurrently without errors", () -> {
            int threadCount = 10;
            PrintStream realOut = System.out;
            ByteArrayOutputStream sharedBaos = new ByteArrayOutputStream();
            PrintStream sharedPs = new PrintStream(sharedBaos);
            System.setOut(sharedPs);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        Hello.main(new String[]{});
                    } catch (Throwable t) {
                        errors.add(t);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();
            System.setOut(realOut);
            assertTrue(errors.isEmpty());
        });
    }

    private static void testConcurrentExecutionProducesSameOutput() {
        assertTest("Concurrent calls produce identical output content", () -> {
            String expected = "Hello, World!" + System.lineSeparator();
            int threadCount = 10;
            PrintStream realOut = System.out;
            ByteArrayOutputStream sharedBaos = new ByteArrayOutputStream();
            PrintStream sharedPs = new PrintStream(sharedBaos);
            System.setOut(sharedPs);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        Hello.main(new String[]{});
                    } catch (Throwable t) {
                        errors.add(t);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();
            System.setOut(realOut);
            assertTrue(errors.isEmpty());
            String allOutput = sharedBaos.toString();
            int occurrences = 0;
            int idx = 0;
            while ((idx = allOutput.indexOf(expected, idx)) != -1) {
                occurrences++;
                idx += expected.length();
            }
            assertEquals(threadCount, occurrences);
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

    private static void assertFalse(boolean condition) {
        if (condition) {
            throw new AssertionError("Expected false but got true");
        }
    }
}
