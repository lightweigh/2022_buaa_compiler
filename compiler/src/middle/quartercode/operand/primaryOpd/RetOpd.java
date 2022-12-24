package middle.quartercode.operand.primaryOpd;

import middle.VarName;

public class RetOpd implements PrimaryOpd {
    // 调用函数后的返回值右值 i = foo();   // 这里分析的是LVal = Exp; 的右边
    private final String name = "RET";
    private VarName funcName;

    public RetOpd(VarName funcName) {
        this.funcName = funcName;
    }

    @Override
    public VarName getVarName() {
        return funcName;
    }

    @Override
    public boolean isGlobalVar() {
        assert false;
        return getVarName().getDepth() == 0;
    }

    @Override
    public void rename(VarName name) {

    }

    @Override
    public String toString() {
        return name + " " + funcName;
    }
}
