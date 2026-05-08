import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class HelloTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== Running HelloTest ===\n");

        testMainOutput();
        testMainWithArgs();
        testClassExists();
        testMainIsStatic();
        testOutputMatchesPython();

        System.out.println("\n=== Test Results ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total:  " + (passed + failed));

        if (failed > 0) {
            System.out.println("\n*** TESTS FAILED ***");
            System.exit(1);
        } else {
            System.out.println("\n*** ALL TESTS PASSED ***");
        }
    }

    static void testMainOutput() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream capturedOut = new PrintStream(baos);
        System.setOut(capturedOut);

        try {
            Hello.main(new String[]{});
            String output = baos.toString().trim();
            assertEquals("testMainOutput: output should be 'Hello, World!'", "Hello, World!", output);
        } finally {
            System.setOut(originalOut);
        }
    }

    static void testMainWithArgs() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream capturedOut = new PrintStream(baos);
        System.setOut(capturedOut);

        try {
            Hello.main(new String[]{"arg1", "arg2"});
            String output = baos.toString().trim();
            assertEquals("testMainWithArgs: output should be 'Hello, World!' regardless of args", "Hello, World!", output);
        } finally {
            System.setOut(originalOut);
        }
    }

    static void testClassExists() {
        try {
            Class<?> cls = Class.forName("Hello");
            assertNotNull("testClassExists: Hello class should exist", cls);
            System.out.println("[PASS] testClassExists: Hello class exists");
            passed++;
        } catch (ClassNotFoundException e) {
            System.out.println("[FAIL] testClassExists: Hello class not found - " + e.getMessage());
            failed++;
        }
    }

    static void testMainIsStatic() {
        try {
            Class<?> cls = Class.forName("Hello");
            java.lang.reflect.Method mainMethod = cls.getMethod("main", String[].class);
            assertTrue("testMainIsStatic: main should be public static",
                       java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers())
                       && java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()));
        } catch (NoSuchMethodException e) {
            System.out.println("[FAIL] testMainIsStatic: main method not found - " + e.getMessage());
            failed++;
        } catch (ClassNotFoundException e) {
            System.out.println("[FAIL] testMainIsStatic: Hello class not found - " + e.getMessage());
            failed++;
        }
    }

    static void testOutputMatchesPython() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream capturedOut = new PrintStream(baos);
        System.setOut(capturedOut);

        try {
            Hello.main(new String[]{});
            String javaOutput = baos.toString().trim();
            String expectedPythonOutput = "Hello, World!";
            assertEquals("testOutputMatchesPython: Java output should match Python output",
                         expectedPythonOutput, javaOutput);
        } finally {
            System.setOut(originalOut);
        }
    }

    static void assertEquals(String testName, String expected, String actual) {
        if (expected.equals(actual)) {
            System.out.println("[PASS] " + testName);
            passed++;
        } else {
            System.out.println("[FAIL] " + testName);
            System.out.println("       Expected: '" + expected + "'");
            System.out.println("       Actual:   '" + actual + "'");
            failed++;
        }
    }

    static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("[PASS] " + testName);
            passed++;
        } else {
            System.out.println("[FAIL] " + testName);
            System.out.println("       Expected: true");
            System.out.println("       Actual:   false");
            failed++;
        }
    }

    static void assertNotNull(String testName, Object obj) {
        if (obj != null) {
            System.out.println("[PASS] " + testName);
            passed++;
        } else {
            System.out.println("[FAIL] " + testName);
            System.out.println("       Expected: non-null");
            System.out.println("       Actual:   null");
            failed++;
        }
    }
}
