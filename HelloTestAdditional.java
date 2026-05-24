import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class HelloTestAdditional {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloTestAdditional - Additional Verification Tests ===\n");

        System.out.println("--- Constructor Tests ---");
        testDefaultConstructorExists();
        testConstructorIsPublic();
        testCanInstantiateHello();

        System.out.println("\n--- No Side Effects Beyond stdout ---");
        testNoFileIO();
        testExitCodeIsZero();

        System.out.println("\n--- Class Design Tests ---");
        testClassIsNotFinal();
        testClassHasExactlyOneMethod();
        testClassHasNoFields();

        System.out.println("\n--- Thread Interruption Tests ---");
        testMainThreadInterruptDoesNotAffectOutput();

        System.out.println("\n--- Output Content Detailed Tests ---");
        testOutputContainsCommaAndSpace();
        testOutputContainsExclamationMark();
        testOutputCaseSensitive();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL TESTS PASSED");
        } else {
            System.out.println("Result: SOME TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testDefaultConstructorExists() {
        assertTest("Hello has a default constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?> ctor = clazz.getConstructor();
            assertNotNull(ctor);
        });
    }

    private static void testConstructorIsPublic() {
        assertTest("Default constructor is public", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?> ctor = clazz.getConstructor();
            assertTrue(Modifier.isPublic(ctor.getModifiers()));
        });
    }

    private static void testCanInstantiateHello() {
        assertTest("Hello can be instantiated via default constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?> ctor = clazz.getConstructor();
            Object instance = ctor.newInstance();
            assertNotNull(instance);
            assertEquals("Hello", instance.getClass().getSimpleName());
        });
    }

    private static void testNoFileIO() {
        assertTest("main() does not create or modify any files", () -> {
            File tempDir = new File(System.getProperty("java.io.tmpdir"));
            long beforeCount = countFiles(tempDir);
            captureMainOutput();
            long afterCount = countFiles(tempDir);
            assertEquals(beforeCount, afterCount);
        });
    }

    private static void testExitCodeIsZero() {
        assertTest("Hello.main() completes without System.exit()", () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(baos));
            try {
                Hello.main(new String[]{});
            } finally {
                System.setOut(originalOut);
            }
            assertTrue(baos.toString().contains("Hello, World!"));
        });
    }

    private static void testClassIsNotFinal() {
        assertTest("Hello class is not final (can be extended)", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isFinal(clazz.getModifiers()));
        });
    }

    private static void testClassHasExactlyOneMethod() {
        assertTest("Hello class has exactly one declared method (main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            assertEquals(1, methods.length);
            assertEquals("main", methods[0].getName());
        });
    }

    private static void testClassHasNoFields() {
        assertTest("Hello class has no declared fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getDeclaredFields().length);
        });
    }

    private static void testMainThreadInterruptDoesNotAffectOutput() {
        assertTest("Thread interruption does not affect main() output", () -> {
            Thread.currentThread().interrupt();
            try {
                String output = captureMainOutput();
                assertEquals("Hello, World!", output.trim());
            } finally {
                Thread.interrupted();
            }
        });
    }

    private static void testOutputContainsCommaAndSpace() {
        assertTest("Output contains ', ' (comma followed by space)", () -> {
            String output = captureMainOutput();
            assertTrue(output.contains(", "));
        });
    }

    private static void testOutputContainsExclamationMark() {
        assertTest("Output contains '!' (exclamation mark)", () -> {
            String output = captureMainOutput();
            assertTrue(output.contains("!"));
        });
    }

    private static void testOutputCaseSensitive() {
        assertTest("Output has correct case: 'H' uppercase, 'W' uppercase", () -> {
            String output = captureMainOutput().trim();
            assertTrue(output.startsWith("Hello"));
            assertTrue(output.contains("World"));
            assertFalse(output.contains("hello"));
            assertFalse(output.contains("world"));
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

    private static long countFiles(File dir) {
        File[] files = dir.listFiles();
        return files != null ? files.length : 0;
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
        } catch (Throwable t) {
            failed++;
            System.out.println("  FAIL: " + name + " - Throwable: " + t.getClass().getSimpleName() + ": " + t.getMessage());
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
