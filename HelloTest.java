import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class HelloTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== Running HelloTest ===\n");

        testMainOutput();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total:  " + (passed + failed));

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testMainOutput() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream capturedOut = new PrintStream(baos);
        System.setOut(capturedOut);

        try {
            Hello.main(new String[]{});
            capturedOut.flush();
        } finally {
            System.setOut(originalOut);
        }

        String output = baos.toString().trim();
        assertEqual("testMainOutput", "Hello, World!", output);
    }

    private static void assertEqual(String testName, String expected, String actual) {
        if (expected.equals(actual)) {
            System.out.println("[PASS] " + testName);
            passed++;
        } else {
            System.out.println("[FAIL] " + testName);
            System.out.println("  Expected: \"" + expected + "\"");
            System.out.println("  Actual:   \"" + actual + "\"");
            failed++;
        }
    }
}
