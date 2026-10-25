
package gui;

import account.EBayAccount;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {
    private final EBayAccount account;

    private final JLabel totalProfitLabel;
    private final JLabel totalSalesLabel;

    public MainFrame() {
        account = new EBayAccount();

        setTitle("eBay Inventory Manager");
        setSize(1250, 800);
        setMinimumSize(new Dimension(950, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(0, 15));

        // Summary statistics at the top.
        JPanel summaryPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        summaryPanel.setBorder(new EmptyBorder(10, 10, 0, 10));

        JPanel profitCard = createSummaryCard("Total Profit");
        JPanel salesCard = createSummaryCard("Total Sales");

        totalProfitLabel = (JLabel) profitCard.getClientProperty("valueLabel");
        totalSalesLabel = (JLabel) salesCard.getClientProperty("valueLabel");

        summaryPanel.add(profitCard);
        summaryPanel.add(salesCard);

        add(summaryPanel, BorderLayout.NORTH);

        // Inventory and sales tabs.
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Inventory", new InventoryPanel(account));
        tabs.addTab("Sales", new SalesPanel(account, this::refreshSummary));

        // Add space between the tabs and the window edges.
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 10, 10, 10));
        contentPanel.add(tabs, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        refreshSummary();
    }

    private JPanel createSummaryCard(String title) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                new Color(210, 210, 210)
            ),
            new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        JLabel valueLabel = new JLabel();
        valueLabel.setFont(new Font("Arial", Font.BOLD, 26));

        card.add(titleLabel);
        card.add(valueLabel);

        card.putClientProperty("valueLabel", valueLabel);

        return card;
    }

    public void refreshSummary() {
        totalProfitLabel.setText(
            String.format("$%.2f", account.getTotalProfit())
        );

        totalSalesLabel.setText(
            String.valueOf(account.getTotalSales())
        );
    }
}
