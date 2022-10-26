package middle.quartercode.array;

import frontend.token.Ident;
import middle.quartercode.operand.MiddleCode;

public class ArrayDef implements MiddleCode {
    // arr int a[][],a[]
    // arr const int a[][],a[]
    // 我直接把二维的拆成一维的吧
    private boolean isConst;
    private Ident ident;
    private int size;

    public ArrayDef(boolean isConst, Ident ident, int size) {
        this.isConst = isConst;
        this.ident = ident;
        this.size = size;
    }

    @Override
    public String toString() {
        return "arr " +
                (isConst ? "const " : "") +
                ident.getContent() + "[" + size + "]\n";
    }

    @Override
    public String getName() {
        // todo no use?
        return ident.getContent();
    }
}
