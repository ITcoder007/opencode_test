import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class HelloExtendedTest {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloExtendedTest - Additional Edge Case Tests ===\n");

        System.out.println("--- Class Metadata Tests ---");
        testClassNotAbstract();
        testClassNotFinal();
        testSuperclassIsObject();
        testNoDeclaredFields();
        testOnlyMainMethodDeclared();
        testHasDefaultConstructor();
        testCanInstantiate();
        testClassHasNoInterfaces();

        System.out.println("\n--- Output Edge Case Tests ---");
        testOutputIsASCII();
        testOutputLength();
        testNoControlCharacters();
        testOutputContainsComma();
        testOutputContainsExclamation();
        testPunctuationPositions();

        System.out.println("\n--- Concurrency / Stress Tests ---");
        testMultipleSequentialCalls();
        testOutputWithEmptyStringArray();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL TESTS PASSED");
        } else {
            System.out.println("Result: SOME TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testClassNotAbstract() {
        assertTest("Hello class is not abstract", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isAbstract(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassNotFinal() {
        assertTest("Hello class is not final", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isFinal(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testSuperclassIsObject() {
        assertTest("Hello extends Object directly", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(Object.class, clazz.getSuperclass());
        }, ClassNotFoundException.class);
    }

    private static void testNoDeclaredFields() {
        assertTest("Hello has no declared fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            Field[] fields = clazz.getDeclaredFields();
            assertEquals(0, fields.length);
        }, ClassNotFoundException.class);
    }

    private static void testOnlyMainMethodDeclared() {
        assertTest("Hello declares only the main method", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            assertEquals(1, methods.length);
            assertEquals("main", methods[0].getName());
        }, ClassNotFoundException.class);
    }

    private static void testHasDefaultConstructor() {
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

    private static void testCanInstantiate() {
        assertTest("Hello can be instantiated", () -> {
            Class<?> clazz = Class.forName("Hello");
            Object instance = clazz.getDeclaredConstructor().newInstance();
            assertNotNull(instance);
            assertTrue(clazz.isInstance(instance));
        }, ReflectiveOperationException.class);
    }

    private static void testClassHasNoInterfaces() {
        assertTest("Hello implements no interfaces", () -> {
            Class<?> clazz = Class.forName("Hello");
            Class<?>[] interfaces = clazz.getInterfaces();
            assertEquals(0, interfaces.length);
        }, ClassNotFoundException.class);
    }

    private static void testOutputIsASCII() {
        assertTest("Output consists only of ASCII characters", () -> {
            String output = captureMainOutput();
            String content = output.replace(System.lineSeparator(), "");
            for (char c : content.toCharArray()) {
                assertTrue(c < 128);
            }
        });
    }

    private static void testOutputLength() {
        assertTest("Output content is exactly 13 characters (Hello, World!)", () -> {
            String output = captureMainOutput();
            String content = output.replace(System.lineSeparator(), "");
            assertEquals(13, content.length());
        });
    }

    private static void testNoControlCharacters() {
        assertTest("Output content contains no control characters", () -> {
            String output = captureMainOutput();
            String content = output.replace(System.lineSeparator(), "");
            for (char c : content.toCharArray()) {
                assertTrue(c >= 32);
            }
        });
    }

    private static void testOutputContainsComma() {
        assertTest("Output contains comma at position 5", () -> {
            String output = captureMainOutput();
            String content = output.replace(System.lineSeparator(), "");
            assertEquals(',', content.charAt(5));
        });
    }

    private static void testOutputContainsExclamation() {
        assertTest("Output ends with exclamation mark", () -> {
            String output = captureMainOutput();
            String content = output.replace(System.lineSeparator(), "");
            assertEquals('!', content.charAt(content.length() - 1));
        });
    }

    private static void testPunctuationPositions() {
        assertTest("Punctuation positions match Python output exactly", () -> {
            String javaOutput = captureMainOutput().replace(System.lineSeparator(), "");
            assertEquals("Hello, World!", javaOutput);
            assertEquals(',', javaOutput.charAt(5));
            assertEquals('!', javaOutput.charAt(12));
            assertEquals(' ', javaOutput.charAt(6));
            assertEquals('H', javaOutput.charAt(0));
            assertEquals('W', javaOutput.charAt(7));
        });
    }

    private static void testMultipleSequentialCalls() {
        assertTest("10 sequential calls all produce identical output", () -> {
            String reference = captureMainOutput();
            for (int i = 0; i < 10; i++) {
                String output = captureMainOutput();
                assertEquals(reference, output);
            }
        });
    }

    private static void testOutputWithEmptyStringArray() {
        assertTest("main() with new String[]{''} outputs correctly", () -> {
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
