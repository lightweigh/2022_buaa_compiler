package backend.mipsCode.instruction;

import backend.Address;
import backend.register.Reg;
import middle.quartercode.operand.primaryOpd.Immediate;

public class Store implements Instruction {
    private Reg src;
    private Reg base;
    private Immediate offset;
    private String label;
    private Reg regOffset;
    private Address addr;

    private String content;

    // sw $t1, -100($t2)
    // sw $t1, ($t2)
    public Store(Reg src, Reg base, Immediate offset) {
        this.src = src;
        this.base = base;
        this.offset = offset;
        this.content = "sw " + src.getName() + ", " +
                ((offset != null && offset.getValue() != 0) ? offset : "") +
                "(" + base.getName() + ")\n";
    }

    // sw $t1, label
    // sw $t1, label + 10000
    public Store(Reg src, String label, Immediate offset) {
        this.src = src;
        this.offset = offset;
        this.label = label;
        this.content = "sw " + src.getName() + ", " +
                label +
                ((offset != null && offset.getValue() != 0) ? " + " + offset : "") + "\n";
    }

    // sw $t1, label($t2)
    public Store(Reg src, String label, Reg regOffset) {
        this.src = src;
        this.label = label;
        this.regOffset = regOffset;
        this.content = "sw " + src.getName() + ", " +
                label + "(" + regOffset.getName() + ")\n";
    }

    // sw $t1, 1000000
    public Store(Reg src, Address addr) {
        this.src = src;
        this.addr = addr;
        if (addr.isRelative()) {
            this.content = "sw " + src.getName() + ", " + (-4-addr.getAddr()) + "($fp)\n";
        } else {
            this.content = "sw " + src.getName() + ", " + addr.getAddr() + "\n";
        }
    }

    @Override
    public String toString() {
        return content;
    }
}
