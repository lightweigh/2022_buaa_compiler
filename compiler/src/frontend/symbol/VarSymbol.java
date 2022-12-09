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

    private boolean isFuncFParam = false;

    public VarSymbol(String symName, boolean isConst, int dimension,
                     int size, int colNum, ArrayList<Integer> values, int depth) {
        super(symName, depth);
        this.isConst = isConst;
        this.dimension = dimension;
        this.size = size;
        this.colNum = colNum;
        this.values = values;
    }

    public VarSymbol(String symName, int dimension, int colNum, int depth) {
        // 函数参数
        // (dimension, colNum) = (0, 0) , (1, 0), (2, colNum)
        super(symName, depth);
        this.dimension = dimension;
        this.isFuncFParam = true;
        this.colNum = colNum;
    }

    public boolean isFuncFParam() {
        return isFuncFParam;
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

    /**
     * 全局初始化、常量取值
     *
     * @param idx
     * @return
     */
    public int getValue(int idx) {
        return values.get(idx);
    }
}
