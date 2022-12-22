package middle.quartercode;

import middle.VarName;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;
import middle.quartercode.operand.primaryOpd.LValOpd;

public class AssignCode implements MiddleCode {
    private LValOpd dst;
    private Operand operand;

    public AssignCode(LValOpd dst, Operand operand) {
        this.dst = dst;
        this.operand = operand;
    }

    public Operand getOperand() {
        return operand;
    }

    @Override
    public VarName getVarName() {
        return dst.getVarName();
    }

    @Override
    public void rename(VarName name) {
        this.dst.rename(name);
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
        return dst + " = " + operand.getVarName() + "\n";
    }
}
