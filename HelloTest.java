import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class HelloTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== HelloTest 开始 ===\n");

        testMainOutput();
        testMainOutputMatchesPython();
        testMainRunsTwiceConsistently();
        testMainWithArgs();
        testClassName();
        testMainAccessible();

        System.out.println("\n=== 测试结果 ===");
        System.out.printf("通过: %d  失败: %d  总计: %d%n", passed, failed, passed + failed);

        if (failed > 0) {
            System.out.println("结果: FAIL");
            System.exit(1);
        } else {
            System.out.println("结果: ALL PASSED");
        }
    }

    static void testMainOutput() {
        String output = captureMainOutput();
        assertEquals("testMainOutput: 输出应为 Hello, World!",
                "Hello, World!" + System.lineSeparator(), output);
    }

    static void testMainOutputMatchesPython() {
        String output = captureMainOutput();
        String expected = "Hello, World!" + System.lineSeparator();
        assertEquals("testMainOutputMatchesPython: Java输出应与Python一致",
                expected, output);
    }

    static void testMainRunsTwiceConsistently() {
        String first = captureMainOutput();
        String second = captureMainOutput();
        assertEquals("testMainRunsTwiceConsistently: 多次调用结果应一致", first, second);
    }

    static void testMainWithArgs() {
        String output = captureMainOutputWithArgs("unused", "args");
        assertEquals("testMainWithArgs: 即使传入参数也应正常输出",
                "Hello, World!" + System.lineSeparator(), output);
    }

    static void testClassName() {
        try {
            Class<?> clazz = Class.forName("Hello");
            assertNotNull("testClassName: Hello类应存在", clazz);
            passed++;
        } catch (ClassNotFoundException e) {
            System.out.printf("  FAIL: testClassName: 找不到Hello类 - %s%n", e.getMessage());
            failed++;
        }
    }

    static void testMainAccessible() {
        try {
            Class<?> clazz = Class.forName("Hello");
            java.lang.reflect.Method mainMethod = clazz.getMethod("main", String[].class);
            assertNotNull("testMainAccessible: main方法应存在", mainMethod);
            passed++;
        } catch (NoSuchMethodException e) {
            System.out.printf("  FAIL: testMainAccessible: 找不到main方法 - %s%n", e.getMessage());
            failed++;
        } catch (ClassNotFoundException e) {
            System.out.printf("  FAIL: testMainAccessible: 找不到Hello类 - %s%n", e.getMessage());
            failed++;
        }
    }

    private static String captureMainOutput() {
        return captureMainOutputWithArgs();
    }

    private static String captureMainOutputWithArgs(String... args) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream captured = new PrintStream(baos, true, StandardCharsets.UTF_8);
        System.setOut(captured);
        try {
            Hello.main(args);
        } finally {
            System.setOut(originalOut);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    private static void assertEquals(String testName, String expected, String actual) {
        if (expected.equals(actual)) {
            System.out.printf("  PASS: %s%n", testName);
            passed++;
        } else {
            System.out.printf("  FAIL: %s%n    期望: %s%n    实际: %s%n",
                    testName, escape(expected), escape(actual));
            failed++;
        }
    }

    private static void assertNotNull(String testName, Object obj) {
        if (obj != null) {
            System.out.printf("  PASS: %s%n", testName);
            passed++;
        } else {
            System.out.printf("  FAIL: %s - 值为null%n", testName);
            failed++;
        }
    }

    private static String escape(String s) {
        return s.replace("\n", "\\n").replace("\r", "\\r");
    }
}
