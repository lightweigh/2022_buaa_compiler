package middle.quartercode;

import middle.VarName;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;
import middle.quartercode.operand.primaryOpd.LValOpd;

public class UnaryCode implements MiddleCode {
    // // 单目运算
    // 对象本身就是dst
    private LValOpd dst;
    private Operand src;
    private Op op;

    public UnaryCode(LValOpd dst, Operand src, Op op) {
        this.dst = dst;
        this.src = src;
        this.op = op;
    }

    public VarName getVarName() {
        return dst.getVarName();
    }

    @Override
    public void rename(VarName name) {
        this.dst.rename(name);
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
        return dst + " = " + op.getName() + src.getVarName() + "\n";
    }
}
