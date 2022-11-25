package middle.quartercode;

import middle.VarName;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

public class PutOut implements MiddleCode {
    private VarName name = null;
    private Operand operand;

    public PutOut(Operand operand) {
        this.operand = operand;
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
        return CodeType.PRINT;
    }

    public Operand getOperand() {
        return operand;
    }

    @Override
    public boolean isGlobalVar() {
        return getVarName().getDepth() == 0;
    }

    @Override
    public String toString() {
        return "printf " + operand.toString() + "\n";
    }
}
