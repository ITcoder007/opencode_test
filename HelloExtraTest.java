import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

public class HelloExtraTest {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloExtraTest - Supplementary Unit Tests ===\n");

        System.out.println("--- Stderr Verification ---");
        testNoStderrOutput();
        testStderrEmptyOnNullArgs();

        System.out.println("\n--- Character-Level Verification ---");
        testOutputIsPureAscii();
        testOutputLength();
        testContainsComma();
        testContainsExclamationMark();
        testFirstCharacterIsUppercaseH();
        testSecondWordStartsWithUppercaseW();
        testContainsSubstringHello();
        testContainsSubstringWorld();

        System.out.println("\n--- Class API Contract ---");
        testOnlyMainAsPublicStaticMethod();
        testMainDoesNotCallSystemExit();
        testClassHasNoPublicFields();
        testClassIsNotFinal();
        testClassHasDefaultConstructor();

        System.out.println("\n--- Encoding & Format Edge Cases ---");
        testOutputBytesMatchUtf8();
        testOutputNoBom();
        testOutputNoCarriageReturnOnUnix();
        testMainCanBeCalledManyTimes();

        System.out.println("\n=== Supplementary Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL SUPPLEMENTARY TESTS PASSED");
        } else {
            System.out.println("Result: SOME SUPPLEMENTARY TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testNoStderrOutput() {
        assertTest("main() writes nothing to stderr", () -> {
            PrintStream originalErr = System.err;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream capturedErr = new PrintStream(baos);
            System.setErr(capturedErr);
            try {
                Hello.main(new String[]{});
            } finally {
                System.setErr(originalErr);
            }
            assertEquals("", baos.toString());
        });
    }

    private static void testStderrEmptyOnNullArgs() {
        assertTest("main(null) writes nothing to stderr", () -> {
            PrintStream originalErr = System.err;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream capturedErr = new PrintStream(baos);
            System.setErr(capturedErr);
            try {
                Hello.main(null);
            } finally {
                System.setErr(originalErr);
            }
            assertEquals("", baos.toString());
        });
    }

    private static void testOutputIsPureAscii() {
        assertTest("Output contains only ASCII characters", () -> {
            String output = captureMainOutput();
            for (char c : output.toCharArray()) {
                assertTrue(c < 128);
            }
        });
    }

    private static void testOutputLength() {
        assertTest("Output content length is exactly 13 characters (Hello, World!)", () -> {
            String output = captureMainOutput().trim();
            assertEquals(13, output.length());
        });
    }

    private static void testContainsComma() {
        assertTest("Output contains comma after Hello", () -> {
            String output = captureMainOutput();
            assertTrue(output.contains(","));
            int commaIdx = output.indexOf(',');
            assertEquals("Hello", output.substring(0, commaIdx));
        });
    }

    private static void testContainsExclamationMark() {
        assertTest("Output ends with exclamation mark (before newline)", () -> {
            String output = captureMainOutput().trim();
            assertTrue(output.endsWith("!"));
        });
    }

    private static void testFirstCharacterIsUppercaseH() {
        assertTest("First character is uppercase 'H'", () -> {
            String output = captureMainOutput();
            assertEquals('H', output.charAt(0));
        });
    }

    private static void testSecondWordStartsWithUppercaseW() {
        assertTest("Second word starts with uppercase 'W'", () -> {
            String output = captureMainOutput().trim();
            int spaceIdx = output.indexOf(' ');
            assertTrue(spaceIdx > 0);
            assertEquals('W', output.charAt(spaceIdx + 1));
        });
    }

    private static void testContainsSubstringHello() {
        assertTest("Output contains substring 'Hello'", () -> {
            String output = captureMainOutput();
            assertTrue(output.contains("Hello"));
        });
    }

    private static void testContainsSubstringWorld() {
        assertTest("Output contains substring 'World'", () -> {
            String output = captureMainOutput();
            assertTrue(output.contains("World"));
        });
    }

    private static void testOnlyMainAsPublicStaticMethod() {
        assertTest("Hello has only 'main' as public static method", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            for (Method m : methods) {
                if (Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers())) {
                    assertEquals("main", m.getName());
                }
            }
        });
    }

    private static void testMainDoesNotCallSystemExit() {
        assertTest("main() does not call System.exit()", () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(baos));
            try {
                Hello.main(new String[]{});
            } finally {
                System.setOut(originalOut);
            }
        });
    }

    private static void testClassHasNoPublicFields() {
        assertTest("Hello class has no public fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            java.lang.reflect.Field[] fields = clazz.getFields();
            assertEquals(0, fields.length);
        });
    }

    private static void testClassIsNotFinal() {
        assertTest("Hello class is not final (standard Java convention for simple classes)", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isFinal(clazz.getModifiers()));
        });
    }

    private static void testClassHasDefaultConstructor() {
        assertTest("Hello has a default public constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            clazz.getDeclaredConstructor();
        });
    }

    private static void testOutputBytesMatchUtf8() {
        assertTest("Output bytes match expected UTF-8 encoding", () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(baos, true, "UTF-8"));
            try {
                Hello.main(new String[]{});
            } finally {
                System.setOut(originalOut);
            }
            String output = baos.toString("UTF-8");
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testOutputNoBom() {
        assertTest("Output does not contain BOM (Byte Order Mark)", () -> {
            String output = captureMainOutput();
            assertFalse(output.startsWith("\uFEFF"));
        });
    }

    private static void testOutputNoCarriageReturnOnUnix() {
        assertTest("Output uses system line separator (no stray CR)", () -> {
            String output = captureMainOutput();
            String content = output.trim();
            assertFalse(content.contains("\r"));
        });
    }

    private static void testMainCanBeCalledManyTimes() {
        assertTest("main() can be called 100 times without issues", () -> {
            for (int i = 0; i < 100; i++) {
                String output = captureMainOutput();
                assertEquals("Hello, World!", output.trim());
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
