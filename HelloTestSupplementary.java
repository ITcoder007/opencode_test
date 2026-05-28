import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class HelloTestSupplementary {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloTestSupplementary - Supplementary Tests for Hello.java ===\n");

        System.out.println("--- Process-Level Verification Tests ---");
        testProcessExecutionExitCode();
        testProcessExecutionOutput();
        testProcessExecutionStderr();

        System.out.println("\n--- Class Metadata Tests ---");
        testClassHasNoDeclaredInterfaces();
        testClassSuperclassIsObject();
        testClassHasDefaultPackage();
        testClassHasNoAnnotations();
        testClassDeclaredMethodsCount();

        System.out.println("\n--- Output Stream Independence Tests ---");
        testOutputToCustomPrintStream();
        testOutputToByteArrayStreamDirectly();
        testOutputPreservedAfterStreamSwitch();

        System.out.println("\n--- Performance & Timing Tests ---");
        testMainCompletesWithinOneSecond();
        testRepeatedExecutionPerformance();

        System.out.println("\n--- Character-Level Python Equivalence Tests ---");
        testCharacterByCharacterMatch();
        testOutputCharCount();
        testEachCharacterInRange();
        testNoCarriageReturnInOutput();

        System.out.println("\n--- Class Loading Isolation Tests ---");
        testMultipleClassForNameLoads();
        testClassForNameAndInstanceMethod();

        System.out.println("\n--- Compilation Artifact Tests ---");
        testClassFileExists();
        testClassFileNonEmpty();

        System.out.println("\n--- Output Immutability Tests ---");
        testOutputHashConsistency();
        testTrimmedOutputLength();
        testOutputContainsNoTabs();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL TESTS PASSED");
        } else {
            System.out.println("Result: SOME TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testProcessExecutionExitCode() {
        assertTest("Process: java Hello exits with code 0", () -> {
            ProcessBuilder pb = new ProcessBuilder("java", "Hello");
            pb.directory(new File(System.getProperty("user.dir")));
            pb.redirectErrorStream(false);
            Process proc = pb.start();
            proc.getOutputStream().close();
            int exitCode = proc.waitFor();
            assertEquals(0, exitCode);
        });
    }

    private static void testProcessExecutionOutput() {
        assertTest("Process: java Hello outputs 'Hello, World!'", () -> {
            ProcessBuilder pb = new ProcessBuilder("java", "Hello");
            pb.directory(new File(System.getProperty("user.dir")));
            pb.redirectErrorStream(false);
            Process proc = pb.start();
            proc.getOutputStream().close();
            String output = new String(proc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            proc.waitFor();
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testProcessExecutionStderr() {
        assertTest("Process: java Hello writes nothing to stderr", () -> {
            ProcessBuilder pb = new ProcessBuilder("java", "Hello");
            pb.directory(new File(System.getProperty("user.dir")));
            pb.redirectErrorStream(false);
            Process proc = pb.start();
            proc.getOutputStream().close();
            proc.getInputStream().readAllBytes();
            String errOutput = new String(proc.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            proc.waitFor();
            String trimmed = errOutput.replace(
                System.lineSeparator(), "").trim();
            for (String line : trimmed.split("\n")) {
                if (line.startsWith("Picked up JAVA_TOOL_OPTIONS")) {
                    trimmed = trimmed.replace(line, "");
                }
            }
            trimmed = trimmed.trim();
            assertTrue(trimmed.isEmpty());
        });
    }

    private static void testClassHasNoDeclaredInterfaces() {
        assertTest("Hello class implements no interfaces", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getInterfaces().length);
        }, ClassNotFoundException.class);
    }

    private static void testClassSuperclassIsObject() {
        assertTest("Hello superclass is java.lang.Object", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(Object.class, clazz.getSuperclass());
        }, ClassNotFoundException.class);
    }

    private static void testClassHasDefaultPackage() {
        assertTest("Hello class is in default (unnamed) package", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals("", clazz.getPackageName());
        }, ClassNotFoundException.class);
    }

    private static void testClassHasNoAnnotations() {
        assertTest("Hello class has no annotations", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getAnnotations().length);
        }, ClassNotFoundException.class);
    }

    private static void testClassDeclaredMethodsCount() {
        assertTest("Hello class has exactly 1 declared method (main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(1, clazz.getDeclaredMethods().length);
        }, ClassNotFoundException.class);
    }

    private static void testOutputToCustomPrintStream() {
        assertTest("Output goes to whichever PrintStream is set as System.out", () -> {
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            PrintStream original = System.out;

            System.setOut(new PrintStream(baos1));
            Hello.main(null);
            String output1 = baos1.toString();

            System.setOut(new PrintStream(baos2));
            Hello.main(null);
            String output2 = baos2.toString();

            System.setOut(original);
            assertEquals(output1, output2);
        });
    }

    private static void testOutputToByteArrayStreamDirectly() {
        assertTest("Output written to ByteArrayOutputStream is complete", () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream original = System.out;
            System.setOut(ps);
            try {
                Hello.main(null);
            } finally {
                System.setOut(original);
            }
            ps.flush();
            assertTrue(baos.size() > 0);
            assertEquals("Hello, World!", baos.toString().trim());
        });
    }

    private static void testOutputPreservedAfterStreamSwitch() {
        assertTest("First stream retains output after switching to second stream", () -> {
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            PrintStream original = System.out;

            System.setOut(new PrintStream(baos1));
            Hello.main(null);

            System.setOut(new PrintStream(baos2));
            Hello.main(null);

            System.setOut(original);

            String first = baos1.toString().trim();
            String second = baos2.toString().trim();
            assertEquals("Hello, World!", first);
            assertEquals("Hello, World!", second);
        });
    }

    private static void testMainCompletesWithinOneSecond() {
        assertTest("main() completes within 1 second", () -> {
            long start = System.nanoTime();
            captureMainOutput();
            long elapsed = System.nanoTime() - start;
            assertTrue(elapsed < 1_000_000_000L);
        });
    }

    private static void testRepeatedExecutionPerformance() {
        assertTest("100 sequential calls complete within 5 seconds", () -> {
            long start = System.nanoTime();
            for (int i = 0; i < 100; i++) {
                captureMainOutput();
            }
            long elapsed = System.nanoTime() - start;
            assertTrue(elapsed < 5_000_000_000L);
        });
    }

    private static void testCharacterByCharacterMatch() {
        assertTest("Each character matches Python output character-by-character", () -> {
            String output = captureMainOutput().trim();
            String pythonOutput = "Hello, World!";
            assertEquals(pythonOutput.length(), output.length());
            for (int i = 0; i < pythonOutput.length(); i++) {
                assertEquals(pythonOutput.charAt(i), output.charAt(i));
            }
        });
    }

    private static void testOutputCharCount() {
        assertTest("Output content (excluding newline) is exactly 13 characters", () -> {
            String output = captureMainOutput().trim();
            assertEquals(13, output.length());
        });
    }

    private static void testEachCharacterInRange() {
        assertTest("All characters are printable ASCII (32-126)", () -> {
            String output = captureMainOutput().trim();
            for (char c : output.toCharArray()) {
                assertTrue(c >= 32 && c <= 126);
            }
        });
    }

    private static void testNoCarriageReturnInOutput() {
        assertTest("Output contains no carriage return (\\r) characters", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("\r"));
        });
    }

    private static void testMultipleClassForNameLoads() {
        assertTest("Class.forName('Hello') returns same Class object on repeated calls", () -> {
            Class<?> c1 = Class.forName("Hello");
            Class<?> c2 = Class.forName("Hello");
            assertSame(c1, c2);
        }, ClassNotFoundException.class);
    }

    private static void testClassForNameAndInstanceMethod() {
        assertTest("Main method obtained via Class.forName is consistent", () -> {
            Class<?> c1 = Class.forName("Hello");
            Class<?> c2 = Class.forName("Hello");
            Method m1 = c1.getMethod("main", String[].class);
            Method m2 = c2.getMethod("main", String[].class);
            assertEquals(m1.getName(), m2.getName());
            assertEquals(m1.getReturnType(), m2.getReturnType());
        }, ClassNotFoundException.class);
    }

    private static void testClassFileExists() {
        assertTest("Hello.class file exists after compilation", () -> {
            File classFile = new File("Hello.class");
            assertTrue(classFile.exists());
        });
    }

    private static void testClassFileNonEmpty() {
        assertTest("Hello.class file is non-empty", () -> {
            File classFile = new File("Hello.class");
            assertTrue(classFile.length() > 0);
        });
    }

    private static void testOutputHashConsistency() {
        assertTest("hashCode() of output is consistent across calls", () -> {
            String output1 = captureMainOutput();
            String output2 = captureMainOutput();
            assertEquals(output1.hashCode(), output2.hashCode());
        });
    }

    private static void testTrimmedOutputLength() {
        assertTest("Trimmed output has length 13", () -> {
            String output = captureMainOutput();
            assertEquals(13, output.trim().length());
        });
    }

    private static void testOutputContainsNoTabs() {
        assertTest("Output contains no tab characters", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("\t"));
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

    private static void assertTest(String name, ThrowingRunnable test,
            Class<? extends Exception> allowed) {
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
                System.out.println("  FAIL: " + name
                    + " - Expected no exception but got: "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
            } else {
                failed++;
                System.out.println("  FAIL: " + name + " - Exception: "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        } catch (Throwable t) {
            failed++;
            System.out.println("  FAIL: " + name + " - Throwable: "
                + t.getClass().getSimpleName() + ": " + t.getMessage());
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

    private static void assertSame(Object expected, Object actual) {
        if (expected != actual) {
            throw new AssertionError("Expected same reference but got different objects");
        }
    }
}
