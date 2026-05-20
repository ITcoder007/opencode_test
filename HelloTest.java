import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class HelloTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUpStreams() {
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
    public void testMainOutputWithArgs() {
        Hello.main(new String[]{"arg1", "arg2"});
        assertEquals("Hello, World!" + System.lineSeparator(), outContent.toString());
    }

    @Test
    public void testMainOutputWithNullArgs() {
        Hello.main(null);
        assertEquals("Hello, World!" + System.lineSeparator(), outContent.toString());
    }
}
