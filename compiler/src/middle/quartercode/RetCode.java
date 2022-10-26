package middle.quartercode;

import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

// 函数内部返回语句
public class RetCode implements MiddleCode {
    private String name=null;
    private Operand operand;

    public RetCode(Operand operand) {
        this.operand = operand;
    }

    @Override
    public String getName() {
        return name;
    }

    public Operand getOperand() {
        return operand;
    }

    @Override
    public String toString() {
        return "RET " + operand.getName() + "\n";
    }
}
