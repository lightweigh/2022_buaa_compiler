package backend.mipsCode.instruction;

import backend.register.Reg;
import middle.quartercode.operand.primaryOpd.Immediate;

public class Li implements Instruction {
    private Reg dst;
    private Immediate immediate;
    private String content;

    public Li(Reg dst, Immediate immediate) {
        this.dst = dst;
        this.immediate = immediate;
        this.content = "li " + dst.getName() + ", " + immediate + "\n";
    }

    @Override
    public String toString() {
        return content;
    }
}
