package model;

import java.time.LocalDate;

public class Sale {
    private int itemId;
    private String itemName;
    private LocalDate dateSold;

    private int quantitySold;
    private double itemCostAtSale;
    private double soldPrice;
    private double shippingCharged;
    private double shippingCost;
    private double fees;

    public Sale(int itemId, String itemName, LocalDate dateSold,
                int quantitySold, double itemCostAtSale,
                double soldPrice, double shippingCharged,
                double shippingCost, double fees) {

        this.itemId = itemId;
        this.itemName = itemName;
        this.dateSold = dateSold;
        this.quantitySold = quantitySold;
        this.itemCostAtSale = itemCostAtSale;
        this.soldPrice = soldPrice;
        this.shippingCharged = shippingCharged;
        this.shippingCost = shippingCost;
        this.fees = fees;
    }

    public int getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public LocalDate getDateSold() {
        return dateSold;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public double getItemCostAtSale() {
        return itemCostAtSale;
    }

    public double getSoldPrice() {
        return soldPrice;
    }

    public double getShippingCharged() {
        return shippingCharged;
    }

    public double getShippingCost() {
        return shippingCost;
    }

    public double getFees() {
        return fees;
    }

    public double getTotalItemCost() {
        return itemCostAtSale * quantitySold;
    }

    public double getProfit() {
        return soldPrice + shippingCharged
            - getTotalItemCost()
            - shippingCost - fees;
    }

    public void updateDetails(
        int quantitySold,
        double soldPrice,
        double shippingCharged,
        double shippingCost,
        double fees) {

        this.quantitySold = quantitySold;
        this.soldPrice = soldPrice;
        this.shippingCharged = shippingCharged;
        this.shippingCost = shippingCost;
        this.fees = fees;
    }
}
