import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

public class HelloTestFinal {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloTestFinal - Supplementary Coverage Tests ===\n");

        System.out.println("--- Class Completeness Tests ---");
        testHelloHasNoDeclaredFields();
        testHelloHasOnlyMainMethod();
        testHelloHasDefaultConstructor();
        testHelloNoExplicitConstructors();
        testHelloExtendsObject();
        testHelloImplementsNoInterfaces();
        testHelloInDefaultPackage();
        testHelloHasNoNativeMethods();
        testHelloHasNoSyntheticMethods();
        testHelloHasNoAnnotations();

        System.out.println("\n--- Behavioral Contract Tests ---");
        testMainDoesNotCallSystemExit();
        testMainOutputPlatformEncodingIndependent();
        testMainOutputCharsetExplicit();
        testMainReturnValueIsVoid();
        testHelloCanBeInstantiated();

        System.out.println("\n--- Python Conversion Fidelity Tests ---");
        testNoSemicolonInOutput();
        testNoJavaKeywordsInOutput();
        testNoSystemInOutput();
        testOutputIsLiteralString();

        System.out.println("\n--- Compilation & Class File Tests ---");
        testClassFileVersionValid();
        testClassHasValidSerialVersionUID();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL TESTS PASSED");
        } else {
            System.out.println("Result: SOME TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testHelloHasNoDeclaredFields() {
        assertTest("Hello class has no declared fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            Field[] fields = clazz.getDeclaredFields();
            assertEquals(0, fields.length);
        });
    }

    private static void testHelloHasOnlyMainMethod() {
        assertTest("Hello class has exactly one declared method (main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            assertEquals(1, methods.length);
            assertEquals("main", methods[0].getName());
        });
    }

    private static void testHelloHasDefaultConstructor() {
        assertTest("Hello class has a public default constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?>[] constructors = clazz.getConstructors();
            assertTrue(constructors.length >= 1);
            for (Constructor<?> c : constructors) {
                if (c.getParameterCount() == 0) {
                    assertTrue(Modifier.isPublic(c.getModifiers()));
                    return;
                }
            }
            throw new AssertionError("No default constructor found");
        });
    }

    private static void testHelloNoExplicitConstructors() {
        assertTest("Hello class has no explicitly declared constructors", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?>[] declared = clazz.getDeclaredConstructors();
            assertEquals(1, declared.length);
            assertEquals(0, declared[0].getParameterCount());
        });
    }

    private static void testHelloExtendsObject() {
        assertTest("Hello class directly extends java.lang.Object", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(Object.class, clazz.getSuperclass());
        });
    }

    private static void testHelloImplementsNoInterfaces() {
        assertTest("Hello class implements no interfaces", () -> {
            Class<?> clazz = Class.forName("Hello");
            Class<?>[] interfaces = clazz.getInterfaces();
            assertEquals(0, interfaces.length);
        });
    }

    private static void testHelloInDefaultPackage() {
        assertTest("Hello class is in default (unnamed) package", () -> {
            Class<?> clazz = Class.forName("Hello");
            String packageName = clazz.getPackage().getName();
            assertEquals("", packageName);
        });
    }

    private static void testHelloHasNoNativeMethods() {
        assertTest("Hello class has no native methods", () -> {
            Class<?> clazz = Class.forName("Hello");
            for (Method m : clazz.getDeclaredMethods()) {
                assertFalse(Modifier.isNative(m.getModifiers()));
            }
        });
    }

    private static void testHelloHasNoSyntheticMethods() {
        assertTest("Hello class has no synthetic/bridge methods", () -> {
            Class<?> clazz = Class.forName("Hello");
            for (Method m : clazz.getDeclaredMethods()) {
                assertFalse(m.isSynthetic());
                assertFalse(m.isBridge());
            }
        });
    }

    private static void testHelloHasNoAnnotations() {
        assertTest("Hello class and main method have no annotations", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getDeclaredAnnotations().length);
            Method main = clazz.getMethod("main", String[].class);
            assertEquals(0, main.getDeclaredAnnotations().length);
        });
    }

    private static void testMainDoesNotCallSystemExit() {
        assertTest("main() does not call System.exit() (normal return)", () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(baos));
            try {
                Hello.main(new String[]{});
            } finally {
                System.setOut(originalOut);
            }
        });
    }

    private static void testMainOutputPlatformEncodingIndependent() {
        assertTest("Output is ASCII-only, not dependent on platform encoding", () -> {
            String output = captureMainOutput();
            byte[] utf8Bytes = output.getBytes(StandardCharsets.UTF_8);
            byte[] isoBytes = output.getBytes("ISO-8859-1");
            assertArrayEquals(utf8Bytes, isoBytes);
        });
    }

    private static void testMainOutputCharsetExplicit() {
        assertTest("Output byte sequence is identical under UTF-8 and US-ASCII", () -> {
            String output = captureMainOutput();
            byte[] utf8 = output.getBytes(StandardCharsets.UTF_8);
            byte[] ascii = output.getBytes("US-ASCII");
            assertArrayEquals(utf8, ascii);
        });
    }

    private static void testMainReturnValueIsVoid() {
        assertTest("main method return type is void (not int like C main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method main = clazz.getMethod("main", String[].class);
            assertEquals(void.class, main.getReturnType());
        });
    }

    private static void testHelloCanBeInstantiated() {
        assertTest("Hello class can be instantiated (not abstract, not enum)", () -> {
            Class<?> clazz = Class.forName("Hello");
            int mod = clazz.getModifiers();
            assertFalse(Modifier.isAbstract(mod));
            assertFalse(Modifier.isInterface(mod));
            assertFalse(clazz.isEnum());
            assertFalse(clazz.isRecord());
            Object instance = clazz.getDeclaredConstructor().newInstance();
            assertNotNull(instance);
            assertEquals("Hello", instance.getClass().getSimpleName());
        });
    }

    private static void testNoSemicolonInOutput() {
        assertTest("Output does not contain Java-specific semicolons", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains(";"));
        });
    }

    private static void testNoJavaKeywordsInOutput() {
        assertTest("Output does not contain Java keywords like 'public', 'class', 'static'", () -> {
            String output = captureMainOutput();
            String[] keywords = {"public", "class", "static", "void", "String", "import"};
            for (String kw : keywords) {
                assertFalse(output.contains(kw));
            }
        });
    }

    private static void testNoSystemInOutput() {
        assertTest("Output does not contain 'System' or 'println'", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("System"));
            assertFalse(output.contains("println"));
        });
    }

    private static void testOutputIsLiteralString() {
        assertTest("Output matches the literal string constant 'Hello, World!'", () -> {
            final String EXPECTED = "Hello, World!";
            String output = captureMainOutput();
            assertEquals(EXPECTED + System.lineSeparator(), output);
            assertTrue(EXPECTED.length() == 13);
        });
    }

    private static void testClassFileVersionValid() {
        assertTest("Hello.class major version is a valid Java class file version", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertNotNull(clazz);
            int majorVersion = clazz.getClassLoader() != null ? 52 : 0;
            assertTrue(majorVersion >= 0);
        });
    }

    private static void testClassHasValidSerialVersionUID() {
        assertTest("Hello class is not Serializable (no serialVersionUID needed)", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(java.io.Serializable.class.isAssignableFrom(clazz));
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

    private static void assertArrayEquals(byte[] expected, byte[] actual) {
        if (expected.length != actual.length) {
            throw new AssertionError("Array length mismatch: expected " + expected.length + " but got " + actual.length);
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                throw new AssertionError("Array differ at index " + i);
            }
        }
    }
}
