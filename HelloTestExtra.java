import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class HelloTestExtra {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloTestExtra - Supplementary Tests ===\n");

        System.out.println("--- Class Structure Completeness ---");
        testClassIsConcrete();
        testClassHasDefaultConstructor();
        testClassHasExactlyOneDeclaredMethod();
        testClassNotFinal();
        testMainNoCheckedExceptions();

        System.out.println("\n--- Output Precision ---");
        testOutputLengthExact();
        testOutputIsPureAscii();
        testOutputContainsNoTab();
        testOutputContainsHelloCommaWorld();

        System.out.println("\n--- Argument Boundary ---");
        testMainWithEmptyArray();
        testMainWithLargeArgsArray();
        testMainWithEmptyStringArg();

        System.out.println("\n--- Behavioral Consistency ---");
        testOutputStableAcrossManyRuns();
        testThreadSafetyBasic();

        System.out.println("\n=== HelloTestExtra Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL TESTS PASSED");
        } else {
            System.out.println("Result: SOME TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testClassIsConcrete() {
        assertTest("Hello class is concrete (not abstract, interface, or enum)", () -> {
            Class<?> clazz = Class.forName("Hello");
            int mod = clazz.getModifiers();
            assertTrue(!Modifier.isAbstract(mod));
            assertTrue(!Modifier.isInterface(mod));
            assertTrue(!clazz.isEnum());
        });
    }

    private static void testClassHasDefaultConstructor() {
        assertTest("Hello class has a public no-arg constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?> ctor = clazz.getConstructor();
            assertNotNull(ctor);
            assertTrue(Modifier.isPublic(ctor.getModifiers()));
        });
    }

    private static void testClassHasExactlyOneDeclaredMethod() {
        assertTest("Hello class has exactly one declared method (main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            assertEquals(1, methods.length);
            assertEquals("main", methods[0].getName());
        });
    }

    private static void testClassNotFinal() {
        assertTest("Hello class is not final", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isFinal(clazz.getModifiers()));
        });
    }

    private static void testMainNoCheckedExceptions() {
        assertTest("main method declares no checked exceptions", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            Class<?>[] exceptions = main.getExceptionTypes();
            assertEquals(0, exceptions.length);
        });
    }

    private static void testOutputLengthExact() {
        assertTest("Output length is exactly 13 chars + system newline", () -> {
            String output = captureMainOutput();
            int expectedLen = "Hello, World!".length() + System.lineSeparator().length();
            assertEquals(expectedLen, output.length());
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

    private static void testOutputContainsNoTab() {
        assertTest("Output contains no tab characters", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("\t"));
        });
    }

    private static void testOutputContainsHelloCommaWorld() {
        assertTest("Output contains the exact substring 'Hello, World!'", () -> {
            String output = captureMainOutput();
            assertTrue(output.contains("Hello, World!"));
        });
    }

    private static void testMainWithEmptyArray() {
        assertTest("main() works with empty String array (not null)", () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(baos));
            try {
                Hello.main(new String[]{});
            } finally {
                System.setOut(originalOut);
            }
            assertEquals("Hello, World!", baos.toString().trim());
        });
    }

    private static void testMainWithLargeArgsArray() {
        assertTest("main() works with large args array (100 elements)", () -> {
            String[] largeArgs = new String[100];
            for (int i = 0; i < 100; i++) {
                largeArgs[i] = "arg" + i;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(baos));
            try {
                Hello.main(largeArgs);
            } finally {
                System.setOut(originalOut);
            }
            assertEquals("Hello, World!", baos.toString().trim());
        });
    }

    private static void testMainWithEmptyStringArg() {
        assertTest("main() works with empty string as argument", () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(baos));
            try {
                Hello.main(new String[]{""});
            } finally {
                System.setOut(originalOut);
            }
            assertEquals("Hello, World!", baos.toString().trim());
        });
    }

    private static void testOutputStableAcrossManyRuns() {
        assertTest("Output is stable across 50 consecutive runs", () -> {
            String first = captureMainOutput();
            for (int i = 0; i < 50; i++) {
                String output = captureMainOutput();
                assertEquals(first, output);
            }
        });
    }

    private static void testThreadSafetyBasic() {
        assertTest("Concurrent calls to main() do not crash", () -> {
            PrintStream devNull = new PrintStream(new ByteArrayOutputStream());
            PrintStream originalOut = System.out;
            System.setOut(devNull);
            try {
                Thread[] threads = new Thread[10];
                boolean[] errors = new boolean[10];
                for (int i = 0; i < threads.length; i++) {
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
                for (Thread t : threads) t.join();
                for (boolean err : errors) {
                    assertFalse(err);
                }
            } finally {
                System.setOut(originalOut);
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
