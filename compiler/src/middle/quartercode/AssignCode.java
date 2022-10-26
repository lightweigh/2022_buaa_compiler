package middle.quartercode;

import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

public class AssignCode implements MiddleCode {
    private String name;
    private Operand operand;

    public AssignCode(String name, Operand operand) {
        this.name = name;
        this.operand = operand;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " = " + operand.getName() + "\n";
    }
}
