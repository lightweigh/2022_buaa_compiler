package backend;

public class Address {
    private int addr;
    private boolean isRelative; // todo 应该是没有绝对的

    public Address(int addr, boolean isRelative) {
        this.addr = addr;
        this.isRelative = isRelative;
    }

    public int getAddr() {
        return addr;
    }

    public boolean isRelative() {
        return isRelative;
    }
}
