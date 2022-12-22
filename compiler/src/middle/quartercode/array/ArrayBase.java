package middle.quartercode.array;

import middle.VarName;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.primaryOpd.LValOpd;

public class ArrayBase implements MiddleCode {
    // 数组基地址
    private LValOpd dst;
    private LValOpd base;   // a, a[0], a[#t0], a[b@1]

    public ArrayBase(LValOpd dst, LValOpd base) {
        this.dst = dst;
        this.base = base;
    }

    public LValOpd getDst() {
        return dst;
    }

    public LValOpd getBase() {
        return base;
    }

    @Override
    public CodeType getCodeType() {
        return CodeType.ARRAY_BASE;
    }

    @Override
    public VarName getVarName() {
        return dst.getVarName();
    }

    @Override
    public void rename(VarName name) {
        assert false;
    }

    @Override
    public boolean isGlobalVar() {
        return false;
    }

    @Override
    public String toString() {
        return dst.toString() + " = &" + base.toString() + "\n";
    }
}
