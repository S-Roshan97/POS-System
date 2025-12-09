import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class POSSystemGUI extends JFrame {
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel totalLabel;
    private double total = 0;

    public POSSystemGUI() {
        setTitle("Supermarket POS System");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Left: Product Catalog
        JPanel productPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        JScrollPane productScroll = new JScrollPane(productPanel);
        add(productScroll, BorderLayout.CENTER);

        // Sample products with images
        Object[][] products = {
            {"Britannia Cake", 28, "images/britannia.png"},
            {"Kellogg's Corn Flakes", 388, "images/kelloggs.png"},
            {"Betty Crocker Fudge", 758, "images/fudge.png"},
            {"Vedaka Almonds", 890, "images/almonds.png"},
            {"Disano Pasta", 99, "images/pasta.png"}
        };

        for (Object[] product : products) {
            String name = (String) product[0];
            double price = (double) product[1];
            String imagePath = (String) product[2];

            // Load image
            ImageIcon icon = new ImageIcon(imagePath);
            Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            icon = new ImageIcon(img);

            // Create button with image + text
            JButton btn = new JButton("<html><center>" + name + "<br>₹" + price + "</center></html>", icon);
            btn.setHorizontalTextPosition(SwingConstants.CENTER);
            btn.setVerticalTextPosition(SwingConstants.BOTTOM);
            btn.addActionListener(e -> addToCart(name, price));

            productPanel.add(btn);
        }

        // Right: Cart
        String[] columns = {"Item", "Qty", "Price"};
        cartModel = new DefaultTableModel(columns, 0);
        cartTable = new JTable(cartModel);

        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        totalLabel = new JLabel("Total: ₹0.00", JLabel.RIGHT);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        cartPanel.add(totalLabel, BorderLayout.SOUTH);

        cartPanel.setPreferredSize(new Dimension(400, getHeight()));
        add(cartPanel, BorderLayout.EAST);

        // Bottom: Payment Buttons
        JPanel paymentPanel = new JPanel();
        JButton cashBtn = new JButton("Cash");
        JButton cardBtn = new JButton("Credit Card");
        JButton upiBtn = new JButton("UPI");
        JButton splitBtn = new JButton("Split Payment");

        paymentPanel.add(cashBtn);
        paymentPanel.add(cardBtn);
        paymentPanel.add(upiBtn);
        paymentPanel.add(splitBtn);
        add(paymentPanel, BorderLayout.SOUTH);
    }

    private void addToCart(String item, double price) {
        cartModel.addRow(new Object[]{item, 1, price});
        total += price;
        totalLabel.setText("Total: ₹" + total);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new POSSystemGUI().setVisible(true));
    }
}
