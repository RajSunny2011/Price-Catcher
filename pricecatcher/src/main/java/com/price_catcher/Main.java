package com.price_catcher;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class Main {
    private static List<CustomItem> itemsList = new ArrayList<>();
    private static final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Site", "Item URL", "Price", "Threshold"}, 0);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Item Price Tracker");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout(10, 10));

            // Panel for input
            JPanel inputPanel = new JPanel(new FlowLayout());
            JTextField urlField = new JTextField(25);
            JButton addButton = new JButton("Add Item");
            JTextField recheckIntervalField = new JTextField(5);
            JButton recheckButton = new JButton("Recheck Prices");
            JLabel statusLabel = new JLabel("Recheck interval (minutes):");

            inputPanel.add(new JLabel("Item URL:"));
            inputPanel.add(urlField);
            inputPanel.add(addButton);
            inputPanel.add(statusLabel);
            inputPanel.add(recheckIntervalField);
            inputPanel.add(recheckButton);

            // Table to display items
            JTable itemTable = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(itemTable);
            // Set the width for each column
            TableColumn column1 = itemTable.getColumnModel().getColumn(0);
            column1.setPreferredWidth(150);
            TableColumn column2 = itemTable.getColumnModel().getColumn(1);
            column2.setPreferredWidth(300);
            TableColumn column3 = itemTable.getColumnModel().getColumn(2);
            column3.setPreferredWidth(100);
            itemTable.setFillsViewportHeight(true);
            itemTable.setFillsViewportHeight(true);

            // Status indicator for loading
            JLabel loadingLabel = new JLabel("Rechecking prices...");
            loadingLabel.setVisible(false);  // Initially hidden
            inputPanel.add(loadingLabel);

            frame.add(inputPanel, BorderLayout.NORTH);
            frame.add(scrollPane, BorderLayout.CENTER);

            try(ObjectInputStream in = new ObjectInputStream(new FileInputStream("items.dat"))) {
                itemsList = (List<CustomItem>) in.readObject();
                for (CustomItem item : itemsList) {
                    tableModel.addRow(new Object[]{item.website, item.url, item.fetchPrice(), item.getThresholdPrice()});
                }
            } catch (Exception e) {
                System.out.println("Error loading items: " + e.getMessage());
            }

            // Action Listener for adding an item
            addButton.addActionListener((ActionEvent e) -> {
                String url = urlField.getText();
                if (url.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid item URL.");
                } else {
                    try {
                        CustomItem item = new AmazonItem(url);
                        itemsList.add(item);
                        // Show dialog for threshold price after adding the item
                        String thresholdPriceString = JOptionPane.showInputDialog(frame, "Enter threshold price for " + item.website, "Set Threshold Price", JOptionPane.PLAIN_MESSAGE);
                        if (thresholdPriceString != null && !thresholdPriceString.isEmpty()) {
                            try {
                                double thresholdPrice = Double.parseDouble(thresholdPriceString);
                                item.setThresholdPrice(thresholdPrice);
                                JOptionPane.showMessageDialog(frame, "Threshold price set to " + thresholdPrice);
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(frame, "Invalid threshold price. Please enter a valid number.");
                            }
                        }
                        tableModel.addRow(new Object[]{item.website, item.url, item.fetchPrice(), item.getThresholdPrice()});
                        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("items.dat"))) {
                            out.writeObject(itemsList);
                        } catch (Exception ex) {
                            System.out.println("Error saving items: " + ex.getMessage());
                        }
                        JOptionPane.showMessageDialog(frame, "Item added successfully!");
                        urlField.setText(""); // clear input field
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Failed to retrieve item data. Please check the URL.");
                    }
                }
            });

            recheckButton.addActionListener((ActionEvent e) -> {
                int intervalMinutes;
                try {
                    intervalMinutes = Integer.parseInt(recheckIntervalField.getText());
                } catch (NumberFormatException ex) {
                    intervalMinutes = 0;
                }    
                if (intervalMinutes < 0) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid recheck interval.");
                    return;
                }
                new RecheckTask(loadingLabel, intervalMinutes, itemsList).execute();
            });

            itemTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    int row = itemTable.rowAtPoint(e.getPoint());
                    int col = itemTable.columnAtPoint(e.getPoint());

                    if (row >= 0 && col >= 0) {
                        String itemURL = tableModel.getValueAt(row, 1).toString();
                        CustomItem selectedItem = getItemByURL(itemURL);

                        if (selectedItem != null) {
                            openItemDetailsWindow(selectedItem);
                        }
                    }
                }
            });

            frame.setSize(900, 500);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static void openItemDetailsWindow(CustomItem item) {
        JFrame detailsFrame = new JFrame("Item Details - " + item.website);
        detailsFrame.setLayout(new BorderLayout(10, 10));
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new GridLayout(0, 2, 1, 0));
        List<Double> priceHistory = new ArrayList<>();
        for (double price : item.getPriceHistory()) {
            priceHistory.add(price);
        }

        // Add components to the panel
        detailsPanel.add(new JLabel("Item URL:"));
        detailsPanel.add(new JLabel(item.url.toString()));
        detailsPanel.add(new JLabel("Price:"));
        detailsPanel.add(new JLabel(String.valueOf(item.getPrice())));
        detailsPanel.add(new JLabel("Threshold Price:"));
        detailsPanel.add(new JLabel(String.valueOf(item.getThresholdPrice())));
        detailsPanel.add(new JLabel("Price History:"));
        detailsPanel.add(new JLabel(priceHistory.stream()
                                    .filter(price -> price > 0)
                                    .map(String::valueOf)
                                    .collect(Collectors.joining(", "))));
        
        detailsFrame.add(detailsPanel, BorderLayout.CENTER);
        detailsFrame.setSize(525, 300);
        detailsFrame.setLocationRelativeTo(null);
        detailsFrame.setVisible(true);
    }
    
    private static CustomItem getItemByURL(String url) {
        for (CustomItem item : itemsList) {
            if (item.getURL().equals(url)) {
                return item;
            }
        }
        return null;  // Return null if no matching item is found
    }

    // SwingWorker to periodically recheck prices in the background
    static class RecheckTask extends SwingWorker<Void, Void> {
        private final JLabel loadingLabel;
        private final int intervalMinutes;
        private final List<CustomItem> itemsList;
    
        public RecheckTask(JLabel loadingLabel, int intervalMinutes, List<CustomItem> itemsList) {
            this.loadingLabel = loadingLabel;
            this.intervalMinutes = intervalMinutes;
            this.itemsList = itemsList;
        }
    
        @Override
        protected Void doInBackground() throws Exception {
            // If the interval is 0, meaning we only run the task once
            if (intervalMinutes == 0) {
                recheckPrices();
            } else {
                while (true) {
                    recheckPrices();
                    Thread.sleep(intervalMinutes * 60000);
                }
            }
            return null;
        }
    
        // Method to recheck the prices for all items
        private void recheckPrices() {
            for (CustomItem item : itemsList) {
                SwingUtilities.invokeLater(() -> loadingLabel.setVisible(true));
                try {
                    double newPrice = item.fetchPrice();
                    SwingUtilities.invokeLater(() -> {
                        for (int i = 0; i < tableModel.getRowCount(); i++) {
                            if (tableModel.getValueAt(i, 1).equals(item.url)) {
                                tableModel.setValueAt(newPrice, i, 2);  // Update price column
                            }
                        }
                    });
                    // If the price is below the threshold, send a notification
                    if (newPrice < item.getThresholdPrice()) {
                        showDesktopNotification(item, newPrice);
                    }
                } catch (Exception ex) {
                    System.out.println("Error fetching price for " + item.url);
                }
            }
            SwingUtilities.invokeLater(() -> loadingLabel.setVisible(false));
        }
    
        // Method to display a desktop notification
        private void showDesktopNotification(CustomItem item, double newPrice) {
            if (SystemTray.isSupported()) {
                SystemTray tray = SystemTray.getSystemTray();
                TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage("icon.png"), "Price Tracker");
                try {
                    tray.add(trayIcon);
                    trayIcon.displayMessage("Price Alert", 
                        "Price for " + item.url.toString() + " has fallen below the threshold! New Price: " + newPrice, 
                        TrayIcon.MessageType.INFO);
                } catch (AWTException e) {
                    System.out.println(e.getMessage());
                }
            } else {
                // If system tray is not supported, show a dialog instead
                JOptionPane.showMessageDialog(null, 
                    "Price for " + item.url.toString() + " has fallen below the threshold!\nNew Price: " + newPrice, 
                    "Price Alert", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
