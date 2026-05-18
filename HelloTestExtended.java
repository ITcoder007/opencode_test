import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;

public class HelloTestExtended {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloTestExtended - Extended Unit Tests ===\n");

        System.out.println("--- Reflection & Structural Tests ---");
        testReflectiveInvocation();
        testClassHasDefaultConstructor();
        testClassHasExactlyOneDeclaredMethod();
        testMainReturnTypeIsVoid();

        System.out.println("\n--- Byte-Level & Encoding Tests ---");
        testOutputByteContent();
        testOutputIsUTF8Compatible();
        testOutputCharacterCount();
        testOutputContainsNoNullBytes();

        System.out.println("\n--- Concurrency & Repeated Execution Tests ---");
        testConcurrentExecution();
        testRapidSequentialExecution();
        testMainDoesNotCallSystemExit();
        testLargeNumberOfInvocations();

        System.out.println("\n--- Edge Case Tests ---");
        testMainWithEmptyStringArray();
        testMainWithSingleEmptyString();
        testMainDoesNotModifySystemErr();
        testOutputDoesNotContainTab();
        testOutputDoesNotContainCarriageReturnAlone();

        System.out.println("\n=== Extended Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL EXTENDED TESTS PASSED");
        } else {
            System.out.println("Result: SOME EXTENDED TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testReflectiveInvocation() {
        assertTest("main can be invoked via reflection", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream original = System.out;
            System.setOut(new PrintStream(baos));
            try {
                main.invoke(null, (Object) new String[]{});
            } finally {
                System.setOut(original);
            }
            assertEquals("Hello, World!", baos.toString().trim());
        });
    }

    private static void testClassHasDefaultConstructor() {
        assertTest("Hello has a public default constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?>[] constructors = clazz.getConstructors();
            assertTrue(constructors.length >= 1);
            Constructor<?> defaultCtor = clazz.getConstructor();
            assertNotNull(defaultCtor);
        });
    }

    private static void testClassHasExactlyOneDeclaredMethod() {
        assertTest("Hello declares exactly one method (main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            assertEquals(1, methods.length);
            assertEquals("main", methods[0].getName());
        });
    }

    private static void testMainReturnTypeIsVoid() {
        assertTest("main method returns void (confirmed via reflection)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            assertEquals(void.class, main.getReturnType());
        });
    }

    private static void testOutputByteContent() {
        assertTest("Output byte content matches expected ASCII bytes", () -> {
            String output = captureMainOutput();
            byte[] expected = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);
            byte[] actual = output.getBytes(StandardCharsets.UTF_8);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < expected.length; i++) {
                assertEquals((int) expected[i], (int) actual[i]);
            }
        });
    }

    private static void testOutputIsUTF8Compatible() {
        assertTest("Output is valid UTF-8 and pure ASCII", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            for (byte b : bytes) {
                assertTrue((b & 0xFF) <= 127);
            }
        });
    }

    private static void testOutputCharacterCount() {
        assertTest("Output has exactly 14 characters including newline", () -> {
            String output = captureMainOutput();
            assertEquals(14, output.length());
        });
    }

    private static void testOutputContainsNoNullBytes() {
        assertTest("Output contains no null (0x00) bytes", () -> {
            String output = captureMainOutput();
            for (int i = 0; i < output.length(); i++) {
                assertTrue(output.charAt(i) != '\0');
            }
        });
    }

    private static void testConcurrentExecution() {
        assertTest("Concurrent main() invocations complete without exceptions", () -> {
            final int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            boolean[] errors = new boolean[threadCount];

            PrintStream originalOut = System.out;
            PrintStream devNull = new PrintStream(new ByteArrayOutputStream());
            System.setOut(devNull);
            try {
                for (int i = 0; i < threadCount; i++) {
                    final int idx = i;
                    threads[i] = new Thread(() -> {
                        try {
                            Hello.main(new String[]{});
                        } catch (Exception e) {
                            errors[idx] = true;
                        }
                    });
                }
                for (Thread t : threads) t.start();
                for (Thread t : threads) t.join(5000);
            } finally {
                System.setOut(originalOut);
            }

            for (int i = 0; i < threadCount; i++) {
                assertFalse(errors[i]);
            }
        });
    }

    private static void testRapidSequentialExecution() {
        assertTest("100 rapid sequential calls all produce correct output", () -> {
            for (int i = 0; i < 100; i++) {
                String output = captureMainOutput();
                assertEquals("Hello, World!", output.trim());
            }
        });
    }

    private static void testMainDoesNotCallSystemExit() {
        assertTest("main does not call System.exit (method returns normally)", () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream original = System.out;
            System.setOut(new PrintStream(baos));
            try {
                Hello.main(new String[]{});
            } finally {
                System.setOut(original);
            }
            assertTrue(true);
        });
    }

    private static void testLargeNumberOfInvocations() {
        assertTest("1000 invocations all produce consistent output", () -> {
            String expected = "Hello, World!" + System.lineSeparator();
            for (int i = 0; i < 1000; i++) {
                String output = captureMainOutput();
                if (!expected.equals(output)) {
                    throw new AssertionError("Mismatch at iteration " + i + ": expected <" + expected + "> but got <" + output + ">");
                }
            }
        });
    }

    private static void testMainWithEmptyStringArray() {
        assertTest("main with new String[]{\"\"} still outputs Hello, World!", () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream original = System.out;
            System.setOut(new PrintStream(baos));
            try {
                Hello.main(new String[]{""});
            } finally {
                System.setOut(original);
            }
            assertEquals("Hello, World!", baos.toString().trim());
        });
    }

    private static void testMainWithSingleEmptyString() {
        assertTest("main ignores arguments content completely", () -> {
            String output1 = captureMainOutputWithArgs(new String[]{});
            String output2 = captureMainOutputWithArgs(new String[]{"--flag", "value"});
            assertEquals(output1, output2);
        });
    }

    private static void testMainDoesNotModifySystemErr() {
        assertTest("main does not write to System.err", () -> {
            ByteArrayOutputStream errBaos = new ByteArrayOutputStream();
            PrintStream originalErr = System.err;
            PrintStream originalOut = System.out;
            System.setErr(new PrintStream(errBaos));
            ByteArrayOutputStream outBaos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outBaos));
            try {
                Hello.main(new String[]{});
            } finally {
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
            assertEquals(0, errBaos.toString().length());
        });
    }

    private static void testOutputDoesNotContainTab() {
        assertTest("Output does not contain tab characters", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("\t"));
        });
    }

    private static void testOutputDoesNotContainCarriageReturnAlone() {
        assertTest("Output does not contain standalone carriage return", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("\r"));
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
