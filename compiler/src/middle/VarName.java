package middle;

import java.util.Objects;

public class VarName {
    private String name;
    private int depth;
    private String localName;
    private boolean dirty = false;    // 针对全局变量的

    private int colNum; // 是否是数组, 二维数组做参数,确定地址?  // 一维数组设为0  // 0维设置为-1
    private boolean isPtr;    //  决定了代码生成时,取内存里面的值还是说计算在内存里的地址(fp+offset) // isArray=false & colNum!=-1时表示的是函数形参a[]或a[][colNum]
    private int ref = 0;

    public VarName(String name, int depth) {
        this.localName = name;
        if (depth <= 0) {
            this.name = name;
        } else {
            this.name = name + "@" + depth;
        }
        this.depth = depth;
        this.colNum = -1;
        this.isPtr = false;
    }

    public VarName(String name, int depth, int colNum, boolean isPtr) {
        this.localName = name;
        if (depth <= 0) {
            this.name = name;
        } else {
            this.name = name + "@" + depth;
        }
        this.depth = depth;
        this.colNum = colNum;
        this.isPtr = isPtr;
    }

    public boolean isArray() {
        return colNum >= 0;
    }

    public boolean isPtr() {
        return isPtr;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public int getRef() {
        return ref;
    }

    public void addOrSubRef(int times) {
        this.ref = ref + times;
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
