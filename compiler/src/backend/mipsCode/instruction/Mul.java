package backend.mipsCode.instruction;

import backend.register.Reg;
import middle.quartercode.operand.primaryOpd.Immediate;

public class Mul implements Instruction {
    private Reg dst;
    private Reg src1;
    private Reg src2;
    private Immediate immediate;
    private String content;

    public Mul(Reg dst, Reg src1, Reg src2) {
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
        this.content = "mul " + dst.getName() + ", " + src1.getName() + ", " + src2.getName() + "\n";
    }

    public Mul(Reg dst, Reg src1, Immediate immediate) {
        this.dst = dst;
        this.src1 = src1;
        this.immediate = immediate;
        this.content = "mul " + dst.getName() + ", " + src1.getName() + ", " + immediate + "\n";
    }

    @Override
    public String toString() {
        return content;
    }
}
