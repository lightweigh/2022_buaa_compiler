package middle.quartercode.function;

import middle.VarName;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

public class FParaCode implements MiddleCode {
    private VarName name;

    public FParaCode(VarName name) {
        this.name = name;
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
        return CodeType.FPARA;
    }

    @Override
    public boolean isGlobalVar() {
        return getVarName().getDepth() == 0;
    }

    @Override
    public String toString() {
        return "para int" + (name.isArray() ? "* " : " ") + name + "\n";

    }
}
