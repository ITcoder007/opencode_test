import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloTestExtended {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloTestExtended - Extended Unit Tests for Hello.java ===\n");

        System.out.println("--- Python Equivalence Tests ---");
        testPythonOutputEquivalence();
        testOutputByteEqualityWithPython();

        System.out.println("\n--- Class Structure Completeness Tests ---");
        testNoExtraPublicMethods();
        testNoDeclaredFields();
        testMainNoCheckedException();
        testClassHasOnlyOneMethod();
        testClassCanBeInstantiated();

        System.out.println("\n--- Encoding & Character Tests ---");
        testOutputIsUTF8Compatible();
        testCommaCharacterCorrect();
        testExclamationCharacterCorrect();
        testOutputLengthExact();
        testOutputContainsNoNonASCII();

        System.out.println("\n--- Concurrency & Edge Case Tests ---");
        testConcurrentMainCalls();
        testMainWithEmptyStringArray();
        testMainWithLargeArgsArray();
        testMainCalledManyTimesRapidly();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL TESTS PASSED");
        } else {
            System.out.println("Result: SOME TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testPythonOutputEquivalence() {
        assertTest("Python output equals Java output", () -> {
            String javaOutput = captureMainOutput().trim();
            ProcessBuilder pb = new ProcessBuilder("python3", "hello.py");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            String pyOutput = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            p.waitFor(10, TimeUnit.SECONDS);
            assertEquals(pyOutput, javaOutput);
        });
    }

    private static void testOutputByteEqualityWithPython() {
        assertTest("Java output bytes match Python output bytes", () -> {
            String javaOutput = captureMainOutput().trim();
            ProcessBuilder pb = new ProcessBuilder("python3", "hello.py");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            String pyOutput = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            p.waitFor(10, TimeUnit.SECONDS);
            assertArrayEquals(pyOutput.getBytes(StandardCharsets.UTF_8), javaOutput.getBytes(StandardCharsets.UTF_8));
        });
    }

    private static void testNoExtraPublicMethods() {
        assertTest("Hello has no public methods beyond main", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            List<String> publicMethods = new ArrayList<>();
            for (Method m : methods) {
                if (Modifier.isPublic(m.getModifiers())) {
                    publicMethods.add(m.getName());
                }
            }
            assertEquals(1, publicMethods.size());
            assertEquals("main", publicMethods.get(0));
        }, ClassNotFoundException.class);
    }

    private static void testNoDeclaredFields() {
        assertTest("Hello class has no declared fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            Field[] fields = clazz.getDeclaredFields();
            assertEquals(0, fields.length);
        }, ClassNotFoundException.class);
    }

    private static void testMainNoCheckedException() {
        assertTest("main method declares no checked exceptions", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            Class<?>[] exceptions = main.getExceptionTypes();
            assertEquals(0, exceptions.length);
        }, ClassNotFoundException.class);
    }

    private static void testClassHasOnlyOneMethod() {
        assertTest("Hello class has exactly one declared method", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            assertEquals(1, methods.length);
        }, ClassNotFoundException.class);
    }

    private static void testClassCanBeInstantiated() {
        assertTest("Hello class can be instantiated via reflection", () -> {
            Class<?> clazz = Class.forName("Hello");
            Object instance = clazz.getDeclaredConstructor().newInstance();
            assertNotNull(instance);
            assertEquals("Hello", instance.getClass().getSimpleName());
        }, ClassNotFoundException.class);
    }

    private static void testOutputIsUTF8Compatible() {
        assertTest("Output is valid UTF-8", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            String reconstructed = new String(bytes, StandardCharsets.UTF_8);
            assertEquals(output, reconstructed);
        });
    }

    private static void testCommaCharacterCorrect() {
        assertTest("Output contains ASCII comma at position 5", () -> {
            String output = captureMainOutput().trim();
            assertTrue(output.length() > 5);
            assertEquals(',', output.charAt(5));
        });
    }

    private static void testExclamationCharacterCorrect() {
        assertTest("Output contains ASCII exclamation at last position", () -> {
            String output = captureMainOutput().trim();
            assertTrue(output.length() > 0);
            assertEquals('!', output.charAt(output.length() - 1));
        });
    }

    private static void testOutputLengthExact() {
        assertTest("Trimmed output length is exactly 13 characters", () -> {
            String output = captureMainOutput().trim();
            assertEquals(13, output.length());
        });
    }

    private static void testOutputContainsNoNonASCII() {
        assertTest("Output contains only ASCII characters", () -> {
            String output = captureMainOutput().trim();
            for (int i = 0; i < output.length(); i++) {
                assertTrue(output.charAt(i) < 128);
            }
        });
    }

    private static void testConcurrentMainCalls() {
        assertTest("Concurrent main() calls do not crash (sequential capture)", () -> {
            int iterations = 50;
            for (int i = 0; i < iterations; i++) {
                String output = captureMainOutput().trim();
                assertEquals("Hello, World!", output);
            }
        });
    }

    private static void testMainWithEmptyStringArray() {
        assertTest("main() works with new String[]{''} (empty string arg)", () -> {
            String output = captureMainOutputWithArgs(new String[]{""});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMainWithLargeArgsArray() {
        assertTest("main() works with large args array (1000 elements)", () -> {
            String[] largeArgs = new String[1000];
            for (int i = 0; i < 1000; i++) {
                largeArgs[i] = "arg" + i;
            }
            String output = captureMainOutputWithArgs(largeArgs);
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMainCalledManyTimesRapidly() {
        assertTest("main() called 100 times produces consistent output", () -> {
            String expected = "Hello, World!";
            for (int i = 0; i < 100; i++) {
                String output = captureMainOutput().trim();
                assertEquals(expected, output);
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

    private static void assertEquals(char expected, char actual) {
        if (expected != actual) {
            throw new AssertionError("Expected <" + expected + "> but got <" + actual + ">");
        }
    }

    private static void assertArrayEquals(byte[] expected, byte[] actual) {
        if (expected.length != actual.length) {
            throw new AssertionError("Array length mismatch: expected " + expected.length + " but got " + actual.length);
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                throw new AssertionError("Array differ at index " + i + ": expected " + expected[i] + " but got " + actual[i]);
            }
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
