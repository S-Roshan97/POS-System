package pos;

public class Supplier {
    private final String name;
    public Supplier(String name){ this.name = name; }
    public String getName(){ return name; }

    public String toLine(){ return Item.escape(name); }
    public static Supplier fromLine(String line){ return new Supplier(Item.unescape(line)); }
}
