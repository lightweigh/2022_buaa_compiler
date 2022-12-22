package middle.quartercode;

import middle.VarName;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;
import middle.quartercode.operand.primaryOpd.LValOpd;

public class BinaryCode implements MiddleCode {
    // // 双目运算
    // 对象本身就是dst
    private LValOpd dst;
    private Operand src1;
    private Operand src2;
    private Op op;

    public BinaryCode(LValOpd dst, Operand src1, Operand src2, String op) {
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
        switch (op) {
            case "*":
                this.op = Op.MUL;
                break;
            case "/":
                this.op = Op.DIV;
                break;
            case "%":
                this.op = Op.MOD;
                break;
            case "+":
                this.op = Op.ADD;
                break;
            case "-":
                this.op = Op.SUB;
                break;
        }
    }

    public Operand getSrc1() {
        return src1;
    }

    public Operand getSrc2() {
        return src2;
    }

    public Op getOp() {
        return op;
    }

    @Override
    public VarName getVarName() {
        return dst.getVarName();
    }

    @Override
    public void rename(VarName name) {
        this.dst.rename(name);
    }

    @Override
    public boolean isGlobalVar() {
        return getVarName().getDepth() == 0;
    }

    @Override
    public CodeType getCodeType() {
        return CodeType.BINARY;
    }

    @Override
    public String toString() {
        return dst + " = " + src1.getVarName() + op.getName() + src2.getVarName() + "\n";
    }
}
