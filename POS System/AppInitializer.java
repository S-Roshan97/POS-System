public class AppInitializer {
    public static void main(String[] args) {
        // Start the POS System GUI
        javax.swing.SwingUtilities.invokeLater(() -> {
            new POSSystemGUI().setVisible(true);
        });
    }
}
