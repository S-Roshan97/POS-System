package pos;

public class Item {
    private final int id;
    private String name;
    private double price;
    private int stock;

    public Item(int id, String name, double price, int stock) {
        this.id = id; this.name = name; this.price = price; this.stock = stock;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }

    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
    public void reduceStock(int qty) { stock = Math.max(0, stock - qty); }

    // CSV helpers
    public String toCsv() {
        return id + "," + escape(name) + "," + price + "," + stock;
    }
    public static Item fromCsv(String line) {
        String[] p = split(line, 4);
        return new Item(Integer.parseInt(p[0]), unescape(p[1]),
                Double.parseDouble(p[2]), Integer.parseInt(p[3]));
    }
    static String escape(String s){ return s.replace(",", "⎜"); }
    static String unescape(String s){ return s.replace("⎜", ","); }
    static String[] split(String s,int n){
        String[] a=s.split(",",-1);
        if(a.length<n){String[] b=new String[n];System.arraycopy(a,0,b,0,a.length);for(int i=a.length;i<n;i++)b[i]="";return b;}
        return a;
    }
}
