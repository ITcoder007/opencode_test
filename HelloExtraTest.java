import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloExtraTest {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloExtraTest - Supplementary Tests for Hello.java ===\n");

        System.out.println("--- Instantiation Tests ---");
        testDefaultConstructorExists();
        testCanInstantiate();

        System.out.println("\n--- Class Integrity Tests ---");
        testNoDeclaredFields();
        testOnlyMainMethod();
        testClassNotAbstract();
        testClassNotFinal();
        testDefaultPackage();
        testExtendsObject();

        System.out.println("\n--- Side Effect Tests ---");
        testStderrUnaffected();
        testSystemOutRestored();

        System.out.println("\n--- Concurrency Tests ---");
        testConcurrentExecution();

        System.out.println("\n--- Encoding Tests ---");
        testOutputIsAscii();

        System.out.println("\n--- Boundary Tests ---");
        testMainWithEmptyStringArray();
        testMainWithLargeArgArray();

        System.out.println("\n=== Extra Test Summary ===");
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
            Constructor<?>[] ctors = Hello.class.getDeclaredConstructors();
            boolean hasDefault = false;
            for (Constructor<?> c : ctors) {
                if (c.getParameterCount() == 0) {
                    hasDefault = true;
                    break;
                }
            }
            assertTrue(hasDefault);
        });
    }

    private static void testCanInstantiate() {
        assertTest("Hello can be instantiated with new", () -> {
            Object instance = Hello.class.getDeclaredConstructor().newInstance();
            assertNotNull(instance);
            assertTrue(instance instanceof Hello);
        });
    }

    private static void testNoDeclaredFields() {
        assertTest("Hello has no declared fields", () -> {
            Field[] fields = Hello.class.getDeclaredFields();
            assertEquals(0, fields.length);
        });
    }

    private static void testOnlyMainMethod() {
        assertTest("Hello has exactly one declared method (main)", () -> {
            Method[] methods = Hello.class.getDeclaredMethods();
            assertEquals(1, methods.length);
            assertEquals("main", methods[0].getName());
        });
    }

    private static void testClassNotAbstract() {
        assertTest("Hello class is not abstract", () -> {
            assertFalse(Modifier.isAbstract(Hello.class.getModifiers()));
        });
    }

    private static void testClassNotFinal() {
        assertTest("Hello class is not final", () -> {
            assertFalse(Modifier.isFinal(Hello.class.getModifiers()));
        });
    }

    private static void testDefaultPackage() {
        assertTest("Hello is in default package", () -> {
            String pkg = Hello.class.getPackage() == null ? "" : Hello.class.getPackage().getName();
            assertEquals("", pkg);
        });
    }

    private static void testExtendsObject() {
        assertTest("Hello extends Object directly", () -> {
            assertEquals(Object.class, Hello.class.getSuperclass());
        });
    }

    private static void testStderrUnaffected() {
        assertTest("main() does not write to stderr", () -> {
            ByteArrayOutputStream errBaos = new ByteArrayOutputStream();
            PrintStream originalErr = System.err;
            System.setErr(new PrintStream(errBaos));
            try {
                Hello.main(new String[]{});
            } finally {
                System.setErr(originalErr);
            }
            assertEquals("", errBaos.toString());
        });
    }

    private static void testSystemOutRestored() {
        assertTest("System.out is unchanged after main()", () -> {
            PrintStream before = System.out;
            Hello.main(new String[]{});
            PrintStream after = System.out;
            assertTrue(before == after);
        });
    }

    private static void testConcurrentExecution() {
        assertTest("Concurrent main() calls do not throw exceptions", () -> {
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

            ByteArrayOutputStream sharedBaos = new ByteArrayOutputStream();
            PrintStream sharedOut = new PrintStream(sharedBaos);
            PrintStream originalOut = System.out;
            System.setOut(sharedOut);
            try {
                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        try {
                            startLatch.await();
                            Hello.main(new String[]{});
                        } catch (Throwable t) {
                            errors.add(t);
                        } finally {
                            doneLatch.countDown();
                        }
                    });
                }
                startLatch.countDown();
                assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
            } finally {
                System.setOut(originalOut);
                executor.shutdown();
            }
            assertEquals(0, errors.size());
            String output = sharedBaos.toString().trim();
            assertTrue(output.contains("Hello, World!"));
        });
    }

    private static void testOutputIsAscii() {
        assertTest("Output contains only ASCII characters", () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(baos));
            try {
                Hello.main(new String[]{});
            } finally {
                System.setOut(originalOut);
            }
            String output = baos.toString();
            for (char c : output.toCharArray()) {
                assertTrue(c < 128);
            }
        });
    }

    private static void testMainWithEmptyStringArray() {
        assertTest("main() works with new String[]{\"\"}", () -> {
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

    private static void testMainWithLargeArgArray() {
        assertTest("main() works with 1000 arguments", () -> {
            String[] largeArgs = new String[1000];
            for (int i = 0; i < 1000; i++) {
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
