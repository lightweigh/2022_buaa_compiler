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

    public void procValue(String op, Immediate immediate) {
        switch (op) {
            case "*":
                this.value *= immediate.value;
                break;
            case "/":
                this.value /= immediate.value;
                break;
            case "%":
                this.value %= immediate.value;
                break;
            case "+":
                this.value += immediate.value;
                break;
            case "-":
                this.value -= immediate.value;
                break;
            default:
                break;
        }
    }

    public void procValue(boolean isNeg) {
        this.value = isNeg ? -this.value : this.value;
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
