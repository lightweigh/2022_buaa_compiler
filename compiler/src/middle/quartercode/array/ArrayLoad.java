package middle.quartercode.array;

import frontend.grammar.LVal;
import middle.VarName;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;
import middle.quartercode.operand.primaryOpd.LValOpd;
import middle.quartercode.operand.primaryOpd.PrimaryOpd;

// 之前没有用ArrayLoad是把它合并到AssignCode里面去了
public class ArrayLoad implements MiddleCode {
    // #t0 = a[#t1]
    private LValOpd dst;
    private LValOpd primaryOpd;

    public ArrayLoad(LValOpd dst, LValOpd primaryOpd) {
        this.primaryOpd = primaryOpd;
        this.dst = dst;
    }

    @Override
    public VarName getVarName() {
        return dst.getVarName();
    }

    @Override
    public void rename(VarName name) {
        dst.rename(name);
    }

    public PrimaryOpd getPrimaryOpd() {
        return primaryOpd;
    }

    public Operand getDst() {
        return dst;
    }

    @Override
    public CodeType getCodeType() {
        return CodeType.ARRAY_LOAD;
    }

    @Override
    public boolean isGlobalVar() {
        return getVarName().getDepth() == 0;
    }

    @Override
    public String toString() {
        return dst.getVarName() + " = " + primaryOpd.toString() + "\n";
    }
}
