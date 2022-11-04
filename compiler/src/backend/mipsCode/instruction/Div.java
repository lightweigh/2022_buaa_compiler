package backend.mipsCode.instruction;

import backend.register.Reg;
import middle.quartercode.operand.primaryOpd.Immediate;

public class Div implements Instruction {
    private Reg dst;
    private Reg src1;
    private Reg src2;
    private Immediate immediate;
    private String content;

    public Div(Reg dst, Reg src1, Reg src2) {
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
        this.content = "div " + dst.getName() + ", " + src1.getName() + ", " + src2.getName() + "\n";
    }

    public Div(Reg dst, Reg src1, Immediate immediate) {
        this.dst = dst;
        this.src1 = src1;
        this.immediate = immediate;
        this.content = "div " + dst.getName() + ", " + src1.getName() + ", " + immediate + "\n";
    }

    public Div(Reg src1, Reg src2) {
        this.src1 = src1;
        this.src2 = src2;
        this.content = "div " + src1.getName() + ", " + src2.getName() + "\n";
        // 搭配 mfhi取余数，mflo取商
    }

    @Override
    public String toString() {
        return content;
    }
}
