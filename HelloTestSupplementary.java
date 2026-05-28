import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class HelloTestSupplementary {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloTestSupplementary - Supplementary Tests for Hello.java ===\n");

        System.out.println("--- Class Instantiation & Constructor Tests ---");
        testDefaultConstructorExists();
        testCanInstantiateHello();
        testInstanceMainOutput();

        System.out.println("\n--- Class Hierarchy Tests ---");
        testExtendsObjectDirectly();
        testNoInterfacesImplemented();
        testClassNotAbstract();
        testClassNotFinal();
        testClassNotInterface();

        System.out.println("\n--- Field & Member Tests ---");
        testNoDeclaredFields();
        testNoStaticFields();

        System.out.println("\n--- Method Inventory Tests ---");
        testOnlyMainMethodDeclared();
        testNoAdditionalPublicMethods();

        System.out.println("\n--- Package & Modifiers Tests ---");
        testDefaultPackage();
        testClassNotInnerClass();
        testClassNotMemberClass();

        System.out.println("\n--- Exit Code Verification ---");
        testMainExitCodeZero();

        System.out.println("\n--- Output Content Deep Analysis ---");
        testOutputContainsComma();
        testOutputContainsExclamationMark();
        testOutputFirstCharacterIsH();
        testOutputLastContentCharIsExclamation();
        testOutputHasExactlyOneComma();
        testOutputWordCount();

        System.out.println("\n--- JVM Integration Tests ---");
        testClassLoadedOnce();
        testClassGetName();
        testClassCanonicalName();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL TESTS PASSED");
        } else {
            System.out.println("Result: SOME TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testDefaultConstructorExists() {
        assertTest("Hello has a default constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            assertTrue(constructors.length >= 1);
        }, ClassNotFoundException.class);
    }

    private static void testCanInstantiateHello() {
        assertTest("Hello can be instantiated via reflection", () -> {
            Class<?> clazz = Class.forName("Hello");
            Object instance = clazz.getDeclaredConstructor().newInstance();
            assertNotNull(instance);
            assertEquals("Hello", instance.getClass().getSimpleName());
        }, Exception.class);
    }

    private static void testInstanceMainOutput() {
        assertTest("Calling main via instance produces correct output", () -> {
            Class<?> clazz = Class.forName("Hello");
            Object instance = clazz.getDeclaredConstructor().newInstance();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(baos));
            try {
                Method main = clazz.getMethod("main", String[].class);
                main.invoke(instance, (Object) new String[]{});
            } finally {
                System.setOut(originalOut);
            }
            assertEquals("Hello, World!", baos.toString().trim());
        }, Exception.class);
    }

    private static void testExtendsObjectDirectly() {
        assertTest("Hello extends java.lang.Object directly", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(Object.class, clazz.getSuperclass());
        }, ClassNotFoundException.class);
    }

    private static void testNoInterfacesImplemented() {
        assertTest("Hello does not implement any interfaces", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getInterfaces().length);
        }, ClassNotFoundException.class);
    }

    private static void testClassNotAbstract() {
        assertTest("Hello class is not abstract", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isAbstract(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassNotFinal() {
        assertTest("Hello class is not final (can be subclassed)", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isFinal(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassNotInterface() {
        assertTest("Hello is a class, not an interface", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(clazz.isInterface());
        }, ClassNotFoundException.class);
    }

    private static void testNoDeclaredFields() {
        assertTest("Hello has no declared fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getDeclaredFields().length);
        }, ClassNotFoundException.class);
    }

    private static void testNoStaticFields() {
        assertTest("Hello has no static fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields) {
                assertFalse(Modifier.isStatic(f.getModifiers()));
            }
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

    private static void testNoAdditionalPublicMethods() {
        assertTest("Hello has no additional public methods beyond main", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] publicMethods = clazz.getMethods();
            int helloMethods = 0;
            for (Method m : publicMethods) {
                if (m.getDeclaringClass() == clazz) {
                    helloMethods++;
                }
            }
            assertEquals(1, helloMethods);
        }, ClassNotFoundException.class);
    }

    private static void testDefaultPackage() {
        assertTest("Hello is in the default (unnamed) package", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals("", clazz.getPackageName());
        }, ClassNotFoundException.class);
    }

    private static void testClassNotInnerClass() {
        assertTest("Hello is not an inner class", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(clazz.isMemberClass());
        }, ClassNotFoundException.class);
    }

    private static void testClassNotMemberClass() {
        assertTest("Hello is not a local or anonymous class", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(clazz.isLocalClass());
            assertFalse(clazz.isAnonymousClass());
        }, ClassNotFoundException.class);
    }

    private static void testMainExitCodeZero() {
        assertTest("Hello.main() completes with exit code 0", () -> {
            ProcessBuilder pb = new ProcessBuilder("java", "Hello");
            pb.redirectErrorStream(true);
            pb.environment().remove("JAVA_TOOL_OPTIONS");
            Process process = pb.start();
            process.getOutputStream().close();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            assertEquals(0, exitCode);
            assertEquals("Hello, World!", output.trim());
        }, Exception.class);
    }

    private static void testOutputContainsComma() {
        assertTest("Output contains a comma", () -> {
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

    private static void testOutputFirstCharacterIsH() {
        assertTest("Output first character is 'H'", () -> {
            String output = captureMainOutput();
            assertTrue(output.length() > 0);
            assertEquals('H', output.charAt(0));
        });
    }

    private static void testOutputLastContentCharIsExclamation() {
        assertTest("Output last content character is '!'", () -> {
            String output = captureMainOutput();
            String trimmed = output.trim();
            assertEquals('!', trimmed.charAt(trimmed.length() - 1));
        });
    }

    private static void testOutputHasExactlyOneComma() {
        assertTest("Output contains exactly one comma", () -> {
            String output = captureMainOutput();
            int commaCount = 0;
            for (char c : output.toCharArray()) {
                if (c == ',') commaCount++;
            }
            assertEquals(1, commaCount);
        });
    }

    private static void testOutputWordCount() {
        assertTest("Output has exactly 2 words (Hello, World!)", () -> {
            String output = captureMainOutput().trim();
            String withoutPunctuation = output.replace(",", "").replace("!", "");
            String[] words = withoutPunctuation.split("\\s+");
            assertEquals(2, words.length);
            assertEquals("Hello", words[0]);
            assertEquals("World", words[1]);
        });
    }

    private static void testClassLoadedOnce() {
        assertTest("Hello class is loaded exactly once (same Class object)", () -> {
            Class<?> c1 = Class.forName("Hello");
            Class<?> c2 = Class.forName("Hello");
            assertSame(c1, c2);
        }, ClassNotFoundException.class);
    }

    private static void testClassGetName() {
        assertTest("Class getName() returns 'Hello'", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals("Hello", clazz.getName());
        }, ClassNotFoundException.class);
    }

    private static void testClassCanonicalName() {
        assertTest("Class getCanonicalName() returns 'Hello'", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals("Hello", clazz.getCanonicalName());
        }, ClassNotFoundException.class);
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
                System.out.println("  FAIL: " + name + " - Exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            } else {
                failed++;
                System.out.println("  FAIL: " + name + " - Exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
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
