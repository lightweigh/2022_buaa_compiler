package middle.quartercode.array;

import frontend.token.Ident;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

import java.util.ArrayList;

public class Array implements MiddleCode {
    // 数组
    private boolean isConst;
    private boolean isDef;
    private Ident ident;
    private int size;   // 二维数组也要展开为一维
    private ArrayList<Operand> operands;
    private ArrayList<MiddleCode> middleCodes;

    public Array(boolean isConst, boolean isDef, Ident ident,
                 int size, ArrayList<Operand> operands) {
        this.isConst = isConst;
        this.isDef = isDef;
        this.ident = ident;
        this.size = size;
        this.operands = operands;
    }

    private void genMiddleCodes() {
        middleCodes.add(new ArrayDef(isConst,ident,size));
        for (int i = 0; i < size; i++) {
            middleCodes.add(new ArrayAssign(ident,i,operands.get(i)));
        }
    }

    public ArrayList<MiddleCode> getMiddleCodes() {
        return middleCodes;
    }

    @Override
    public String toString() {
        // todo 中间代码，Code 创建数组。
        // 不要了
        StringBuilder sb = new StringBuilder();
        sb.append(isDef ? isConst ? "const " : "" : "" +
                ident.getContent() + "[" + size + "]\n");
        for (int i = 0; i < operands.size(); i++) {
            // sb.append(ident.getContent()).append("[").append(i).append("]").append(" = ").append(operands.get(i).getValue()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String getName() {
        // todo no use?
        return ident.getContent();
    }
}
