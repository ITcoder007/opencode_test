import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.*;

class HelloTest {

    @Test
    void testMainOutputsHelloWorld() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try {
            Hello.main(new String[]{});
            String output = baos.toString().trim();
            assertEquals("Hello, World!", output);
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testMainWithArgumentsDoesNotCrash() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try {
            Hello.main(new String[]{"arg1", "arg2"});
            String output = baos.toString().trim();
            assertEquals("Hello, World!", output);
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testMainOutputEndsWithNewline() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try {
            Hello.main(new String[]{});
            String output = baos.toString();
            assertTrue(output.endsWith("\n"), "Output should end with a newline");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testMainOutputIsExactlyHelloWorld() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try {
            Hello.main(new String[]{});
            String output = baos.toString();
            assertEquals("Hello, World!\n", output);
        } finally {
            System.setOut(originalOut);
        }
    }
}
