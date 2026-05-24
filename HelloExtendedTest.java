import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

public class HelloExtendedTest {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloExtendedTest - Additional Coverage ===\n");

        System.out.println("--- Class Completeness Tests ---");
        testHelloExtendsObject();
        testHelloImplementsNoInterfaces();
        testHelloHasNoDeclaredFields();
        testHelloHasOnlyMainMethod();
        testHelloHasDefaultConstructor();
        testDefaultConstructorIsPublic();

        System.out.println("\n--- Stderr Isolation Tests ---");
        testMainDoesNotWriteToStderr();
        testMainDoesNotWriteToStderrWithArgs();

        System.out.println("\n--- Output Characteristic Tests ---");
        testOutputIsPureASCII();
        testOutputLengthIsExact();
        testOutputDoesNotContainTab();
        testOutputDoesNotContainCarriageReturn();

        System.out.println("\n--- Argument Boundary Tests ---");
        testMainWithEmptyStringArray();
        testMainWithLargeArgsArray();
        testMainWithSingleEmptyArg();
        testMainWithUnicodeArgs();

        System.out.println("\n--- Concurrency Tests ---");
        testConcurrentMainCalls();
        testConcurrentMainOutputConsistency();

        System.out.println("\n--- Behavioral Tests ---");
        testMainDoesNotThrowException();
        testMainDoesNotSetSystemExit();
        testMainReturnTypeIsVoid();
        testMainIsNotSynchronized();
        testClassIsNotFinal();
        testClassIsNotAbstract();

        System.out.println("\n--- Repeated Execution Stress Tests ---");
        testRepeatedExecution100Times();
        testRepeatedExecutionOutputConsistency();

        System.out.println("\n=== Extended Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL EXTENDED TESTS PASSED");
        } else {
            System.out.println("Result: SOME EXTENDED TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testHelloExtendsObject() {
        assertTest("Hello extends Object directly", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(Object.class, clazz.getSuperclass());
        }, ClassNotFoundException.class);
    }

    private static void testHelloImplementsNoInterfaces() {
        assertTest("Hello implements no interfaces", () -> {
            Class<?> clazz = Class.forName("Hello");
            Class<?>[] interfaces = clazz.getInterfaces();
            assertEquals(0, interfaces.length);
        }, ClassNotFoundException.class);
    }

    private static void testHelloHasNoDeclaredFields() {
        assertTest("Hello has no declared fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            Field[] fields = clazz.getDeclaredFields();
            assertEquals(0, fields.length);
        }, ClassNotFoundException.class);
    }

    private static void testHelloHasOnlyMainMethod() {
        assertTest("Hello has exactly one declared method (main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            assertEquals(1, methods.length);
            assertEquals("main", methods[0].getName());
        }, ClassNotFoundException.class);
    }

    private static void testHelloHasDefaultConstructor() {
        assertTest("Hello has a default constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            assertTrue(constructors.length >= 1);
            boolean hasDefault = false;
            for (Constructor<?> c : constructors) {
                if (c.getParameterCount() == 0) {
                    hasDefault = true;
                }
            }
            assertTrue(hasDefault);
        }, ClassNotFoundException.class);
    }

    private static void testDefaultConstructorIsPublic() {
        assertTest("Default constructor is public", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?> defaultCtor = clazz.getDeclaredConstructor();
            assertTrue(Modifier.isPublic(defaultCtor.getModifiers()));
        }, Exception.class);
    }

    private static void testMainDoesNotWriteToStderr() {
        assertTest("main() does not write to stderr", () -> {
            PrintStream originalErr = System.err;
            ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
            ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setErr(new PrintStream(baosErr));
            System.setOut(new PrintStream(baosOut));
            try {
                Hello.main(new String[]{});
            } finally {
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
            assertEquals("", baosErr.toString());
        });
    }

    private static void testMainDoesNotWriteToStderrWithArgs() {
        assertTest("main() does not write to stderr with non-empty args", () -> {
            PrintStream originalErr = System.err;
            ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
            ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setErr(new PrintStream(baosErr));
            System.setOut(new PrintStream(baosOut));
            try {
                Hello.main(new String[]{"test"});
            } finally {
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
            assertEquals("", baosErr.toString());
        });
    }

    private static void testOutputIsPureASCII() {
        assertTest("Output is pure ASCII (no non-ASCII bytes)", () -> {
            String output = captureMainOutput();
            for (char c : output.toCharArray()) {
                assertTrue(c < 128);
            }
        });
    }

    private static void testOutputLengthIsExact() {
        assertTest("Output length is exactly 'Hello, World!' + newline length", () -> {
            String output = captureMainOutput();
            int expectedLength = "Hello, World!".length() + System.lineSeparator().length();
            assertEquals(expectedLength, output.length());
        });
    }

    private static void testOutputDoesNotContainTab() {
        assertTest("Output does not contain tab character", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("\t"));
        });
    }

    private static void testOutputDoesNotContainCarriageReturn() {
        assertTest("Output does not contain standalone carriage return", () -> {
            String output = captureMainOutput();
            String withoutNewline = output.replace(System.lineSeparator(), "");
            assertFalse(withoutNewline.contains("\r"));
        });
    }

    private static void testMainWithEmptyStringArray() {
        assertTest("main() works with empty String array element", () -> {
            String output = captureMainOutputWithArgs(new String[]{""});
            assertEquals("Hello, World!" + System.lineSeparator(), output);
        });
    }

    private static void testMainWithLargeArgsArray() {
        assertTest("main() handles large args array (1000 elements)", () -> {
            String[] largeArgs = new String[1000];
            for (int i = 0; i < 1000; i++) {
                largeArgs[i] = "arg" + i;
            }
            String output = captureMainOutputWithArgs(largeArgs);
            assertEquals("Hello, World!" + System.lineSeparator(), output);
        });
    }

    private static void testMainWithSingleEmptyArg() {
        assertTest("main() with single empty string arg produces correct output", () -> {
            String output = captureMainOutputWithArgs(new String[]{""});
            String expected = "Hello, World!" + System.lineSeparator();
            assertEquals(expected, output);
        });
    }

    private static void testMainWithUnicodeArgs() {
        assertTest("main() with Unicode args does not affect output", () -> {
            String output = captureMainOutputWithArgs(new String[]{"\u4f60\u597d", "\u4e16\u754c"});
            assertEquals("Hello, World!" + System.lineSeparator(), output);
        });
    }

    private static void testConcurrentMainCalls() {
        assertTest("Concurrent main() calls from 10 threads all succeed", () -> {
            int threadCount = 10;
            CyclicBarrier barrier = new CyclicBarrier(threadCount);
            AtomicBoolean anyFailure = new AtomicBoolean(false);
            List<Thread> threads = new ArrayList<>();

            for (int i = 0; i < threadCount; i++) {
                Thread t = new Thread(() -> {
                    try {
                        barrier.await();
                        Hello.main(new String[]{});
                    } catch (Exception e) {
                        anyFailure.set(true);
                    }
                });
                threads.add(t);
                t.start();
            }

            for (Thread t : threads) {
                t.join(5000);
            }

            assertFalse(anyFailure.get());
        });
    }

    private static void testConcurrentMainOutputConsistency() {
        assertTest("Concurrent main() calls produce consistent output", () -> {
            int threadCount = 5;
            CyclicBarrier barrier = new CyclicBarrier(threadCount);
            String[] results = new String[threadCount];
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    try {
                        barrier.await();
                        results[index] = captureMainOutput();
                    } catch (Exception e) {
                        results[index] = "ERROR: " + e.getMessage();
                    }
                });
                threads[i].start();
            }

            for (Thread t : threads) {
                t.join(5000);
            }

            String expected = "Hello, World!" + System.lineSeparator();
            for (String result : results) {
                assertEquals(expected, result);
            }
        });
    }

    private static void testMainDoesNotThrowException() {
        assertTest("main() does not throw any exception with normal args", () -> {
            try {
                Hello.main(new String[]{});
                assertTrue(true);
            } catch (Throwable t) {
                throw new AssertionError("main() threw unexpected: " + t.getClass().getSimpleName() + ": " + t.getMessage());
            }
        });
    }

    private static void testMainDoesNotSetSystemExit() {
        assertTest("main() does not call System.exit()", () -> {
            Hello.main(new String[]{});
            assertTrue(true);
        });
    }

    private static void testMainReturnTypeIsVoid() {
        assertTest("main method return type is void", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            assertEquals(void.class, main.getReturnType());
        }, Exception.class);
    }

    private static void testMainIsNotSynchronized() {
        assertTest("main method is not synchronized", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            assertFalse(Modifier.isSynchronized(main.getModifiers()));
        }, Exception.class);
    }

    private static void testClassIsNotFinal() {
        assertTest("Hello class is not final", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isFinal(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassIsNotAbstract() {
        assertTest("Hello class is not abstract", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isAbstract(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testRepeatedExecution100Times() {
        assertTest("main() executes 100 times without failure", () -> {
            for (int i = 0; i < 100; i++) {
                Hello.main(new String[]{});
            }
            assertTrue(true);
        });
    }

    private static void testRepeatedExecutionOutputConsistency() {
        assertTest("100 consecutive executions produce identical output", () -> {
            String expected = "Hello, World!" + System.lineSeparator();
            for (int i = 0; i < 100; i++) {
                String output = captureMainOutput();
                assertEquals(expected, output);
            }
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
        assertTest(name, test, null);
    }

    private static void assertTest(String name, ThrowingRunnable test, Class<? extends Exception> allowed) {
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
                System.out.println("  FAIL: " + name + " - Expected no exception but got: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            } else {
                failed++;
                System.out.println("  FAIL: " + name + " - Exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
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
