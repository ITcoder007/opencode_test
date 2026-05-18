import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HelloExtendedTest {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;
    private static final List<String> failures = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("=== HelloExtendedTest - Extended Unit Tests for Hello.java ===\n");

        System.out.println("--- greet() Method Structure Tests ---");
        testGreetMethodExists();
        testGreetMethodIsPrivate();
        testGreetMethodIsStatic();
        testGreetMethodReturnsVoid();
        testGreetMethodHasNoParameters();
        testGreetMethodOutputMatchesMain();

        System.out.println("\n--- Class Metadata Tests ---");
        testClassIsNotAbstract();
        testClassIsNotFinal();
        testClassHasNoFields();
        testClassHasNoOtherPublicMethods();
        testClassDeclaringClassLoader();

        System.out.println("\n--- Byte-Level Output Tests ---");
        testOutputBytesMatchUTF8();
        testOutputByteLength();
        testOutputContainsNoBOM();
        testOutputIsPureASCII();

        System.out.println("\n--- stderr Tests ---");
        testNoStderrOutput();
        testNoStderrWithArgs();

        System.out.println("\n--- Concurrency Tests ---");
        testConcurrentExecution();
        testConcurrentExecutionConsistency();

        System.out.println("\n--- Stress / Repetition Tests ---");
        testMainCalled100Times();
        testMainCalledWithEmptyStringArray();

        System.out.println("\n--- Character-Level Output Tests ---");
        testOutputCharacterByCharacter();
        testOutputLengthExact();
        testOutputContainsNoTabOrCR();
        testCommaAndSpacePosition();

        System.out.println("\n--- Behavioral Equivalence Tests ---");
        testJavaOutputEqualsPythonOutput();
        testNoSystemExitCalled();
        testMainDoesNotThrowCheckedException();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed > 0) {
            System.out.println("\nFailed tests:");
            for (String f : failures) {
                System.out.println("  - " + f);
            }
            System.out.println("\nResult: SOME TESTS FAILED");
            System.exit(1);
        } else {
            System.out.println("Result: ALL TESTS PASSED");
        }
    }

    private static void testGreetMethodExists() {
        assertTest("greet() method exists", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method greet = clazz.getDeclaredMethod("greet");
            assertNotNull(greet);
        });
    }

    private static void testGreetMethodIsPrivate() {
        assertTest("greet() is private", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method greet = clazz.getDeclaredMethod("greet");
            assertTrue(Modifier.isPrivate(greet.getModifiers()));
        });
    }

    private static void testGreetMethodIsStatic() {
        assertTest("greet() is static", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method greet = clazz.getDeclaredMethod("greet");
            assertTrue(Modifier.isStatic(greet.getModifiers()));
        });
    }

    private static void testGreetMethodReturnsVoid() {
        assertTest("greet() returns void", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method greet = clazz.getDeclaredMethod("greet");
            assertEquals(void.class, greet.getReturnType());
        });
    }

    private static void testGreetMethodHasNoParameters() {
        assertTest("greet() has no parameters", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method greet = clazz.getDeclaredMethod("greet");
            assertEquals(0, greet.getParameterCount());
        });
    }

    private static void testGreetMethodOutputMatchesMain() {
        assertTest("greet() output matches main() output", () -> {
            String mainOutput = captureMainOutput();
            String greetOutput = captureGreetOutput();
            assertEquals(mainOutput, greetOutput);
        });
    }

    private static void testClassIsNotAbstract() {
        assertTest("Hello class is not abstract", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isAbstract(clazz.getModifiers()));
        });
    }

    private static void testClassIsNotFinal() {
        assertTest("Hello class is not final", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isFinal(clazz.getModifiers()));
        });
    }

    private static void testClassHasNoFields() {
        assertTest("Hello class has no declared fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            Field[] fields = clazz.getDeclaredFields();
            assertEquals(0, fields.length);
        });
    }

    private static void testClassHasNoOtherPublicMethods() {
        assertTest("Only public method is main()", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            int publicCount = 0;
            for (Method m : methods) {
                if (Modifier.isPublic(m.getModifiers())) {
                    publicCount++;
                }
            }
            assertEquals(1, publicCount);
        });
    }

    private static void testClassDeclaringClassLoader() {
        assertTest("Hello class is top-level (not a member class)", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertNull(clazz.getDeclaringClass());
        });
    }

    private static void testOutputBytesMatchUTF8() {
        assertTest("Output bytes match UTF-8 encoded 'Hello, World!\\n'", () -> {
            String output = captureMainOutput();
            byte[] expected = ("Hello, World!" + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
            byte[] actual = output.getBytes(StandardCharsets.UTF_8);
            assertArrayEquals(expected, actual);
        });
    }

    private static void testOutputByteLength() {
        assertTest("Output byte length is correct (13 chars + newline)", () -> {
            String output = captureMainOutput();
            int expectedLen = "Hello, World!".getBytes(StandardCharsets.UTF_8).length
                    + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;
            byte[] actual = output.getBytes(StandardCharsets.UTF_8);
            assertEquals(expectedLen, actual.length);
        });
    }

    private static void testOutputContainsNoBOM() {
        assertTest("Output does not start with UTF-8 BOM", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            if (bytes.length >= 3) {
                assertFalse((bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF);
            }
        });
    }

    private static void testOutputIsPureASCII() {
        assertTest("All output characters are ASCII (0x00-0x7F)", () -> {
            String output = captureMainOutput();
            for (int i = 0; i < output.length(); i++) {
                char c = output.charAt(i);
                assertTrue(c <= 0x7F);
            }
        });
    }

    private static void testNoStderrOutput() {
        assertTest("main() writes nothing to stderr", () -> {
            ByteArrayOutputStream errBaos = new ByteArrayOutputStream();
            PrintStream originalErr = System.err;
            System.setErr(new PrintStream(errBaos));
            try {
                captureMainOutput();
            } finally {
                System.setErr(originalErr);
            }
            assertEquals("", errBaos.toString());
        });
    }

    private static void testNoStderrWithArgs() {
        assertTest("main() writes nothing to stderr even with args", () -> {
            ByteArrayOutputStream errBaos = new ByteArrayOutputStream();
            PrintStream originalErr = System.err;
            System.setErr(new PrintStream(errBaos));
            try {
                captureMainOutputWithArgs(new String[]{"test"});
            } finally {
                System.setErr(originalErr);
            }
            assertEquals("", errBaos.toString());
        });
    }

    private static void testConcurrentExecution() {
        assertTest("Concurrent calls to main() all succeed", () -> {
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        PrintStream originalOut = System.out;
                        System.setOut(new PrintStream(baos));
                        try {
                            Hello.main(new String[]{});
                        } finally {
                            System.setOut(originalOut);
                        }
                        String out = baos.toString().trim();
                        if ("Hello, World!".equals(out)) {
                            successCount.incrementAndGet();
                        } else {
                            failCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            boolean completed = latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();
            assertTrue(completed);
            assertEquals(threadCount, successCount.get() + failCount.get());
            assertEquals(0, failCount.get());
        });
    }

    private static void testConcurrentExecutionConsistency() {
        assertTest("All concurrent outputs are identical", () -> {
            int threadCount = 10;
            List<String> outputs = new ArrayList<>();
            Object lock = new Object();

            Thread[] threads = new Thread[threadCount];
            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PrintStream originalOut = System.out;
                    System.setOut(new PrintStream(baos));
                    try {
                        Hello.main(new String[]{});
                    } finally {
                        System.setOut(originalOut);
                    }
                    synchronized (lock) {
                        outputs.add(baos.toString());
                    }
                });
                threads[i].start();
            }

            for (Thread t : threads) {
                t.join(5000);
            }

            assertEquals(threadCount, outputs.size());
            String first = outputs.get(0);
            for (String out : outputs) {
                assertEquals(first, out);
            }
        });
    }

    private static void testMainCalled100Times() {
        assertTest("Calling main() 100 times produces consistent output", () -> {
            String first = captureMainOutput();
            for (int i = 0; i < 99; i++) {
                String output = captureMainOutput();
                assertEquals(first, output);
            }
        });
    }

    private static void testMainCalledWithEmptyStringArray() {
        assertTest("main(new String[]{''}) handles empty string arg", () -> {
            String output = captureMainOutputWithArgs(new String[]{""});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testOutputCharacterByCharacter() {
        assertTest("Output characters match 'Hello, World!' exactly", () -> {
            String output = captureMainOutput();
            String content = output.replace(System.lineSeparator(), "");
            String expected = "Hello, World!";
            for (int i = 0; i < expected.length(); i++) {
                assertEquals(expected.charAt(i), content.charAt(i));
            }
            assertEquals(expected.length(), content.length());
        });
    }

    private static void testOutputLengthExact() {
        assertTest("Output length is exactly 13 + newline length", () -> {
            String output = captureMainOutput();
            assertEquals("Hello, World!".length() + System.lineSeparator().length(), output.length());
        });
    }

    private static void testOutputContainsNoTabOrCR() {
        assertTest("Output contains no tab or carriage return", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("\t"));
            assertFalse(output.contains("\r"));
        });
    }

    private static void testCommaAndSpacePosition() {
        assertTest("Comma at position 5, space at position 6", () -> {
            String output = captureMainOutput().replace(System.lineSeparator(), "");
            assertEquals(',', output.charAt(5));
            assertEquals(' ', output.charAt(6));
        });
    }

    private static void testJavaOutputEqualsPythonOutput() {
        assertTest("Java output is semantically identical to Python output", () -> {
            String javaOutput = captureMainOutput().trim();
            String pythonOutput = "Hello, World!";
            assertEquals(pythonOutput, javaOutput);
        });
    }

    private static void testNoSystemExitCalled() {
        assertTest("main() does not call System.exit (method completes normally)", () -> {
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

    private static void testMainDoesNotThrowCheckedException() {
        assertTest("main() does not throw any checked exceptions", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            Class<?>[] exceptions = main.getExceptionTypes();
            assertEquals(0, exceptions.length);
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

    private static String captureGreetOutput() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(baos));
        try {
            Class<?> clazz = Class.forName("Hello");
            Method greet = clazz.getDeclaredMethod("greet");
            greet.setAccessible(true);
            greet.invoke(null);
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
            failures.add(name + " - " + e.getMessage());
            System.out.println("  FAIL: " + name + " - " + e.getMessage());
        } catch (Exception e) {
            failed++;
            failures.add(name + " - " + e.getClass().getSimpleName() + ": " + e.getMessage());
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

    private static void assertNull(Object obj) {
        if (obj != null) {
            throw new AssertionError("Expected null but got <" + obj + ">");
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

    private static void assertArrayEquals(byte[] expected, byte[] actual) {
        if (expected.length != actual.length) {
            throw new AssertionError("Array length mismatch: expected " + expected.length + " but got " + actual.length);
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                throw new AssertionError("Array mismatch at index " + i + ": expected " + expected[i] + " but got " + actual[i]);
            }
        }
    }
}
