import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Properties;

public class HelloTestSupplementary {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloTestSupplementary - Supplementary Tests for Hello.java ===\n");

        System.out.println("--- Class Metadata Tests ---");
        testDefaultPackage();
        testDefaultConstructorExists();
        testDefaultConstructorCanInstantiate();
        testSuperclassIsObject();
        testNoInterfacesImplemented();
        testClassIsNotFinal();
        testClassIsNotAbstract();
        testOnlyOnePublicStaticMethod();
        testClassIsNotEnum();

        System.out.println("\n--- Behavior Contract Tests ---");
        testMainDoesNotModifySystemProperties();
        testThreadInterruptStatusPreserved();
        testMainReturnsNormally();
        testOutputLocaleIndependent();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL TESTS PASSED");
        } else {
            System.out.println("Result: SOME TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testDefaultPackage() {
        assertTest("Hello class is in default package (no package declaration)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Package pkg = clazz.getPackage();
            assertTrue(pkg == null || pkg.getName().isEmpty());
        });
    }

    private static void testDefaultConstructorExists() {
        assertTest("Hello has a default no-arg constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?>[] constructors = clazz.getConstructors();
            boolean hasDefault = false;
            for (Constructor<?> c : constructors) {
                if (c.getParameterCount() == 0) {
                    hasDefault = true;
                    break;
                }
            }
            assertTrue(hasDefault);
        });
    }

    private static void testDefaultConstructorCanInstantiate() {
        assertTest("Hello can be instantiated via default constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Object instance = clazz.getDeclaredConstructor().newInstance();
            assertNotNull(instance);
            assertEquals("Hello", instance.getClass().getSimpleName());
        });
    }

    private static void testSuperclassIsObject() {
        assertTest("Hello extends Object directly", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(Object.class, clazz.getSuperclass());
        });
    }

    private static void testNoInterfacesImplemented() {
        assertTest("Hello does not implement any interfaces", () -> {
            Class<?> clazz = Class.forName("Hello");
            Class<?>[] interfaces = clazz.getInterfaces();
            assertEquals(0, interfaces.length);
        });
    }

    private static void testClassIsNotFinal() {
        assertTest("Hello class is not final (can be subclassed)", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isFinal(clazz.getModifiers()));
        });
    }

    private static void testClassIsNotAbstract() {
        assertTest("Hello class is not abstract", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(Modifier.isAbstract(clazz.getModifiers()));
        });
    }

    private static void testOnlyOnePublicStaticMethod() {
        assertTest("Hello has exactly one public static method (main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            int publicStaticCount = 0;
            for (Method m : methods) {
                int mod = m.getModifiers();
                if (Modifier.isPublic(mod) && Modifier.isStatic(mod)) {
                    publicStaticCount++;
                }
            }
            assertEquals(1, publicStaticCount);
        });
    }

    private static void testClassIsNotEnum() {
        assertTest("Hello class is not an enum", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(clazz.isEnum());
        });
    }

    private static void testMainDoesNotModifySystemProperties() {
        assertTest("main() does not modify System.properties", () -> {
            Properties before = (Properties) System.getProperties().clone();
            captureMainOutput();
            Properties after = System.getProperties();
            assertEquals(before.size(), after.size());
            for (String key : before.stringPropertyNames()) {
                assertEquals(before.getProperty(key), after.getProperty(key));
            }
        });
    }

    private static void testThreadInterruptStatusPreserved() {
        assertTest("main() does not clear thread interrupt status", () -> {
            Thread.currentThread().interrupt();
            captureMainOutput();
            assertTrue(Thread.interrupted());
        });
    }

    private static void testMainReturnsNormally() {
        assertTest("main() returns normally without calling System.exit", () -> {
            captureMainOutput();
        });
    }

    private static void testOutputLocaleIndependent() {
        assertTest("Output is identical across different default Locales", () -> {
            Locale original = Locale.getDefault();
            Locale[] testLocales = {
                Locale.ENGLISH,
                Locale.FRENCH,
                Locale.GERMAN,
                Locale.JAPANESE,
                Locale.CHINESE,
                Locale.of("tr", "TR")
            };
            String reference = null;
            for (Locale locale : testLocales) {
                Locale.setDefault(locale);
                String output = captureMainOutput();
                if (reference == null) {
                    reference = output;
                } else {
                    assertEquals(reference, output);
                }
            }
            Locale.setDefault(original);
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
