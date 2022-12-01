import java.util.ArrayList;
import java.util.Scanner;
import java.util.InputMismatchException;
import java.time.*;

public class Main {
    public static void main(String[] args) {
        Test test = new Test();
        int n = Input.nextInt("the days of the shop working");
        for (int i = 0; i < n; i++) {
            test.startDay();
            for (int j = 0, animalNumber = Input.nextInt("the number of new animals"); j < animalNumber; j++) {
                try {
                    test.purchase();
                } catch (InsufficientBalanceException e) {
                    System.out.println("Cannot buy:" + e);
                }
            }
            for (int j = 0, customerNumber = Input.nextInt("the number of new customers"); j < customerNumber; j++) {
                try {
                    test.treat();
                } catch (AnimalNotFountException e) {
                    System.out.println("No any animal:" + e + "\nplease come tomorrow(break)");
                    break;
                } catch (isNotWorkingException e) {
                    System.out.println("Resting:" + e + "\nplease come tomorrow(break)");
                    break;
                }
            }
            test.endDay();
        }
    }
}

enum Gender {
    male(true), female(false);

    Gender(boolean isMale) {
    }
}

abstract class Animal {
    protected final String name;
    protected final int age;
    protected final Gender gender;
    protected final double price;

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public Gender getGender() {
        return gender;
    }

    public double getPrice() {
        return price;
    }

    public Animal(String name, int age, Gender gender, double price) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.price = price;
    }

    @Override
    public abstract String toString();
}

final class ChineseFieldDog extends Animal {
    private boolean isVaccineInjected;

    public void doVaccineInject() {
        isVaccineInjected = true;
    }

    public ChineseFieldDog(String name, int age, Gender gender, boolean isVaccineInjected) {
        super(name, age, gender, 100);
        this.isVaccineInjected = isVaccineInjected;
    }

    @Override
    public String toString() {
        return String.format("name: %s\tage: %d\tgender: %s\tprice: %f\t", name, age, gender.name(), price)
                + String.format("%s", isVaccineInjected ? "is vaccineInjected" : "isn't vaccineInjected");
    }
}

final class Cat extends Animal {
    public Cat(String name, int age, Gender gender) {
        super(name, age, gender, 200);
    }

    @Override
    public String toString() {
        return String.format("name: %s\tage: %d\tgender: %s\tprice: %f", name, age, gender.name(), price);
    }
}

final class Pig extends Animal {
    private double weight;

    public void feed(double foodWeight) {
        weight += foodWeight;
    }

    public Pig(String name, int age, Gender gender, double weight) {
        super(name, age, gender, 200);
        this.weight = weight;
    }

    @Override
    public String toString() {
        return String.format("name: %s\tage: %d\tgender: %s\tprice: %f\t", name, age, gender.name(), price)
                + String.format("weight: %f", weight);
    }
}

final class Customer {
    private final String name;
    private int comeCount;
    private LocalDate latestComeTime;

    public void visit(LocalDate localDate) {
        comeCount++;
        latestComeTime = localDate;
    }

    public Customer(String name) {
        this.name = name;
        this.comeCount = 0;
    }

    @Override
    public String toString() {
        return String.format("name: %s\tcomeCount: %d\tlatestComeTime: %s", name, comeCount, latestComeTime);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Customer) {
            return name.equals(((Customer) o).name);
        }
        return false;
    }
}

interface AnimalShop {
    void purchase(Animal animal);

    void treat(Customer customer, LocalDate localDate);

    void rest();
}

final class MyAnimalShop implements AnimalShop {
    private final double eachProfitTimes;
    private double amount;
    private double yesterdayAmount;
    ArrayList<Animal> animals;
    ArrayList<Customer> customers = new ArrayList<>();
    private boolean isWorking;

    public MyAnimalShop(double amount, ArrayList<Animal> animals, double eachProfitTimes) {
        this.amount = amount;
        this.yesterdayAmount = amount;
        this.animals = animals;
        this.eachProfitTimes = eachProfitTimes;
    }

    @Override
    public void purchase(Animal animal) {
        if (amount < animal.getPrice()) {
            throw new InsufficientBalanceException(
                    String.format("the amount is %f, want to buy animal %s with %f",
                            amount, animal.getName(), animal.getPrice())
            );
        }
        amount -= animal.getPrice();
        animals.add(animal);
    }

    @Override
    public void treat(Customer customer, LocalDate localDate) {
        if (!isWorking) {
            throw new isNotWorkingException("shop is resting");
        }

        int index = customers.indexOf(customer);
        if (index != -1) {
            customers.get(index).visit(localDate);
        } else {
            customer.visit(localDate);
            customers.add(customer);
        }

        if (animals.size() == 0) {
            throw new AnimalNotFountException("haven't any animal");
        }

        Animal saleAnimal = animals.get(animals.size() - 1);
        System.out.println("sale: " + saleAnimal);
        amount += saleAnimal.getPrice() * eachProfitTimes;
        animals.remove(animals.size() - 1);
    }

    public void startDay(LocalDate localDate) {
        isWorking = localDate.getDayOfWeek() != DayOfWeek.SATURDAY && localDate.getDayOfWeek() != DayOfWeek.SUNDAY;
    }

    public void endDay() {
        if (isWorking) {
            customers.forEach(System.out::println);
            System.out.println("today's profit is " + (amount - yesterdayAmount));
            yesterdayAmount = amount;
            customers.clear();
        }
    }

    @Override
    public void rest() {
        endDay();
    }
}

final class Test {
    private double amount;
    private final ArrayList<Animal> animals = new ArrayList<>();
    private LocalDate localDate = LocalDate.now();
    MyAnimalShop myAnimalShop;

    private void catchAnimals() {
        int n = Input.nextInt("the number of animals");
        for (int i = 0; i < n; i++) {
            animals.add(Input.nextAnimal());
        }
    }

    private void initialMoney() {
        amount = Input.nextDouble("the initial money of the shop");
    }

    public Test() {
        initialMoney();
        catchAnimals();
        myAnimalShop = new MyAnimalShop(amount, animals, Input.nextDouble("the price multiple sold"));
        System.out.println("Congratulation, your shop opened successfully!");
    }

    public void startDay() {
        localDate = localDate.plusDays(1);
        System.out.println("A new day! " + localDate + " " + localDate.getDayOfWeek());
        myAnimalShop.startDay(localDate);
    }

    public void purchase() {
        myAnimalShop.purchase(Input.nextAnimal());
    }

    public void treat() {
        myAnimalShop.treat(Input.nextCustomer(), localDate);
    }

    public void endDay() {
        myAnimalShop.endDay();
    }
}

// input check: int, double, String, Animal, Customer
interface Input {
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
                System.out.printf("the %s should be double, please input again\n", _name);
                scanner.next();
            }
        }
        return x;
    }

    static String next(String _name) {
        String x = null;
        Scanner scanner = new Scanner(System.in);
        System.out.printf("Input %s\n", _name);
        while (x == null) {
            try {
                x = scanner.next();
            } catch (InputMismatchException e) {
                System.out.printf("the %s should be String, please input again\n", _name);
            }
        }
        return x;
    }

    static boolean nextBoolean(String _name) {
        String input = Input.next(_name + "('yes' or 'no')");
        while (!"yes".equals(input) && !"no".equals(input)) {
            System.out.println("please input 'yes' or 'no', input again");
            input = Input.next(_name + "('yes' or 'no')");
        }
        return "yes".equals(input);
    }

    static Animal nextAnimal() {
        String species = Input.next("the species of new animal(dog or cat or pig)");
        while (!("dog".equals(species) || "cat".equals(species) || "pig".equals(species))) {
            System.out.println("no such species, input another");
            species = Input.next("the species of new animal(dog or cat or pig)");
        }
        String name = Input.next("the name of this " + species);
        int age = Input.nextInt("the age of this " + species);
        Gender gender = Input.nextBoolean("weather male") ? Gender.male : Gender.female;
        return switch (species) {
            case "dog" -> {
                boolean isVaccineInjected = Input.nextBoolean("weather injected");
                yield new ChineseFieldDog(name, age, gender, isVaccineInjected);
            }
            case "cat" -> new Cat(name, age, gender);
            case "pig" -> {
                double weight = Input.nextDouble("the weight of this " + species);
                yield new Pig(name, age, gender, weight);
            }
            default -> throw new NotSuchAnimalException(name + ", this species of animal is not exist");
        };
    }

    static Customer nextCustomer() {
        String name = Input.next("the name of new customer");
        return new Customer(name);
    }
}

// have no money
final class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException() {
        super();
    }

    public InsufficientBalanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientBalanceException(String message) {
        super(message);
    }

    public InsufficientBalanceException(Throwable cause) {
        super(cause);
    }
}

// have no animal
final class AnimalNotFountException extends RuntimeException {
    public AnimalNotFountException() {
        super();
    }

    public AnimalNotFountException(String message, Throwable cause) {
        super(message, cause);
    }

    public AnimalNotFountException(String message) {
        super(message);
    }

    public AnimalNotFountException(Throwable cause) {
        super(cause);
    }
}

// is not working
final class isNotWorkingException extends RuntimeException {
    public isNotWorkingException() {
        super();
    }

    public isNotWorkingException(String message, Throwable cause) {
        super(message, cause);
    }

    public isNotWorkingException(String message) {
        super(message);
    }

    public isNotWorkingException(Throwable cause) {
        super(cause);
    }
}

// not such Animal
final class NotSuchAnimalException extends RuntimeException {
    public NotSuchAnimalException() {
        super();
    }

    public NotSuchAnimalException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotSuchAnimalException(String message) {
        super(message);
    }

    public NotSuchAnimalException(Throwable cause) {
        super(cause);
    }
}
