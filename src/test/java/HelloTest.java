import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class HelloTest {

    private ByteArrayOutputStream capturedOut;
    private PrintStream originalOut;

    @BeforeEach
    void setUpStreams() {
        originalOut = System.out;
        capturedOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    void testMainOutputsHelloWorld() {
        Hello.main(new String[]{});
        String output = capturedOut.toString().trim();
        assertEquals("Hello, World!", output);
    }

    @Test
    void testMainOutputIsExactlyOneLine() {
        Hello.main(new String[]{});
        String output = capturedOut.toString();
        long lineCount = output.lines().count();
        assertEquals(1, lineCount, "Output should contain exactly one line");
    }

    @Test
    void testMainOutputContainsHello() {
        Hello.main(new String[]{});
        String output = capturedOut.toString();
        assertTrue(output.contains("Hello"), "Output should contain 'Hello'");
    }

    @Test
    void testMainOutputContainsWorld() {
        Hello.main(new String[]{});
        String output = capturedOut.toString();
        assertTrue(output.contains("World"), "Output should contain 'World'");
    }

    @Test
    void testMainIgnoresArgs() {
        Hello.main(new String[]{"arg1", "arg2"});
        String output = capturedOut.toString().trim();
        assertEquals("Hello, World!", output, "Output should be the same regardless of args");
    }
}
