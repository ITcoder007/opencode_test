import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
    public void testMainOutputsHelloWorld() {
        Hello.main(new String[]{});
        assertEquals("Hello, World!" + System.lineSeparator(), outContent.toString());
    }

    @Test
    public void testMainHandlesEmptyArgs() {
        Hello.main(new String[]{});
        String output = outContent.toString();
        assertNotNull(output);
        assertEquals("Hello, World!" + System.lineSeparator(), output);
    }

    @Test
    public void testMainHandlesNullArgs() {
        Hello.main(null);
        assertEquals("Hello, World!" + System.lineSeparator(), outContent.toString());
    }
}
