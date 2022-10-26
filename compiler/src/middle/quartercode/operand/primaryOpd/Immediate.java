package middle.quartercode.operand.primaryOpd;

import frontend.grammar.exp.Operator;

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
    public String getName() {
        return String.valueOf(value);
    }
}
