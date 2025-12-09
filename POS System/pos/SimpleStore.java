package pos;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SimpleStore {
    private final File dataDir = new File("data");
    private final File itemsFile      = new File(dataDir, "items.csv");
    private final File customersFile  = new File(dataDir, "customers.csv");
    private final File suppliersFile  = new File(dataDir, "suppliers.txt");
    private final File salesFile      = new File(dataDir, "sales.txt"); // basic log

    private final Map<Integer, Item> inventory   = new LinkedHashMap<>();
    private final Map<Integer, Customer> customers = new LinkedHashMap<>();
    private final List<Supplier> suppliers = new ArrayList<>();
    private final List<Invoice> salesHistory = new ArrayList<>();

    public SimpleStore() {
        if (!dataDir.exists()) dataDir.mkdirs();
        loadAll();
        if (inventory.isEmpty() && customers.isEmpty() && suppliers.isEmpty()) seed();
    }

    // ---- Public accessors ----
    public Map<Integer, Item> getInventory(){ return inventory; }
    public Map<Integer, Customer> getCustomers(){ return customers; }
    public List<Supplier> getSuppliers(){ return suppliers; }
    public List<Invoice> getSalesHistory(){ return salesHistory; }

    public void saveAll(){
        saveItems(); saveCustomers(); saveSuppliers(); // sales saved on confirm
    }

    public void logSale(Invoice inv){
        salesHistory.add(inv);
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(salesFile, true), StandardCharsets.UTF_8))) {
            pw.println(new Date() + " :: " + inv.toString().replace("\n"," | "));
        } catch (Exception ignored) {}
    }

    // ---- Load/Save helpers ----
    private void loadAll(){ loadItems(); loadCustomers(); loadSuppliers(); loadSales(); }

    private void loadItems() {
        if (!itemsFile.exists()) return;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(itemsFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null){
                line = line.trim(); if (line.isEmpty()) continue;
                Item it = Item.fromCsv(line);
                inventory.put(it.getId(), it);
            }
        } catch (Exception ignored){}
    }
    private void saveItems(){
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(itemsFile), StandardCharsets.UTF_8))) {
            for (Item it : inventory.values()) pw.println(it.toCsv());
        } catch (Exception ignored){}
    }

    private void loadCustomers(){
        if (!customersFile.exists()) return;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(customersFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null){
                line = line.trim(); if (line.isEmpty()) continue;
                Customer c = Customer.fromCsv(line);
                customers.put(c.getId(), c);
            }
        } catch (Exception ignored){}
    }
    private void saveCustomers(){
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(customersFile), StandardCharsets.UTF_8))) {
            for (Customer c : customers.values()) pw.println(c.toCsv());
        } catch (Exception ignored){}
    }

    private void loadSuppliers(){
        if (!suppliersFile.exists()) return;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(suppliersFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null){
                line = line.trim(); if (line.isEmpty()) continue;
                suppliers.add(Supplier.fromLine(line));
            }
        } catch (Exception ignored){}
    }
    private void saveSuppliers(){
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(suppliersFile), StandardCharsets.UTF_8))){
            for (Supplier s: suppliers) pw.println(s.toLine());
        } catch (Exception ignored){}
    }

    private void loadSales(){
        // optional: not reconstructing items per sale (kept as a flat log)
        if (!salesFile.exists()) return;
    }

    private void seed(){
        inventory.put(101, new Item(101, "Apple iPhone 14", 999.00, 10));
        inventory.put(102, new Item(102, "Samsung Galaxy S23", 899.00, 8));
        inventory.put(103, new Item(103, "Dell Inspiron 15", 1199.00, 5));
        inventory.put(104, new Item(104, "HP LaserJet Pro", 249.00, 12));
        inventory.put(105, new Item(105, "Logitech MX Master 3", 99.00, 20));

        customers.put(1, new Customer(1, "John Doe"));
        customers.put(2, new Customer(2, "Jane Smith"));
        customers.put(3, new Customer(3, "Ibrahim Khan"));

        suppliers.add(new Supplier("TechWorld Distributors"));
        suppliers.add(new Supplier("Global Gadgets Ltd."));
        suppliers.add(new Supplier("Mega Electronics PLC"));

        saveAll();
    }
}
