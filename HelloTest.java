import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Hello - Python to Java 转换验证")
public class HelloTest {

    private ByteArrayOutputStream capturedOut;
    private PrintStream originalOut;

    @BeforeEach
    void captureSystemOut() {
        originalOut = System.out;
        capturedOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut));
    }

    @AfterEach
    void restoreSystemOut() {
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("getGreeting 应返回与 Python print 一致的问候语")
    void testGetGreeting() {
        String expected = "Hello, World!";
        assertEquals(expected, Hello.getGreeting(),
                "Java getGreeting() 输出应与 Python print 内容一致");
    }

    @Test
    @DisplayName("main 方法应向 stdout 输出 Hello, World!")
    void testMainOutput() {
        Hello.main(new String[]{});
        String output = capturedOut.toString();
        assertEquals("Hello, World!" + System.lineSeparator(), output,
                "Java main() 的 stdout 输出应与 Python 程序完全一致");
    }

    @Test
    @DisplayName("getGreeting 返回值不应为 null")
    void testGetGreetingNotNull() {
        org.junit.jupiter.api.Assertions.assertNotNull(Hello.getGreeting(),
                "getGreeting() 不应返回 null");
    }

    @Test
    @DisplayName("getGreeting 返回值应非空字符串")
    void testGetGreetingNonEmpty() {
        org.junit.jupiter.api.Assertions.assertFalse(Hello.getGreeting().isEmpty(),
                "getGreeting() 不应返回空字符串");
    }
}
