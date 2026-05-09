import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class HelloTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== Running HelloTest ===\n");

        testCompiles();
        testOutputEqualsPythonOutput();
        testOutputIsHelloWorld();
        testOutputEndsWithNewline();
        testNoExtraWhitespaceIssues();

        System.out.println("\n=== Results ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total:  " + (passed + failed));

        if (failed > 0) {
            System.out.println("\n*** SOME TESTS FAILED ***");
            System.exit(1);
        } else {
            System.out.println("\n*** ALL TESTS PASSED ***");
        }
    }

    static void testCompiles() {
        String name = "testCompiles - Hello.class exists after compilation";
        try {
            Class<?> cls = Class.forName("Hello");
            assertEqual(name, "Hello", cls.getSimpleName());
        } catch (ClassNotFoundException e) {
            fail(name, "Hello class not found: " + e.getMessage());
        }
    }

    static void testOutputEqualsPythonOutput() {
        String name = "testOutputEqualsPythonOutput";
        String output = captureMainOutput();
        assertEqual(name, "Hello, World!\n", output);
    }

    static void testOutputIsHelloWorld() {
        String name = "testOutputIsHelloWorld - exact output is 'Hello, World!'";
        String output = captureMainOutput();
        assertEqual(name, "Hello, World!", output.trim());
    }

    static void testOutputEndsWithNewline() {
        String name = "testOutputEndsWithNewline";
        String output = captureMainOutput();
        assertTrue(name, output.endsWith("\n"));
    }

    static void testNoExtraWhitespaceIssues() {
        String name = "testNoExtraWhitespaceIssues - no leading/trailing spaces in trimmed output";
        String output = captureMainOutput().trim();
        assertTrue(name, output.equals(output.trim()));
    }

    private static String captureMainOutput() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        System.setOut(new PrintStream(baos));
        System.setErr(new PrintStream(new ByteArrayOutputStream()));
        try {
            Hello.main(new String[]{});
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
        return baos.toString();
    }

    private static void assertEqual(String testName, String expected, String actual) {
        if (expected.equals(actual)) {
            pass(testName);
        } else {
            fail(testName, "Expected: [" + escape(expected) + "], Actual: [" + escape(actual) + "]");
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            pass(testName);
        } else {
            fail(testName, "Expected true but was false");
        }
    }

    private static void pass(String name) {
        passed++;
        System.out.println("  PASS: " + name);
    }

    private static void fail(String name, String msg) {
        failed++;
        System.out.println("  FAIL: " + name + " - " + msg);
    }

    private static String escape(String s) {
        return s.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
