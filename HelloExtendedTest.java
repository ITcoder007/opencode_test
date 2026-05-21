import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloExtendedTest {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloExtendedTest - Extended Coverage Tests ===\n");

        System.out.println("--- Class Design Tests ---");
        testClassIsNotAbstract();
        testClassIsNotInterface();
        testClassHasDefaultConstructor();
        testClassHasNoPublicFields();
        testMainIsOnlyDeclaredMethod();
        testClassInDefaultPackage();

        System.out.println("\n--- Character & Encoding Tests ---");
        testOutputIsUTF8();
        testOutputContainsComma();
        testOutputContainsExclamationMark();
        testOutputContainsSpaceAfterComma();
        testOutputByteLength();
        testOutputCharCount();
        testNoNonASCIICharacters();
        testOutputIsPrintableASCII();

        System.out.println("\n--- Concurrency & Stress Tests ---");
        testConcurrentExecution();
        testRapidRepeatedExecution();
        testOutputConsistencyAcrossRuns();

        System.out.println("\n--- Semantic Correctness Tests ---");
        testOutputMatchesHelloWorldGreetingPattern();
        testOutputIsExactlyHelloWorldPhrase();
        testNoExtraWhitespaceInOutput();
        testMainDoesNotCallSystemExit();
        testMainReturnsNormally();

        System.out.println("\n--- Edge Case Tests ---");
        testMainWithEmptyStringArray();
        testMainWithSingleEmptyStringArg();
        testOutputSameAsPythonSysStdout();
        testClassCanBeLoadedMultipleTimes();

        System.out.println("\n=== Extended Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL TESTS PASSED");
        } else {
            System.out.println("Result: SOME TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testClassIsNotAbstract() {
        assertTest("Hello class is not abstract", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isAbstract(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassIsNotInterface() {
        assertTest("Hello class is not an interface", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isInterface(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassHasDefaultConstructor() {
        assertTest("Hello has a public default constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?>[] constructors = clazz.getConstructors();
            assertTrue(constructors.length >= 1);
            boolean hasNoArg = false;
            for (Constructor<?> c : constructors) {
                if (c.getParameterCount() == 0) {
                    hasNoArg = true;
                    assertTrue(Modifier.isPublic(c.getModifiers()));
                }
            }
            assertTrue(hasNoArg);
        }, ClassNotFoundException.class);
    }

    private static void testClassHasNoPublicFields() {
        assertTest("Hello class has no public fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            java.lang.reflect.Field[] fields = clazz.getFields();
            assertEquals(0, fields.length);
        }, ClassNotFoundException.class);
    }

    private static void testMainIsOnlyDeclaredMethod() {
        assertTest("main is the only declared public method", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            int publicStaticCount = 0;
            for (Method m : methods) {
                if (Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers())) {
                    publicStaticCount++;
                }
            }
            assertEquals(1, publicStaticCount);
        }, ClassNotFoundException.class);
    }

    private static void testClassInDefaultPackage() {
        assertTest("Hello class is in default (unnamed) package", () -> {
            Class<?> clazz = Class.forName("Hello");
            Package pkg = clazz.getPackage();
            assertTrue(pkg == null || pkg.getName().isEmpty());
        }, ClassNotFoundException.class);
    }

    private static void testOutputIsUTF8() {
        assertTest("Output bytes are valid UTF-8", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            String decoded = new String(bytes, StandardCharsets.UTF_8);
            assertEquals(output, decoded);
        });
    }

    private static void testOutputContainsComma() {
        assertTest("Output contains a comma character", () -> {
            String output = captureMainOutput();
            assertTrue(output.contains(","));
        });
    }

    private static void testOutputContainsExclamationMark() {
        assertTest("Output contains an exclamation mark", () -> {
            String output = captureMainOutput();
            assertTrue(output.contains("!"));
        });
    }

    private static void testOutputContainsSpaceAfterComma() {
        assertTest("Output has a space after the comma", () -> {
            String output = captureMainOutput();
            assertTrue(output.contains(", "));
        });
    }

    private static void testOutputByteLength() {
        assertTest("Output byte length matches expected", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            int expectedLen = "Hello, World!".getBytes(StandardCharsets.UTF_8).length
                    + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;
            assertEquals(expectedLen, bytes.length);
        });
    }

    private static void testOutputCharCount() {
        assertTest("Output character count matches expected", () -> {
            String output = captureMainOutput();
            int expectedChars = "Hello, World!".length() + System.lineSeparator().length();
            assertEquals(expectedChars, output.length());
        });
    }

    private static void testNoNonASCIICharacters() {
        assertTest("Output contains only ASCII characters", () -> {
            String output = captureMainOutput();
            for (char c : output.toCharArray()) {
                assertTrue(c < 128);
            }
        });
    }

    private static void testOutputIsPrintableASCII() {
        assertTest("All output characters are printable ASCII or newline", () -> {
            String output = captureMainOutput();
            for (char c : output.toCharArray()) {
                boolean printable = (c >= 32 && c <= 126) || c == '\n' || c == '\r';
                assertTrue(printable);
            }
        });
    }

    private static void testConcurrentExecution() {
        assertTest("Concurrent execution of main() does not crash", () -> {
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
            PrintStream originalOut = System.out;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));

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
            }
            executor.shutdown();
            if (!errors.isEmpty()) {
                throw new AssertionError("Concurrent errors: " + errors.size() + " - first: " + errors.get(0).getMessage());
            }
            String combined = baos.toString().trim();
            String[] lines = combined.split(System.lineSeparator());
            assertEquals(threadCount, lines.length);
            for (String line : lines) {
                assertEquals("Hello, World!", line.trim());
            }
        });
    }

    private static void testRapidRepeatedExecution() {
        assertTest("Rapid 20x execution all produce correct output", () -> {
            for (int i = 0; i < 20; i++) {
                String output = captureMainOutput();
                assertEquals("Hello, World!", output.trim());
            }
        });
    }

    private static void testOutputConsistencyAcrossRuns() {
        assertTest("Output is consistent across 10 sequential runs", () -> {
            String first = captureMainOutput();
            for (int i = 1; i < 10; i++) {
                String current = captureMainOutput();
                assertEquals(first, current);
            }
        });
    }

    private static void testOutputMatchesHelloWorldGreetingPattern() {
        assertTest("Output matches 'Hello' greeting pattern", () -> {
            String output = captureMainOutput().trim();
            assertTrue(output.startsWith("Hello"));
            assertTrue(output.contains("World"));
        });
    }

    private static void testOutputIsExactlyHelloWorldPhrase() {
        assertTest("Output is exactly the classic 'Hello, World!' phrase", () -> {
            String output = captureMainOutput().trim();
            assertEquals("Hello, World!", output);
            assertTrue(output.startsWith("Hello, "));
            assertTrue(output.endsWith("World!"));
        });
    }

    private static void testNoExtraWhitespaceInOutput() {
        assertTest("No double spaces or tabs in output", () -> {
            String output = captureMainOutput().trim();
            assertFalse(output.contains("  "));
            assertFalse(output.contains("\t"));
        });
    }

    private static void testMainDoesNotCallSystemExit() {
        assertTest("main() does not call System.exit (returns normally)", () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(baos));
            try {
                Hello.main(new String[]{});
            } finally {
                System.setOut(originalOut);
            }
            assertTrue(true);
        });
    }

    private static void testMainReturnsNormally() {
        assertTest("main() completes without throwing any exception", () -> {
            boolean completed = false;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(baos));
            try {
                Hello.main(new String[]{});
                completed = true;
            } finally {
                System.setOut(originalOut);
            }
            assertTrue(completed);
        });
    }

    private static void testMainWithEmptyStringArray() {
        assertTest("main({}) with empty string array produces correct output", () -> {
            String output = captureMainOutputWithArgs(new String[]{});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMainWithSingleEmptyStringArg() {
        assertTest("main with single empty string arg produces correct output", () -> {
            String output = captureMainOutputWithArgs(new String[]{""});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testOutputSameAsPythonSysStdout() {
        assertTest("Java output is same as Python print('Hello, World!') to stdout", () -> {
            String javaOutput = captureMainOutput().trim();
            String expectedPythonOutput = "Hello, World!";
            assertEquals(expectedPythonOutput, javaOutput);
        });
    }

    private static void testClassCanBeLoadedMultipleTimes() {
        assertTest("Hello class can be loaded multiple times via reflection", () -> {
            for (int i = 0; i < 20; i++) {
                Class<?> clazz = Class.forName("Hello");
                assertNotNull(clazz);
                assertEquals("Hello", clazz.getSimpleName());
            }
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
