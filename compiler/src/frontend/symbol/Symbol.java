package frontend.symbol;

public class Symbol {
    private String symName;
    private int depth;

    public Symbol(String symName, int depth) {
        this.symName = symName;
        this.depth = depth;
    }

    public String getSymName() {
        return symName;
    }

    public int getDepth() {
        return depth;
    }
}
