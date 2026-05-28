import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class HelloTestSupplementary {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloTestSupplementary - 补充测试维度 ===\n");

        System.out.println("--- 类修饰符完整性测试 ---");
        testClassNotAbstract();
        testClassNotFinal();
        testClassNotInterface();
        testClassNotEnum();
        testClassNotAnnotation();

        System.out.println("\n--- 类结构完整性测试 ---");
        testOnlyOneDeclaredMethod();
        testNoDeclaredFields();
        testDefaultConstructorExists();
        testDefaultPackage();
        testExtendsObject();
        testNoImplementedInterfaces();

        System.out.println("\n--- 实例化测试 ---");
        testCanInstantiateHello();
        testInstanceIsTypeHello();

        System.out.println("\n--- 子进程退出码测试 ---");
        testExitCodeZero();

        System.out.println("\n--- 安全边界测试 ---");
        testNoFileIOOperations();
        testNoSystemPropertyModification();

        System.out.println("\n--- 源文件规范测试 ---");
        testSourceFileEncoding();
        testSourceFileHasNoPackageDeclaration();
        testSourceFileSingleClass();

        System.out.println("\n--- 性能基线测试 ---");
        testExecutionTimeReasonable();

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

    private static void testClassNotInterface() {
        assertTest("Hello class is not an interface", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(clazz.isInterface());
        }, ClassNotFoundException.class);
    }

    private static void testClassNotEnum() {
        assertTest("Hello class is not an enum", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(clazz.isEnum());
        }, ClassNotFoundException.class);
    }

    private static void testClassNotAnnotation() {
        assertTest("Hello class is not an annotation", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertFalse(clazz.isAnnotation());
        }, ClassNotFoundException.class);
    }

    private static void testOnlyOneDeclaredMethod() {
        assertTest("Hello has exactly one declared method (main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] methods = clazz.getDeclaredMethods();
            assertEquals(1, methods.length);
            assertEquals("main", methods[0].getName());
        }, ClassNotFoundException.class);
    }

    private static void testNoDeclaredFields() {
        assertTest("Hello has no declared fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getDeclaredFields().length);
        }, ClassNotFoundException.class);
    }

    private static void testDefaultConstructorExists() {
        assertTest("Hello has a default public constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?>[] constructors = clazz.getConstructors();
            assertTrue(constructors.length >= 1);
            boolean foundDefault = false;
            for (Constructor<?> c : constructors) {
                if (c.getParameterCount() == 0) {
                    assertTrue(Modifier.isPublic(c.getModifiers()));
                    foundDefault = true;
                }
            }
            assertTrue(foundDefault);
        }, ClassNotFoundException.class);
    }

    private static void testDefaultPackage() {
        assertTest("Hello class is in the default package", () -> {
            Class<?> clazz = Class.forName("Hello");
            String pkg = clazz.getPackage() == null ? "" : clazz.getPackage().getName();
            assertEquals("", pkg);
        }, ClassNotFoundException.class);
    }

    private static void testExtendsObject() {
        assertTest("Hello class extends java.lang.Object", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(Object.class, clazz.getSuperclass());
        }, ClassNotFoundException.class);
    }

    private static void testNoImplementedInterfaces() {
        assertTest("Hello class implements no interfaces", () -> {
            Class<?> clazz = Class.forName("Hello");
            assertEquals(0, clazz.getInterfaces().length);
        }, ClassNotFoundException.class);
    }

    private static void testCanInstantiateHello() {
        assertTest("Hello can be instantiated with new", () -> {
            Class<?> clazz = Class.forName("Hello");
            Object instance = clazz.getDeclaredConstructor().newInstance();
            assertNotNull(instance);
        }, Exception.class);
    }

    private static void testInstanceIsTypeHello() {
        assertTest("Instance of Hello has correct runtime type", () -> {
            Class<?> clazz = Class.forName("Hello");
            Object instance = clazz.getDeclaredConstructor().newInstance();
            assertEquals(clazz, instance.getClass());
        }, Exception.class);
    }

    private static void testExitCodeZero() {
        assertTest("Running 'java Hello' exits with code 0", () -> {
            ProcessBuilder pb = new ProcessBuilder("java", "Hello");
            pb.directory(new File(System.getProperty("user.dir")));
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String rawOutput = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();
            assertEquals(0, exitCode);
            StringBuilder filtered = new StringBuilder();
            for (String line : rawOutput.split("\n")) {
                if (!line.startsWith("Picked up JAVA_TOOL_OPTIONS")) {
                    filtered.append(line).append("\n");
                }
            }
            String output = filtered.toString();
            if (output.endsWith("\n") && !System.lineSeparator().equals("\n")) {
                output = output.substring(0, output.length() - 1) + System.lineSeparator();
            }
            assertEquals("Hello, World!" + System.lineSeparator(), output);
        });
    }

    private static void testNoFileIOOperations() {
        assertTest("main() does not create or modify any files", () -> {
            File dir = new File(System.getProperty("user.dir"));
            String[] before = dir.list((d, name) -> !name.startsWith("."));
            java.util.Set<String> beforeSet = new java.util.HashSet<>(java.util.Arrays.asList(before));
            captureMainOutput();
            String[] after = dir.list((d, name) -> !name.startsWith("."));
            java.util.Set<String> afterSet = new java.util.HashSet<>(java.util.Arrays.asList(after));
            assertEquals(beforeSet.size(), afterSet.size());
            assertTrue(beforeSet.containsAll(afterSet));
        });
    }

    private static void testNoSystemPropertyModification() {
        assertTest("main() does not modify system properties", () -> {
            java.util.Properties before = (java.util.Properties) System.getProperties().clone();
            captureMainOutput();
            java.util.Properties after = System.getProperties();
            for (String key : before.stringPropertyNames()) {
                assertEquals(before.getProperty(key), after.getProperty(key));
            }
            assertTrue(before.stringPropertyNames().containsAll(after.stringPropertyNames()));
        });
    }

    private static void testSourceFileEncoding() {
        assertTest("Hello.java source file is valid UTF-8", () -> {
            Path sourceFile = Path.of("Hello.java");
            byte[] bytes = Files.readAllBytes(sourceFile);
            String content = new String(bytes, StandardCharsets.UTF_8);
            assertNotNull(content);
            assertTrue(content.length() > 0);
        });
    }

    private static void testSourceFileHasNoPackageDeclaration() {
        assertTest("Hello.java has no package declaration", () -> {
            Path sourceFile = Path.of("Hello.java");
            String content = Files.readString(sourceFile, StandardCharsets.UTF_8);
            for (String line : content.split("\n")) {
                String trimmed = line.trim();
                assertFalse(trimmed.startsWith("package "));
            }
        });
    }

    private static void testSourceFileSingleClass() {
        assertTest("Hello.java contains exactly one public class", () -> {
            Path sourceFile = Path.of("Hello.java");
            String content = Files.readString(sourceFile, StandardCharsets.UTF_8);
            int count = 0;
            for (String line : content.split("\n")) {
                String trimmed = line.trim();
                if (trimmed.startsWith("public class ")) {
                    count++;
                }
            }
            assertEquals(1, count);
        });
    }

    private static void testExecutionTimeReasonable() {
        assertTest("main() executes within 100ms", () -> {
            long start = System.nanoTime();
            captureMainOutput();
            long elapsed = System.nanoTime() - start;
            long elapsedMs = elapsed / 1_000_000;
            assertTrue(elapsedMs < 100);
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
