package middle.quartercode;

import middle.VarName;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

public class AssignCode implements MiddleCode {
    private VarName name;
    private Operand operand;

    public AssignCode(VarName name, Operand operand) {
        this.name = name;
        this.operand = operand;
    }

    public Operand getOperand() {
        return operand;
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
    public CodeType getCodeType() {
        return CodeType.ASSIGN;
    }

    @Override
    public boolean isGlobalVar() {
        return getVarName().getDepth() == 0;
    }

    @Override
    public String toString() {
        return name + " = " + operand.getVarName() + "\n";
    }
}
