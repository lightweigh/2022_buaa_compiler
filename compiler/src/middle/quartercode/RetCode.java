package middle.quartercode;

import middle.VarName;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

// 函数内部返回语句
public class RetCode implements MiddleCode {
    private VarName name = null;
    private Operand operand;

    public RetCode(Operand operand) {
        this.operand = operand;
    }

    public boolean hasRetValue() {
        return operand != null;
    }

    @Override
    public VarName getVarName() {
        if (operand == null)    return new VarName("no ret value", 0);
        return operand.getVarName();
    }

    @Override
    public void rename(VarName name) {
        this.name = name;
    }

    @Override
    public CodeType getCodeType() {
        return CodeType.RET;
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
        return "RET " + (operand != null ? operand.getVarName() : "") + "\n";
    }
}
