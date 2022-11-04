package backend.mipsCode.global;

import backend.mipsCode.instruction.Instruction;

public class Text implements Instruction {
    @Override
    public String toString() {
        return ".text\n";
    }
}
