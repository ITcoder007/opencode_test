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
        System.out.println("=== HelloExtendedTest - Supplementary Unit Tests ===\n");

        System.out.println("--- Stderr & Output Integrity Tests ---");
        testStderrIsEmpty();
        testOutputByteLength();
        testOutputIsPrintableASCII();

        System.out.println("\n--- Class Attribute Tests ---");
        testClassIsNotAbstract();
        testClassIsNotInterface();
        testClassIsNotFinal();
        testClassHasDefaultConstructor();
        testNoExtraPublicMethods();

        System.out.println("\n--- Stability & Conformance Tests ---");
        testMultipleRunsStability();
        testOutputEncodingIsUTF8Compatible();
        testOutputDoesNotContainTab();
        testOutputDoesNotContainCarriageReturn();
        testMainDoesNotThrowException();
        testClassHasOnlyOneDeclaredMethod();

        System.out.println("\n--- Python Parity Tests ---");
        testOutputLengthMatchesPythonPrintln();
        testOutputContainsSubstringHello();
        testOutputContainsSubstringWorld();

        System.out.println("\n=== Extended Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL EXTENDED TESTS PASSED");
        } else {
            System.out.println("Result: SOME EXTENDED TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testStderrIsEmpty() {
        assertTest("stderr is empty after main() runs", () -> {
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;
            ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
            ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baosOut));
            System.setErr(new PrintStream(baosErr));
            try {
                Hello.main(new String[]{});
            } finally {
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
            assertEquals("", baosErr.toString());
        });
    }

    private static void testOutputByteLength() {
        assertTest("output byte length is exactly 'Hello, World!' + newline", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            int expectedLen = "Hello, World!".getBytes(StandardCharsets.UTF_8).length
                    + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;
            assertEquals(expectedLen, bytes.length);
        });
    }

    private static void testOutputIsPrintableASCII() {
        assertTest("output contains only printable ASCII characters and newline", () -> {
            String output = captureMainOutput();
            for (char c : output.toCharArray()) {
                boolean printable = (c >= 32 && c <= 126) || c == '\n' || c == '\r';
                assertTrue(printable);
            }
        });
    }

    private static void testClassIsNotAbstract() {
        assertTest("Hello class is not abstract", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isAbstract(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassIsNotInterface() {
        assertTest("Hello class is not an interface", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isInterface(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassIsNotFinal() {
        assertTest("Hello class is not final (matches Python openness)", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isFinal(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassHasDefaultConstructor() {
        assertTest("Hello has a public no-arg constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?>[] constructors = clazz.getConstructors();
            boolean hasDefault = false;
            for (Constructor<?> c : constructors) {
                if (c.getParameterCount() == 0) {
                    hasDefault = true;
                    break;
                }
            }
            assertTrue(hasDefault);
        }, ClassNotFoundException.class);
    }

    private static void testNoExtraPublicMethods() {
        assertTest("Hello has exactly one declared public method (main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            int publicMethodCount = 0;
            for (Method m : methods) {
                if (Modifier.isPublic(m.getModifiers())) {
                    publicMethodCount++;
                }
            }
            assertEquals(1, publicMethodCount);
        }, ClassNotFoundException.class);
    }

    private static void testMultipleRunsStability() {
        assertTest("running main() 100 times produces identical output each time", () -> {
            String first = captureMainOutput();
            for (int i = 0; i < 99; i++) {
                String current = captureMainOutput();
                assertEquals(first, current);
            }
        });
    }

    private static void testOutputEncodingIsUTF8Compatible() {
        assertTest("output round-trips through UTF-8 without corruption", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            String restored = new String(bytes, StandardCharsets.UTF_8);
            assertEquals(output, restored);
        });
    }

    private static void testOutputDoesNotContainTab() {
        assertTest("output does not contain tab characters", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("\t"));
        });
    }

    private static void testOutputDoesNotContainCarriageReturn() {
        assertTest("output does not contain standalone carriage return", () -> {
            String output = captureMainOutput();
            String lineSep = System.lineSeparator();
            String withoutLineSep = output.replace(lineSep, "");
            assertFalse(withoutLineSep.contains("\r"));
        });
    }

    private static void testMainDoesNotThrowException() {
        assertTest("main() does not throw any exception", () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(baos));
            try {
                Hello.main(new String[]{});
            } catch (Throwable t) {
                throw new AssertionError("main() threw " + t.getClass().getSimpleName() + ": " + t.getMessage());
            } finally {
                System.setOut(originalOut);
            }
        });
    }

    private static void testClassHasOnlyOneDeclaredMethod() {
        assertTest("Hello class declares exactly one method total", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            assertEquals(1, methods.length);
        }, ClassNotFoundException.class);
    }

    private static void testOutputLengthMatchesPythonPrintln() {
        assertTest("output content length matches 'Hello, World!' (13 chars)", () -> {
            String output = captureMainOutput();
            String content = output.trim();
            assertEquals(13, content.length());
        });
    }

    private static void testOutputContainsSubstringHello() {
        assertTest("output contains substring 'Hello'", () -> {
            String output = captureMainOutput();
            assertTrue(output.contains("Hello"));
        });
    }

    private static void testOutputContainsSubstringWorld() {
        assertTest("output contains substring 'World'", () -> {
            String output = captureMainOutput();
            assertTrue(output.contains("World"));
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
