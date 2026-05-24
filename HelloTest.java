import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class HelloTest {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloTest - Unit Tests for Hello.java ===\n");

        System.out.println("--- Basic Structure Tests ---");
        testClassExists();
        testClassIsPublic();
        testClassHasMainMethod();
        testMainMethodSignature();
        testMainMethodIsPublicStatic();
        testMainMethodReturnsVoid();

        System.out.println("\n--- Output Correctness Tests ---");
        testMainOutput();
        testOutputMatchesPython();
        testOutputEndsWithNewline();
        testNoLeadingWhitespace();
        testNoTrailingSpacesBeforeNewline();
        testExactOutputFormat();
        testSingleLineOutput();

        System.out.println("\n--- Robustness Tests ---");
        testMainWithNullArgs();
        testMainWithNonEmptyArgs();
        testIdempotency();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("ALL TESTS PASSED.");
        } else {
            System.out.println("SOME TESTS FAILED!");
            System.exit(1);
        }
    }

    private static void assertCondition(boolean condition, String testName, String message) {
        total++;
        if (condition) {
            passed++;
            System.out.println("  [PASS] " + testName);
        } else {
            failed++;
            System.out.println("  [FAIL] " + testName + " -- " + message);
        }
    }

    private static String captureMainOutput(String[] args) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        try {
            Hello.main(args);
        } finally {
            System.setOut(originalOut);
        }
        return captured.toString();
    }

    private static String getExpectedPythonOutput() {
        return "Hello, World!" + System.lineSeparator();
    }

    // --- Basic Structure Tests ---

    private static void testClassExists() {
        try {
            Class<?> cls = Class.forName("Hello");
            assertCondition(cls != null, "testClassExists", "Hello class should exist");
        } catch (ClassNotFoundException e) {
            assertCondition(false, "testClassExists", "Hello class not found: " + e.getMessage());
        }
    }

    private static void testClassIsPublic() {
        try {
            Class<?> cls = Class.forName("Hello");
            int modifiers = cls.getModifiers();
            assertCondition(Modifier.isPublic(modifiers), "testClassIsPublic",
                    "Hello class should be public");
        } catch (ClassNotFoundException e) {
            assertCondition(false, "testClassIsPublic", "Hello class not found");
        }
    }

    private static void testClassHasMainMethod() {
        try {
            Class<?> cls = Class.forName("Hello");
            Method main = cls.getDeclaredMethod("main", String[].class);
            assertCondition(main != null, "testClassHasMainMethod",
                    "Hello should have a main method");
        } catch (NoSuchMethodException e) {
            assertCondition(false, "testClassHasMainMethod",
                    "main method not found: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            assertCondition(false, "testClassHasMainMethod", "Hello class not found");
        }
    }

    private static void testMainMethodSignature() {
        try {
            Class<?> cls = Class.forName("Hello");
            Method main = cls.getDeclaredMethod("main", String[].class);
            Class<?>[] paramTypes = main.getParameterTypes();
            assertCondition(paramTypes.length == 1 && paramTypes[0] == String[].class,
                    "testMainMethodSignature",
                    "main method should accept String[] as parameter");
        } catch (Exception e) {
            assertCondition(false, "testMainMethodSignature", e.getMessage());
        }
    }

    private static void testMainMethodIsPublicStatic() {
        try {
            Class<?> cls = Class.forName("Hello");
            Method main = cls.getDeclaredMethod("main", String[].class);
            int mods = main.getModifiers();
            assertCondition(Modifier.isPublic(mods) && Modifier.isStatic(mods),
                    "testMainMethodIsPublicStatic",
                    "main method should be public static");
        } catch (Exception e) {
            assertCondition(false, "testMainMethodIsPublicStatic", e.getMessage());
        }
    }

    private static void testMainMethodReturnsVoid() {
        try {
            Class<?> cls = Class.forName("Hello");
            Method main = cls.getDeclaredMethod("main", String[].class);
            assertCondition(main.getReturnType() == void.class,
                    "testMainMethodReturnsVoid",
                    "main method should return void");
        } catch (Exception e) {
            assertCondition(false, "testMainMethodReturnsVoid", e.getMessage());
        }
    }

    // --- Output Correctness Tests ---

    private static void testMainOutput() {
        String output = captureMainOutput(new String[]{});
        assertCondition(output.contains("Hello, World!"), "testMainOutput",
                "Output should contain 'Hello, World!', got: " + repr(output));
    }

    private static void testOutputMatchesPython() {
        String output = captureMainOutput(new String[]{});
        String expected = getExpectedPythonOutput();
        assertCondition(output.equals(expected), "testOutputMatchesPython",
                "Java output should match Python output.\nExpected: " + repr(expected) + "\nActual:   " + repr(output));
    }

    private static void testOutputEndsWithNewline() {
        String output = captureMainOutput(new String[]{});
        assertCondition(output.endsWith(System.lineSeparator()), "testOutputEndsWithNewline",
                "Output should end with newline, got: " + repr(output));
    }

    private static void testNoLeadingWhitespace() {
        String output = captureMainOutput(new String[]{});
        String line = output.trim();
        assertCondition(line.equals(line.replaceAll("^\\s+", "")), "testNoLeadingWhitespace",
                "Output should have no leading whitespace");
    }

    private static void testNoTrailingSpacesBeforeNewline() {
        String output = captureMainOutput(new String[]{});
        String line = output.replace(System.lineSeparator(), "");
        assertCondition(line.equals(line.trim()), "testNoTrailingSpacesBeforeNewline",
                "Output should have no trailing spaces before newline, got: '" + line + "'");
    }

    private static void testExactOutputFormat() {
        String output = captureMainOutput(new String[]{});
        String expected = "Hello, World!" + System.lineSeparator();
        assertCondition(output.equals(expected), "testExactOutputFormat",
                "Output format should be exactly 'Hello, World!\\n'");
    }

    private static void testSingleLineOutput() {
        String output = captureMainOutput(new String[]{});
        String trimmed = output.trim();
        long lineCount = trimmed.split("\\r?\\n").length;
        assertCondition(lineCount == 1, "testSingleLineOutput",
                "Output should be exactly one line, got " + lineCount + " lines");
    }

    // --- Robustness Tests ---

    private static void testMainWithNullArgs() {
        try {
            String output = captureMainOutput(null);
            assertCondition(output.contains("Hello, World!"), "testMainWithNullArgs",
                    "main(null) should still produce output");
        } catch (NullPointerException e) {
            assertCondition(false, "testMainWithNullArgs",
                    "main(null) threw NullPointerException");
        } catch (Exception e) {
            assertCondition(false, "testMainWithNullArgs",
                    "main(null) threw " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private static void testMainWithNonEmptyArgs() {
        String output = captureMainOutput(new String[]{"arg1", "arg2"});
        assertCondition(output.contains("Hello, World!"), "testMainWithNonEmptyArgs",
                "main should ignore args and still output 'Hello, World!'");
    }

    private static void testIdempotency() {
        String output1 = captureMainOutput(new String[]{});
        String output2 = captureMainOutput(new String[]{});
        assertCondition(output1.equals(output2), "testIdempotency",
                "Calling main twice should produce identical output");
    }

    private static String repr(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                default: sb.append(c);
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
