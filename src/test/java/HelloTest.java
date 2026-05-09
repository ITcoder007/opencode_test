import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Hello class tests")
class HelloTest {

    @Test
    @DisplayName("getGreeting returns expected string")
    void testGetGreeting() {
        assertEquals("Hello, World!", Hello.getGreeting());
    }

    @Test
    @DisplayName("getGreeting returns non-null and non-empty")
    void testGetGreetingNotNull() {
        assertNotNull(Hello.getGreeting());
        assertFalse(Hello.getGreeting().isEmpty());
    }

    @Test
    @DisplayName("getGreeting matches Python output format")
    void testMatchesPythonOutput() {
        String expected = "Hello, World!";
        assertEquals(expected, Hello.getGreeting(),
            "Java output should match Python print('Hello, World!')");
    }
}
