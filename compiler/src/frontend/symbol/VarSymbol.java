package frontend.symbol;

import java.util.ArrayList;

public class VarSymbol extends Symbol {
    // const or non-const     常量、变量
    // a or a[0] or a[0][0]

    private boolean isConst;
    private int dimension;
    private int size;
    private int colNum;
    private boolean isInit;
    private ArrayList<Integer> values;

    public VarSymbol(String symName, boolean isConst, int dimension, int size, int colNum, ArrayList<Integer> values) {
        super(symName);
        this.isConst = isConst;
        this.dimension = dimension;
        this.size = size;
        this.colNum = colNum;
        this.values = values;
    }

    public boolean isConst() {
        return isConst;
    }

    public int getDimension() {
        return dimension;
    }

    public int getColNum() {
        return colNum;
    }

    public int getValue(int idx) {
        return values.get(idx);
    }
}
