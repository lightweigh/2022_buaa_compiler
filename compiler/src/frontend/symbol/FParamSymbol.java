package frontend.symbol;

public class FParamSymbol extends Symbol {
    // a, a[], a[][n]
    private int type;   // 0: scala  1: pointer  2: array of pointers
    private int pointNum;   // 0:a, 1:a[], >1:a[][n]

    public FParamSymbol(String symName, int type) {
        super(symName);
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public int getPointNum() {
        return pointNum;
    }
}
