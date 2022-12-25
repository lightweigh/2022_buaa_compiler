package backend.mipsCode.instruction;

import backend.register.Reg;

public class Mflo implements Instruction {
    private Reg dst;
    private String content;

    public Mflo(Reg dst) {
        this.dst = dst;
        this.content = "mflo " + dst.getName() + "\n";
    }

    @Override
    public String toString() {
        return content;
    }
}
