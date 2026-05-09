import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

@DisplayName("Hello class tests")
public class HelloTest {

    @Test
    @DisplayName("getGreeting should return Hello, World!")
    void testGetGreeting() {
        assertEquals("Hello, World!", Hello.getGreeting());
    }

    @Test
    @DisplayName("main should print Hello, World! to stdout")
    void testMainOutput() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        try {
            Hello.main(new String[]{});
            assertEquals("Hello, World!" + System.lineSeparator(), outContent.toString());
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    @DisplayName("getGreeting return type should be String")
    void testGetGreetingReturnType() {
        Object result = Hello.getGreeting();
        assertInstanceOf(String.class, result);
    }

    @Test
    @DisplayName("getGreeting should not return null")
    void testGetGreetingNotNull() {
        assertNotNull(Hello.getGreeting());
    }

    @Test
    @DisplayName("getGreeting should not be empty")
    void testGetGreetingNotEmpty() {
        assertFalse(Hello.getGreeting().isEmpty());
    }

    @Test
    @DisplayName("getGreeting should be consistent across calls")
    void testGetGreetingConsistency() {
        String first = Hello.getGreeting();
        String second = Hello.getGreeting();
        assertEquals(first, second);
    }

    @Test
    @DisplayName("getGreeting should contain expected content")
    void testGetGreetingContent() {
        String greeting = Hello.getGreeting();
        assertTrue(greeting.contains("Hello"));
        assertTrue(greeting.contains("World"));
    }

    @Test
    @DisplayName("main should handle null args gracefully")
    void testMainWithNullArgs() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        try {
            Hello.main(null);
            assertEquals("Hello, World!" + System.lineSeparator(), outContent.toString());
        } finally {
            System.setOut(originalOut);
        }
    }
}
