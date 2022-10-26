package middle.quartercode.array;

import frontend.token.Ident;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

// 可以是初始化，也可以是后续赋值
public class ArrayAssign implements MiddleCode {
    // a[] =
    // const?
    private Ident ident;
    private int idx;
    private Operand operand;

    public ArrayAssign(Ident ident, int idx, Operand operand) {
        this.ident = ident;
        this.idx = idx;
        this.operand = operand;
    }

    @Override
    public String toString() {
        return ident.getContent() + "[" + idx + "] = " + operand.getName() + "\n";
    }

    @Override
    public String getName() {
        // todo no use?
        return ident.getContent();
    }
}
