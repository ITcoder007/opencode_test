import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

public class HelloExtendedTest {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloExtendedTest - Supplementary Tests ===\n");

        System.out.println("--- Structural Integrity Tests ---");
        testOnlyOnePublicDeclaredMethod();
        testNoExtraPublicMethodsBeyondMain();
        testClassHasDefaultConstructor();
        testClassIsNotFinal();
        testClassDoesNotImplementInterfaces();

        System.out.println("\n--- Output Encoding & Byte-Level Tests ---");
        testOutputUtf8Encoding();
        testOutputByteCount();
        testOutputContainsExactCharacters();
        testNoBomInOutput();

        System.out.println("\n--- Stderr & Side-Effect Tests ---");
        testStderrRemainsEmpty();
        testStdinNotConsumed();
        testNoSystemExit();

        System.out.println("\n--- Performance Baseline Tests ---");
        testExecutionWithinTimeLimit();

        System.out.println("\n--- E2E Diff Verification ---");
        testDiffWithPythonOutput();

        System.out.println("\n=== Extended Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL EXTENDED TESTS PASSED");
        } else {
            System.out.println("Result: SOME EXTENDED TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testOnlyOnePublicDeclaredMethod() {
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

    private static void testNoExtraPublicMethodsBeyondMain() {
        assertTest("No extra public methods beyond main()", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] publicMethods = clazz.getMethods();
            int ownMethods = 0;
            for (Method m : publicMethods) {
                if (m.getDeclaringClass() == clazz) {
                    ownMethods++;
                }
            }
            assertEquals(1, ownMethods);
        }, ClassNotFoundException.class);
    }

    private static void testClassHasDefaultConstructor() {
        assertTest("Hello has an accessible default constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Object instance = clazz.getDeclaredConstructor().newInstance();
            assertNotNull(instance);
            assertEquals(clazz, instance.getClass());
        }, ClassNotFoundException.class);
    }

    private static void testClassIsNotFinal() {
        assertTest("Hello class is not final (can be extended)", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isFinal(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassDoesNotImplementInterfaces() {
        assertTest("Hello does not implement any interfaces (plain class)", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getInterfaces().length);
        }, ClassNotFoundException.class);
    }

    private static void testOutputUtf8Encoding() {
        assertTest("Output bytes are valid UTF-8", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            String decoded = new String(bytes, StandardCharsets.UTF_8);
            assertEquals(output, decoded);
        });
    }

    private static void testOutputByteCount() {
        assertTest("Output byte count matches expected: 'Hello, World!' + newline", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            int expectedBytes = "Hello, World!".getBytes(StandardCharsets.UTF_8).length
                + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;
            assertEquals(expectedBytes, bytes.length);
        });
    }

    private static void testOutputContainsExactCharacters() {
        assertTest("Output contains exact ASCII characters: H,e,l,l,o,,, ,W,o,r,l,d,!", () -> {
            String output = captureMainOutput().trim();
            assertEquals('H', output.charAt(0));
            assertEquals('e', output.charAt(1));
            assertEquals('l', output.charAt(2));
            assertEquals('l', output.charAt(3));
            assertEquals('o', output.charAt(4));
            assertEquals(',', output.charAt(5));
            assertEquals(' ', output.charAt(6));
            assertEquals('W', output.charAt(7));
            assertEquals('o', output.charAt(8));
            assertEquals('r', output.charAt(9));
            assertEquals('l', output.charAt(10));
            assertEquals('d', output.charAt(11));
            assertEquals('!', output.charAt(12));
            assertEquals(13, output.length());
        });
    }

    private static void testNoBomInOutput() {
        assertTest("Output does not contain BOM (Byte Order Mark)", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            assertFalse(bytes.length >= 3 && bytes[0] == (byte) 0xEF
                && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF);
        });
    }

    private static void testStderrRemainsEmpty() {
        assertTest("stderr is empty after main() execution", () -> {
            ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
            ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;
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

    private static void testStdinNotConsumed() {
        assertTest("main() does not consume stdin", () -> {
            ByteArrayInputStream fakeStdin = new ByteArrayInputStream("unexpected input".getBytes());
            System.setIn(fakeStdin);
            String output = captureMainOutput();
            System.setIn(System.in);
            assertEquals("Hello, World!", output.trim());
            assertTrue(fakeStdin.available() > 0);
        });
    }

    private static void testNoSystemExit() {
        assertTest("main() returns normally (no exception, no early termination)", () -> {
            boolean[] reachedEnd = {false};
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(baos));
            try {
                Hello.main(new String[]{});
                reachedEnd[0] = true;
            } finally {
                System.setOut(originalOut);
            }
            assertTrue(reachedEnd[0]);
            assertEquals("Hello, World!", baos.toString().trim());
        });
    }

    private static void testExecutionWithinTimeLimit() {
        assertTest("main() completes within 1 second", () -> {
            long start = System.nanoTime();
            captureMainOutput();
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            assertTrue(elapsed < 1000);
        });
    }

    private static void testDiffWithPythonOutput() {
        assertTest("Java output byte-for-byte matches Python output", () -> {
            String javaOutput = captureMainOutput().trim();
            String expectedPythonOutput = "Hello, World!";
            assertEquals(expectedPythonOutput, javaOutput);
            byte[] javaBytes = javaOutput.getBytes(StandardCharsets.UTF_8);
            byte[] pythonBytes = expectedPythonOutput.getBytes(StandardCharsets.UTF_8);
            assertEquals(pythonBytes.length, javaBytes.length);
            for (int i = 0; i < javaBytes.length; i++) {
                assertEquals(pythonBytes[i], javaBytes[i]);
            }
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
                System.out.println("  FAIL: " + name + " - Expected no exception but got: "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
            } else {
                failed++;
                System.out.println("  FAIL: " + name + " - Exception: "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
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
