package aut.ap;

public class Main {
    public static void main(String[] args) {
        int a = 1;
        int b = 1;
        for (int i = 0; i < 10; i++) {
            a = a + b;
            b = a - b;
        }
        System.out.println(a);
    }
}