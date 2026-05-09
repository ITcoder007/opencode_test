import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class HelloTest {

    @Test
    void testGetGreeting() {
        Hello hello = new Hello();
        assertEquals("Hello, World!", hello.getGreeting());
    }

    @Test
    void testGetGreetingNotNull() {
        Hello hello = new Hello();
        assertNotNull(hello.getGreeting());
    }

    @Test
    void testGetGreetingNotEmpty() {
        Hello hello = new Hello();
        assertFalse(hello.getGreeting().isEmpty());
    }

    @Test
    void testGetGreetingStartsWithHello() {
        Hello hello = new Hello();
        assertTrue(hello.getGreeting().startsWith("Hello"));
    }

    @Test
    void testGetGreetingContainsWorld() {
        Hello hello = new Hello();
        assertTrue(hello.getGreeting().contains("World"));
    }

    @Test
    void testMainOutput() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        try {
            Hello.main(new String[]{});
            assertEquals("Hello, World!" + System.lineSeparator(), outContent.toString());
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testMultipleInstancesReturnSameGreeting() {
        Hello hello1 = new Hello();
        Hello hello2 = new Hello();
        assertEquals(hello1.getGreeting(), hello2.getGreeting());
    }
}
