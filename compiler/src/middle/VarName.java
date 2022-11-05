package middle;

import java.util.Objects;

public class VarName {
    private String name;
    private int depth;
    private String localName;
    private boolean dirty = false;    // 针对全局变量的

    public VarName(String name, int depth) {
        this.localName = name;
        if (depth <= 0) {
            this.name = name;
        } else {
            this.name = name + "@" + depth;
        }
        this.depth = depth;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public String getLocalName() {
        return localName;
    }

    public int getDepth() {
        return depth;
    }

    public boolean isGlobalVar() {
        return depth == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VarName varName = (VarName) o;
        return depth == varName.depth &&
                Objects.equals(name, varName.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, depth);
    }

    @Override
    public String toString() {
        return name;
    }
}
