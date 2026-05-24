import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
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
        System.out.println("=== HelloTestExtended - Extended Tests for Hello.java ===\n");

        System.out.println("--- Byte-Level Verification Tests ---");
        testOutputByteContent();
        testOutputLengthExact();
        testOutputIsPureASCII();

        System.out.println("\n--- Encoding & Character Tests ---");
        testOutputIsUTF8Compatible();
        testNoBOMInOutput();
        testNoControlCharactersInContent();

        System.out.println("\n--- State & Side Effect Tests ---");
        testSystemOutRestoredAfterMain();
        testNoStaticStatePollution();
        testSystemErrUnaffected();

        System.out.println("\n--- Reflection Invocation Tests ---");
        testMainInvocableViaReflection();
        testMainAccessibleViaReflection();
        testMainInvokeWithNullArgsViaReflection();

        System.out.println("\n--- Concurrency Tests ---");
        testConcurrentExecution();
        testConcurrentOutputConsistency();

        System.out.println("\n--- Stress & Repeated Execution Tests ---");
        testRepeatedExecution1000Times();
        testMainWithLargeArgsArray();
        testMainWithEmptyStringArg();

        System.out.println("\n--- Python Equivalence Deep Tests ---");
        testOutputVsPythonByteLevel();
        testPoundSignNotInJavaOutput();

        System.out.println("\n--- Edge Case Tests ---");
        testMainCalledFromSeparateThread();
        testMainDoesNotThrowAnyException();
        testMainDoesNotWriteToStderr();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL TESTS PASSED");
        } else {
            System.out.println("Result: SOME TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testOutputByteContent() {
        assertTest("Output byte content matches expected exactly", () -> {
            String output = captureMainOutput();
            byte[] expected = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);
            byte[] actual = output.getBytes(StandardCharsets.UTF_8);
            assertArrayEquals(expected, actual);
        });
    }

    private static void testOutputLengthExact() {
        assertTest("Output length is exactly 14 bytes (13 chars + newline)", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            assertEquals(14, bytes.length);
        });
    }

    private static void testOutputIsPureASCII() {
        assertTest("Output contains only ASCII characters", () -> {
            String output = captureMainOutput();
            for (char c : output.toCharArray()) {
                assertTrue(c < 128);
            }
        });
    }

    private static void testOutputIsUTF8Compatible() {
        assertTest("Output is valid UTF-8 and decodes correctly", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            String decoded = new String(bytes, StandardCharsets.UTF_8);
            assertEquals(output, decoded);
        });
    }

    private static void testNoBOMInOutput() {
        assertTest("Output has no UTF-8 BOM prefix", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            assertFalse(bytes.length >= 3 && bytes[0] == (byte) 0xEF
                    && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF);
        });
    }

    private static void testNoControlCharactersInContent() {
        assertTest("Output content has no unexpected control characters", () -> {
            String output = captureMainOutput();
            String content = output.trim();
            for (char c : content.toCharArray()) {
                assertTrue(c >= 32 || c == 10 || c == 13);
            }
        });
    }

    private static void testSystemOutRestoredAfterMain() {
        assertTest("System.out is restored after main() execution", () -> {
            PrintStream original = System.out;
            captureMainOutput();
            assertSame(original, System.out);
        });
    }

    private static void testNoStaticStatePollution() {
        assertTest("Multiple calls produce identical results (no state accumulation)", () -> {
            String first = captureMainOutput();
            String second = captureMainOutput();
            String third = captureMainOutput();
            assertEquals(first, second);
            assertEquals(second, third);
        });
    }

    private static void testSystemErrUnaffected() {
        assertTest("main() does not write to System.err", () -> {
            PrintStream originalErr = System.err;
            ByteArrayOutputStream errBaos = new ByteArrayOutputStream();
            PrintStream fakeErr = new PrintStream(errBaos);
            System.setErr(fakeErr);
            try {
                captureMainOutput();
            } finally {
                System.setErr(originalErr);
            }
            assertEquals("", errBaos.toString());
        });
    }

    private static void testMainInvocableViaReflection() {
        assertTest("main() can be invoked via reflection", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(baos));
            try {
                main.invoke(null, (Object) new String[]{});
            } finally {
                System.setOut(originalOut);
            }
            assertEquals("Hello, World!", baos.toString().trim());
        });
    }

    private static void testMainAccessibleViaReflection() {
        assertTest("main method is accessible (modifiers check)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            assertTrue(main.canAccess(null));
        });
    }

    private static void testMainInvokeWithNullArgsViaReflection() {
        assertTest("main() via reflection with null args does not throw", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(baos));
            try {
                main.invoke(null, (Object) null);
            } finally {
                System.setOut(originalOut);
            }
            assertEquals("Hello, World!", baos.toString().trim());
        });
    }

    private static void testConcurrentExecution() {
        assertTest("main() can be called from 10 concurrent threads without error", () -> {
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        Hello.main(null);
                    } catch (Throwable t) {
                        errors.add(t);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            boolean completed = latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();
            assertTrue(completed);
            assertTrue(errors.isEmpty());
        });
    }

    private static void testConcurrentOutputConsistency() {
        assertTest("Concurrent calls to main() produce consistent output (shared stream)", () -> {
            int count = 10;
            ByteArrayOutputStream sharedBaos = new ByteArrayOutputStream();
            PrintStream sharedPs = new PrintStream(sharedBaos);
            ExecutorService executor = Executors.newFixedThreadPool(4);
            CountDownLatch latch = new CountDownLatch(count);
            PrintStream originalOut = System.out;

            System.setOut(sharedPs);
            try {
                for (int i = 0; i < count; i++) {
                    executor.submit(() -> {
                        try {
                            Hello.main(null);
                        } finally {
                            latch.countDown();
                        }
                    });
                }
                boolean completed = latch.await(5, TimeUnit.SECONDS);
                executor.shutdown();
                assertTrue(completed);
            } finally {
                System.setOut(originalOut);
            }

            sharedPs.flush();
            String output = sharedBaos.toString();
            String[] lines = output.split("\n");
            assertEquals(count, lines.length);
            for (String line : lines) {
                assertEquals("Hello, World!", line);
            }
        });
    }

    private static void testRepeatedExecution1000Times() {
        assertTest("main() produces identical output across 1000 sequential calls", () -> {
            String first = captureMainOutput();
            for (int i = 0; i < 999; i++) {
                String output = captureMainOutput();
                if (!first.equals(output)) {
                    throw new AssertionError("Output differed at iteration " + (i + 1));
                }
            }
        });
    }

    private static void testMainWithLargeArgsArray() {
        assertTest("main() handles 10000-element args array without issue", () -> {
            String[] bigArgs = new String[10000];
            for (int i = 0; i < bigArgs.length; i++) {
                bigArgs[i] = "arg" + i;
            }
            String output = captureMainOutputWithArgs(bigArgs);
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMainWithEmptyStringArg() {
        assertTest("main() handles args containing empty string", () -> {
            String output = captureMainOutputWithArgs(new String[]{""});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testOutputVsPythonByteLevel() {
        assertTest("Java output bytes match Python print('Hello, World!') bytes", () -> {
            String output = captureMainOutput();
            byte[] javaBytes = output.getBytes(StandardCharsets.UTF_8);
            byte[] pythonExpected = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);
            assertArrayEquals(pythonExpected, javaBytes);
        });
    }

    private static void testPoundSignNotInJavaOutput() {
        assertTest("Java output does not contain Python comment characters", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("#"));
        });
    }

    private static void testMainCalledFromSeparateThread() {
        assertTest("main() produces correct output when called from a new Thread", () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            Thread t = new Thread(() -> {
                System.setOut(new PrintStream(baos));
                try {
                    Hello.main(null);
                } finally {
                    System.setOut(originalOut);
                }
            });
            t.start();
            t.join(3000);
            assertEquals("Hello, World!", baos.toString().trim());
            assertSame(originalOut, System.out);
        });
    }

    private static void testMainDoesNotThrowAnyException() {
        assertTest("main() does not throw any checked or unchecked exception", () -> {
            try {
                captureMainOutput();
            } catch (Throwable t) {
                throw new AssertionError("main() threw unexpected: " + t.getClass().getName() + ": " + t.getMessage());
            }
        });
    }

    private static void testMainDoesNotWriteToStderr() {
        assertTest("main() writes nothing to stderr", () -> {
            PrintStream originalErr = System.err;
            ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            System.setErr(new PrintStream(errContent));
            try {
                captureMainOutput();
            } finally {
                System.setErr(originalErr);
            }
            assertEquals(0, errContent.toString().length());
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

    private static void assertArrayEquals(byte[] expected, byte[] actual) {
        if (expected.length != actual.length) {
            throw new AssertionError("Array length mismatch: expected " + expected.length + " but got " + actual.length);
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                throw new AssertionError("Array differ at index " + i + ": expected <" + expected[i] + "> but got <" + actual[i] + ">");
            }
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
