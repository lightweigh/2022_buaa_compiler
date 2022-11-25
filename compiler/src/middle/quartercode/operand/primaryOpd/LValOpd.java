package middle.quartercode.operand.primaryOpd;

import middle.VarName;
import middle.quartercode.operand.Operand;

public class LValOpd implements PrimaryOpd {
    // a; a[t2]; a[1]
    private VarName name;
    private Operand idx;

    public LValOpd(VarName name, Operand idx) {
        this.name = name;
        this.idx = idx;
    }

    // t1 ...
    public LValOpd(VarName name) {
        this.name = name;
        this.idx = null;
    }

    @Override
    public VarName getVarName() {
        return name;
    }

    @Override
    public void rename(VarName name) {
        this.name = name;
    }

    public Operand getIdx() {
        return idx;
    }

    @Override
    public boolean isGlobalVar() {
        return getVarName().getDepth() == 0;
    }

    public boolean isArray() {
        return idx != null;
    }

    @Override
    public String toString() {
        return name + (idx != null ? "[" + idx.getVarName() + "]" : "");
    }
}
