package frontend;

import java.util.ArrayList;
import java.util.List;

public class Line {

    private int lineNo;
    private String sentence;

    public Line(int lineNo, String sentence) {
        this.lineNo = lineNo;
        this.sentence = sentence;
    }

    @Override
    public String toString() {
        return sentence;
    }
}
