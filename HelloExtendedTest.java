import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class HelloExtendedTest {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloExtendedTest - Extended Tests for Hello.java ===\n");

        System.out.println("--- UTF-8 Encoding Tests ---");
        testOutputIsUTF8Compatible();
        testOutputBytesMatchASCII();

        System.out.println("\n--- Concurrency Tests ---");
        testConcurrentMainCalls();

        System.out.println("\n--- Performance Tests ---");
        testMainCompletesQuickly();

        System.out.println("\n--- Byte-Level Verification ---");
        testExactByteOutput();
        testNoBOMInOutput();
        testNoCarriageReturnOnly();

        System.out.println("\n--- Cross-Language Equivalence ---");
        testJavaMatchesPythonOutput();

        System.out.println("\n--- Source File Verification ---");
        testSourceFileExists();
        testSourceFileCompilesCleanly();

        System.out.println("\n--- Edge Case Tests ---");
        testMainWithEmptyStringArray();
        testMainWithLargeArgsArray();
        testOutputStreamIntegrity();

        System.out.println("\n=== Extended Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL EXTENDED TESTS PASSED");
        } else {
            System.out.println("Result: SOME EXTENDED TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testOutputIsUTF8Compatible() {
        assertTest("Output is valid UTF-8", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            String decoded = new String(bytes, StandardCharsets.UTF_8);
            assertEquals(output, decoded);
        });
    }

    private static void testOutputBytesMatchASCII() {
        assertTest("All output bytes are ASCII-range", () -> {
            String output = captureMainOutput().trim();
            for (char c : output.toCharArray()) {
                assertTrue(c < 128);
            }
        });
    }

    private static final Object STDOUT_LOCK = new Object();

    private static void testConcurrentMainCalls() {
        assertTest("Concurrent main() calls produce correct output", () -> {
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            String[] results = new String[threadCount];
            for (int i = 0; i < threadCount; i++) {
                final int idx = i;
                threads[i] = new Thread(() -> {
                    synchronized (STDOUT_LOCK) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        PrintStream originalOut = System.out;
                        System.setOut(new PrintStream(baos));
                        try {
                            Hello.main(new String[]{});
                        } finally {
                            System.setOut(originalOut);
                        }
                        results[idx] = baos.toString();
                    }
                });
            }
            for (Thread t : threads) t.start();
            for (Thread t : threads) t.join(5000);
            for (int i = 0; i < threadCount; i++) {
                assertNotNull(results[i]);
                assertEquals("Hello, World!", results[i].trim());
            }
        });
    }

    private static void testMainCompletesQuickly() {
        assertTest("main() completes in under 1 second", () -> {
            long start = System.nanoTime();
            Hello.main(new String[]{});
            long elapsed = System.nanoTime() - start;
            assertTrue(elapsed < 1_000_000_000L);
        });
    }

    private static void testExactByteOutput() {
        assertTest("Exact byte output matches 'Hello, World!\\n' (LF)", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            byte[] expected = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);
            assertEquals(expected.length, bytes.length);
            for (int i = 0; i < expected.length; i++) {
                assertEquals(expected[i], bytes[i]);
            }
        });
    }

    private static void testNoBOMInOutput() {
        assertTest("Output has no UTF-8 BOM", () -> {
            byte[] bytes = captureMainOutput().getBytes(StandardCharsets.UTF_8);
            if (bytes.length >= 3) {
                assertFalse((bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF);
            }
        });
    }

    private static void testNoCarriageReturnOnly() {
        assertTest("Output uses LF (\\n) not CR (\\r) alone", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("\r") && !output.contains("\r\n"));
        });
    }

    private static void testJavaMatchesPythonOutput() {
        assertTest("Java output matches Python hello.py output exactly", () -> {
            String javaOutput = captureMainOutput().trim();
            ProcessBuilder pb = new ProcessBuilder("python3", "hello.py");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            String pythonOutput = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            p.waitFor();
            assertEquals(pythonOutput, javaOutput);
        });
    }

    private static void testSourceFileExists() {
        assertTest("Hello.java source file exists", () -> {
            File f = new File("Hello.java");
            assertTrue(f.exists());
            assertTrue(f.length() > 0);
        });
    }

    private static void testSourceFileCompilesCleanly() {
        assertTest("Hello.java compiles without errors", () -> {
            ProcessBuilder pb = new ProcessBuilder("javac", "-encoding", "UTF-8", "Hello.java");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            String output = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = p.waitFor();
            assertTrue(exitCode == 0);
        });
    }

    private static void testMainWithEmptyStringArray() {
        assertTest("main() works with new String[]{\"\"}", () -> {
            String output = captureMainOutputWithArgs(new String[]{""});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMainWithLargeArgsArray() {
        assertTest("main() works with 1000 arguments", () -> {
            String[] largeArgs = new String[1000];
            for (int i = 0; i < 1000; i++) largeArgs[i] = "arg" + i;
            String output = captureMainOutputWithArgs(largeArgs);
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testOutputStreamIntegrity() {
        assertTest("System.out is restored after main()", () -> {
            PrintStream original = System.out;
            captureMainOutput();
            assertEquals(original, System.out);
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

    private static void assertEquals(long expected, long actual) {
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

    private static void assertTrue(long value) {
        if (value == 0) {
            throw new AssertionError("Expected true (non-zero) but got zero");
        }
    }

    private static void assertFalse(boolean condition) {
        if (condition) {
            throw new AssertionError("Expected false but got true");
        }
    }
}
