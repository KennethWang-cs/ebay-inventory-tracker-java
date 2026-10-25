
package account;

import model.Good;
import model.Sale;
import storage.FileStorage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class EBayAccount {
    private final List<Good> allGoods;
    private final List<Sale> sales;
    private final FileStorage storage;
    private int nextId;

    public EBayAccount() {
        allGoods = new ArrayList<>();
        sales = new ArrayList<>();
        storage = new FileStorage();
        nextId = 1;

        loadData();
    }

    private void loadData() {
        try {
            FileStorage.Data data = storage.load();

            allGoods.addAll(data.goods);
            sales.addAll(data.sales);

            for (Good good : allGoods) {
                nextId = Math.max(nextId, good.getId() + 1);
            }

        } catch (IOException ex) {
            throw new IllegalStateException(
                "Could not load inventory data. "
                    + "Check data/inventory.txt.",
                ex
            );
        }
    }

    private void saveData() {
        try {
            storage.save(allGoods, sales);
        } catch (IOException ex) {
            throw new IllegalStateException(
                "Could not save inventory data.",
                ex
            );
        }
    }

    public Good addGood(Good good) {
        allGoods.add(good);
        nextId = Math.max(nextId, good.getId() + 1);

        saveData();

        return good;
    }

    public int getNextId() {
        return nextId;
    }

    public List<Good> getAllGoods() {
        return new ArrayList<>(allGoods);
    }

    public List<Good> getActiveGoods() {
        List<Good> active = new ArrayList<>();

        for (Good good : allGoods) {
            if (good.getQuantity() > 0) {
                active.add(good);
            }
        }

        return active;
    }

    public List<Sale> getSales() {
        return new ArrayList<>(sales);
    }

    public int getInventoryQuantity() {
        int total = 0;

        for (Good good : allGoods) {
            total += good.getQuantity();
        }

        return total;
    }

    public double getTotalProfit() {
        double total = 0;

        for (Sale sale : sales) {
            total += sale.getProfit();
        }

        return total;
    }

    public int getTotalSales() {
        return sales.size();
    }

    public Sale recordSale(int itemId, int quantitySold,
                           double soldPrice,
                           double shippingCharged,
                           double shippingCost,
                           double fees) {

        if (quantitySold <= 0) {
            throw new IllegalArgumentException(
                "Quantity sold must be positive."
            );
        }

        if (!Double.isFinite(soldPrice) || soldPrice < 0
            || !Double.isFinite(shippingCharged)
            || shippingCharged < 0
            || !Double.isFinite(shippingCost)
            || shippingCost < 0
            || !Double.isFinite(fees) || fees < 0) {

            throw new IllegalArgumentException(
                "Sale amounts must be valid non-negative numbers."
            );
        }

        Good selected = null;

        for (Good good : allGoods) {
            if (good.getId() == itemId) {
                selected = good;
                break;
            }
        }

        if (selected == null) {
            throw new IllegalArgumentException("Item not found.");
        }

        if (selected.getQuantity() < quantitySold) {
            throw new IllegalArgumentException(
                "Not enough inventory available."
            );
        }

        Sale sale = new Sale(
            selected.getId(),
            selected.getName(),
            LocalDate.now(),
            quantitySold,
            selected.getCost(),
            soldPrice,
            shippingCharged,
            shippingCost,
            fees
        );

        sales.add(sale);
        selected.setQuantity(
            selected.getQuantity() - quantitySold
        );

        saveData();

        return sale;
    }

    public void editSale(
        Sale saleToEdit,
        int newQuantitySold,
        double soldPrice,
        double shippingCharged,
        double shippingCost,
        double fees) {

        if (saleToEdit == null) {
            throw new IllegalArgumentException("Sale cannot be null.");
        }

        if (newQuantitySold <= 0) {
            throw new IllegalArgumentException(
                "Quantity sold must be greater than zero.");
        }

        if (!Double.isFinite(soldPrice) || soldPrice < 0
            || !Double.isFinite(shippingCharged) || shippingCharged < 0
            || !Double.isFinite(shippingCost) || shippingCost < 0
            || !Double.isFinite(fees) || fees < 0) {
            throw new IllegalArgumentException(
                "Sale amounts must be valid, non-negative numbers.");
        }

        if (!sales.contains(saleToEdit)) {
            throw new IllegalArgumentException(
                "Sale was not found.");
        }

        Good item = null;

        for (Good good : allGoods) {
            if (good.getId() == saleToEdit.getItemId()) {
                item = good;
                break;
            }
        }

        if (item == null) {
            throw new IllegalArgumentException(
                "The original inventory item was not found.");
        }

        int oldQuantitySold = saleToEdit.getQuantitySold();

        // Calculate inventory as though the original sale
        // had been undone, then apply the corrected sale.
        int updatedInventoryQuantity =
            item.getQuantity() + oldQuantitySold - newQuantitySold;

        if (updatedInventoryQuantity < 0) {
            throw new IllegalArgumentException(
                "Not enough inventory to increase the quantity sold.");
        }

        int previousInventoryQuantity = item.getQuantity();

        double oldSoldPrice = saleToEdit.getSoldPrice();
        double oldShippingCharged = saleToEdit.getShippingCharged();
        double oldShippingCost = saleToEdit.getShippingCost();
        double oldFees = saleToEdit.getFees();

        item.setQuantity(updatedInventoryQuantity);

        saleToEdit.updateDetails(
            newQuantitySold,
            soldPrice,
            shippingCharged,
            shippingCost,
            fees
        );

        try {
            saveData();
        } catch (IllegalStateException ex) {
            item.setQuantity(previousInventoryQuantity);

            saleToEdit.updateDetails(
                oldQuantitySold,
                oldSoldPrice,
                oldShippingCharged,
                oldShippingCost,
                oldFees
            );

            throw ex;
        }
    }

    public void deleteGood(int itemId) {
        Good itemToDelete = null;

        for (Good good : allGoods) {
            if (good.getId() == itemId) {
                itemToDelete = good;
                break;
            }
        }

        if (itemToDelete == null) {
            throw new IllegalArgumentException(
                "Inventory item not found."
            );
        }

        for (Sale sale : sales) {
            if (sale.getItemId() == itemId) {
                throw new IllegalArgumentException(
                    "This item has recorded sales and cannot be deleted."
                );
            }
        }

        Good deletedItem = itemToDelete;

        allGoods.remove(deletedItem);

        try {
            saveData();
        } catch (IllegalStateException ex) {
            allGoods.add(deletedItem);
            throw ex;
        }
    }
}
