package middle.quartercode;

import middle.VarName;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

public class BinaryCode implements MiddleCode {
    // // 双目运算
    // 对象本身就是dst
    private VarName name;
    private Operand src1;
    private Operand src2;
    private Op op;

    public BinaryCode(VarName name, Operand src1, Operand src2, String op) {
        this.name = name;
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
        return name;
    }

    @Override
    public void rename(VarName name) {
        this.name = name;
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
        return name + " = " + src1.getVarName() + op.getName() + src2.getVarName() + "\n";
    }
}
