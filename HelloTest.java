import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class HelloTest {

    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    @Before
    public void setUpStreams() {
        outContent = new ByteArrayOutputStream();
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
    public void testMainOutputWithArgs() {
        Hello.main(new String[]{"arg1", "arg2"});
        assertEquals("Hello, World!" + System.lineSeparator(), outContent.toString());
    }

    @Test
    public void testMainOutputWithNullArgs() {
        Hello.main(null);
        assertEquals("Hello, World!" + System.lineSeparator(), outContent.toString());
    }

    @Test
    public void testOutputContainsExpectedText() {
        Hello.main(new String[]{});
        assertTrue(outContent.toString().contains("Hello, World!"));
    }

    @Test
    public void testOutputEndsWithNewline() {
        Hello.main(new String[]{});
        assertTrue(outContent.toString().endsWith(System.lineSeparator()));
    }

    @Test
    public void testOutputIsExactlyOneLine() {
        Hello.main(new String[]{});
        String output = outContent.toString().trim();
        assertFalse(output.contains("\n"));
    }

    @Test
    public void testMultipleInvocations() {
        Hello.main(new String[]{});
        String first = outContent.toString();
        outContent.reset();
        Hello.main(new String[]{});
        String second = outContent.toString();
        assertEquals(first, second);
    }

    @Test
    public void testOutputLength() {
        Hello.main(new String[]{});
        String expected = "Hello, World!" + System.lineSeparator();
        assertEquals(expected.length(), outContent.toString().length());
    }
}
