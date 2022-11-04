package middle.quartercode.array;

import middle.VarName;
import middle.quartercode.operand.MiddleCode;

public class ArrayDef implements MiddleCode {
    // arr int a[][],a[]
    // arr const int a[][],a[]
    // 我直接把二维的拆成一维的吧
    private VarName name;
    private boolean isConst;
    // private Ident ident;
    private int size;

    public ArrayDef(VarName name, boolean isConst, int size) {
        this.name = name;
        this.isConst = isConst;
        this.size = size;
    }

    @Override
    public String toString() {
        return "arr " +
                (isConst ? "const " : "") +
                name + "[" + size + "]\n";
    }

    public int getSize() {
        return size;
    }


    @Override
    public VarName getVarName() {
        // todo no use?
        return name;
    }

    @Override
    public void rename(VarName name) {
        this.name = name;
    }

    @Override
    public boolean isGlobalVar() {
        return name.getDepth() == 0;
    }

    @Override
    public CodeType getCodeType() {
        return CodeType.ARRAY_DEF;
    }
}
