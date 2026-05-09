import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
    public void testMainOutputNotNull() {
        Hello.main(new String[]{});
        assertNotNull(outContent.toString());
    }

    @Test
    public void testMainOutputContainsHelloWorld() {
        Hello.main(new String[]{});
        String output = outContent.toString().trim();
        assertEquals("Hello, World!", output);
    }

    @Test
    public void testMainOutputIsSingleLine() {
        Hello.main(new String[]{});
        String output = outContent.toString().trim();
        assertEquals(1, output.split(System.lineSeparator()).length);
    }
}
