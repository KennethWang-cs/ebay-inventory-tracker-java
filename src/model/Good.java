package model;

import java.time.LocalDate;

public class Good {
    private int id;
    private String name;
    private String brand;
    private int quantity;
    private double cost;
    private LocalDate dateListed;

    public Good(int id, String name, String brand,
                int quantity, double cost, LocalDate dateListed) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.quantity = quantity;
        this.cost = cost;
        this.dateListed = dateListed;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getCost() {
        return cost;
    }

    public LocalDate getDateListed() {
        return dateListed;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException(
                "Quantity cannot be negative."
            );
        }
        this.quantity = quantity;
    }

    public void setCost(double cost) {
        if (cost < 0) {
            throw new IllegalArgumentException(
                "Cost cannot be negative."
            );
        }
        this.cost = cost;
    }

    @Override
    public String toString() {
        return name + " (" + brand + ")";
    }
}
