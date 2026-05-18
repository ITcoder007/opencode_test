import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

public class HelloExtendedTest {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloExtendedTest - Additional Boundary & Edge Case Tests ===\n");

        System.out.println("--- Reflection & API Contract Tests ---");
        testClassHasExactlyOneDeclaredMethod();
        testNoAdditionalPublicMethodsBeyondMain();
        testClassHasDefaultConstructor();
        testClassIsNotFinal();
        testClassIsNotAbstract();
        testClassHasNoFields();
        testMainReturnTypeIsVoid();
        testClassHasNoInterfaces();
        testClassDirectSuperclassIsObject();
        testClassHasNoInnerClasses();

        System.out.println("\n--- Output Encoding & Format Tests ---");
        testOutputIsUTF8Decodable();
        testOutputContainsExactlyOneExclamationMark();
        testOutputContainsComma();
        testOutputContainsSpaceAfterComma();
        testOutputLengthIsCorrect();
        testOutputNoCarriageReturn();
        testOutputContainsWorld();
        testOutputFirstCharacterIsH();

        System.out.println("\n--- Repeated Invocation & Thread Safety Tests ---");
        testRepeatedInvocation100Times();
        try { testConcurrentInvocation(); } catch (InterruptedException e) { failed++; total++; System.out.println("  FAIL: Concurrent test interrupted"); }

        System.out.println("\n--- Argument Robustness Tests ---");
        testMainWithEmptyStringArray();
        testMainWithSingleEmptyArg();
        testMainWithLargeArgsArray();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL TESTS PASSED");
        } else {
            System.out.println("Result: SOME TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testClassHasExactlyOneDeclaredMethod() {
        assertTest("Hello class has exactly one declared method", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(1, clazz.getDeclaredMethods().length);
        });
    }

    private static void testNoAdditionalPublicMethodsBeyondMain() {
        assertTest("Only main is a public method (no extra public methods)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] publics = clazz.getMethods();
            int helloMethods = 0;
            for (Method m : publics) {
                if (m.getDeclaringClass() == clazz) {
                    helloMethods++;
                }
            }
            assertEquals(1, helloMethods);
        });
    }

    private static void testClassHasDefaultConstructor() {
        assertTest("Hello class has a default (no-arg) constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?>[] ctors = clazz.getDeclaredConstructors();
            boolean hasDefault = false;
            for (Constructor<?> c : ctors) {
                if (c.getParameterCount() == 0) {
                    hasDefault = true;
                }
            }
            assertTrue(hasDefault || ctors.length == 0);
        });
    }

    private static void testClassIsNotFinal() {
        assertTest("Hello class is not final (can be extended)", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isFinal(clazz.getModifiers()));
        });
    }

    private static void testClassIsNotAbstract() {
        assertTest("Hello class is not abstract (can be instantiated)", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isAbstract(clazz.getModifiers()));
        });
    }

    private static void testClassHasNoFields() {
        assertTest("Hello class has no declared fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getDeclaredFields().length);
        });
    }

    private static void testMainReturnTypeIsVoid() {
        assertTest("main method return type is void (not Void or other)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            assertEquals(void.class, main.getReturnType());
        });
    }

    private static void testClassHasNoInterfaces() {
        assertTest("Hello class implements no interfaces", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getInterfaces().length);
        });
    }

    private static void testClassDirectSuperclassIsObject() {
        assertTest("Hello class extends Object directly", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(Object.class, clazz.getSuperclass());
        });
    }

    private static void testClassHasNoInnerClasses() {
        assertTest("Hello class has no inner or member classes", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getDeclaredClasses().length);
        });
    }

    private static void testOutputIsUTF8Decodable() {
        assertTest("Output is valid UTF-8 decodable", () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(baos, true, StandardCharsets.UTF_8));
            try {
                Hello.main(new String[]{});
            } finally {
                System.setOut(originalOut);
            }
            String output = baos.toString(StandardCharsets.UTF_8);
            assertNotNull(output);
            assertTrue(output.length() > 0);
        });
    }

    private static void testOutputContainsExactlyOneExclamationMark() {
        assertTest("Output contains exactly one exclamation mark", () -> {
            String output = captureMainOutput();
            String trimmed = output.trim();
            int count = 0;
            for (char c : trimmed.toCharArray()) {
                if (c == '!') count++;
            }
            assertEquals(1, count);
        });
    }

    private static void testOutputContainsComma() {
        assertTest("Output contains a comma after Hello", () -> {
            String output = captureMainOutput();
            assertTrue(output.contains(","));
        });
    }

    private static void testOutputContainsSpaceAfterComma() {
        assertTest("Output contains a space after comma (Hello, World!)", () -> {
            String output = captureMainOutput();
            assertTrue(output.contains(", "));
        });
    }

    private static void testOutputLengthIsCorrect() {
        assertTest("Trimmed output length is 13 characters", () -> {
            String output = captureMainOutput().trim();
            assertEquals(13, output.length());
        });
    }

    private static void testOutputNoCarriageReturn() {
        assertTest("Output contains no carriage return characters", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("\r"));
        });
    }

    private static void testOutputContainsWorld() {
        assertTest("Output contains 'World'", () -> {
            String output = captureMainOutput();
            assertTrue(output.contains("World"));
        });
    }

    private static void testOutputFirstCharacterIsH() {
        assertTest("First character of output is 'H'", () -> {
            String output = captureMainOutput().trim();
            assertTrue(output.length() > 0);
            assertEquals('H', output.charAt(0));
        });
    }

    private static void testRepeatedInvocation100Times() {
        assertTest("Repeated invocation 100 times produces identical output", () -> {
            String first = captureMainOutput();
            for (int i = 0; i < 100; i++) {
                String current = captureMainOutput();
                if (!first.equals(current)) {
                    throw new AssertionError("Output differed at iteration " + i);
                }
            }
        });
    }

    private static void testConcurrentInvocation() throws InterruptedException {
        assertTest("Concurrent invocation from 10 threads produces correct output", () -> {
            Thread[] threads = new Thread[10];
            String[] results = new String[10];
            boolean[] errors = new boolean[10];

            for (int i = 0; i < 10; i++) {
                final int idx = i;
                threads[i] = new Thread(() -> {
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        PrintStream ps = new PrintStream(baos, true);
                        PrintStream originalOut = System.out;
                        synchronized (System.out) {
                            System.setOut(ps);
                            try {
                                Hello.main(new String[]{});
                            } finally {
                                System.setOut(originalOut);
                            }
                        }
                        results[idx] = baos.toString().trim();
                    } catch (Exception e) {
                        errors[idx] = true;
                    }
                });
            }

            for (Thread t : threads) t.start();
            for (Thread t : threads) t.join(5000);

            for (int i = 0; i < 10; i++) {
                assertFalse(errors[i]);
                assertEquals("Hello, World!", results[i]);
            }
        });
    }

    private static void testMainWithEmptyStringArray() {
        assertTest("main() with empty String array produces correct output", () -> {
            String output = captureMainOutputWithArgs(new String[]{});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMainWithSingleEmptyArg() {
        assertTest("main() with single empty string arg produces correct output", () -> {
            String output = captureMainOutputWithArgs(new String[]{""});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMainWithLargeArgsArray() {
        assertTest("main() with 1000-element args array produces correct output", () -> {
            String[] largeArgs = new String[1000];
            for (int i = 0; i < 1000; i++) {
                largeArgs[i] = "arg" + i;
            }
            String output = captureMainOutputWithArgs(largeArgs);
            assertEquals("Hello, World!", output.trim());
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
