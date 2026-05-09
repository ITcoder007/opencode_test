public class Hello {
    public String getGreeting() {
        return "Hello, World!";
    }

    public static void main(String[] args) {
        Hello hello = new Hello();
        System.out.println(hello.getGreeting());
    }
}
