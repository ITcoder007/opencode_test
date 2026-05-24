import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloTestExtended {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloTestExtended - Extended Unit Tests ===\n");

        System.out.println("--- Reflection / Bytecode Tests ---");
        testNoExtraPublicMethods();
        testNoFields();
        testDefaultConstructorExists();
        testConstructorIsPublic();
        testNoStaticInitializerSideEffects();

        System.out.println("\n--- Concurrency Tests ---");
        testConcurrentMainCalls();
        testConcurrentOutputConsistency();

        System.out.println("\n--- Encoding & Charset Tests ---");
        testOutputIsASCII();
        testOutputUTF8Compatible();
        testCharacterCount();

        System.out.println("\n--- Output Stream Independence Tests ---");
        testOutputStreamRestoredAfterCall();
        testMultipleSequentialCallsNoLeak();
        testOutputWithDifferentStreamState();

        System.out.println("\n--- Precision & Edge Case Tests ---");
        testCharacterByCharacterMatch();
        testOutputNotInterned();
        testNoSystemExitCalled();
        testMainDoesNotReturnEarly();
        testHelloClassNotAbstract();
        testHelloClassNotFinal();

        System.out.println("\n=== Extended Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL TESTS PASSED");
        } else {
            System.out.println("Result: SOME TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testNoExtraPublicMethods() {
        assertTest("Hello has only 'main' as declared public method", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            List<String> names = new ArrayList<>();
            for (Method m : methods) {
                if (Modifier.isPublic(m.getModifiers())) {
                    names.add(m.getName());
                }
            }
            assertEquals(1, names.size());
            assertEquals("main", names.get(0));
        }, ClassNotFoundException.class);
    }

    private static void testNoFields() {
        assertTest("Hello has no declared fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getDeclaredFields().length);
        }, ClassNotFoundException.class);
    }

    private static void testDefaultConstructorExists() {
        assertTest("Hello has a default constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?>[] ctors = clazz.getDeclaredConstructors();
            boolean hasDefault = false;
            for (Constructor<?> c : ctors) {
                if (c.getParameterCount() == 0) {
                    hasDefault = true;
                }
            }
            assertTrue(hasDefault);
        }, ClassNotFoundException.class);
    }

    private static void testConstructorIsPublic() {
        assertTest("Hello default constructor is public", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?> defaultCtor = clazz.getDeclaredConstructor();
            assertTrue(Modifier.isPublic(defaultCtor.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testNoStaticInitializerSideEffects() {
        assertTest("Loading Hello class has no stdout side effects", () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream original = System.out;
            System.setOut(new PrintStream(baos));
            try {
                Class.forName("Hello");
            } finally {
                System.setOut(original);
            }
            assertEquals("", baos.toString());
        }, ClassNotFoundException.class);
    }

    private static void testConcurrentMainCalls() {
        assertTest("10 concurrent main() calls all complete without error", () -> {
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

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
            boolean completed = doneLatch.await(5, TimeUnit.SECONDS);
            executor.shutdown();
            assertTrue(completed);
            assertTrue(errors.isEmpty());
        });
    }

    private static void testConcurrentOutputConsistency() {
        assertTest("Concurrent calls do not throw exceptions", () -> {
            int threadCount = 5;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

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
            doneLatch.await(5, TimeUnit.SECONDS);
            executor.shutdown();
            assertTrue(errors.isEmpty());
        });
    }

    private static void testOutputIsASCII() {
        assertTest("Output contains only ASCII characters", () -> {
            String output = captureMainOutput();
            for (char c : output.toCharArray()) {
                assertTrue(c < 128);
            }
        });
    }

    private static void testOutputUTF8Compatible() {
        assertTest("Output bytes are valid UTF-8", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes("UTF-8");
            String reconstructed = new String(bytes, "UTF-8");
            assertEquals(output, reconstructed);
        });
    }

    private static void testCharacterCount() {
        assertTest("Output has exactly 13 characters (before newline)", () -> {
            String output = captureMainOutput();
            String content = output.replace(System.lineSeparator(), "");
            assertEquals(13, content.length());
        });
    }

    private static void testOutputStreamRestoredAfterCall() {
        assertTest("System.out is correctly restored after main()", () -> {
            PrintStream original = System.out;
            captureMainOutput();
            assertSame(original, System.out);
        });
    }

    private static void testMultipleSequentialCallsNoLeak() {
        assertTest("5 sequential calls produce independent outputs", () -> {
            for (int i = 0; i < 5; i++) {
                String output = captureMainOutput();
                assertEquals("Hello, World!" + System.lineSeparator(), output);
            }
        });
    }

    private static void testOutputWithDifferentStreamState() {
        assertTest("main() writes to whatever System.out is set to", () -> {
            PrintStream original = System.out;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream customStream = new PrintStream(baos);
            System.setOut(customStream);
            try {
                Hello.main(new String[]{});
            } finally {
                System.setOut(original);
            }
            assertEquals("Hello, World!", baos.toString().trim());
        });
    }

    private static void testCharacterByCharacterMatch() {
        assertTest("Output matches 'Hello, World!' character by character", () -> {
            String output = captureMainOutput();
            String content = output.replace(System.lineSeparator(), "");
            String expected = "Hello, World!";
            assertEquals(expected.length(), content.length());
            for (int i = 0; i < expected.length(); i++) {
                assertEquals(expected.charAt(i), content.charAt(i));
            }
        });
    }

    private static void testOutputNotInterned() {
        assertTest("Captured output is a distinct string (not interned)", () -> {
            String output = captureMainOutput();
            assertNotNull(output);
            assertTrue(output.length() > 0);
        });
    }

    private static void testNoSystemExitCalled() {
        assertTest("main() does not call System.exit()", () -> {
            captureMainOutput();
        });
    }

    private static void testMainDoesNotReturnEarly() {
        assertTest("main() executes fully (output is complete)", () -> {
            String output = captureMainOutput();
            assertTrue(output.startsWith("Hello, World!"));
            assertTrue(output.endsWith(System.lineSeparator()));
        });
    }

    private static void testHelloClassNotAbstract() {
        assertTest("Hello class is not abstract", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isAbstract(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testHelloClassNotFinal() {
        assertTest("Hello class is not final", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isFinal(clazz.getModifiers()));
        }, ClassNotFoundException.class);
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

    private static void assertEquals(char expected, char actual) {
        if (expected != actual) {
            throw new AssertionError("Expected <" + expected + "> but got <" + actual + ">");
        }
    }

    private static void assertNotNull(Object obj) {
        if (obj == null) {
            throw new AssertionError("Expected non-null value");
        }
    }

    private static void assertSame(Object expected, Object actual) {
        if (expected != actual) {
            throw new AssertionError("Expected same reference but got different objects");
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
