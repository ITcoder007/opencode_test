import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HelloTest {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloTest - Unit Tests for Hello.java ===\n");

        System.out.println("--- Basic Structure Tests ---");
        testClassExists();
        testClassName();
        testClassIsPublic();
        testClassNotAbstract();
        testClassNotInterface();
        testClassNotEnum();
        testClassNotFinal();
        testClassHasDefaultConstructor();

        System.out.println("\n--- Method Structure Tests ---");
        testClassHasMainMethod();
        testMainMethodSignature();
        testPrintGreetingExists();
        testPrintGreetingIsPrivate();
        testPrintGreetingIsStatic();
        testPrintGreetingReturnsVoid();
        testPrintGreetingHasNoParams();
        testDeclaredMethodCount();

        System.out.println("\n--- Output Correctness Tests ---");
        testMainOutput();
        testOutputMatchesPython();
        testOutputEndsWithNewline();
        testNoLeadingWhitespace();
        testNoTrailingSpacesBeforeNewline();
        testExactOutputFormat();
        testOutputContainsExpectedSubstring();
        testOutputLengthIsExpected();

        System.out.println("\n--- Robustness Tests ---");
        testMainWithNullArgs();
        testMainWithNonEmptyArgs();
        testMainWithEmptyStringArgs();
        testMainWithLargeArgsArray();
        testIdempotency();
        testMultipleRapidCalls();
        testSingleLineOutput();
        testSystemOutRestoredAfterCall();
        testNormalTermination();

        System.out.println("\n--- Concurrent Execution Tests ---");
        testConcurrentExecution();
        testConcurrentOutputConsistency();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL TESTS PASSED");
        } else {
            System.out.println("Result: SOME TESTS FAILED");
            System.exit(1);
        }
    }

    // ======================== Basic Structure Tests ========================

    private static void testClassExists() {
        assertTest("Hello class exists and can be loaded", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertNotNull(clazz);
        }, ClassNotFoundException.class);
    }

    private static void testClassName() {
        assertTest("Class is named 'Hello'", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals("Hello", clazz.getSimpleName());
        }, ClassNotFoundException.class);
    }

    private static void testClassIsPublic() {
        assertTest("Hello class is public", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertTrue(Modifier.isPublic(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassNotAbstract() {
        assertTest("Hello class is not abstract", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isAbstract(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassNotInterface() {
        assertTest("Hello class is not an interface", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(clazz.isInterface());
        }, ClassNotFoundException.class);
    }

    private static void testClassNotEnum() {
        assertTest("Hello class is not an enum", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(clazz.isEnum());
        }, ClassNotFoundException.class);
    }

    private static void testClassNotFinal() {
        assertTest("Hello class is not final", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isFinal(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassHasDefaultConstructor() {
        assertTest("Hello class has a default constructor (can be instantiated)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?> ctor = clazz.getDeclaredConstructor();
            assertNotNull(ctor);
            Object instance = ctor.newInstance();
            assertNotNull(instance);
        });
    }

    // ======================== Method Structure Tests ========================

    private static void testClassHasMainMethod() {
        assertTest("Hello has a main method", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            assertNotNull(main);
        }, NoSuchMethodException.class);
    }

    private static void testMainMethodSignature() {
        assertTest("main method is public static void with String[] param", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            int mod = main.getModifiers();
            assertTrue(Modifier.isPublic(mod));
            assertTrue(Modifier.isStatic(mod));
            assertEquals(void.class, main.getReturnType());
            Class<?>[] params = main.getParameterTypes();
            assertEquals(1, params.length);
            assertEquals(String[].class, params[0]);
        }, NoSuchMethodException.class);
    }

    private static void testPrintGreetingExists() {
        assertTest("printGreeting method exists", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method m = clazz.getDeclaredMethod("printGreeting");
            assertNotNull(m);
        });
    }

    private static void testPrintGreetingIsPrivate() {
        assertTest("printGreeting is private", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method m = clazz.getDeclaredMethod("printGreeting");
            assertTrue(Modifier.isPrivate(m.getModifiers()));
        });
    }

    private static void testPrintGreetingIsStatic() {
        assertTest("printGreeting is static", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method m = clazz.getDeclaredMethod("printGreeting");
            assertTrue(Modifier.isStatic(m.getModifiers()));
        });
    }

    private static void testPrintGreetingReturnsVoid() {
        assertTest("printGreeting returns void", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method m = clazz.getDeclaredMethod("printGreeting");
            assertEquals(void.class, m.getReturnType());
        });
    }

    private static void testPrintGreetingHasNoParams() {
        assertTest("printGreeting has no parameters", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method m = clazz.getDeclaredMethod("printGreeting");
            assertEquals(0, m.getParameterCount());
        });
    }

    private static void testDeclaredMethodCount() {
        assertTest("Hello has exactly 2 declared methods (main + printGreeting)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            assertEquals(2, methods.length);
        });
    }

    // ======================== Output Correctness Tests ========================

    private static void testMainOutput() {
        assertTest("main() outputs 'Hello, World!'", () -> {
            String output = captureMainOutput();
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testOutputMatchesPython() {
        assertTest("Java output matches Python print('Hello, World!') output", () -> {
            String output = captureMainOutput();
            String expected = "Hello, World!";
            assertEquals(expected, output.trim());
        });
    }

    private static void testOutputEndsWithNewline() {
        assertTest("Output ends with system newline (matches println behavior)", () -> {
            String output = captureMainOutput();
            assertTrue(output.endsWith(System.lineSeparator()));
        });
    }

    private static void testNoLeadingWhitespace() {
        assertTest("Output has no leading whitespace", () -> {
            String output = captureMainOutput();
            String content = output.replace(System.lineSeparator(), "");
            assertEquals(content, content.replaceAll("^\\s+", ""));
        });
    }

    private static void testNoTrailingSpacesBeforeNewline() {
        assertTest("No trailing spaces before newline", () -> {
            String output = captureMainOutput();
            String line = output.split(System.lineSeparator())[0];
            assertEquals(line, line.replaceAll("\\s+$", ""));
        });
    }

    private static void testExactOutputFormat() {
        assertTest("Exact output is 'Hello, World!' + newline", () -> {
            String output = captureMainOutput();
            assertEquals("Hello, World!" + System.lineSeparator(), output);
        });
    }

    private static void testOutputContainsExpectedSubstring() {
        assertTest("Output contains 'Hello, World' as substring", () -> {
            String output = captureMainOutput();
            assertTrue(output.contains("Hello, World"));
        });
    }

    private static void testOutputLengthIsExpected() {
        assertTest("Output length matches expected ('Hello, World!' + newline)", () -> {
            String output = captureMainOutput();
            int expectedLen = "Hello, World!".length() + System.lineSeparator().length();
            assertEquals(expectedLen, output.length());
        });
    }

    // ======================== Robustness Tests ========================

    private static void testMainWithNullArgs() {
        assertTest("main() handles null args without crashing", () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(baos));
            try {
                Hello.main(null);
            } finally {
                System.setOut(originalOut);
            }
            assertEquals("Hello, World!", baos.toString().trim());
        });
    }

    private static void testMainWithNonEmptyArgs() {
        assertTest("main() ignores extra arguments gracefully", () -> {
            String output = captureMainOutputWithArgs(new String[]{"arg1", "arg2"});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMainWithEmptyStringArgs() {
        assertTest("main() handles empty string args without issue", () -> {
            String output = captureMainOutputWithArgs(new String[]{"", ""});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMainWithLargeArgsArray() {
        assertTest("main() handles large args array (1000 elements)", () -> {
            String[] largeArgs = new String[1000];
            for (int i = 0; i < 1000; i++) {
                largeArgs[i] = "arg" + i;
            }
            String output = captureMainOutputWithArgs(largeArgs);
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testIdempotency() {
        assertTest("Running main() twice produces identical output", () -> {
            String first = captureMainOutput();
            String second = captureMainOutput();
            assertEquals(first, second);
        });
    }

    private static void testMultipleRapidCalls() {
        assertTest("Running main() 10 times rapidly produces consistent output", () -> {
            String first = captureMainOutput();
            for (int i = 0; i < 9; i++) {
                String subsequent = captureMainOutput();
                assertEquals(first, subsequent);
            }
        });
    }

    private static void testSingleLineOutput() {
        assertTest("Output is exactly one line", () -> {
            String output = captureMainOutput();
            String withoutTrailingNewline = output.endsWith(System.lineSeparator())
                ? output.substring(0, output.length() - System.lineSeparator().length())
                : output;
            assertFalse(withoutTrailingNewline.contains(System.lineSeparator()));
        });
    }

    private static void testSystemOutRestoredAfterCall() {
        assertTest("System.out is properly restored after main() call", () -> {
            PrintStream original = System.out;
            captureMainOutput();
            assertSame(original, System.out);
        });
    }

    private static void testNormalTermination() {
        assertTest("main() terminates normally without throwing exceptions", () -> {
            AtomicInteger exceptionCount = new AtomicInteger(0);
            Thread t = new Thread(() -> {
                try {
                    Hello.main(new String[]{});
                } catch (Throwable e) {
                    exceptionCount.incrementAndGet();
                }
            });
            t.start();
            t.join(5000);
            assertEquals(0, exceptionCount.get());
            assertFalse(t.isAlive());
        });
    }

    // ======================== Concurrent Execution Tests ========================

    private static void testConcurrentExecution() {
        assertTest("Concurrent calls to main() do not throw exceptions", () -> {
            int threadCount = 10;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            AtomicInteger errorCount = new AtomicInteger(0);

            PrintStream originalOut = System.out;
            PrintStream devNull = new PrintStream(new ByteArrayOutputStream());
            System.setOut(devNull);

            try {
                for (int i = 0; i < threadCount; i++) {
                    new Thread(() -> {
                        try {
                            startLatch.await(5, TimeUnit.SECONDS);
                            Hello.main(new String[]{});
                        } catch (Throwable e) {
                            errorCount.incrementAndGet();
                        } finally {
                            doneLatch.countDown();
                        }
                    }).start();
                }

                startLatch.countDown();
                assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
            } finally {
                System.setOut(originalOut);
            }

            assertEquals(0, errorCount.get());
        });
    }

    private static void testConcurrentOutputConsistency() {
        assertTest("Sequential concurrent-style calls each produce correct output", () -> {
            int count = 20;
            List<String> results = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                results.add(captureMainOutput());
            }
            String expected = "Hello, World!" + System.lineSeparator();
            for (String result : results) {
                assertEquals(expected, result);
            }
        });
    }

    // ======================== Helpers ========================

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

    private static void assertSame(Object expected, Object actual) {
        if (expected != actual) {
            throw new AssertionError("Expected same reference but got different objects");
        }
    }
}
