package middle.quartercode.function;

import middle.VarName;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

public class RParaCode implements MiddleCode {
    private Operand operand;

    public RParaCode(Operand operand) {
        this.operand = operand;
    }

    @Override
    public VarName getVarName() {
        return operand.getVarName();
    }

    @Override
    public void rename(VarName name) {
        operand.rename(name);
    }

    @Override
    public CodeType getCodeType() {
        return CodeType.RPARA;
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
        return "push " + operand.getVarName() + "\n";
    }
}
