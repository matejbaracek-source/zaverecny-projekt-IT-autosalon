package cz.autosalon.ui;

import cz.autosalon.logic.Inventory;
import cz.autosalon.model.Vehicle;

import cz.autosalon.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
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
    private JLabel lblBudget; // Popisek pro zobrazení zůstatku
    
    private List<User> users;
    private boolean isManagerLoggedIn = false;
    private JPanel managerPanelContainer; // Hlavní panel pro manažerskou záložku

    public CarSalonApp() {
        inventory = new Inventory();
        
        // Inicializace uživatelů
        users = new ArrayList<>();
        users.add(new User("manager", "admin123", "MANAGER"));

        
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

        // Mezera a zobrazení finančního zůstatku
        filterPanel.add(Box.createHorizontalStrut(30)); // Mezera
        lblBudget = new JLabel("Rozpočet: " + virtualBudget + " Kč");
        lblBudget.setFont(new Font("Arial", Font.BOLD, 12));
        lblBudget.setForeground(new Color(0, 128, 0)); // Zelená barva pro peníze
        filterPanel.add(lblBudget);

        panel.add(filterPanel, BorderLayout.NORTH);

        // Tabulka (Table)
        String[] columns = {"ID", "Značka", "Model", "Rok", "Cena"};
        customerModel = new DefaultTableModel(columns, 0);
        customerTable = new JTable(customerModel);
        refreshTable(customerModel, inventory.getAllVehicles());
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
                refreshTable(customerModel, inventory.filterVehicles(brand, price, year));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Neplatná cena nebo rok!");
            }
        });

        return panel;
    }

    private JPanel createManagerPanel() {
        managerPanelContainer = new JPanel(new BorderLayout());

        // 1. Přihlašovací obrazovka (jednoduchý vzhled)
        JPanel loginPanel = new JPanel(); // Výchozí je FlowLayout
        
        loginPanel.add(new JLabel("Jméno:"));
        JTextField usernameField = new JTextField(10);
        loginPanel.add(usernameField);

        loginPanel.add(new JLabel("Heslo:"));
        JPasswordField passwordField = new JPasswordField(10);
        loginPanel.add(passwordField);

        JButton btnLogin = new JButton("Přihlásit");
        loginPanel.add(btnLogin);

        // 2. Skutečné manažerské rozhraní
        JPanel actualManagerPanel = new JPanel(new BorderLayout());

        // Tabulka pro manažera
        String[] columns = {"ID", "Značka", "Model", "Rok", "Cena"};
        managerModel = new DefaultTableModel(columns, 0);
        managerTable = new JTable(managerModel);
        refreshTable(managerModel, inventory.getAllVehicles());
        actualManagerPanel.add(new JScrollPane(managerTable), BorderLayout.CENTER);

        // Ovládací tlačítka
        JPanel controlPanel = new JPanel();
        JButton btnAdd = new JButton("Přidat auto");
        JButton btnDelete = new JButton("Smazat vybrané");
        JButton btnStats = new JButton("Zobrazit statistiky");
        JButton btnLogout = new JButton("Odhlásit se");

        controlPanel.add(btnAdd);
        controlPanel.add(btnDelete);
        controlPanel.add(btnStats);
        controlPanel.add(btnLogout);
        actualManagerPanel.add(controlPanel, BorderLayout.SOUTH);

        // Na začátku přidáme do kontejneru jen přihlašovací panel
        managerPanelContainer.add(loginPanel, BorderLayout.CENTER);

        // Logika přihlášení
        btnLogin.addActionListener(e -> {
            String name = usernameField.getText();
            String pwd = new String(passwordField.getPassword());
            
            boolean success = false;
            // Prohledáme uživatele
            for (User u : users) {
                if (u.getName().equals(name) && u.getPassword().equals(pwd) && u.getRole().equals("MANAGER")) {
                    success = true;
                    break;
                }
            }

            if (success) {
                isManagerLoggedIn = true;
                // Odebereme přihlášení a přidáme skutečný panel
                managerPanelContainer.remove(loginPanel);
                managerPanelContainer.add(actualManagerPanel, BorderLayout.CENTER);
                managerPanelContainer.revalidate(); // Překreslení
                managerPanelContainer.repaint();
                
                // Vymazání políček
                usernameField.setText("");
                passwordField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Neplatné přihlašovací údaje!");
            }
        });

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
        
        btnLogout.addActionListener(e -> {
            isManagerLoggedIn = false;
            // Odebereme manažerský panel a vrátíme přihlášení
            managerPanelContainer.remove(actualManagerPanel);
            managerPanelContainer.add(loginPanel, BorderLayout.CENTER);
            managerPanelContainer.revalidate();
            managerPanelContainer.repaint();
        });

        return managerPanelContainer;
    }

    private void refreshTable(DefaultTableModel model, List<Vehicle> list) {
        model.setRowCount(0);
        for (Vehicle v : list) {
            model.addRow(new Object[]{v.getId(), v.getBrand(), v.getModel(), v.getYear(), v.getPrice()});
        }
    }

    private void refreshAllTables() {
        List<Vehicle> all = inventory.getAllVehicles();
        refreshTable(customerModel, all);
        refreshTable(managerModel, all);
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
            lblBudget.setText("Rozpočet: " + virtualBudget + " Kč"); // Aktualizace popisku
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
