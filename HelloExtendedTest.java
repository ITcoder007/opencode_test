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
        System.out.println("=== HelloExtendedTest - Additional Unit Tests ===\n");

        System.out.println("--- Class Integrity Tests ---");
        testClassNotAbstract();
        testClassNotInterface();
        testClassNotEnum();
        testClassHasDefaultConstructor();
        testClassCanBeInstantiated();
        testNoExtraPublicMethods();

        System.out.println("\n--- Output Stream Safety Tests ---");
        testNoStderrOutput();
        testSystemOutRestoredAfterCall();
        testNoStderrWithNullArgs();
        testNoStderrWithNonEmptyArgs();

        System.out.println("\n--- Output Content Edge Cases ---");
        testOutputCharCount();
        testAllPrintableASCII();
        testNoTabCharacters();
        testNoBOMInOutput();
        testNoBlankLinesInOutput();
        testEmptyArgsVsNoArgsOutput();
        testOutputIsUTF8Decodable();
        testNoCarriageReturnAlone();
        testOutputDoesNotContainDigits();
        testPunctuationInOutput();

        System.out.println("\n--- Execution Behavior Tests ---");
        testExecutionTimeWithinLimit();
        testMultipleRapidExecutions();
        testMainThreadDoesNotThrow();
        testLargeArgsArrayIgnored();

        System.out.println("\n--- Conversion Fidelity Tests ---");
        testExactPythonParity();
        testNoExtraSemicolonsInOutput();
        testNoJavaPackageName();

        System.out.println("\n=== Extended Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL EXTENDED TESTS PASSED");
        } else {
            System.out.println("Result: SOME EXTENDED TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testClassNotAbstract() {
        assertTest("Hello class is not abstract", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isAbstract(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassNotInterface() {
        assertTest("Hello class is not an interface", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isInterface(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassNotEnum() {
        assertTest("Hello class is not an enum", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(clazz.isEnum());
        }, ClassNotFoundException.class);
    }

    private static void testClassHasDefaultConstructor() {
        assertTest("Hello has a default (no-arg) constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?> ctor = clazz.getDeclaredConstructor();
            assertNotNull(ctor);
        }, Exception.class);
    }

    private static void testClassCanBeInstantiated() {
        assertTest("Hello can be instantiated with new Hello()", () -> {
            Class<?> clazz = Class.forName("Hello");
            Object instance = clazz.getDeclaredConstructor().newInstance();
            assertNotNull(instance);
            assertEquals("Hello", instance.getClass().getSimpleName());
        }, Exception.class);
    }

    private static void testNoExtraPublicMethods() {
        assertTest("Hello has exactly one public method (main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] publicMethods = clazz.getDeclaredMethods();
            int publicCount = 0;
            for (Method m : publicMethods) {
                if (Modifier.isPublic(m.getModifiers())) {
                    publicCount++;
                }
            }
            assertEquals(1, publicCount);
        }, ClassNotFoundException.class);
    }

    private static void testNoStderrOutput() {
        assertTest("No output to stderr during normal execution", () -> {
            PrintStream originalErr = System.err;
            ByteArrayOutputStream errBaos = new ByteArrayOutputStream();
            PrintStream captureErr = new PrintStream(errBaos);
            System.setErr(captureErr);
            try {
                captureMainOutput();
            } finally {
                System.setErr(originalErr);
            }
            assertEquals("", errBaos.toString());
        });
    }

    private static void testSystemOutRestoredAfterCall() {
        assertTest("System.out is unchanged after calling main()", () -> {
            PrintStream original = System.out;
            captureMainOutput();
            assertSame(original, System.out);
        });
    }

    private static void testNoStderrWithNullArgs() {
        assertTest("No stderr output when main called with null args", () -> {
            PrintStream originalErr = System.err;
            ByteArrayOutputStream errBaos = new ByteArrayOutputStream();
            System.setErr(new PrintStream(errBaos));
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            try {
                Hello.main(null);
            } finally {
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
            assertEquals("", errBaos.toString());
        });
    }

    private static void testNoStderrWithNonEmptyArgs() {
        assertTest("No stderr output when main called with non-empty args", () -> {
            PrintStream originalErr = System.err;
            ByteArrayOutputStream errBaos = new ByteArrayOutputStream();
            System.setErr(new PrintStream(errBaos));
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            try {
                Hello.main(new String[]{"test"});
            } finally {
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
            assertEquals("", errBaos.toString());
        });
    }

    private static void testOutputCharCount() {
        assertTest("Output content has exactly 13 characters ('Hello, World!')", () -> {
            String output = captureMainOutput().trim();
            assertEquals(13, output.length());
        });
    }

    private static void testAllPrintableASCII() {
        assertTest("All characters in output are printable ASCII (32-126)", () -> {
            String output = captureMainOutput().trim();
            for (char c : output.toCharArray()) {
                assertTrue(c >= 32 && c <= 126);
            }
        });
    }

    private static void testNoTabCharacters() {
        assertTest("Output contains no tab characters", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("\t"));
        });
    }

    private static void testNoBOMInOutput() {
        assertTest("Output does not start with UTF-8 BOM", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            assertFalse(bytes.length >= 3
                && bytes[0] == (byte) 0xEF
                && bytes[1] == (byte) 0xBB
                && bytes[2] == (byte) 0xBF);
        });
    }

    private static void testNoBlankLinesInOutput() {
        assertTest("Output contains no blank lines", () -> {
            String output = captureMainOutput();
            String[] lines = output.split(System.lineSeparator());
            for (String line : lines) {
                assertFalse(line.trim().isEmpty());
            }
        });
    }

    private static void testEmptyArgsVsNoArgsOutput() {
        assertTest("Output with empty String[] matches output with no args", () -> {
            String withEmpty = captureMainOutputWithArgs(new String[]{});
            String withNull;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream original = System.out;
            System.setOut(new PrintStream(baos));
            try {
                Hello.main(null);
            } finally {
                System.setOut(original);
            }
            withNull = baos.toString();
            assertEquals(withEmpty, withNull);
        });
    }

    private static void testOutputIsUTF8Decodable() {
        assertTest("Output is valid UTF-8 decodable without errors", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            String redecoded = new String(bytes, StandardCharsets.UTF_8);
            assertEquals(output, redecoded);
        });
    }

    private static void testNoCarriageReturnAlone() {
        assertTest("Output does not contain lone carriage return (\\r)", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("\r"));
        });
    }

    private static void testOutputDoesNotContainDigits() {
        assertTest("'Hello, World!' contains no digits", () -> {
            String output = captureMainOutput().trim();
            for (char c : output.toCharArray()) {
                assertFalse(Character.isDigit(c));
            }
        });
    }

    private static void testPunctuationInOutput() {
        assertTest("Output contains expected punctuation (comma and exclamation)", () -> {
            String output = captureMainOutput().trim();
            assertTrue(output.contains(","));
            assertTrue(output.contains("!"));
        });
    }

    private static void testExecutionTimeWithinLimit() {
        assertTest("main() completes within 1 second", () -> {
            long start = System.nanoTime();
            captureMainOutput();
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            assertTrue(elapsed < 1000);
        });
    }

    private static void testMultipleRapidExecutions() {
        assertTest("10 rapid consecutive executions all produce correct output", () -> {
            for (int i = 0; i < 10; i++) {
                String output = captureMainOutput().trim();
                assertEquals("Hello, World!", output);
            }
        });
    }

    private static void testMainThreadDoesNotThrow() {
        assertTest("main() does not throw any exception", () -> {
            try {
                captureMainOutput();
            } catch (Throwable t) {
                throw new AssertionError("main() threw: " + t.getClass().getSimpleName() + ": " + t.getMessage());
            }
        });
    }

    private static void testLargeArgsArrayIgnored() {
        assertTest("main() ignores large args array (100 elements)", () -> {
            String[] bigArgs = new String[100];
            for (int i = 0; i < 100; i++) {
                bigArgs[i] = "arg" + i;
            }
            String output = captureMainOutputWithArgs(bigArgs).trim();
            assertEquals("Hello, World!", output);
        });
    }

    private static void testExactPythonParity() {
        assertTest("Java output byte-for-byte identical to Python print('Hello, World!')", () -> {
            String javaOutput = captureMainOutput();
            String pythonExpected = "Hello, World!" + System.lineSeparator();
            byte[] javaBytes = javaOutput.getBytes(StandardCharsets.UTF_8);
            byte[] pythonBytes = pythonExpected.getBytes(StandardCharsets.UTF_8);
            assertEquals(pythonBytes.length, javaBytes.length);
            for (int i = 0; i < javaBytes.length; i++) {
                assertEquals(pythonBytes[i], javaBytes[i]);
            }
        });
    }

    private static void testNoExtraSemicolonsInOutput() {
        assertTest("Output does not contain semicolons", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains(";"));
        });
    }

    private static void testNoJavaPackageName() {
        assertTest("Hello class has no package declaration (default package)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Package pkg = clazz.getPackage();
            assertTrue(pkg == null || pkg.getName().isEmpty());
        }, ClassNotFoundException.class);
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

    private static void assertEquals(byte expected, byte actual) {
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

    private static void assertSame(Object expected, Object actual) {
        if (expected != actual) {
            throw new AssertionError("Expected same reference but got different objects");
        }
    }
}
