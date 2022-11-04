package backend.mipsCode.instruction;

import backend.register.Reg;

public class Mfhi implements Instruction {
    private Reg dst;
    private String content;

    public Mfhi(Reg dst) {
        this.dst = dst;
        this.content = "mfhi " + dst.getName() + "\n";
    }

    @Override
    public String toString() {
        return content;
    }
}
