import static org.junit.Assert.*;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class HelloTest {

    @Test
    public void testMainOutput() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));
        try {
            Hello.main(new String[]{});
            assertEquals("Hello, World!\n", bos.toString());
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testMainOutputContent() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));
        try {
            Hello.main(new String[]{});
            String output = bos.toString().trim();
            assertEquals("Hello, World!", output);
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testMainOutputLength() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));
        try {
            Hello.main(new String[]{});
            String output = bos.toString();
            assertTrue(output.length() > 0);
            assertFalse(output.isEmpty());
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testMainWithNullArgs() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));
        try {
            Hello.main(null);
            assertEquals("Hello, World!\n", bos.toString());
        } finally {
            System.setOut(originalOut);
        }
    }
}
