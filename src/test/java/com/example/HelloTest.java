package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HelloTest {

    @Test
    void testGetGreeting() {
        String greeting = Hello.getGreeting();
        assertEquals("Hello, World!", greeting);
    }

    @Test
    void testGetGreetingNotNull() {
        assertNotNull(Hello.getGreeting());
    }

    @Test
    void testGetGreetingNotEmpty() {
        assertFalse(Hello.getGreeting().isEmpty());
    }

    @Test
    void testGetGreetingStartsWithHello() {
        assertTrue(Hello.getGreeting().startsWith("Hello"));
    }

    @Test
    void testGetGreetingContainsWorld() {
        assertTrue(Hello.getGreeting().contains("World"));
    }

    @Test
    void testGetGreetingLength() {
        assertEquals(13, Hello.getGreeting().length());
    }

    @Test
    void testMainOutput() {
        String output = Hello.captureMainOutput();
        assertEquals("Hello, World!", output);
    }

    @Test
    void testMainOutputMatchesGreeting() {
        assertEquals(Hello.getGreeting(), Hello.captureMainOutput());
    }
}
