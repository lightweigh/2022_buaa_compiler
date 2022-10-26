package middle.quartercode.operand.primaryOpd;

public class RetOpd implements PrimaryOpd {
    // 调用函数后的返回值右值 i = foo();   // 这里分析的是LVal = Exp; 的右边
    private final String name = "RET";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
