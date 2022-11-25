package middle;

import java.util.Objects;

public class VarName {
    private String name;
    private int depth;
    private String localName;
    private boolean dirty = false;    // 针对全局变量的

    private int colNum; // 主要是用来 二维数组做参数,确定地址?  // 一维数组设为0  // 0维设置为-1

    public VarName(String name, int depth) {
        this.localName = name;
        if (depth <= 0) {
            this.name = name;
        } else {
            this.name = name + "@" + depth;
        }
        this.depth = depth;
        this.colNum = -1;
    }

    public VarName(String name, int depth, int colNum) {
        this.localName = name;
        if (depth <= 0) {
            this.name = name;
        } else {
            this.name = name + "@" + depth;
        }
        this.depth = depth;
        this.colNum = colNum;
    }

    public boolean isArray() {
        return colNum >= 0;
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

    public int getColNum() {
        return colNum;
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
