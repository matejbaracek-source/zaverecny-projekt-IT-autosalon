import cz.autosalon.ui.CarSalonApp;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Spuštění aplikace v Event Dispatch Thread (standard pro Swing)
        SwingUtilities.invokeLater(() -> {
            new CarSalonApp().setVisible(true);
        });
    }
}