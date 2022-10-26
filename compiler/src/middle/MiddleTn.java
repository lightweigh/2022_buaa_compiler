package middle;

public class MiddleTn {
    private int idx = 0;

    public String genTemporyName() {
        return "t"+idx++;
    }

    public void clear() {
        idx = 0;
    }
}
