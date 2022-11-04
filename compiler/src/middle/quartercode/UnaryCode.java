package middle.quartercode;

import middle.VarName;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

public class UnaryCode implements MiddleCode {
    // // 单目运算
    // 对象本身就是dst
    private VarName name;
    private Operand src;
    private Op op;

    public UnaryCode(VarName name, Operand src, Op op) {
        this.name = name;
        this.src = src;
        this.op = op;
    }

    public VarName getVarName() {
        return name;
    }

    @Override
    public void rename(VarName name) {
        this.name = name;
    }

    @Override
    public CodeType getCodeType() {
        return CodeType.UNARY;
    }

    public Operand getSrc() {
        return src;
    }

    public Op getOp() {
        return op;
    }

    @Override
    public boolean isGlobalVar() {
        return getVarName().getDepth() == 0;
    }

    @Override
    public String toString() {
        return name + " = " + op.getName() + src.getVarName() + "\n";
    }
}
