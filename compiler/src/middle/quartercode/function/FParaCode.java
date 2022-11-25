package middle.quartercode.function;

import middle.VarName;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

public class FParaCode implements MiddleCode {
    private VarName name;
    private boolean isAddr;
    private Operand offset; // 对于变量,offset为null;对于一维数组,offset为Immediate(0);对于二维数组 colNum

    public FParaCode(VarName name, boolean isAddr, Operand offset) {
        this.name = name;
        this.isAddr = isAddr;
        this.offset = offset;
    }

    @Override
    public VarName getVarName() {
        return name;
    }

    @Override
    public void rename(VarName name) {
        this.name = name;
    }

    public boolean isAddr() {
        return isAddr;
    }

    public Operand getOffset() {
        return offset;
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
