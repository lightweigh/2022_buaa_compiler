package backend.mipsCode.instruction;

import backend.register.Reg;

public class Sll implements Instruction {
    // sll $t1, $t0, 10
    private Reg dst;
    private Reg src;
    private int offset;
    private String content;

    public Sll(Reg dst, Reg src, int offset) {
        this.dst = dst;
        this.src = src;
        this.offset = offset;
        this.content = "sll " + dst.getName() + ", " +
                src.getName() + ", " +
                offset + "\n";
    }

    @Override
    public String toString() {
        return content;
    }
}
