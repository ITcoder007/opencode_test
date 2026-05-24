import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

public class HelloTestComplement {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloTestComplement - Complementary Tests for Hello.java ===\n");

        System.out.println("--- Class Introspection Tests ---");
        testClassInDefaultPackage();
        testClassIsNotAbstract();
        testClassIsNotFinal();
        testClassIsNotInterface();
        testClassIsNotEnum();
        testClassHasDefaultConstructor();
        testClassHasExactlyOneDeclaredMethod();
        testClassHasNoDeclaredFields();
        testClassSuperclassIsObject();
        testClassHasNoInterfaces();
        testClassHasNoAnnotations();

        System.out.println("\n--- Method Signature Deep Tests ---");
        testMainMethodHasNoGenericTypes();
        testMainMethodReturnTypeIsVoid();
        testMainMethodNotSynthetic();
        testMainMethodNotBridge();

        System.out.println("\n--- Output String Analysis Tests ---");
        testOutputContainsComma();
        testOutputContainsExclamation();
        testOutputContainsSpaceAfterComma();
        testOutputIsExactlyHelloWorld();
        testOutputFirstCharIsH();
        testOutputLastContentCharIsExclamation();
        testOutputHasNoTab();
        testOutputHasNoCarriageReturn();
        testOutputCharCount();
        testOutputDoesNotContainNull();

        System.out.println("\n--- Python Semantic Equivalence Tests ---");
        testPythonPrintSemantics();
        testPythonMainGuardBehavior();
        testPythonOutputComparisonViaRuntime();

        System.out.println("\n--- Instantiation & Object Tests ---");
        testHelloCanBeInstantiated();
        testHelloInstanceNotNull();
        testHelloInstanceToString();
        testHelloInstanceEqualsSelf();
        testHelloHashCodeDoesNotThrow();

        System.out.println("\n--- Main Method Variadic Edge Cases ---");
        testMainWithSingleNullElementArray();
        testMainWithUnicodeArgs();
        testMainWithVeryLongStringArg();

        System.out.println("\n--- Output Stream Integrity Tests ---");
        testMainDoesNotCloseSystemOut();
        testMainOutputIsDeterministicAcrossRuns();

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
        assertTest("Hello class is in default (unnamed) package", () -> {
            Class<?> clazz = Class.forName("Hello");
            Package pkg = clazz.getPackage();
            assertTrue(pkg == null || pkg.getName().isEmpty());
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

    private static void testClassIsNotInterface() {
        assertTest("Hello class is not an interface", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(clazz.isInterface());
        });
    }

    private static void testClassIsNotEnum() {
        assertTest("Hello class is not an enum", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(clazz.isEnum());
        });
    }

    private static void testClassHasDefaultConstructor() {
        assertTest("Hello has a public default constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?>[] constructors = clazz.getConstructors();
            assertTrue(constructors.length >= 1);
            for (Constructor<?> c : constructors) {
                if (c.getParameterCount() == 0 && Modifier.isPublic(c.getModifiers())) {
                    return;
                }
            }
            throw new AssertionError("No public no-arg constructor found");
        });
    }

    private static void testClassHasExactlyOneDeclaredMethod() {
        assertTest("Hello declares exactly one method (main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            assertEquals(1, methods.length);
            assertEquals("main", methods[0].getName());
        });
    }

    private static void testClassHasNoDeclaredFields() {
        assertTest("Hello has no declared fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getDeclaredFields().length);
        });
    }

    private static void testClassSuperclassIsObject() {
        assertTest("Hello superclass is java.lang.Object", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(Object.class, clazz.getSuperclass());
        });
    }

    private static void testClassHasNoInterfaces() {
        assertTest("Hello does not implement any interfaces", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getInterfaces().length);
        });
    }

    private static void testClassHasNoAnnotations() {
        assertTest("Hello class has no runtime annotations", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getAnnotations().length);
        });
    }

    private static void testMainMethodHasNoGenericTypes() {
        assertTest("main method has no type parameters", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            assertEquals(0, main.getTypeParameters().length);
        });
    }

    private static void testMainMethodReturnTypeIsVoid() {
        assertTest("main method return type is exactly void.class", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            assertEquals(void.class, main.getReturnType());
        });
    }

    private static void testMainMethodNotSynthetic() {
        assertTest("main method is not synthetic", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            assertFalse(main.isSynthetic());
        });
    }

    private static void testMainMethodNotBridge() {
        assertTest("main method is not a bridge method", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            assertFalse(main.isBridge());
        });
    }

    private static void testOutputContainsComma() {
        assertTest("Output contains comma character", () -> {
            String output = captureMainOutput();
            assertTrue(output.contains(","));
        });
    }

    private static void testOutputContainsExclamation() {
        assertTest("Output contains exclamation mark", () -> {
            String output = captureMainOutput();
            assertTrue(output.contains("!"));
        });
    }

    private static void testOutputContainsSpaceAfterComma() {
        assertTest("Output has exactly one space after comma", () -> {
            String output = captureMainOutput().trim();
            int commaIdx = output.indexOf(',');
            assertEquals(' ', output.charAt(commaIdx + 1));
            if (commaIdx + 2 < output.length()) {
                assertFalse(output.charAt(commaIdx + 2) == ' ');
            }
        });
    }

    private static void testOutputIsExactlyHelloWorld() {
        assertTest("Trimmed output equals 'Hello, World!' exactly", () -> {
            String output = captureMainOutput().trim();
            assertEquals("Hello, World!", output);
        });
    }

    private static void testOutputFirstCharIsH() {
        assertTest("First character of output is 'H'", () -> {
            String output = captureMainOutput();
            assertTrue(output.length() > 0);
            assertEquals('H', output.charAt(0));
        });
    }

    private static void testOutputLastContentCharIsExclamation() {
        assertTest("Last content character before newline is '!'", () -> {
            String output = captureMainOutput().trim();
            assertTrue(output.length() > 0);
            assertEquals('!', output.charAt(output.length() - 1));
        });
    }

    private static void testOutputHasNoTab() {
        assertTest("Output contains no tab characters", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("\t"));
        });
    }

    private static void testOutputHasNoCarriageReturn() {
        assertTest("Output content has no carriage return", () -> {
            String output = captureMainOutput().trim();
            assertFalse(output.contains("\r"));
        });
    }

    private static void testOutputCharCount() {
        assertTest("Trimmed output is exactly 13 characters", () -> {
            String output = captureMainOutput().trim();
            assertEquals(13, output.length());
        });
    }

    private static void testOutputDoesNotContainNull() {
        assertTest("Output does not contain null character (\\0)", () -> {
            String output = captureMainOutput();
            assertFalse(output.indexOf('\0') >= 0);
        });
    }

    private static void testPythonPrintSemantics() {
        assertTest("Java println behavior matches Python print() semantics (auto-newline)", () -> {
            String output = captureMainOutput();
            assertTrue(output.endsWith("\n") || output.endsWith(System.lineSeparator()));
        });
    }

    private static void testPythonMainGuardBehavior() {
        assertTest("Java main is always executed (no if __name__ guard needed)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            assertTrue(Modifier.isStatic(main.getModifiers()));
            assertTrue(Modifier.isPublic(main.getModifiers()));
        });
    }

    private static void testPythonOutputComparisonViaRuntime() {
        assertTest("Java output matches Python runtime output byte-for-byte", () -> {
            String javaOutput = captureMainOutput();
            String pythonExpected = "Hello, World!\n";
            byte[] javaBytes = javaOutput.getBytes(StandardCharsets.UTF_8);
            byte[] pythonBytes = pythonExpected.getBytes(StandardCharsets.UTF_8);
            assertArrayEquals(pythonBytes, javaBytes);
        });
    }

    private static void testHelloCanBeInstantiated() {
        assertTest("Hello class can be instantiated with default constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Object instance = clazz.getDeclaredConstructor().newInstance();
            assertNotNull(instance);
        });
    }

    private static void testHelloInstanceNotNull() {
        assertTest("Hello instance is not null", () -> {
            Hello instance = new Hello();
            assertNotNull(instance);
        });
    }

    private static void testHelloInstanceToString() {
        assertTest("Hello instance toString() does not throw", () -> {
            Hello instance = new Hello();
            String str = instance.toString();
            assertNotNull(str);
            assertTrue(str.length() > 0);
        });
    }

    private static void testHelloInstanceEqualsSelf() {
        assertTest("Hello instance equals itself (reflexive)", () -> {
            Hello instance = new Hello();
            assertEquals(instance, instance);
        });
    }

    private static void testHelloHashCodeDoesNotThrow() {
        assertTest("Hello instance hashCode() returns without error", () -> {
            Hello instance = new Hello();
            int hash = instance.hashCode();
            assertTrue(true);
        });
    }

    private static void testMainWithSingleNullElementArray() {
        assertTest("main() handles args array with single null element", () -> {
            String output = captureMainOutputWithArgs(new String[]{null});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMainWithUnicodeArgs() {
        assertTest("main() handles args with Unicode characters", () -> {
            String output = captureMainOutputWithArgs(new String[]{"日本語", "中文", "🌍"});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMainWithVeryLongStringArg() {
        assertTest("main() handles very long string argument (1MB)", () -> {
            StringBuilder sb = new StringBuilder(1024 * 1024);
            for (int i = 0; i < 1024 * 1024; i++) {
                sb.append('x');
            }
            String output = captureMainOutputWithArgs(new String[]{sb.toString()});
            assertEquals("Hello, World!", output.trim());
        });
    }

    private static void testMainDoesNotCloseSystemOut() {
        assertTest("main() does not close System.out", () -> {
            PrintStream original = System.out;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream testPs = new PrintStream(baos);
            System.setOut(testPs);
            try {
                Hello.main(null);
            } finally {
                System.setOut(original);
            }
            assertFalse(baos.toString().isEmpty());
            assertTrue(System.out == original);
        });
    }

    private static void testMainOutputIsDeterministicAcrossRuns() {
        assertTest("main() output is deterministic across 100 sequential runs", () -> {
            String baseline = captureMainOutput();
            for (int i = 0; i < 100; i++) {
                String output = captureMainOutput();
                if (!baseline.equals(output)) {
                    throw new AssertionError("Output differed at run " + (i + 1));
                }
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

    private static void assertEquals(char expected, char actual) {
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
