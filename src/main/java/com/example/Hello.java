package com.example;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class Hello {

    public static String getGreeting() {
        return "Hello, World!";
    }

    public static void main(String[] args) {
        System.out.println(getGreeting());
    }

    public static String captureMainOutput() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(baos));
        try {
            main(new String[]{});
        } finally {
            System.setOut(originalOut);
        }
        return baos.toString().trim();
    }
}
