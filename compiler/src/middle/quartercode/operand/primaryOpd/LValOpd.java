package middle.quartercode.operand.primaryOpd;

import middle.quartercode.operand.Operand;

public class LValOpd implements PrimaryOpd {
    // a; a[t2]; a[1]
    private String name;
    private Operand idx;

    public LValOpd(String name, Operand idx) {
        this.name = name;
        this.idx = idx;
    }

    @Override
    public String getName() {
        return name;
    }

    public Operand getIdx() {
        return idx;
    }

    @Override
    public String toString() {
        return name + (idx != null ? "[" + idx.getName() + "]" : "");
    }
}
