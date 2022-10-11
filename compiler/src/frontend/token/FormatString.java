package frontend.token;

import java.util.ArrayList;

public class FormatString extends Token {
    private ArrayList<Integer> formatChars = new ArrayList<>();
    private String inner;
    private boolean hasIllegalSym;

    public FormatString(Type refType, int line, String content) {
        super(refType, line, content);
        inner = content.substring(1, content.length() - 1);    // strip ""
        hasIllegalSym=false;
        /*if (inner.matches("[_a-zA-Z0-9]*"))
            //  十进制编码为32,33,40-126的ASCII字符，'\'（编码92）出现当且仅当为'\n'
            // ' ','!','(', ')',*/
        for (int i = 0; i < inner.length(); i++) {
            int c = inner.charAt(i);

            if (c == 92) {  // \
                if (i+1==inner.length() || inner.charAt(i+1) != 'n') {
                    hasIllegalSym=true;
                }
            }
            if (c == 37) {   // %
                if (i+1==inner.length() || inner.charAt(i+1) != 'd') {
                    hasIllegalSym=true;
                } else {
                    formatChars.add(i);
                }
            } else if (c != 32 && c != 33 && !(c>=40 && c <= 126)) {
                hasIllegalSym = true;
            }

        }
    }

    public boolean hasIllegalSym() {
        return hasIllegalSym;
    }

    public int getFormedCharNum() {
        return formatChars.size();
    }
}
