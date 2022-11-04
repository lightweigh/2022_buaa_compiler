package middle.quartercode.operand.primaryOpd;

import middle.VarName;

public class RetOpd implements PrimaryOpd {
    // 调用函数后的返回值右值 i = foo();   // 这里分析的是LVal = Exp; 的右边
    private final String name = "RET";

    @Override
    public VarName getVarName() {
        return new VarName("RET", 0);
    }

    @Override
    public boolean isGlobalVar() {
        return getVarName().getDepth() == 0;
    }

    @Override
    public void rename(VarName name) {

    }

    @Override
    public String toString() {
        return name;
    }
}
