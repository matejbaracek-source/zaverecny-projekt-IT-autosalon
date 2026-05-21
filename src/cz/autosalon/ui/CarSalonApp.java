package cz.autosalon.ui;

import cz.autosalon.logic.Inventory;
import cz.autosalon.model.Vehicle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Hlavní třída aplikace s grafickým rozhraním.
 * (Main application class with GUI.)
 */
public class CarSalonApp extends JFrame {
    private Inventory inventory;
    private JTable customerTable;
    private JTable managerTable;
    private DefaultTableModel customerModel;
    private DefaultTableModel managerModel;
    
    private double virtualBudget = 1000000.0; // Výchozí rozpočet zákazníka

    public CarSalonApp() {
        inventory = new Inventory();
        
        setTitle("Virtuální Autosalon");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // 1. Zákaznická část (Customer Tab)
        tabbedPane.addTab("Zákazník", createCustomerPanel());

        // 2. Administrace (Manager Tab)
        tabbedPane.addTab("Manažer", createManagerPanel());

        add(tabbedPane);
    }

    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Horní panel pro filtry (Filter panel)
        JPanel filterPanel = new JPanel();
        filterPanel.add(new JLabel("Značka:"));
        JTextField brandFilter = new JTextField(10);
        filterPanel.add(brandFilter);
        
        filterPanel.add(new JLabel("Max Cena:"));
        JTextField priceFilter = new JTextField(10);
        filterPanel.add(priceFilter);

        filterPanel.add(new JLabel("Rok:"));
        JTextField yearFilter = new JTextField(5);
        filterPanel.add(yearFilter);

        JButton btnFilter = new JButton("Filtrovat");
        filterPanel.add(btnFilter);

        panel.add(filterPanel, BorderLayout.NORTH);

        // Tabulka (Table)
        String[] columns = {"ID", "Značka", "Model", "Rok", "Cena"};
        customerModel = new DefaultTableModel(columns, 0);
        customerTable = new JTable(customerModel);
        refreshCustomerTable(inventory.getAllVehicles());
        panel.add(new JScrollPane(customerTable), BorderLayout.CENTER);

        // Tlačítko pro nákup (Buy button)
        JButton btnBuy = new JButton("Koupit vybrané auto");
        btnBuy.addActionListener(e -> simulatePurchase());
        panel.add(btnBuy, BorderLayout.SOUTH);

        btnFilter.addActionListener(e -> {
            String brand = brandFilter.getText();
            Double price = null;
            Integer year = null;
            try {
                if (!priceFilter.getText().isEmpty()) price = Double.parseDouble(priceFilter.getText());
                if (!yearFilter.getText().isEmpty()) year = Integer.parseInt(yearFilter.getText());
                refreshCustomerTable(inventory.filterVehicles(brand, price, year));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Neplatná cena nebo rok!");
            }
        });

        return panel;
    }

    private JPanel createManagerPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Tabulka pro manažera
        String[] columns = {"ID", "Značka", "Model", "Rok", "Cena"};
        managerModel = new DefaultTableModel(columns, 0);
        managerTable = new JTable(managerModel);
        refreshManagerTable(inventory.getAllVehicles());
        panel.add(new JScrollPane(managerTable), BorderLayout.CENTER);

        // Ovládací tlačítka (Control buttons)
        JPanel controlPanel = new JPanel();
        JButton btnAdd = new JButton("Přidat auto");
        JButton btnDelete = new JButton("Smazat vybrané");
        JButton btnStats = new JButton("Zobrazit statistiky");

        controlPanel.add(btnAdd);
        controlPanel.add(btnDelete);
        controlPanel.add(btnStats);
        panel.add(controlPanel, BorderLayout.SOUTH);

        // Logika tlačítek
        btnAdd.addActionListener(e -> showAddDialog());
        btnDelete.addActionListener(e -> {
            int row = managerTable.getSelectedRow();
            if (row != -1) {
                String id = (String) managerModel.getValueAt(row, 0);
                inventory.removeVehicle(id);
                refreshAllTables();
            }
        });
        btnStats.addActionListener(e -> {
            String stats = "Celkový počet aut: " + inventory.getTotalCount() +
                           "\nCelková hodnota: " + inventory.getTotalValue() + " Kč";
            JOptionPane.showMessageDialog(this, stats, "Statistiky", JOptionPane.INFORMATION_MESSAGE);
        });

        return panel;
    }

    private void refreshCustomerTable(List<Vehicle> list) {
        customerModel.setRowCount(0);
        for (Vehicle v : list) {
            customerModel.addRow(new Object[]{v.getId(), v.getBrand(), v.getModel(), v.getYear(), v.getPrice()});
        }
    }

    private void refreshManagerTable(List<Vehicle> list) {
        managerModel.setRowCount(0);
        for (Vehicle v : list) {
            managerModel.addRow(new Object[]{v.getId(), v.getBrand(), v.getModel(), v.getYear(), v.getPrice()});
        }
    }

    private void refreshAllTables() {
        List<Vehicle> all = inventory.getAllVehicles();
        refreshCustomerTable(all);
        refreshManagerTable(all);
    }

    private void simulatePurchase() {
        int row = customerTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vyberte auto z tabulky!");
            return;
        }

        double price = (double) customerModel.getValueAt(row, 4);
        String id = (String) customerModel.getValueAt(row, 0);

        if (virtualBudget >= price) {
            virtualBudget -= price;
            inventory.removeVehicle(id);
            refreshAllTables();
            JOptionPane.showMessageDialog(this, "Nákup úspěšný! Zůstatek: " + virtualBudget + " Kč");
        } else {
            JOptionPane.showMessageDialog(this, "Nedostatek peněz! Chybí vám: " + (price - virtualBudget) + " Kč");
        }
    }

    private void showAddDialog() {
        // Jednoduchý dialog pro přidání auta
        JTextField brandField = new JTextField();
        JTextField modelField = new JTextField();
        JTextField yearField = new JTextField();
        JTextField priceField = new JTextField();

        Object[] message = {
            "Značka:", brandField,
            "Model:", modelField,
            "Rok:", yearField,
            "Cena:", priceField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Přidat nové auto", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String id = String.valueOf(System.currentTimeMillis());
                Vehicle v = new Vehicle(id, brandField.getText(), modelField.getText(),
                                        Integer.parseInt(yearField.getText()),
                                        Double.parseDouble(priceField.getText()), "");
                inventory.addVehicle(v);
                refreshAllTables();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Chyba při zadávání dat!");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CarSalonApp().setVisible(true);
        });
    }
}
