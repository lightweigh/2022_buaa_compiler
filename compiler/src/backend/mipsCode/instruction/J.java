package backend.mipsCode.instruction;

public class J implements Instruction {
    private String label;
    private String content;

    public J(String label) {
        this.label = label;
        this.content = "j " + label + "\n";
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return content;
    }
}
