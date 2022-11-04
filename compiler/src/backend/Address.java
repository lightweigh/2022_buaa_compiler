package backend;

public class Address {
    private int addr;
    private boolean isRelative;

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
