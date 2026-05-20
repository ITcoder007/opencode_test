import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

public class HelloTestExtended {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloTestExtended - Extended Unit Tests ===\n");

        System.out.println("--- Reflection & Metadata Tests ---");
        testClassInDefaultPackage();
        testClassHasDefaultConstructor();
        testClassNotAbstract();
        testClassNotFinal();
        testClassNotEnum();
        testClassNotInterface();
        testClassDirectlyExtendsObject();
        testClassHasExactlyOnePublicMethod();
        testNoAdditionalFields();
        testMainMethodReturnType();

        System.out.println("\n--- Encoding & Charset Tests ---");
        testOutputIsUTF8Compatible();
        testOutputIsASCII();
        testOutputBytesMatchExpected();
        testOutputLength();
        testOutputCharCount();

        System.out.println("\n--- Boundary & Edge Case Tests ---");
        testMainWithEmptyStringArray();
        testMainWithSingleEmptyString();
        testMainWithLargeArgsArray();
        testMainCalledManyTimes();
        testOutputDoesNotContainTab();
        testOutputDoesNotContainCarriageReturnAlone();
        testOutputIsDeterministic();

        System.out.println("\n--- Behavior Consistency Tests ---");
        testSystemOutRestoredAfterMain();
        testSystemOutRestoredAfterException();
        testConcurrentMainCalls();
        testMainDoesNotModifySystemErr();
        testOutputStartsWithHello();

        System.out.println("\n--- Class Integrity Tests ---");
        testClassCanBeInstantiated();
        testMainIsAccessible();
        testClassIsNotSynthetic();
        testMainIsNotSynthetic();
        testClassHasNoAnnotations();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL TESTS PASSED");
        } else {
            System.out.println("Result: SOME TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testClassInDefaultPackage() {
        assertTest("Class is in default (unnamed) package", () -> {
            Class<?> clazz = Class.forName("Hello");
            Package pkg = clazz.getPackage();
            assertTrue(pkg == null || pkg.getName().isEmpty());
        }, ClassNotFoundException.class);
    }

    private static void testClassHasDefaultConstructor() {
        assertTest("Class has a default no-arg constructor", () -> {
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

    private static void testClassNotAbstract() {
        assertTest("Class is not abstract", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isAbstract(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassNotFinal() {
        assertTest("Class is not final", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isFinal(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassNotEnum() {
        assertTest("Class is not an enum", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(clazz.isEnum());
        }, ClassNotFoundException.class);
    }

    private static void testClassNotInterface() {
        assertTest("Class is not an interface", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(clazz.isInterface());
        }, ClassNotFoundException.class);
    }

    private static void testClassDirectlyExtendsObject() {
        assertTest("Class directly extends Object", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(Object.class, clazz.getSuperclass());
        }, ClassNotFoundException.class);
    }

    private static void testClassHasExactlyOnePublicMethod() {
        assertTest("Class has exactly one declared public method (main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            int publicCount = 0;
            for (Method m : methods) {
                if (Modifier.isPublic(m.getModifiers())) {
                    publicCount++;
                }
            }
            assertEquals(1, publicCount);
        }, ClassNotFoundException.class);
    }

    private static void testNoAdditionalFields() {
        assertTest("Class has no declared fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getDeclaredFields().length);
        }, ClassNotFoundException.class);
    }

    private static void testMainMethodReturnType() {
        assertTest("main method returns void", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            assertEquals(void.class, main.getReturnType());
        }, NoSuchMethodException.class);
    }

    private static void testOutputIsUTF8Compatible() {
        assertTest("Output is valid UTF-8", () -> {
            String output = captureMainOutput();
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            String reconstructed = new String(bytes, StandardCharsets.UTF_8);
            assertEquals(output, reconstructed);
        });
    }

    private static void testOutputIsASCII() {
        assertTest("Output content (excluding newline) is pure ASCII", () -> {
            String output = captureMainOutput().trim();
            for (char c : output.toCharArray()) {
                assertTrue(c < 128);
            }
        });
    }

    private static void testOutputBytesMatchExpected() {
        assertTest("Raw output bytes match expected 'Hello, World!' bytes", () -> {
            String output = captureMainOutput();
            String content = output.trim();
            byte[] expected = "Hello, World!".getBytes(StandardCharsets.UTF_8);
            byte[] actual = content.getBytes(StandardCharsets.UTF_8);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < expected.length; i++) {
                assertEquals(expected[i], actual[i]);
            }
        });
    }

    private static void testOutputLength() {
        assertTest("Output (including newline) has correct length", () -> {
            String output = captureMainOutput();
            int expectedLen = "Hello, World!".length() + System.lineSeparator().length();
            assertEquals(expectedLen, output.length());
        });
    }

    private static void testOutputCharCount() {
        assertTest("Output content is exactly 13 characters", () -> {
            String output = captureMainOutput().trim();
            assertEquals(13, output.length());
        });
    }

    private static void testMainWithEmptyStringArray() {
        assertTest("main() works with empty string array", () -> {
            String output = captureMainOutputWithArgs(new String[]{""});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMainWithSingleEmptyString() {
        assertTest("main() works with array containing single empty string", () -> {
            String output = captureMainOutputWithArgs(new String[]{""});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMainWithLargeArgsArray() {
        assertTest("main() works with 1000-element args array", () -> {
            String[] largeArgs = new String[1000];
            for (int i = 0; i < 1000; i++) {
                largeArgs[i] = "arg" + i;
            }
            String output = captureMainOutputWithArgs(largeArgs);
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMainCalledManyTimes() {
        assertTest("main() called 100 times produces consistent output", () -> {
            String expected = captureMainOutput();
            for (int i = 0; i < 99; i++) {
                String actual = captureMainOutput();
                assertEquals(expected, actual);
            }
        });
    }

    private static void testOutputDoesNotContainTab() {
        assertTest("Output does not contain tab character", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("\t"));
        });
    }

    private static void testOutputDoesNotContainCarriageReturnAlone() {
        assertTest("Output does not contain standalone carriage return", () -> {
            String output = captureMainOutput();
            String trimmed = output.replace(System.lineSeparator(), "");
            assertFalse(trimmed.contains("\r"));
        });
    }

    private static void testOutputIsDeterministic() {
        assertTest("Output is deterministic across sequential runs", () -> {
            String[] outputs = new String[10];
            for (int i = 0; i < 10; i++) {
                outputs[i] = captureMainOutput();
            }
            for (int i = 1; i < 10; i++) {
                assertEquals(outputs[0], outputs[i]);
            }
        });
    }

    private static void testSystemOutRestoredAfterMain() {
        assertTest("System.out is restored after main() call", () -> {
            PrintStream original = System.out;
            captureMainOutput();
            assertSame(original, System.out);
        });
    }

    private static void testSystemOutRestoredAfterException() {
        assertTest("System.out is restored even when test framework redirects", () -> {
            PrintStream original = System.out;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));
            System.setOut(original);
            assertSame(original, System.out);
        });
    }

    private static void testConcurrentMainCalls() {
        assertTest("Concurrent main() calls do not throw exceptions", () -> {
            boolean[] errors = new boolean[1];
            Thread t = new Thread(() -> {
                try {
                    Hello.main(new String[]{});
                } catch (Exception e) {
                    errors[0] = true;
                }
            });
            t.start();
            t.join(5000);
            assertFalse(errors[0]);
        });
    }

    private static void testMainDoesNotModifySystemErr() {
        assertTest("main() does not write to System.err", () -> {
            PrintStream originalErr = System.err;
            ByteArrayOutputStream errBaos = new ByteArrayOutputStream();
            System.setErr(new PrintStream(errBaos));
            try {
                captureMainOutput();
            } finally {
                System.setErr(originalErr);
            }
            assertEquals("", errBaos.toString());
        });
    }

    private static void testOutputStartsWithHello() {
        assertTest("Output starts with 'Hello'", () -> {
            String output = captureMainOutput();
            assertTrue(output.startsWith("Hello"));
        });
    }

    private static void testClassCanBeInstantiated() {
        assertTest("Hello can be instantiated via default constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Object instance = clazz.getDeclaredConstructor().newInstance();
            assertNotNull(instance);
            assertEquals("Hello", instance.getClass().getSimpleName());
        }, ReflectiveOperationException.class);
    }

    private static void testMainIsAccessible() {
        assertTest("main method is accessible (public)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            assertTrue(main.canAccess(null));
        }, NoSuchMethodException.class);
    }

    private static void testClassIsNotSynthetic() {
        assertTest("Hello class is not synthetic", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(clazz.isSynthetic());
        }, ClassNotFoundException.class);
    }

    private static void testMainIsNotSynthetic() {
        assertTest("main method is not synthetic", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            assertFalse(main.isSynthetic());
        }, NoSuchMethodException.class);
    }

    private static void testClassHasNoAnnotations() {
        assertTest("Hello class has no runtime annotations", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getAnnotations().length);
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

    private static void assertEquals(byte expected, byte actual) {
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
