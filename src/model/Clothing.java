package model;

import java.time.LocalDate;

public class Clothing extends Good {
    private String size;
    private String color;
    private String category;

    public Clothing(int id, String name, String brand,
                    int quantity, double cost, LocalDate dateListed,
                    String size, String color, String category) {

        super(id, name, brand, quantity, cost, dateListed);

        this.size = size;
        this.color = color;
        this.category = category;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public String getCategory() {
        return category;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
