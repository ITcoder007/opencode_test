import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class HelloTest {

    private final ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();
    private PrintStream originalOut;

    @BeforeEach
    void setUpStreams() {
        originalOut = System.out;
        System.setOut(new PrintStream(capturedOut));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    void mainOutputsHelloWorld() {
        Hello.main(new String[]{});
        String output = capturedOut.toString();
        assertEquals("Hello, World!" + System.lineSeparator(), output,
                "main方法应输出 'Hello, World!' 并换行");
    }

    @Test
    void mainHandlesNullArgs() {
        assertDoesNotThrow(() -> Hello.main(null),
                "main方法接受null参数时不应抛出异常");
    }

    @Test
    void mainHandlesEmptyArgs() {
        assertDoesNotThrow(() -> Hello.main(new String[]{}),
                "main方法接受空数组时不应抛出异常");
    }

    @Test
    void mainHandlesNonNullArgs() {
        assertDoesNotThrow(() -> Hello.main(new String[]{"arg1", "arg2"}),
                "main方法接受非空参数时不应抛出异常");
    }

    @Test
    void classCanBeInstantiated() {
        assertDoesNotThrow(() -> {
            Hello instance = new Hello();
            assertNotNull(instance, "Hello实例不应为null");
        }, "Hello类应可通过默认构造函数实例化");
    }

    @Test
    void outputIsExactlyHelloWorld() {
        Hello.main(new String[]{});
        String output = capturedOut.toString().trim();
        assertEquals("Hello, World!", output,
                "输出内容（去除换行后）应精确为 'Hello, World!'");
    }

    @Test
    void outputContainsNoExtraContent() {
        Hello.main(new String[]{});
        String output = capturedOut.toString();
        long lineCount = output.lines().count();
        assertEquals(1, lineCount,
                "输出应只有一行，不包含额外内容");
    }
}
