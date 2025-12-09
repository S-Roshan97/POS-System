package pos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class POSSystemGUI extends JFrame {

    private final SimpleStore store;

    // Table/model
    private JTable invoiceTable;
    private DefaultTableModel tableModel;

    // Footer labels
    private JLabel subtotalLabel;
    private JLabel discountLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;

    // Payment
    private JComboBox<String> paymentCombo;

    // Current invoice
    private Invoice currentInvoice;

    // Discount state
    private String discountType = "AMOUNT";
    private double discountValue = 0.0;

    // LKR currency formatter
    private final NumberFormat LKR = NumberFormat.getCurrencyInstance(new Locale("en", "LK"));

    // Live inventory dialog
    private JDialog inventoryDialog;

    // Animated background panel
    private AnimatedBackgroundPanel backgroundPanel;

    public POSSystemGUI(SimpleStore store) {
        this.store = store;
        setupGlobalLookAndFeel();
        buildUI();
    }

    private void setupGlobalLookAndFeel() {
        UIManager.put("OptionPane.messageFont", new FontUIResource("Segoe UI", Font.PLAIN, 16));
        UIManager.put("OptionPane.buttonFont", new FontUIResource("Segoe UI", Font.BOLD, 14));
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));
    }

    private void buildUI() {
        setTitle("SuperMart POS System");
        setSize(1400, 800);
        setMinimumSize(new Dimension(1200, 700));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (inventoryDialog != null && inventoryDialog.isDisplayable()) {
                    inventoryDialog.dispose();
                }
                System.exit(0);
            }
        });

        // ========= ANIMATED BACKGROUND PANEL =========
        backgroundPanel = new AnimatedBackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout());
        setContentPane(backgroundPanel);

        // ========= HEADER PANEL =========
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("SuperMart POS System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel, BorderLayout.WEST);

        JLabel timeLabel = new JLabel("Today, " + new SimpleDateFormat("MMM dd").format(new java.util.Date()));
        timeLabel.setForeground(Color.LIGHT_GRAY);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        header.add(timeLabel, BorderLayout.EAST);

        backgroundPanel.add(header, BorderLayout.NORTH);

        // ========= BUTTON PANEL (3x5 on TOP) =========
        JPanel top = new JPanel(new GridLayout(3, 5, 12, 10));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Color palette
        Color cNew = new Color(0, 122, 204);
        Color cAdd = new Color(46, 125, 50);
        Color cEdit = new Color(255, 143, 0);
        Color cDelete = new Color(213, 0, 0);
        Color cAction = new Color(156, 39, 176);
        Color cView = new Color(98, 0, 238);
        Color cConfirm = new Color(0, 150, 136);
        Color cExport = new Color(121, 85, 72);
        Color cMisc = new Color(158, 158, 158);

        JButton btnNewInvoice = styledBtn("New Invoice", cNew);
        JButton btnAddItem = styledBtn("Add Item", cAdd);
        JButton btnChangeQty = styledBtn("Change Qty", cEdit);
        JButton btnDeleteRow = styledBtn("Delete Row", cDelete);
        JButton btnClearInvoice = styledBtn("Clear Invoice", cAction);

        JButton btnAddNewItem = styledBtn("Add New Item", cAdd);
        JButton btnDeleteItem = styledBtn("Delete Item", cDelete);
        JButton btnDiscount = styledBtn("Discount", cEdit);
        JButton btnViewItems = styledBtn("View Items", cView);
        JButton btnViewSales = styledBtn("View Sales", cView);

        JButton btnConfirm = styledBtn("Confirm Invoice", cConfirm);
        JButton btnExport = styledBtn("Export PDF", cExport);
        JButton btnCustomers = styledBtn("Customers", cView);
        JButton btnRefresh = styledBtn("Refresh", cMisc);
        JButton btnExit = styledBtn("Exit", cDelete);

        top.add(btnNewInvoice);
        top.add(btnAddItem);
        top.add(btnChangeQty);
        top.add(btnDeleteRow);
        top.add(btnClearInvoice);

        top.add(btnAddNewItem);
        top.add(btnDeleteItem);
        top.add(btnDiscount);
        top.add(btnViewItems);
        top.add(btnViewSales);

        top.add(btnConfirm);
        top.add(btnExport);
        top.add(btnCustomers);
        top.add(btnRefresh);
        top.add(btnExit);

        backgroundPanel.add(top, BorderLayout.NORTH);

        // ========= INVOICE TABLE =========
        String[] cols = {"Item ID", "Name", "Qty", "Price", "Total", "__ITEM"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }

            @Override
            public Class<?> getColumnClass(int column) {
                return switch (column) {
                    case 0, 2 -> Integer.class;
                    case 3, 4 -> Double.class;
                    default -> String.class;
                };
            }
        };

        invoiceTable = new JTable(tableModel);
        invoiceTable.setRowHeight(30);
        invoiceTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        invoiceTable.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        invoiceTable.setGridColor(new Color(200, 200, 200));
        invoiceTable.setSelectionBackground(new Color(70, 130, 180));
        invoiceTable.setSelectionForeground(Color.WHITE);

        TableColumnModel cm = invoiceTable.getColumnModel();
        cm.removeColumn(cm.getColumn(cm.getColumnCount() - 1));

        JScrollPane scroll = new JScrollPane(invoiceTable);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Invoice Items", 0, 0, new Font("Segoe UI", Font.ITALIC, 13), Color.GRAY));
        backgroundPanel.add(scroll, BorderLayout.CENTER);

        // ========= BOTTOM PANEL: Totals & Payment =========
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // --- Left: Totals ---
        JPanel totals = new JPanel(new GridLayout(2, 2, 15, 10));
        totals.setOpaque(false);
        subtotalLabel = new JLabel("Subtotal: " + fmtAmount(0));
        discountLabel = new JLabel("Discount: " + fmtAmount(0));
        taxLabel = new JLabel("Tax (0%): " + fmtAmount(0));
        totalLabel = new JLabel("Grand Total: " + fmtAmount(0));

        Font fBold = new Font("Segoe UI", Font.BOLD, 16);
        for (JLabel lbl : new JLabel[]{subtotalLabel, discountLabel, taxLabel, totalLabel}) {
            lbl.setFont(fBold);
            lbl.setForeground(Color.WHITE);
            totals.add(lbl);
        }

        // --- Right: Payment ---
        JPanel payPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        payPanel.setOpaque(false);
        JLabel paymentLabel = new JLabel("Payment: ");
        paymentLabel.setForeground(Color.WHITE);  // ✅ White font
        paymentCombo = new JComboBox<>(new String[]{"Cash", "Card", "Credit"});
        paymentCombo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        paymentCombo.setPreferredSize(new Dimension(120, 32));
        payPanel.add(paymentLabel);
        payPanel.add(paymentCombo);

        bottomPanel.add(totals, BorderLayout.WEST);
        bottomPanel.add(payPanel, BorderLayout.EAST);

        backgroundPanel.add(bottomPanel, BorderLayout.SOUTH);

        // ========= ACTION LISTENERS =========
        btnNewInvoice.addActionListener(e -> newInvoice());
        btnAddItem.addActionListener(e -> addItem());
        btnChangeQty.addActionListener(e -> changeQty());
        btnDeleteRow.addActionListener(e -> deleteRow());
        btnClearInvoice.addActionListener(e -> clearInvoice());
        btnDiscount.addActionListener(e -> applyDiscount());
        btnConfirm.addActionListener(e -> confirmInvoice());
        btnExport.addActionListener(e -> exportPDF());
        btnViewSales.addActionListener(e -> viewSales());
        btnCustomers.addActionListener(e -> viewCustomers());
        btnViewItems.addActionListener(e -> showInventoryLive());
        btnAddNewItem.addActionListener(e -> addNewItem());
        btnDeleteItem.addActionListener(e -> deleteInventoryItem());
        btnRefresh.addActionListener(e -> updateTotals());
        btnExit.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Exit the POS system?", "Confirm Exit",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (inventoryDialog != null) inventoryDialog.dispose();
                System.exit(0);
            }
        });
    }

    private JButton styledBtn(String text, Color bgColor) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBackground(bgColor);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(180, 50));

        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setBackground(bgColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(bgColor);
            }
        });

        return b;
    }

    // ========= ANIMATED BACKGROUND PANEL =========
    class AnimatedBackgroundPanel extends JPanel {
        private float hue = 0f;
        private final Timer timer;

        public AnimatedBackgroundPanel() {
            setOpaque(false);
            timer = new Timer(100, e -> {
                hue = (hue + 0.003f) % 1.0f;
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            Color darkBlue = Color.getHSBColor(hue, 0.4f, 0.15f);
            Color darkTeal = Color.getHSBColor((hue + 0.2f) % 1.0f, 0.3f, 0.2f);
            GradientPaint gp = new GradientPaint(0, 0, darkBlue, w, h, darkTeal);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);

            g2d.setColor(new Color(255, 255, 255, 10));
            int spacing = 60;
            for (int x = 0; x < w; x += spacing) {
                g2d.drawLine(x, 0, x, h);
            }
            for (int y = 0; y < h; y += spacing) {
                g2d.drawLine(0, y, w, y);
            }
        }
    }

    // ========= LIVE INVENTORY WINDOW =========
    private void showInventoryLive() {
        if (inventoryDialog != null && inventoryDialog.isVisible()) {
            inventoryDialog.toFront();
            return;
        }

        inventoryDialog = new JDialog(this, "📦 Live Inventory", false);
        inventoryDialog.setSize(650, 500);
        inventoryDialog.setLocationByPlatform(true);
        inventoryDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        inventoryDialog.setLayout(new BorderLayout());

        String[] cols = {"ID", "Name", "Price", "Stock"};
        Object[][] data = new Object[store.getInventory().size()][4];
        int i = 0;
        for (Item item : store.getInventory().values()) {
            data[i++] = new Object[]{
                item.getId(),
                item.getName(),
                fmtAmount(item.getPrice()),
                item.getStock()
            };
        }

        JTable liveTable = new JTable(data, cols);
        liveTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        liveTable.setRowHeight(26);
        liveTable.setEnabled(false);
        liveTable.setGridColor(Color.LIGHT_GRAY);

        inventoryDialog.add(new JScrollPane(liveTable), BorderLayout.CENTER);

        JButton close = new JButton("Close");
        close.addActionListener(e -> inventoryDialog.dispose());
        JPanel bottom = new JPanel();
        bottom.add(close);
        inventoryDialog.add(bottom, BorderLayout.SOUTH);

        inventoryDialog.setVisible(true);

        Timer timer = new Timer(2000, e -> {
            if (!inventoryDialog.isDisplayable()) {
                ((Timer) e.getSource()).stop();
                return;
            }
            showInventoryLive();
        });
        timer.start();
    }

    // ========== ACTIONS ==========

    private void newInvoice() {
        try {
            int nextId = getNextAvailableCustomerId();

            int choice = JOptionPane.showConfirmDialog(this,
                    "Create new invoice for Customer ID: " + nextId + "?",
                    "New Customer", JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) return;

            String name = JOptionPane.showInputDialog(this, "Enter Customer Name:");
            if (name == null || name.trim().isEmpty()) {
                msg("Name cannot be empty.");
                return;
            }
            name = name.trim();

            Customer customer = new Customer(nextId, name);
            store.getCustomers().put(nextId, customer);
            store.saveAll();
            msg("✅ Created customer: " + name + " (ID: " + nextId + ")");

            String showroom = JOptionPane.showInputDialog(this, "Enter Showroom (optional):", "Showroom");
            if (showroom == null) showroom = "Showroom";
            showroom = showroom.trim().isEmpty() ? "Showroom" : showroom.trim();

            currentInvoice = new Invoice(customer, showroom, 0.00);
            tableModel.setRowCount(0);
            discountType = "AMOUNT";
            discountValue = 0.0;
            updateTotals();
            msg("New invoice started for " + customer.getName());
        } catch (Exception ex) {
            msg("Failed to create invoice: " + ex.getMessage());
        }
    }

    private int getNextAvailableCustomerId() {
        if (store.getCustomers().isEmpty()) return 1;
        return store.getCustomers().keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
    }

    private void addItem() {
        if (!ensureInvoice()) return;
        showInventoryLive();

        try {
            String idStr = keypad("Enter Item ID (see Live Inventory)");
            if (idStr == null || idStr.isEmpty()) return;
            int id = Integer.parseInt(idStr);
            Item item = store.getInventory().get(id);
            if (item == null) {
                msg("Item not found!");
                return;
            }

            String qtyStr = keypad("Enter Quantity (Stock: " + item.getStock() + ")");
            if (qtyStr == null || qtyStr.isEmpty()) return;
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0 || qty > item.getStock()) {
                msg("Invalid quantity!");
                return;
            }

            item.reduceStock(qty);
            store.saveAll();

            double lineTotal = qty * item.getPrice();
            tableModel.addRow(new Object[]{
                item.getId(), item.getName(), qty, item.getPrice(), lineTotal, item
            });
            updateTotals();
        } catch (Exception ex) {
            msg("Error adding item: " + ex.getMessage());
        }
    }

    private void addNewItem() {
        showInventoryLive();

        try {
            String idStr = keypad("Enter Item ID");
            if (idStr == null || idStr.isEmpty()) return;
            int id = Integer.parseInt(idStr);

            if (store.getInventory().containsKey(id)) {
                msg("Item with ID " + id + " already exists!");
                return;
            }

            String name = JOptionPane.showInputDialog(this, "Enter Item Name:");
            if (name == null || name.trim().isEmpty()) {
                msg("Name cannot be empty.");
                return;
            }

            String priceStr = keypad("Enter Price (LKR)");
            if (priceStr == null || priceStr.isEmpty()) return;
            double price = Double.parseDouble(priceStr);

            String stockStr = keypad("Enter Stock");
            if (stockStr == null || stockStr.isEmpty()) return;
            int stock = Integer.parseInt(stockStr);

            Item newItem = new Item(id, name, price, stock);
            store.getInventory().put(id, newItem);
            store.saveAll();

            msg("✅ Added: " + name);
            showInventoryLive();
        } catch (Exception ex) {
            msg("Error: " + ex.getMessage());
        }
    }

    private void deleteInventoryItem() {
        showInventoryLive();

        try {
            String idStr = keypad("Enter Item ID to delete");
            if (idStr == null || idStr.isEmpty()) return;
            int id = Integer.parseInt(idStr);
            Item item = store.getInventory().get(id);
            if (item == null) {
                msg("Not found!");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Delete " + item.getName() + "?");
            if (confirm != JOptionPane.YES_OPTION) return;

            store.getInventory().remove(id);
            store.saveAll();

            for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
                Item it = (Item) tableModel.getValueAt(i, 5);
                if (it.getId() == id) {
                    tableModel.removeRow(i);
                }
            }
            updateTotals();
            msg("🗑️ Deleted: " + item.getName());
        } catch (Exception ex) {
            msg("Invalid ID.");
        }
    }

    private void changeQty() {
        if (!ensureInvoice()) return;
        int row = invoiceTable.getSelectedRow();
        if (row < 0) {
            msg("Select a row first.");
            return;
        }

        int modelRow = invoiceTable.convertRowIndexToModel(row);
        Item item = (Item) tableModel.getValueAt(modelRow, 5);
        int oldQty = (int) tableModel.getValueAt(modelRow, 2);

        try {
            String qtyStr = keypad("New Quantity for " + item.getName());
            if (qtyStr == null || qtyStr.isEmpty()) return;
            int newQty = Integer.parseInt(qtyStr);
            if (newQty <= 0) {
                msg("Must be > 0");
                return;
            }

            int diff = newQty - oldQty;
            if (diff > 0) {
                if (diff > item.getStock()) {
                    msg("Not enough stock!");
                    return;
                }
                item.reduceStock(diff);
            } else if (diff < 0) {
                item.setStock(item.getStock() + (-diff));
            }
            store.saveAll();

            tableModel.setValueAt(newQty, modelRow, 2);
            tableModel.setValueAt(item.getPrice() * newQty, modelRow, 4);
            updateTotals();
        } catch (Exception ex) {
            msg("Invalid input.");
        }
    }

    private void deleteRow() {
        if (!ensureInvoice()) return;
        int row = invoiceTable.getSelectedRow();
        if (row < 0) {
            msg("Select row.");
            return;
        }

        int modelRow = invoiceTable.convertRowIndexToModel(row);
        Item item = (Item) tableModel.getValueAt(modelRow, 5);
        int qty = (int) tableModel.getValueAt(modelRow, 2);
        item.setStock(item.getStock() + qty);
        store.saveAll();
        tableModel.removeRow(modelRow);
        updateTotals();
    }

    private void clearInvoice() {
        if (!ensureInvoice()) return;
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            Item it = (Item) tableModel.getValueAt(i, 5);
            int qty = (int) tableModel.getValueAt(i, 2);
            it.setStock(it.getStock() + qty);
        }
        tableModel.setRowCount(0);
        store.saveAll();
        updateTotals();
    }

    private void applyDiscount() {
        if (!ensureInvoice()) return;
        String[] types = {"Percent (%)", "Amount (LKR)"};
        int choice = JOptionPane.showOptionDialog(this, "Select type:", "Discount",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, types, types[0]);
        if (choice == JOptionPane.CLOSED_OPTION) return;

        String input = keypad("Enter discount");
        if (input == null || input.isEmpty()) return;

        try {
            double val = Double.parseDouble(input);
            if (val < 0) {
                msg("Cannot be negative.");
                return;
            }
            if (choice == 0 && val > 100) {
                msg("Percent ≤ 100");
                return;
            }

            discountType = choice == 0 ? "PERCENT" : "AMOUNT";
            discountValue = val;
            updateTotals();
        } catch (Exception ex) {
            msg("Invalid number.");
        }
    }

    private void confirmInvoice() {
        if (!ensureInvoice() || tableModel.getRowCount() == 0) {
            msg("No items.");
            return;
        }

        Invoice inv = new Invoice(currentInvoice.getCustomer(), currentInvoice.getShowroom(), currentInvoice.getTaxRate());
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Item it = (Item) tableModel.getValueAt(i, 5);
            int qty = (int) tableModel.getValueAt(i, 2);
            inv.addItem(it, qty);
        }
        currentInvoice = inv;
        store.logSale(currentInvoice);

        msg("Invoice confirmed. Total: " + fmtAmount(computeSubtotalFromTable() * (1 + currentTax())));
        tableModel.setRowCount(0);
        discountType = "AMOUNT";
        discountValue = 0.0;
        updateTotals();
        currentInvoice = null;
    }

    private void exportPDF() {
        if (currentInvoice == null && tableModel.getRowCount() == 0) {
            if (store.getSalesHistory().isEmpty()) {
                msg("Nothing to export.");
                return;
            }
        }

        try {
            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String filename = "invoice_" + ts + ".pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            Paragraph title = new Paragraph("SuperMart POS - Sales Invoice\n\n",
                    new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD));
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);

            if (currentInvoice != null) {
                document.add(new Paragraph("Customer : " + currentInvoice.getCustomer().getName()));
                document.add(new Paragraph("Showroom : " + currentInvoice.getShowroom()));
            } else {
                Invoice last = store.getSalesHistory().get(store.getSalesHistory().size() - 1);
                document.add(new Paragraph("Customer : " + last.getCustomer().getName()));
                document.add(new Paragraph("Showroom : " + last.getShowroom()));
            }
            document.add(new Paragraph("Date     : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())));
            document.add(new Paragraph("Payment  : " + paymentCombo.getSelectedItem()));
            document.add(new Paragraph(" "));

            PdfPTable t = new PdfPTable(4);
            t.setWidthPercentage(100);
            t.addCell("Item");
            t.addCell("Price (LKR)");
            t.addCell("Qty");
            t.addCell("Line Total (LKR)");

            if (tableModel.getRowCount() > 0) {
                for (int r = 0; r < tableModel.getRowCount(); r++) {
                    Item it = (Item) tableModel.getValueAt(r, 5);
                    int qty = (int) tableModel.getValueAt(r, 2);
                    t.addCell(it.getName());
                    t.addCell(fmtAmount(it.getPrice()));
                    t.addCell(String.valueOf(qty));
                    t.addCell(fmtAmount(it.getPrice() * qty));
                }
                document.add(t);

                double subtotal = computeSubtotalFromTable();
                double discount = computeDiscount(subtotal);
                double tax = (subtotal - discount) * 0.00;
                double grand = (subtotal - discount) + tax;
                if (grand < 0) grand = 0;

                document.add(new Paragraph("\nSubtotal    : " + fmtAmount(subtotal)));
                document.add(new Paragraph("Discount    : " + (discountType.equals("PERCENT") ? (String.format("%.2f", discountValue) + "% = ") : "") + fmtAmount(discount)));
                document.add(new Paragraph("Tax (0%)   : " + fmtAmount(tax)));
                document.add(new Paragraph("Grand Total : " + fmtAmount(grand)));
            } else {
                Invoice last = store.getSalesHistory().get(store.getSalesHistory().size() - 1);
                for (Invoice.Line ln : last.getLines()) {
                    t.addCell(ln.getItem().getName());
                    t.addCell(fmtAmount(ln.getItem().getPrice()));
                    t.addCell(String.valueOf(ln.getQuantity()));
                    t.addCell(fmtAmount(ln.getLineTotal()));
                }
                document.add(t);

                document.add(new Paragraph("\nSubtotal    : " + fmtAmount(last.calculateSubtotal())));
                document.add(new Paragraph("Tax (0%)   : " + fmtAmount(last.calculateSubtotal() * 0.00)));
                document.add(new Paragraph("Grand Total : " + fmtAmount(last.calculateTotal())));
            }

            document.add(new Paragraph("\nThank you!"));
            document.close();

            msg("Exported: " + filename);
            try {
                Desktop.getDesktop().open(new File(filename));
            } catch (Exception ignored) {}
        } catch (Exception e) {
            msg("Export failed: " + e.getMessage());
        }
    }

    private void viewSales() {
        if (store.getSalesHistory().isEmpty()) {
            msg("No sales yet.");
            return;
        }

        StringBuilder sb = new StringBuilder("Sales History (latest first):\n\n");
        for (int i = store.getSalesHistory().size() - 1; i >= 0; i--) {
            Invoice inv = store.getSalesHistory().get(i);
            sb.append("Invoice #").append(i + 1).append("\n");
            sb.append("  Customer: ").append(inv.getCustomer().getName()).append("\n");
            sb.append("  Subtotal: ").append(fmtAmount(inv.calculateSubtotal())).append("\n");
            sb.append("  Tax (0%): ").append(fmtAmount(inv.calculateSubtotal() * 0.00)).append("\n");
            sb.append("  Total: ").append(fmtAmount(inv.calculateTotal())).append("\n");
            sb.append("  Items:\n");

            for (Invoice.Line line : inv.getLines()) {
                sb.append("    • ").append(line.getItem().getName())
                        .append(" x").append(line.getQuantity())
                        .append(" @ ").append(fmtAmount(line.getItem().getPrice()))
                        .append(" = ").append(fmtAmount(line.getLineTotal())).append("\n");
            }
            sb.append("-".repeat(50)).append("\n");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(800, 600));
        JOptionPane.showMessageDialog(this, scroll, "Sales History", JOptionPane.PLAIN_MESSAGE);
    }

    private void viewCustomers() {
        if (store.getCustomers().isEmpty()) {
            msg("No customers.");
            return;
        }

        StringBuilder sb = new StringBuilder("<html><table width='100%' border='1' cellpadding='5' cellspacing='0'>");
        sb.append("<tr bgcolor='#f0f0f0'><th>ID</th><th>Name</th></tr>");
        for (Customer c : store.getCustomers().values()) {
            sb.append(String.format("<tr><td>%d</td><td>%s</td></tr>", c.getId(), c.getName()));
        }
        sb.append("</table></html>");

        JLabel label = new JLabel(sb.toString());
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(label);
        scroll.setPreferredSize(new Dimension(500, 400));

        JButton btnRemove = new JButton("Remove Customer");
        btnRemove.setBackground(new Color(213, 0, 0));
        btnRemove.setForeground(Color.WHITE);
        btnRemove.addActionListener(e -> removeCustomer());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnRemove);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, "Customers", JOptionPane.INFORMATION_MESSAGE);
    }

    private void removeCustomer() {
        String idStr = JOptionPane.showInputDialog(this, "Enter Customer ID to remove:");
        if (idStr == null || idStr.trim().isEmpty()) return;

        try {
            int id = Integer.parseInt(idStr);
            Customer customer = store.getCustomers().get(id);
            if (customer == null) {
                msg("Customer not found!");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Remove customer: " + customer.getName() + "?", "Confirm Remove",
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            store.getCustomers().remove(id);
            store.saveAll();
            msg("✅ Removed customer: " + customer.getName());
        } catch (Exception e) {
            msg("Invalid ID.");
        }
    }

    // ========== HELPERS ==========

    private boolean ensureInvoice() {
        if (currentInvoice == null) {
            msg("Start a new invoice first!");
            return false;
        }
        return true;
    }

    private double computeSubtotalFromTable() {
        double subtotal = 0;
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            Object cellValue = tableModel.getValueAt(row, 4);
            if (cellValue instanceof Number) {
                subtotal += ((Number) cellValue).doubleValue();
            }
        }
        return subtotal;
    }

    private double computeDiscount(double subtotal) {
        if (discountValue <= 0) return 0;
        return "PERCENT".equals(discountType)
                ? subtotal * (discountValue / 100.0)
                : Math.min(discountValue, subtotal);
    }

    private double currentTax() {
        return 0.00;
    }

    private String fmtAmount(double amount) {
        return LKR.format(amount);
    }

    private void msg(String s) {
        JOptionPane.showMessageDialog(this, s);
    }

    private String keypad(String title) {
        JDialog d = new JDialog(this, title, true);
        d.setLayout(new BorderLayout(10, 10));
        d.setSize(320, 420);
        d.setLocationRelativeTo(this);

        JTextField field = new JTextField();
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setFont(new Font("Segoe UI", Font.BOLD, 24));
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    d.dispose();
                } else if (e.getKeyChar() == 'c' || e.getKeyChar() == 'C') {
                    field.setText("");
                    e.consume();
                }
            }
        });
        d.add(field, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(4, 3, 10, 10));
        String[] keys = {"1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "0", "C"};
        for (String k : keys) {
            JButton b = new JButton(k);
            b.setFont(new Font("Segoe UI", Font.BOLD, 20));
            b.addActionListener(e -> {
                if ("C".equals(k)) field.setText("");
                else field.setText(field.getText() + k);
            });
            grid.add(b);
        }
        d.add(grid, BorderLayout.CENTER);

        JButton ok = new JButton("OK");
        ok.setFont(new Font("Segoe UI", Font.BOLD, 18));
        ok.addActionListener(e -> d.dispose());
        JPanel south = new JPanel();
        south.add(ok);
        d.add(south, BorderLayout.SOUTH);

        d.setVisible(true);
        return field.getText().trim();
    }

    private void updateTotals() {
        double subtotal = computeSubtotalFromTable();
        double discount = computeDiscount(subtotal);
        double tax = (subtotal - discount) * currentTax();
        double grand = (subtotal - discount) + tax;
        if (grand < 0) grand = 0;

        subtotalLabel.setText("Subtotal: " + fmtAmount(subtotal));
        discountLabel.setText("Discount: " + (discountType.equals("PERCENT") ? (String.format("%.2f", discountValue) + "% = ") : "") + fmtAmount(discount));
        taxLabel.setText("Tax (0%): " + fmtAmount(tax));
        totalLabel.setText("Grand Total: " + fmtAmount(grand));
    }
}