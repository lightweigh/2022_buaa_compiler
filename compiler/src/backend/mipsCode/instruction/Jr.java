package backend.mipsCode.instruction;

import backend.register.Reg;

public class Jr implements Instruction {
    private Reg reg;
    private String content;

    public Jr(Reg reg) {
        this.reg = reg;
        this.content = "jr " + reg.getName() + "\n";
    }

    @Override
    public String toString() {
        return content;
    }
}
