package backend.mipsCode.instruction;

import backend.register.Reg;

public class Jal implements Instruction {
    private String label;
    private String content;

    public Jal(String label) {
        this.label = label;
        this.content = "jal " + label + "\n";
    }

    @Override
    public String toString() {
        return content;
    }
}
