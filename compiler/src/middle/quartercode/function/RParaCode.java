package middle.quartercode.function;

import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

public class RParaCode implements MiddleCode {
    private Operand operand;

    public RParaCode(Operand operand) {
        this.operand = operand;
    }

    @Override
    public String getName() {
        return operand.getName();
    }

    public Operand getOperand() {
        return operand;
    }

    @Override
    public String toString() {
        return "push " + operand.getName() + "\n";
    }
}
