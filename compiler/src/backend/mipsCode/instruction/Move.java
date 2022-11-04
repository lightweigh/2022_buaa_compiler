package backend.mipsCode.instruction;

import backend.register.Reg;

public class Move implements Instruction {
    // move $t0, $s0
    private Reg dst;
    private Reg src;

    private String content;

    public Move(Reg dst, Reg src) {
        this.dst = dst;
        this.src = src;
        this.content = "move " + dst.getName() + ", " + src.getName() + "\n";
    }

    @Override
    public String toString() {
        return content;
    }
}
