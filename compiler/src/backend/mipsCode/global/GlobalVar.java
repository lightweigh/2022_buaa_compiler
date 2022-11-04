package backend.mipsCode.global;

import backend.mipsCode.MipsCode;
import middle.VarName;

import java.util.ArrayList;

public class GlobalVar implements MipsCode {
    private VarName name;
    private int type;
    private ArrayList<Integer> values;

    public GlobalVar(VarName name, int type, ArrayList<Integer> values) {
        this.name = name;
        this.type = type;
        this.values = values;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(": ");
        if (type == 0) {
            sb.append(".word ");
            if (values != null) {
                sb.append(values.get(0));
            } else {
                sb.append("0");
            }
        } else {
            sb.append(".space ").append(4 * values.size());
            for (Integer value : values) {
                sb.append(" ").append(value);
            }
        }
        sb.append("\n");
        return sb.toString();
    }
}
