package frontend.symbol;

public class FParamSymbol extends Symbol {
    // a, a[], a[][n]
    private int type;   // 0: scala  1: pointer  2: array of pointers
    private int colNum;   // 0:a, 1:a[], >1:a[][n]

    public FParamSymbol(String symName, int type, int colNum, int depth) {
        super(symName, depth);
        this.type = type;
        this.colNum = colNum;
    }

    public int getType() {
        return type;
    }

    public int getColNum() {
        return colNum;
    }
}
