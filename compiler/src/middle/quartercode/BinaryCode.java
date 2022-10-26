package middle.quartercode;

import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

public class BinaryCode implements MiddleCode {
    // // 单目运算
    // 对象本身就是dst
    private String name;
    private Operand src1;
    private Operand src2;
    private Op op;

    public BinaryCode(String name, Operand src1, Operand src2, String op) {
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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " = " + src1.getName() + op.getName() + src2.getName() + "\n";
    }
}
