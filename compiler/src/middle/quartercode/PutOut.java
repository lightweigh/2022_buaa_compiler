package middle.quartercode;

import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

public class PutOut implements MiddleCode {
    private String name = null;
    private Operand operand;

    public PutOut(Operand operand) {
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
        return "printf " + operand.getName() + "\n";
    }
}
