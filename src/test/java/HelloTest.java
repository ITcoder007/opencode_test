import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.*;

class HelloTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    void testMainPrintsHelloWorld() {
        Hello.main(new String[]{});
        String output = outContent.toString().trim();
        assertEquals("Hello, World!", output,
            "Hello.main() should print 'Hello, World!' to stdout");
    }

    @Test
    void testMainOutputNotNull() {
        Hello.main(new String[]{});
        String output = outContent.toString();
        assertNotNull(output, "Output should not be null");
    }

    @Test
    void testMainOutputNotEmpty() {
        Hello.main(new String[]{});
        String output = outContent.toString().trim();
        assertFalse(output.isEmpty(), "Output should not be empty");
    }

    @Test
    void testMainOutputContainsHello() {
        Hello.main(new String[]{});
        String output = outContent.toString();
        assertTrue(output.contains("Hello"),
            "Output should contain 'Hello'");
    }

    @Test
    void testMainOutputContainsWorld() {
        Hello.main(new String[]{});
        String output = outContent.toString();
        assertTrue(output.contains("World"),
            "Output should contain 'World'");
    }

    @Test
    void testMainOutputEndsWithNewline() {
        Hello.main(new String[]{});
        String output = outContent.toString();
        assertTrue(output.endsWith("\n"),
            "println output should end with newline");
    }

    @Test
    void testMainOutputExactFormat() {
        Hello.main(new String[]{});
        assertEquals("Hello, World!\n", outContent.toString(),
            "Output should exactly match 'Hello, World!\\n'");
    }
}
