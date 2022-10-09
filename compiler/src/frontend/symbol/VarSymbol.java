package frontend.symbol;

public class VarSymbol extends Symbol {
    // const or non-const or fParam     常量、变量、形参
    // a or a[0] or a[0][0]

    private boolean isConst;
    private int dimension;

    public VarSymbol(String symName, boolean isConst, int dimension) {
        super(symName);
        this.isConst = isConst;
        this.dimension = dimension;
    }

    public boolean isConst() {
        return isConst;
    }

    public int getDimension() {
        return dimension;
    }
}
