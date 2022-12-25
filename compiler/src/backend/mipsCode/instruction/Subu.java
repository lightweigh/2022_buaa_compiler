package backend.mipsCode.instruction;

import backend.register.Reg;
import middle.quartercode.operand.primaryOpd.Immediate;

public class Subu implements Instruction {
    private Reg dst;
    private Reg src1;
    private Reg src2;
    private String content;

    public Subu(Reg dst, Reg src1, Reg src2) {
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
        this.content = "subu " + dst.getName() + ", " +
                src1.getName() + ", " +
                src2.getName() + "\n";
    }

    @Override
    public String toString() {
        return content;
    }
}
