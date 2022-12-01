import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        int option = 0;
        while (option == 0) {
            option = Input.nextInt("option(1 to thread, 2 to email)");
            if (option == 1)
                testThread();
            else if (option == 2)
                testEmail();
            else option = 0;
        }
    }

    static void testThread() {
        outputArray test1 = new outputArray();
        outputArray test2 = new outputArray();
        Thread thread1 = new outputThread(test1);
        Thread thread2 = new outputThread(test2);
        thread1.start();
        thread2.start();
    }

    static void testEmail() {
        if (emailCheck(Input.nextString("the email")))
            System.out.println("right format");
        else
            System.out.println("wrong format");
    }

    static boolean emailCheck(String email) {
        return email.matches("(.*)@(.*).com");
    }
}

class outputThread extends Thread {
    outputArray test;

    public outputThread(outputArray test) {
        this.test = test;
    }

    @Override
    public void run() {
        try {
            test.print();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

class outputArray {
    private static final class Locker {
        public static final Object lock = new Object();
    }

    ArrayList<Integer> array = new ArrayList<>();

    public outputArray() {
        int n = Input.nextInt("the size of array");
        while (n-- != 0)
            array.add(Input.nextInt("each number of array"));
    }

    static private boolean needWait = true;

    public void print() throws InterruptedException {
        synchronized (Locker.lock) {
            for (Integer integer : array) {
                System.out.println(integer);
                if (needWait) {
                    Locker.lock.notifyAll();
                    Locker.lock.wait();
                }
            }
            needWait = false;
            Locker.lock.notifyAll();
        }
    }
}

//input int/double/boolean/string and check error
final class Input {
    static int nextInt(String _name) {
        Integer x = null;
        Scanner scanner = new Scanner(System.in);
        System.out.printf("Input %s\n", _name);
        while (x == null) {
            try {
                x = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.printf("%s should be int, please input again\n", _name);
                scanner.next();
            }
        }
        return x;
    }

    static double nextDouble(String _name) {
        Double x = null;
        Scanner scanner = new Scanner(System.in);
        System.out.printf("Input %s\n", _name);
        while (x == null) {
            try {
                x = scanner.nextDouble();
            } catch (InputMismatchException e) {
                System.out.printf("%s should be double, please input again\n", _name);
                scanner.next();
            }
        }
        return x;
    }

    static String nextString(String _name) {
        String x = null;
        Scanner scanner = new Scanner(System.in);
        System.out.printf("Input %s\n", _name);
        while (x == null) {
            try {
                x = scanner.next();
            } catch (InputMismatchException e) {
                System.out.printf("%s should be String, please input again\n", _name);
            }
        }
        return x;
    }

    static boolean nextBoolean(String _name) {
        String input = Input.nextString(_name + "('yes' or 'no')");
        while (!"yes".equals(input) && !"no".equals(input)) {
            System.out.println("please input 'yes' or 'no', input again");
            input = Input.nextString(_name + "('yes' or 'no')");
        }
        return "yes".equals(input);
    }
}