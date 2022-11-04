package backend.mipsCode.instruction;

import backend.register.Reg;

public class La implements Instruction {
    private Reg dst;
    private String label;
    private String content;

    public La(Reg dst, String label) {
        this.dst = dst;
        this.label = label;
        this.content = "la " + dst.getName() + ", " + label + "\n";
    }

    @Override
    public String toString() {
        return content;
    }
}
