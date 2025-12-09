package pos;

public class Customer {
    private final int id;
    private String name;

    public Customer(int id, String name) {
        this.id = id; this.name = name;
    }
    public int getId(){ return id; }
    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }

    public String toCsv(){ return id + "," + Item.escape(name); }
    public static Customer fromCsv(String line){
        String[] p = Item.split(line, 2);
        return new Customer(Integer.parseInt(p[0]), Item.unescape(p[1]));
    }
}
