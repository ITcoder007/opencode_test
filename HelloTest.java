import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

        System.out.println("\n--- Extended Coverage Tests ---");
        testClassNotAbstract();
        testClassNotFinal();
        testClassExtendsObject();
        testOnlyOnePublicMethod();
        testOutputByteLength();
        testOutputIsUTF8();
        testSystemOutRestored();
        testMainReturnsNormally();
        testConcurrentExecution();

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

    private static void testClassNotAbstract() {
        assertTest("Hello class is not abstract", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isAbstract(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassNotFinal() {
        assertTest("Hello class is not final", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isFinal(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassExtendsObject() {
        assertTest("Hello class extends Object directly", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(Object.class, clazz.getSuperclass());
        }, ClassNotFoundException.class);
    }

    private static void testOnlyOnePublicMethod() {
        assertTest("Hello has exactly one declared public static method (main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            int publicStaticCount = 0;
            for (Method m : methods) {
                int mod = m.getModifiers();
                if (Modifier.isPublic(mod) && Modifier.isStatic(mod)) {
                    publicStaticCount++;
                }
            }
            assertEquals(1, publicStaticCount);
        }, ClassNotFoundException.class);
    }

    private static void testOutputByteLength() {
        assertTest("Output byte length matches expected ASCII content", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            int expectedLen = "Hello, World!".getBytes(StandardCharsets.UTF_8).length
                    + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;
            assertEquals(expectedLen, bytes.length);
        });
    }

    private static void testOutputIsUTF8() {
        assertTest("Output is valid UTF-8 and contains only ASCII characters", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            for (byte b : bytes) {
                assertTrue((b & 0xFF) <= 127);
            }
        });
    }

    private static void testSystemOutRestored() {
        assertTest("System.out is properly restored after captureMainOutput", () -> {
            PrintStream before = System.out;
            captureMainOutput();
            PrintStream after = System.out;
            assertTrue(before == after);
        });
    }

    private static void testMainReturnsNormally() {
        assertTest("main() returns normally without calling System.exit", () -> {
            boolean[] completed = {false};
            Thread t = new Thread(() -> {
                Hello.main(new String[]{});
                completed[0] = true;
            });
            t.start();
            t.join(3000);
            assertTrue(completed[0]);
        });
    }

    private static void testConcurrentExecution() {
        assertTest("Concurrent execution of main() produces correct output", () -> {
            int threadCount = 5;
            String[] results = new String[threadCount];
            Thread[] threads = new Thread[threadCount];
            for (int i = 0; i < threadCount; i++) {
                final int idx = i;
                threads[i] = new Thread(() -> {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    synchronized (System.out) {
                        PrintStream originalOut = System.out;
                        System.setOut(new PrintStream(baos));
                        try {
                            Hello.main(new String[]{});
                        } finally {
                            System.setOut(originalOut);
                        }
                    }
                    results[idx] = baos.toString().trim();
                });
            }
            for (Thread t : threads) t.start();
            for (Thread t : threads) t.join(3000);
            for (int i = 0; i < threadCount; i++) {
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
