package gui;

import account.EBayAccount;
import model.Clothing;
import model.Good;

import javax.swing.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;

public class InventoryPanel extends JPanel {
    private final EBayAccount account;
    private final DefaultTableModel tableModel;
    private JTable table;

    public InventoryPanel(EBayAccount account) {
        this.account = account;
        setLayout(new BorderLayout(10, 10));

        tableModel = new DefaultTableModel(
            new String[]{
                "ID", "Name", "Brand", "Category",
                "Size", "Color", "Quantity", "Cost", "Date Listed"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton addButton = new JButton("Add Item");
        addButton.addActionListener(e -> showAddItemDialog());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshTable());

        JButton editButton = new JButton("Edit Item");
        editButton.addActionListener(e -> editSelectedItem());

        JButton deleteButton = new JButton("Delete Item");
        deleteButton.addActionListener(e -> deleteSelectedItem());


        JPanel buttons = new JPanel();
        buttons.add(addButton);
        buttons.add(refreshButton);
        buttons.add(editButton);
        buttons.add(deleteButton);
        add(buttons, BorderLayout.SOUTH);

        refreshTable();
    }

    private void showAddItemDialog() {
        JTextField nameField = new JTextField();
        JTextField brandField = new JTextField();
        JTextField quantityField = new JTextField("1");
        JTextField costField = new JTextField("0.00");

        JComboBox<String> categoryBox = new JComboBox<>(
            new String[]{
                "Shirt", "Pants", "Hoodie", "Jacket",
                "Shoes", "Hat", "Other"
            }
        );

        JTextField sizeField = new JTextField();
        JTextField colorField = new JTextField();

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));

        form.add(new JLabel("Item name:"));
        form.add(nameField);
        form.add(new JLabel("Brand:"));
        form.add(brandField);
        form.add(new JLabel("Category:"));
        form.add(categoryBox);
        form.add(new JLabel("Quantity:"));
        form.add(quantityField);
        form.add(new JLabel("Cost per item ($):"));
        form.add(costField);
        form.add(new JLabel("Size:"));
        form.add(sizeField);
        form.add(new JLabel("Color:"));
        form.add(colorField);

        int result = JOptionPane.showConfirmDialog(
            this,
            form,
            "Add Inventory Item",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String name = nameField.getText().trim();
            String brand = brandField.getText().trim();
            String category = (String) categoryBox.getSelectedItem();
            String size = sizeField.getText().trim();
            String color = colorField.getText().trim();

            if (name.isEmpty() || brand.isEmpty()) {
                throw new IllegalArgumentException(
                    "Item name and brand are required."
                );
            }

            int quantity = Integer.parseInt(
                quantityField.getText().trim()
            );

            double cost = Double.parseDouble(
                costField.getText().trim()
            );

            if (quantity <= 0) {
                throw new IllegalArgumentException(
                    "Quantity must be greater than zero."
                );
            }

            if (!Double.isFinite(cost) || cost < 0) {
                throw new IllegalArgumentException(
                    "Cost must be a valid non-negative number."
                );
            }

            int id = account.getNextId();
            LocalDate dateListed = LocalDate.now();

            Good good;

            if (!"Other".equals(category)) {
                good = new Clothing(
                    id, name, brand, quantity, cost, dateListed,
                    size, color, category
                );
            } else {
                good = new Good(
                    id, name, brand, quantity, cost, dateListed
                );
            }

            account.addGood(good);
            refreshTable();

            JOptionPane.showMessageDialog(
                this,
                "Item added successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Quantity must be a whole number and cost must be numeric.",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE
            );
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void editSelectedItem() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Please select an item to edit.",
                "No Item Selected",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int itemId = (int) tableModel.getValueAt(selectedRow, 0);

        Good selectedGood = null;

        for (Good good : account.getActiveGoods()) {
            if (good.getId() == itemId) {
                selectedGood = good;
                break;
            }
        }

        if (selectedGood == null) {
            JOptionPane.showMessageDialog(
                this,
                "Could not find the selected item.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        JTextField nameField = new JTextField(selectedGood.getName());
        JTextField brandField = new JTextField(selectedGood.getBrand());
        JTextField quantityField =
            new JTextField(String.valueOf(selectedGood.getQuantity()));
        JTextField costField =
            new JTextField(String.valueOf(selectedGood.getCost()));

        JTextField sizeField = new JTextField();
        JTextField colorField = new JTextField();

        String[] categories = {
            "Shirt", "Pants", "Hoodie", "Jacket",
            "Shoes", "Hat", "Other"
        };

        JComboBox<String> categoryBox = new JComboBox<>(categories);

        if (selectedGood instanceof Clothing clothing) {
            sizeField.setText(clothing.getSize());
            colorField.setText(clothing.getColor());
            categoryBox.setSelectedItem(clothing.getCategory());
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));

        form.add(new JLabel("Name:"));
        form.add(nameField);

        form.add(new JLabel("Brand:"));
        form.add(brandField);

        form.add(new JLabel("Category:"));
        form.add(categoryBox);

        form.add(new JLabel("Size:"));
        form.add(sizeField);

        form.add(new JLabel("Color:"));
        form.add(colorField);

        form.add(new JLabel("Quantity:"));
        form.add(quantityField);

        form.add(new JLabel("Cost per item:"));
        form.add(costField);

        int result = JOptionPane.showConfirmDialog(
            this,
            form,
            "Edit Inventory Item",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String name = nameField.getText().trim();
            String brand = brandField.getText().trim();
            String category = (String) categoryBox.getSelectedItem();
            String size = sizeField.getText().trim();
            String color = colorField.getText().trim();

            int quantity = Integer.parseInt(quantityField.getText().trim());
            double cost = Double.parseDouble(costField.getText().trim());

            if (name.isEmpty() || brand.isEmpty()) {
                throw new IllegalArgumentException(
                    "Name and brand cannot be empty."
                );
            }

            if (quantity < 0) {
                throw new IllegalArgumentException(
                    "Quantity cannot be negative."
                );
            }

            if (!Double.isFinite(cost) || cost < 0) {
                throw new IllegalArgumentException(
                    "Cost must be a valid, non-negative number."
                );
            }

            selectedGood.setName(name);
            selectedGood.setBrand(brand);
            selectedGood.setQuantity(quantity);
            selectedGood.setCost(cost);

            if (selectedGood instanceof Clothing clothing) {
                clothing.setSize(size);
                clothing.setColor(color);
                clothing.setCategory(category);
            }

            // We'll add the persistent save call here in the next step.
            refreshTable();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Quantity must be a whole number, and cost must be numeric.",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE
            );
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void deleteSelectedItem() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Please select an item to delete.",
                "No Item Selected",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int itemId = (int) tableModel.getValueAt(modelRow, 0);
        String itemName = String.valueOf(
            tableModel.getValueAt(modelRow, 1)
        );

        int confirmation = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to permanently delete "
                + itemName + "?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            account.deleteGood(itemId);
            refreshTable();

            JOptionPane.showMessageDialog(
                this,
                itemName + " was deleted successfully.",
                "Item Deleted",
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Cannot Delete Item",
                JOptionPane.ERROR_MESSAGE
            );
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Save Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);

        for (Good good : account.getActiveGoods()) {
            String category = "";
            String size = "";
            String color = "";

            if (good instanceof Clothing clothing) {
                category = clothing.getCategory();
                size = clothing.getSize();
                color = clothing.getColor();
            }

            tableModel.addRow(new Object[]{
                good.getId(),
                good.getName(),
                good.getBrand(),
                category,
                size,
                color,
                good.getQuantity(),
                String.format("$%.2f", good.getCost()),
                good.getDateListed()
            });
        }
    }
}
