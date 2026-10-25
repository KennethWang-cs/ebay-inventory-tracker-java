
package gui;

import account.EBayAccount;
import model.Good;
import model.Sale;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

public class SalesPanel extends JPanel {

    private final Runnable onDataChanged;
    private final EBayAccount account;
    private final DefaultTableModel tableModel;
    private final JTable table;

    public SalesPanel(EBayAccount account, Runnable onDataChanged) {
        this.account = account;
        this.onDataChanged = onDataChanged;

        setLayout(new BorderLayout(10, 10));

        tableModel = new DefaultTableModel(
            new String[]{
                "Item ID", "Item", "Date Sold", "Quantity",
                "Item Cost", "Sold Price", "Shipping Charged",
                "Shipping Cost", "Fees", "Profit"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton recordButton = new JButton("Record Sale");
        recordButton.addActionListener(e -> showRecordSaleDialog());

        JButton editButton = new JButton("Edit Sale");
        editButton.addActionListener(e -> showEditSaleDialog());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshTable());

        JPanel buttons = new JPanel();
        buttons.add(recordButton);
        buttons.add(editButton);
        buttons.add(refreshButton);

        add(buttons, BorderLayout.SOUTH);

        refreshTable();
    }

    private void showRecordSaleDialog() {
        List<Good> inventory = account.getActiveGoods();

        if (inventory.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "There are no active inventory items to sell.",
                "No Inventory",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        JComboBox<Good> itemBox =
            new JComboBox<>(inventory.toArray(new Good[0]));

        JTextField quantityField = new JTextField("1");
        JTextField priceField = new JTextField("0.00");
        JTextField shippingChargedField = new JTextField("0.00");
        JTextField shippingCostField = new JTextField("0.00");
        JTextField feesField = new JTextField("0.00");

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));

        form.add(new JLabel("Inventory item:"));
        form.add(itemBox);
        form.add(new JLabel("Quantity sold:"));
        form.add(quantityField);
        form.add(new JLabel("Total sold price ($):"));
        form.add(priceField);
        form.add(new JLabel("Shipping charged ($):"));
        form.add(shippingChargedField);
        form.add(new JLabel("Shipping cost ($):"));
        form.add(shippingCostField);
        form.add(new JLabel("Total marketplace fees ($):"));
        form.add(feesField);
        form.add(new JLabel("Item cost per unit:"));
        form.add(new JLabel("Shown in inventory"));

        int result = JOptionPane.showConfirmDialog(
            this, form, "Record Sale",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Good selected = (Good) itemBox.getSelectedItem();

            if (selected == null) {
                throw new IllegalArgumentException(
                    "Please select an inventory item."
                );
            }

            int quantity = Integer.parseInt(
                quantityField.getText().trim()
            );

            Sale sale = account.recordSale(
                selected.getId(),
                quantity,
                parseAmount(priceField.getText()),
                parseAmount(shippingChargedField.getText()),
                parseAmount(shippingCostField.getText()),
                parseAmount(feesField.getText())
            );

            refreshTable();
            onDataChanged.run();

            JOptionPane.showMessageDialog(
                this,
                String.format(
                    "Sale recorded successfully!%n%n"
                        + "Quantity sold: %d%n"
                        + "Total item cost: $%.2f%n"
                        + "Profit: $%.2f",
                    sale.getQuantitySold(),
                    sale.getTotalItemCost(),
                    sale.getProfit()
                ),
                "Sale Recorded",
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (NumberFormatException ex) {
            showError(
                "Quantity must be a whole number, and all amounts must be numeric.",
                "Invalid Input"
            );
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage(), "Invalid Sale");
        } catch (IllegalStateException ex) {
            showError(ex.getMessage(), "Save Error");
        }
    }

    private void showEditSaleDialog() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            showError("Please select a sale to edit.", "No Sale Selected");
            return;
        }

        // Convert the visible row to its original model row.
        // This matters when the table has been sorted.
        int modelRow = table.convertRowIndexToModel(selectedRow);

        List<Sale> sales = account.getSales();

        if (modelRow < 0 || modelRow >= sales.size()) {
            showError("Could not find the selected sale.", "Error");
            return;
        }

        Sale selectedSale = sales.get(modelRow);

        JTextField quantityField = new JTextField(
            String.valueOf(selectedSale.getQuantitySold())
        );
        JTextField priceField = new JTextField(
            String.valueOf(selectedSale.getSoldPrice())
        );
        JTextField shippingChargedField = new JTextField(
            String.valueOf(selectedSale.getShippingCharged())
        );
        JTextField shippingCostField = new JTextField(
            String.valueOf(selectedSale.getShippingCost())
        );
        JTextField feesField = new JTextField(
            String.valueOf(selectedSale.getFees())
        );

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));

        form.add(new JLabel("Item:"));
        form.add(new JLabel(selectedSale.getItemName()));

        form.add(new JLabel("Date sold:"));
        form.add(new JLabel(selectedSale.getDateSold().toString()));

        form.add(new JLabel("Item cost per unit:"));
        form.add(new JLabel(
            String.format("$%.2f", selectedSale.getItemCostAtSale())
        ));

        form.add(new JLabel("Quantity sold:"));
        form.add(quantityField);

        form.add(new JLabel("Total sold price ($):"));
        form.add(priceField);

        form.add(new JLabel("Shipping charged ($):"));
        form.add(shippingChargedField);

        form.add(new JLabel("Shipping cost ($):"));
        form.add(shippingCostField);

        form.add(new JLabel("Total marketplace fees ($):"));
        form.add(feesField);

        int result = JOptionPane.showConfirmDialog(
            this,
            form,
            "Edit Sale",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int quantity = Integer.parseInt(
                quantityField.getText().trim()
            );

            double price = parseAmount(priceField.getText());
            double shippingCharged =
                parseAmount(shippingChargedField.getText());
            double shippingCost =
                parseAmount(shippingCostField.getText());
            double fees = parseAmount(feesField.getText());

            account.editSale(
                selectedSale,
                quantity,
                price,
                shippingCharged,
                shippingCost,
                fees
            );

            refreshTable();
            onDataChanged.run();

            JOptionPane.showMessageDialog(
                this,
                String.format(
                    "Sale updated successfully!%n%n"
                        + "Quantity sold: %d%n"
                        + "Total item cost: $%.2f%n"
                        + "Updated profit: $%.2f",
                    selectedSale.getQuantitySold(),
                    selectedSale.getTotalItemCost(),
                    selectedSale.getProfit()
                ),
                "Sale Updated",
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (NumberFormatException ex) {
            showError(
                "Quantity must be a whole number, and all amounts must be numeric.",
                "Invalid Input"
            );
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage(), "Invalid Sale");
        } catch (IllegalStateException ex) {
            showError(ex.getMessage(), "Save Error");
        }
    }

    private double parseAmount(String input) {
        double amount = Double.parseDouble(input.trim());

        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException(
                "Amounts must be valid non-negative numbers."
            );
        }

        return amount;
    }

    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(
            this,
            message,
            title,
            JOptionPane.ERROR_MESSAGE
        );
    }

    private void refreshTable() {
        tableModel.setRowCount(0);

        for (Sale sale : account.getSales()) {
            tableModel.addRow(new Object[]{
                sale.getItemId(),
                sale.getItemName(),
                sale.getDateSold(),
                sale.getQuantitySold(),
                String.format("$%.2f", sale.getItemCostAtSale()),
                String.format("$%.2f", sale.getSoldPrice()),
                String.format("$%.2f", sale.getShippingCharged()),
                String.format("$%.2f", sale.getShippingCost()),
                String.format("$%.2f", sale.getFees()),
                String.format("$%.2f", sale.getProfit())
            });
        }
    }
}
