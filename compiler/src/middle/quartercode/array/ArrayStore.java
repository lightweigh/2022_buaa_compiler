package middle.quartercode.array;

import middle.VarName;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;
import middle.quartercode.operand.primaryOpd.PrimaryOpd;

public class ArrayStore implements MiddleCode {
    private PrimaryOpd primaryOpd;
    private Operand src;

    public ArrayStore(PrimaryOpd primaryOpd, Operand src) {
        this.primaryOpd = primaryOpd;
        this.src = src;
    }

    public PrimaryOpd getPrimaryOpd() {
        return primaryOpd;
    }

    public Operand getSrc() {
        return src;
    }

    @Override
    public VarName getVarName() {
        return primaryOpd.getVarName();
    }

    @Override
    public void rename(VarName name) {
        primaryOpd.rename(name);
    }

    @Override
    public CodeType getCodeType() {
        return CodeType.ARRAY_STORE;
    }

    @Override
    public boolean isGlobalVar() {
        return getVarName().getDepth() == 0;
    }

    @Override
    public String toString() {
        return primaryOpd.toString() + " = " + src.getVarName() + "\n";
    }
}
