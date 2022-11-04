package backend.mipsCode.instruction;

import backend.register.Reg;

public class Neg implements Instruction {
    private Reg dst;
    private Reg src;
    private String content;

    public Neg(Reg dst, Reg src) {
        this.dst = dst;
        this.src = src;
        this.content = "neg " + dst.getName() + ", " + src.getName() + "\n";
    }

    @Override
    public String toString() {
        return content;
    }
}
