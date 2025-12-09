package pos;

import java.util.ArrayList;
import java.util.List;

public class Invoice {
    public static class Line {
        private final Item item;
        private final int quantity;
        public Line(Item item, int quantity){ this.item = item; this.quantity = quantity; }
        public Item getItem(){ return item; }
        public int getQuantity(){ return quantity; }
        public double getLineTotal(){ return item.getPrice() * quantity; }
    }

    private final Customer customer;
    private final String showroom;
    private final double taxRate;
    private final List<Line> items = new ArrayList<>();

    public Invoice(Customer customer, String showroom, double taxRate) {
        this.customer = customer; this.showroom = showroom; this.taxRate = taxRate;
    }
    public void addItem(Item item, int qty){ items.add(new Line(item, qty)); }
    public List<Line> getLines(){ return items; }
    public Customer getCustomer(){ return customer; }
    public String getShowroom(){ return showroom; }
    public double getTaxRate(){ return taxRate; }

    public double calculateSubtotal(){
        double s=0; for(Line l:items) s+=l.getLineTotal(); return s;
    }
    public double calculateTotal(){
        return calculateSubtotal() * (1 + taxRate);
    }

    @Override public String toString(){
        StringBuilder sb=new StringBuilder();
        sb.append("Invoice for ").append(customer.getName()).append(" @ ").append(showroom).append("\n");
        for(Line l:items){
            sb.append(l.getItem().getName()).append(" x").append(l.getQuantity())
              .append(" = $").append(String.format("%.2f", l.getLineTotal())).append("\n");
        }
        sb.append("Subtotal: $").append(String.format("%.2f", calculateSubtotal())).append("\n");
        sb.append("Total: $").append(String.format("%.2f", calculateTotal()));
        return sb.toString();
    }
}
