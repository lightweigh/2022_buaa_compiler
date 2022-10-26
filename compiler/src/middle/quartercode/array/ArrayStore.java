package middle.quartercode.array;

import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;
import middle.quartercode.operand.primaryOpd.PrimaryOpd;

public class ArrayStore implements MiddleCode {
    private PrimaryOpd primaryOpd;
    private Operand src;

    public ArrayStore(PrimaryOpd primaryOpd, Operand src) {
        this.primaryOpd = primaryOpd;
        this.src = src;
    }

    @Override
    public String getName() {
        return primaryOpd.getName();
    }

    @Override
    public String toString() {
        return primaryOpd.toString() + " = " + src.getName() + "\n";
    }
}
