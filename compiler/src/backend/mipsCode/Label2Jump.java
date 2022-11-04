package backend.mipsCode;

public class Label2Jump implements MipsCode {
    private String label;

    public Label2Jump(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "\n" + label + ":\n";
    }
}
