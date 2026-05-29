package cz.autosalon.ui;

import cz.autosalon.logic.Inventory;
import cz.autosalon.model.Vehicle;

import cz.autosalon.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.text.DecimalFormat;
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
    private JButton btnBudget; // Tlačítko pro zobrazení a přidání zůstatku
    
    private List<User> users;
    private boolean isManagerLoggedIn = false;
    private JPanel managerPanelContainer; // Hlavní panel pro manažerskou záložku

    public CarSalonApp() {
        inventory = new Inventory();
        
        // Inicializace uživatelů
        users = new ArrayList<>();
        users.add(new User("manager", "admin123", "MANAGER"));

        
        // Small UI improvements: use system look & feel and set default fonts
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 12));
            UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 12));
            UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 12));
        } catch (Exception ignored) {
        }

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
        brandFilter.setToolTipText("Filtr podle značky");
        
        filterPanel.add(new JLabel("Max Cena:"));
        JTextField priceFilter = new JTextField(10);
        filterPanel.add(priceFilter);
        priceFilter.setToolTipText("Maximální cena (např. 250000)");

        filterPanel.add(new JLabel("Rok:"));
        JTextField yearFilter = new JTextField(5);
        filterPanel.add(yearFilter);
        yearFilter.setToolTipText("Rok výroby (např. 2018)");

        JButton btnFilter = new JButton("Filtrovat");
        filterPanel.add(btnFilter);
        btnFilter.setToolTipText("Aplikovat filtry");

        // Mezera a zobrazení finančního zůstatku (nyní jako tlačítko)
        filterPanel.add(Box.createHorizontalStrut(30)); // Mezera
        btnBudget = new JButton("Rozpočet: " + virtualBudget + " Kč");
        btnBudget.setFont(new Font("Arial", Font.BOLD, 12));
        btnBudget.setForeground(new Color(0, 128, 0)); // Zelená barva pro peníze
        btnBudget.setToolTipText("Klikněte pro vložení peněz do rozpočtu");
        btnBudget.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Zadejte částku k vložení (Kč):", "Vložit peníze", JOptionPane.QUESTION_MESSAGE);
            if (input != null && !input.trim().isEmpty()) {
                try {
                    double deposit = Double.parseDouble(input.trim());
                    if (deposit > 0) {
                        virtualBudget += deposit;
                        btnBudget.setText("Rozpočet: " + virtualBudget + " Kč");
                        JOptionPane.showMessageDialog(this, "Úspěšně vloženo " + deposit + " Kč.", "Úspěch", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Částka musí být větší než nula!", "Chyba", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Neplatná částka! Zadejte prosím číslo.", "Chyba", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        filterPanel.add(btnBudget);

        panel.add(filterPanel, BorderLayout.NORTH);

        // Tabulka (Table)
        String[] columns = {"ID", "Značka", "Model", "Rok", "Cena"};
        customerModel = new DefaultTableModel(columns, 0);
        customerTable = new JTable(customerModel);
        customerTable.setRowHeight(24);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        customerTable.getColumnModel().getColumn(4).setCellRenderer(createCurrencyRenderer());
        refreshTable(customerModel, inventory.getAllVehicles());
        panel.add(new JScrollPane(customerTable), BorderLayout.CENTER);

        // Tlačítko pro nákup (Buy button)
        JButton btnBuy = new JButton("Koupit vybrané auto");
        btnBuy.addActionListener(e -> simulatePurchase());
        btnBuy.setToolTipText("Koupit vybrané auto z tabulky");
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnBuy);
        panel.add(south, BorderLayout.SOUTH);

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
        usernameField.setToolTipText("Jméno manažera");

        loginPanel.add(new JLabel("Heslo:"));
        JPasswordField passwordField = new JPasswordField(10);
        loginPanel.add(passwordField);
        passwordField.setToolTipText("Heslo manažera");

        JButton btnLogin = new JButton("Přihlásit");
        loginPanel.add(btnLogin);
        btnLogin.setToolTipText("Přihlásit se jako manažer");

        // 2. Skutečné manažerské rozhraní
        JPanel actualManagerPanel = new JPanel(new BorderLayout());

        // Tabulka pro manažera
        String[] columns = {"ID", "Značka", "Model", "Rok", "Cena"};
        managerModel = new DefaultTableModel(columns, 0);
        managerTable = new JTable(managerModel);
        managerTable.setRowHeight(24);
        managerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        managerTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        managerTable.getColumnModel().getColumn(4).setCellRenderer(createCurrencyRenderer());
        refreshTable(managerModel, inventory.getAllVehicles());
        actualManagerPanel.add(new JScrollPane(managerTable), BorderLayout.CENTER);

        // Ovládací tlačítka
        JPanel controlPanel = new JPanel();
        JButton btnAdd = new JButton("Přidat auto");
        JButton btnDelete = new JButton("Smazat vybrané");
        JButton btnStats = new JButton("Zobrazit statistiky");
        JButton btnLogout = new JButton("Odhlásit se");

        btnAdd.setToolTipText("Přidat nové auto");
        btnDelete.setToolTipText("Smazat vybraný záznam");
        btnStats.setToolTipText("Zobrazit statistiky skladu");
        btnLogout.setToolTipText("Odhlásit se z účtu");

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

    private DefaultTableCellRenderer createCurrencyRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            private final DecimalFormat df = new DecimalFormat("#,##0.00");
            @Override
            protected void setValue(Object value) {
                if (value instanceof Number) {
                    setText(df.format(((Number) value).doubleValue()) + " Kč");
                } else {
                    super.setValue(value);
                }
            }
        };
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        return renderer;
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
            btnBudget.setText("Rozpočet: " + virtualBudget + " Kč"); // Aktualizace popisku
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
}
