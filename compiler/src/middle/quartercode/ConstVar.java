package middle.quartercode;

import frontend.token.Ident;
import middle.quartercode.operand.primaryOpd.Immediate;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

public class ConstVar implements MiddleCode {
    // int i;					# var int i
    // int i = 1;				# var int i = 1
    // const int i = 10;		# const int i = 10

    // 常量和变量哈
    private boolean isConst;
    private Ident ident;
    private Operand operand;

    public ConstVar(boolean isConst, Ident ident, Operand operand) {
        this.isConst = isConst;
        this.ident = ident;
        this.operand = operand;
    }

    @Override
    public String toString() {
        return (isConst ? "const int " : "var int ") +
                ident.getContent() +
                (operand != null ? " = " + operand.getName() : "") + "\n";
    }

    @Override
    public String getName() {
        // todo no use?
        return ident.getContent();
    }
}
