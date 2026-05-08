import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class HelloTest {

    private ByteArrayOutputStream capturedOutput;
    private PrintStream originalOut;

    @BeforeEach
    void setUpStreams() {
        originalOut = System.out;
        capturedOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOutput));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    void testGetMessageReturnsHelloWorld() {
        String result = Hello.getMessage();
        assertEquals("Hello, World!", result);
    }

    @Test
    void testGetMessageIsNotNull() {
        assertNotNull(Hello.getMessage());
    }

    @Test
    void testGetMessageIsNotEmpty() {
        assertFalse(Hello.getMessage().isEmpty());
    }

    @Test
    void testMainPrintsHelloWorld() {
        Hello.main(new String[]{});
        assertEquals("Hello, World!" + System.lineSeparator(), capturedOutput.toString());
    }

    @Test
    void testMainWithNullArgs() {
        Hello.main(null);
        assertEquals("Hello, World!" + System.lineSeparator(), capturedOutput.toString());
    }
}
