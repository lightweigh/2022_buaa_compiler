package middle.quartercode;

import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

public class UnaryCode implements MiddleCode {
    // // 单目运算
    // 对象本身就是dst
    private String name;
    private Operand src;
    private Op op;

    public UnaryCode(String name, Operand src, Op op) {
        this.name = name;
        this.src = src;
        this.op = op;
    }

    public String getName() {
        return name;
    }

    public Operand getSrc() {
        return src;
    }

    public Op getOp() {
        return op;
    }

    @Override
    public String toString() {
        return name + " = " + op.getName() + src.getName() + "\n";
    }
}
