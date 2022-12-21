package backend.mipsCode;

public class Annotation implements MipsCode {
    private String label;

    public Annotation(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "# " + label + "---------------\n";
    }
}
