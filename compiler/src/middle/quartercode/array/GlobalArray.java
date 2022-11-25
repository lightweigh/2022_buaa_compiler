package middle.quartercode.array;

import middle.VarName;
import middle.quartercode.operand.MiddleCode;

import java.util.ArrayList;

public class GlobalArray implements MiddleCode {
    // 数组
    private VarName name;
    private int size;   // 二维数组也要展开为一维
    private ArrayList<Integer> values;

    public GlobalArray(VarName name, int size, ArrayList<Integer> values) {
        this.name = name;
        this.size = size;
        this.values = values;
    }

    public int getSize() {
        return size;
    }

    public ArrayList<Integer> getValues() {
        return values;
    }

    @Override
    public VarName getVarName() {
        return name;
    }

    @Override
    public void rename(VarName name) {
        this.name = name;
    }

    @Override
    public boolean isGlobalVar() {
        return true;
    }

    @Override
    public CodeType getCodeType() {
        return CodeType.GLOBAL_ARRAY;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name.toString());
        sb.append("[").append(size).append("]");
        if (!values.isEmpty()) {
            sb.append(": ");
            for (int value : values) {
                sb.append(value).append(" ");
            }
        }
        sb.append("\n");
        return sb.toString();
    }
}
