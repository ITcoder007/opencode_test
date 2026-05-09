import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class HelloTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private PrintStream originalOut;

    @Before
    public void setUpStreams() {
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    public void testMainOutput() {
        Hello.main(new String[]{});
        assertEquals("Hello, World!" + System.lineSeparator(), outContent.toString());
    }

    @Test
    public void testMainOutputNotEmpty() {
        Hello.main(new String[]{});
        assertFalse("Output should not be empty", outContent.toString().isEmpty());
    }

    @Test
    public void testMainOutputContainsHelloWorld() {
        Hello.main(new String[]{});
        assertTrue("Output should contain 'Hello, World!'",
                outContent.toString().contains("Hello, World!"));
    }

    @Test
    public void testMainNoException() {
        try {
            Hello.main(new String[]{});
        } catch (Exception e) {
            fail("Hello.main() should not throw an exception: " + e.getMessage());
        }
    }

    @Test
    public void testMainOutputExactLineCount() {
        Hello.main(new String[]{});
        String[] lines = outContent.toString().split(System.lineSeparator());
        assertEquals("Output should contain exactly 1 line", 1, lines.length);
    }
}
