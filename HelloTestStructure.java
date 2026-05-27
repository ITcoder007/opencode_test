import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class HelloTestStructure {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloTestStructure - Structural Completeness Tests ===\n");

        System.out.println("--- Class Hierarchy Tests ---");
        testClassExtendsObject();
        testClassImplementsNoInterfaces();
        testClassIsNotAbstract();
        testClassIsNotFinal();

        System.out.println("\n--- Member Completeness Tests ---");
        testClassHasNoDeclaredFields();
        testClassHasExactlyOneMethod();
        testClassHasNoDeclaredConstructors();
        testClassHasNoInnerClasses();
        testClassHasNoAnnotations();

        System.out.println("\n--- Conversion Integrity Tests ---");
        testNoPythonShebangLine();
        testNoPythonIndentationArtifacts();
        testFileExtensionIsJava();

        System.out.println("\n--- Exit Code Tests ---");
        testMainExitsCleanly();
        testProcessExitCodeIsZero();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL TESTS PASSED");
        } else {
            System.out.println("Result: SOME TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testClassExtendsObject() {
        assertTest("Hello extends Object directly", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(Object.class, clazz.getSuperclass());
        }, ClassNotFoundException.class);
    }

    private static void testClassImplementsNoInterfaces() {
        assertTest("Hello implements no interfaces", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getInterfaces().length);
        }, ClassNotFoundException.class);
    }

    private static void testClassIsNotAbstract() {
        assertTest("Hello is not abstract", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isAbstract(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassIsNotFinal() {
        assertTest("Hello is not final (allows subclassing)", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isFinal(clazz.getModifiers()));
        }, ClassNotFoundException.class);
    }

    private static void testClassHasNoDeclaredFields() {
        assertTest("Hello has no declared fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getDeclaredFields().length);
        }, ClassNotFoundException.class);
    }

    private static void testClassHasExactlyOneMethod() {
        assertTest("Hello has exactly one declared method (main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            assertEquals(1, methods.length);
            assertEquals("main", methods[0].getName());
        }, ClassNotFoundException.class);
    }

    private static void testClassHasNoDeclaredConstructors() {
        assertTest("Hello has only default constructor (no declared constructors)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            boolean hasOnlyDefault = true;
            for (Constructor<?> c : constructors) {
                if (c.getParameterCount() > 0) {
                    hasOnlyDefault = false;
                    break;
                }
            }
            assertTrue(hasOnlyDefault);
        }, ClassNotFoundException.class);
    }

    private static void testClassHasNoInnerClasses() {
        assertTest("Hello has no inner or nested classes", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getDeclaredClasses().length);
        }, ClassNotFoundException.class);
    }

    private static void testClassHasNoAnnotations() {
        assertTest("Hello has no annotations (clean conversion)", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getAnnotations().length);
        }, ClassNotFoundException.class);
    }

    private static void testNoPythonShebangLine() {
        assertTest("Java source has no Python shebang line", () -> {
            String source = new String(java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get("Hello.java")));
            assertFalse(source.contains("#!/"));
            assertFalse(source.contains("/usr/bin/env python"));
        });
    }

    private static void testNoPythonIndentationArtifacts() {
        assertTest("Java source has no Python indentation artifacts", () -> {
            String source = new String(java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get("Hello.java")));
            assertFalse(source.contains("__name__"));
            assertFalse(source.contains("def "));
            assertFalse(source.contains("print("));
        });
    }

    private static void testFileExtensionIsJava() {
        assertTest("Source file has .java extension", () -> {
            assertTrue(java.nio.file.Files.exists(java.nio.file.Paths.get("Hello.java")));
        });
    }

    private static void testMainExitsCleanly() {
        assertTest("main() completes without System.exit()", () -> {
            String output = captureMainOutput();
            assertNotNull(output);
            assertTrue(output.contains("Hello, World!"));
        });
    }

    private static void testProcessExitCodeIsZero() {
        assertTest("Running Hello as subprocess exits with code 0", () -> {
            ProcessBuilder pb = new ProcessBuilder("java", "Hello");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.getInputStream().transferTo(java.io.OutputStream.nullOutputStream());
            int exitCode = process.waitFor();
            assertEquals(0, exitCode);
        });
    }

    private static String captureMainOutput() {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalOut = System.out;
        System.setOut(new java.io.PrintStream(baos));
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
