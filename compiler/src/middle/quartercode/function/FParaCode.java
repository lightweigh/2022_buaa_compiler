package middle.quartercode.function;

import middle.VarName;
import middle.quartercode.operand.MiddleCode;

public class FParaCode implements MiddleCode {
    private VarName name;
    private boolean isAddr;
    private int num;

    public FParaCode(VarName name, boolean isAddr, int num) {
        this.name = name;
        this.isAddr = isAddr;
        this.num = num;
    }

    @Override
    public VarName getVarName() {
        return name;
    }

    @Override
    public void rename(VarName name) {
        this.name = name;
    }

    public int getNum() {
        return num;
    }

    public boolean isAddr() {
        return isAddr;
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
        return "para int" + (isAddr ? "* " : " ") + name + "\n";

    }
}
