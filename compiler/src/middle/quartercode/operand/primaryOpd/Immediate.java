package middle.quartercode.operand.primaryOpd;

import middle.VarName;

public class Immediate implements PrimaryOpd {
    // 确定的常数值
    private int value;

    public Immediate(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * 不可变对象
     * @param op
     * @param value
     * @return
     */
    public Immediate procValue(String op, int value) {
        Immediate res = new Immediate(this.value);
        switch (op) {
            case "*":
                res.value = this.value * value;
                break;
            case "/":
                res.value = this.value / value;
                break;
            case "%":
                res.value = this.value % value;
                break;
            case "+":
                res.value = this.value + value;
                break;
            case "-":
                res.value = this.value - value;
                break;
            default:
                break;
        }
        return res;
    }

    public Immediate procValue(boolean isNeg) {
        return isNeg ? new Immediate(-this.value) : new Immediate(this.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public VarName getVarName() {
        return new VarName(String.valueOf(value), 0);
    }

    @Override
    public boolean isGlobalVar() {
        return getVarName().getDepth() == 0;
    }

    @Override
    public void rename(VarName name) {
    }
}
