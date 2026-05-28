import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;

public class HelloTestSupplementary {

    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloTestSupplementary - Supplementary Tests ===\n");

        System.out.println("--- Class Metadata Integrity Tests ---");
        testNoDeclaredFields();
        testOnlyMainPublicMethod();
        testDefaultConstructorOnly();
        testNoInnerClasses();
        testNoDeclaredConstructorsWithArgs();

        System.out.println("\n--- Output Hash Determinism Tests ---");
        testOutputSHA256Deterministic();
        testOutputSHA256MatchesExpected();
        testOutputMD5Consistency();

        System.out.println("\n--- Process-Level Tests ---");
        testProcessExitCode();
        testProcessOutputMatches();

        System.out.println("\n--- File Structure Compliance Tests ---");
        testNoPackageDeclaration();
        testNoImportStatements();
        testFileExists();
        testClassFileExists();

        System.out.println("\n--- Python Equivalence Boundary Tests ---");
        testOutputDoesNotContainDefKeyword();
        testOutputDoesNotContainIfNameMain();
        testOutputDoesNotContainPythonShebang();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total: " + total + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("Result: ALL TESTS PASSED");
        } else {
            System.out.println("Result: SOME TESTS FAILED");
            System.exit(1);
        }
    }

    private static void testNoDeclaredFields() {
        assertTest("Hello class has no declared fields", () -> {
            Class<?> clazz = Class.forName("Hello");
            Field[] fields = clazz.getDeclaredFields();
            assertEquals(0, fields.length);
        });
    }

    private static void testOnlyMainPublicMethod() {
        assertTest("Hello has exactly one public method (main)", () -> {
            Class<?> clazz = Class.forName("Hello");
            Method[] publicMethods = clazz.getDeclaredMethods();
            int publicCount = 0;
            for (Method m : publicMethods) {
                if (Modifier.isPublic(m.getModifiers())) {
                    publicCount++;
                }
            }
            assertEquals(1, publicCount);
        });
    }

    private static void testDefaultConstructorOnly() {
        assertTest("Hello has default no-arg constructor", () -> {
            Class<?> clazz = Class.forName("Hello");
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            assertEquals(1, constructors.length);
            assertEquals(0, constructors[0].getParameterCount());
        });
    }

    private static void testNoInnerClasses() {
        assertTest("Hello has no inner or nested classes", () -> {
            Class<?> clazz = Class.forName("Hello");
            Class<?>[] innerClasses = clazz.getDeclaredClasses();
            assertEquals(0, innerClasses.length);
        });
    }

    private static void testNoDeclaredConstructorsWithArgs() {
        assertTest("Hello has no constructors with parameters", () -> {
            Class<?> clazz = Class.forName("Hello");
            for (Constructor<?> c : clazz.getDeclaredConstructors()) {
                assertEquals(0, c.getParameterCount());
            }
        });
    }

    private static void testOutputSHA256Deterministic() {
        assertTest("SHA-256 hash of output is deterministic across 50 runs", () -> {
            String firstHash = sha256Hex(captureMainOutput());
            for (int i = 0; i < 49; i++) {
                String currentHash = sha256Hex(captureMainOutput());
                if (!firstHash.equals(currentHash)) {
                    throw new AssertionError("SHA-256 hash differed at iteration " + (i + 1));
                }
            }
        });
    }

    private static void testOutputSHA256MatchesExpected() {
        assertTest("SHA-256 hash matches expected value for 'Hello, World!\\n'", () -> {
            String output = captureMainOutput();
            String hash = sha256Hex(output);
            String expectedOutput = "Hello, World!" + System.lineSeparator();
            String expectedHash = sha256Hex(expectedOutput);
            assertEquals(expectedHash, hash);
        });
    }

    private static void testOutputMD5Consistency() {
        assertTest("MD5 hash is consistent (fast integrity check)", () -> {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] first = md.digest(captureMainOutput().getBytes(StandardCharsets.UTF_8));
            byte[] second = md.digest(captureMainOutput().getBytes(StandardCharsets.UTF_8));
            assertByteArrayEquals(first, second);
        });
    }

    private static void testProcessExitCode() {
        assertTest("Running 'java Hello' exits with code 0", () -> {
            ProcessBuilder pb = new ProcessBuilder("java", "Hello");
            pb.directory(new File(System.getProperty("user.dir")));
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.getInputStream().transferTo(OutputStreamHolder.NULL);
            int exitCode = process.waitFor();
            assertEquals(0, exitCode);
        });
    }

    private static void testProcessOutputMatches() {
        assertTest("Subprocess output matches in-process output", () -> {
            ProcessBuilder pb = new ProcessBuilder("java", "Hello");
            pb.directory(new File(System.getProperty("user.dir")));
            pb.redirectErrorStream(true);
            pb.environment().remove("JAVA_TOOL_OPTIONS");
            Process process = pb.start();
            String processOutput = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            process.waitFor();
            String inProcessOutput = captureMainOutput();
            assertEquals(inProcessOutput.trim(), processOutput.trim());
        });
    }

    private static void testNoPackageDeclaration() {
        assertTest("Hello.java has no package declaration (matches Python no-module)", () -> {
            String source = Files.readString(new File("Hello.java").toPath());
            for (String line : source.split("\\R")) {
                String trimmed = line.trim();
                if (trimmed.startsWith("package ")) {
                    throw new AssertionError("Found package declaration: " + trimmed);
                }
            }
        });
    }

    private static void testNoImportStatements() {
        assertTest("Hello.java has no import statements (self-contained)", () -> {
            String source = Files.readString(new File("Hello.java").toPath());
            for (String line : source.split("\\R")) {
                String trimmed = line.trim();
                if (trimmed.startsWith("import ")) {
                    throw new AssertionError("Found import statement: " + trimmed);
                }
            }
        });
    }

    private static void testFileExists() {
        assertTest("Hello.java source file exists", () -> {
            File f = new File("Hello.java");
            assertTrue(f.exists() && f.isFile());
        });
    }

    private static void testClassFileExists() {
        assertTest("Hello.class compiled file exists", () -> {
            File f = new File("Hello.class");
            assertTrue(f.exists() && f.isFile());
        });
    }

    private static void testOutputDoesNotContainDefKeyword() {
        assertTest("Output does not contain Python 'def' keyword", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("def "));
        });
    }

    private static void testOutputDoesNotContainIfNameMain() {
        assertTest("Output does not contain Python '__name__' idiom", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("__name__"));
        });
    }

    private static void testOutputDoesNotContainPythonShebang() {
        assertTest("Output does not contain Python shebang line", () -> {
            String output = captureMainOutput();
            assertFalse(output.contains("#!/"));
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

    private static String sha256Hex(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static class OutputStreamHolder {
        static final OutputStream NULL = new OutputStream() {
            @Override public void write(int b) {}
            @Override public void write(byte[] b, int off, int len) {}
        };
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

    private static void assertByteArrayEquals(byte[] expected, byte[] actual) {
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
