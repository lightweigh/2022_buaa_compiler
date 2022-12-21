package backend.mipsCode.instruction;

import backend.Address;
import backend.register.Reg;
import middle.quartercode.operand.primaryOpd.Immediate;

public class Load implements Instruction {
    // lw dst, offset(base)

    // lw $t1, -100($t2)
    // lw $t1, ($t2)
    // lw $t1, label
    // lw $t1, label($t2)
    // lw $t1, label + 10000
    private Reg dst = null;     // 目标寄存器
    private Reg base = null;       // 存放地址的寄存器 或者 标签
    private String label = null;
    private Immediate offset;
    private Reg regOffset = null;
    private Address addr;

    private String content;

    // lw $t1, -100($t2)
    // lw $t1, ($t2)
    public Load(Reg dst, Reg base, Immediate offset) {
        this.dst = dst;
        this.base = base;
        this.offset = offset;
        this.content = "lw " + dst.getName() + ", " +
                ((offset != null && offset.getValue() != 0) ? offset : "") +
                "(" + base.getName() + ")\n";
    }

    // lw $t1, label
    // lw $t1, label + 10000
    public Load(Reg dst, String label, Immediate offset) {
        this.dst = dst;
        this.label = label;
        this.offset = offset;
        this.content = "lw " + dst.getName() + ", " +
                label +
                ((offset != null && offset.getValue() != 0) ? "+ " + offset : "") +
                "\n";
    }

    // lw $t1, label($t2)
    public Load(Reg dst, String label, Reg regOffset) {
        this.dst = dst;
        this.label = label;
        this.regOffset = regOffset;
        this.content = "lw " + dst.getName() + ", " +
                label +
                "(" + regOffset.getName() + ")\n";
    }

    // lw $t1, 10000
    // lw $t1, offset($sp)
    public Load(Reg dst, Address addr) {
        this.dst = dst;
        this.addr = addr;
        if (addr.isRelative()) {
            // this.content = "lw " + dst.getName()+ ", " + (-4-addr.getAddr()) + "($fp)\n";
            this.content = "lw " + dst.getName()+ ", " + (-addr.getAddr()) + "($fp)\n";
        } else {
            this.content = "lw " + dst.getName() + ", " + addr.getAddr() + "\n";
        }
    }

    @Override
    public String toString() {
        return content;
    }
}
