import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;

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

        System.out.println("\n--- Extended Structural Tests ---");
        testClassHasDefaultConstructor();
        testClassHasNoDeclaredFields();
        testClassHasOnlyMainMethod();
        testMainMethodReturnType();

        System.out.println("\n--- Extended Output Tests ---");
        testOutputByteLength();
        testOutputCharsetIsUTF8();
        testOutputContainsHelloWorld();
        testOutputCharacterByCharacter();
        testOutputHasNoTabs();
        testOutputHasNoCarriageReturn();
        testOutputIsNotNullOrEmpty();
        testMultipleSequentialInvocations();
        testStderrRemainsEmpty();
        testOutputMatchesPythonExactly();

        System.out.println("\n--- Thread Safety Tests ---");
        testConcurrentInvocations();

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

    private static void testClassHasDefaultConstructor() {
        assertTest("Hello class has a default (no-arg) constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            boolean hasDefault = false;
            for (Constructor<?> c : constructors) {
                if (c.getParameterCount() == 0) {
                    hasDefault = true;
                    break;
                }
            }
            assertTrue(hasDefault || constructors.length == 0);
        }, ClassNotFoundException.class);
    }

    private static void testClassHasNoDeclaredFields() {
        assertTest("Hello class has no declared instance fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            Field[] fields = clazz.getDeclaredFields();
            assertEquals(0, fields.length);
        }, ClassNotFoundException.class);
    }

    private static void testClassHasOnlyMainMethod() {
        assertTest("Hello class has only the main method (no extra public methods)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            assertEquals(1, methods.length);
            assertEquals("main", methods[0].getName());
        }, ClassNotFoundException.class);
    }

    private static void testMainMethodReturnType() {
        assertTest("main method returns void (not a value-producing method)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            assertEquals(void.class, main.getReturnType());
        }, NoSuchMethodException.class);
    }

    private static void testOutputByteLength() {
        assertTest("Output byte length matches 'Hello, World!' + newline", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            int expectedLen = "Hello, World!".getBytes(StandardCharsets.UTF_8).length
                    + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;
            assertEquals(expectedLen, bytes.length);
        });
    }

    private static void testOutputCharsetIsUTF8() {
        assertTest("Output is valid UTF-8 encoded", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            String reconstructed = new String(bytes, StandardCharsets.UTF_8);
            assertEquals(output, reconstructed);
        });
    }

    private static void testOutputContainsHelloWorld() {
        assertTest("Output contains substring 'Hello, World!'", () -> {
            String output = captureMainOutput();
            assertTrue(output.contains("Hello, World!"));
        });
    }

    private static void testOutputCharacterByCharacter() {
        assertTest("Output content matches 'Hello, World!' character by character", () -> {
            String output = captureMainOutput().replace(System.lineSeparator(), "");
            String expected = "Hello, World!";
            assertEquals(expected.length(), output.length());
            for (int i = 0; i < expected.length(); i++) {
                assertEquals(expected.charAt(i), output.charAt(i));
            }
        });
    }

    private static void testOutputHasNoTabs() {
        assertTest("Output contains no tab characters", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("\t"));
        });
    }

    private static void testOutputHasNoCarriageReturn() {
        assertTest("Output content has no standalone carriage return characters", () -> {
            String output = captureMainOutput();
            String content = output.replace(System.lineSeparator(), "");
            assertFalse(content.contains("\r"));
        });
    }

    private static void testOutputIsNotNullOrEmpty() {
        assertTest("Output is not null and not empty", () -> {
            String output = captureMainOutput();
            assertNotNull(output);
            assertTrue(output.length() > 0);
        });
    }

    private static void testMultipleSequentialInvocations() {
        assertTest("Running main() 10 times produces identical output each time", () -> {
            String first = captureMainOutput();
            for (int i = 0; i < 9; i++) {
                String subsequent = captureMainOutput();
                assertEquals(first, subsequent);
            }
        });
    }

    private static void testStderrRemainsEmpty() {
        assertTest("main() produces no output on stderr", () -> {
            PrintStream originalErr = System.err;
            ByteArrayOutputStream errBaos = new ByteArrayOutputStream();
            PrintStream errCapture = new PrintStream(errBaos);
            PrintStream originalOut = System.out;
            ByteArrayOutputStream outBaos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outBaos));
            System.setErr(errCapture);
            try {
                Hello.main(new String[]{});
            } finally {
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
            String stderr = errBaos.toString();
            assertEquals(0, stderr.length());
        });
    }

    private static void testOutputMatchesPythonExactly() {
        assertTest("Java output exactly matches Python print output (byte-level)", () -> {
            String javaOutput = captureMainOutput();
            String pythonExpected = "Hello, World!" + System.lineSeparator();
            assertEquals(pythonExpected, javaOutput);
        });
    }

    private static void testConcurrentInvocations() {
        assertTest("Concurrent invocations from multiple threads all produce correct output", () -> {
            int threadCount = 5;
            Thread[] threads = new Thread[threadCount];
            String[] results = new String[threadCount];
            boolean[] errors = new boolean[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        PrintStream originalOut = System.out;
                        System.setOut(new PrintStream(baos));
                        try {
                            Hello.main(new String[]{});
                        } finally {
                            System.setOut(originalOut);
                        }
                        results[index] = baos.toString().trim();
                    } catch (Exception e) {
                        errors[index] = true;
                    }
                });
            }

            for (Thread t : threads) t.start();
            for (Thread t : threads) t.join(5000);

            for (int i = 0; i < threadCount; i++) {
                assertFalse(errors[i]);
                assertEquals("Hello, World!", results[i]);
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

    private static void assertFalse(boolean condition) {
        if (condition) {
            throw new AssertionError("Expected false but got true");
        }
    }
}
