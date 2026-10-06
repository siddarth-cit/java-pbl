package model;

/**
 * Demonstrates OOP Core Concept: Abstract Class & Encapsulation.
 * Represents a generic person with basic demographic information.
 */
public abstract class Person {

    protected String name;
    protected int age;
    protected String phone;

    public Person(String name, int age, String phone) {
        this.name = name;
        this.age = age;
        this.phone = phone;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Abstract Method - Must be implemented by subclasses (Polymorphism)
    public abstract void displayDetails();
}
