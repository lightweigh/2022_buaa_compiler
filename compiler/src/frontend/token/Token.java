package frontend.token;

import frontend.grammar.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.regex.Pattern;

public class Token implements Component {
    private Type refType;
    private int row;
//    private int col;
    private String content;

    public Token(Type refType, int row, String content) {
        this.refType = refType;
        this.row = row;
        this.content = content;
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        output.write(String.format("%s %s\n", refType, content));
    }

    public enum Type {

        MAINTK("main", true),
        CONSTTK("const", true),
        INTTK("int", true),
        BREAKTK("break", true),
        CONTINUETK("continue", true),
        IFTK("if", true),
        ELSETK("else", true),
        WHILETK("while", true),
        GETINTTK("getint", true),// todo: getint() ?
        PRINTFTK("printf", true),
        RETURNTK("return", true),
        VOIDTK("void", true),
        FORTK("for", true),

        IDENFR("[_a-zA-Z][_a-zA-Z0-9]*", false),
        INTCON("[0-9]+", false),
        STRCON("\\\".*?\\\"", false),    // todo


        LEQ("<=", false),
        LSS("<", false),
        GEQ(">=", false),
        GRE(">", false),
        EQL("==", false),
        NEQ("!=", false),

        NOT("!", false),
        OR("\\|\\|", false),
        AND("&&", false),

        PLUS("\\+", false),
        MINU("-", false),
        MULT("\\*", false),
        DIV("/", false),
        MOD("%", false),


        ASSIGN("=", false),
        SEMICN(";", false),
        COMMA(",", false),
        LPARENT("\\(", false),
        RPARENT("\\)", false),
        LBRACK("\\[", false),
        RBRACK("]", false),
        LBRACE("\\{", false),
        RBRACE("}", false);

        private Pattern pattern;

        Type(String pattern, boolean isTK) {
            if (isTK) {
                this.pattern = Pattern.compile("^" + pattern + "(?![_A-Za-z0-9]+)");
            } else {
                this.pattern = Pattern.compile("^" + pattern);
            }
        }

        public Pattern getPattern() {
            return pattern;
        }
    }

    public String getContent() {
        return content;
    }

    public Type getRefType() {
        return refType;
    }

    public int getRow() {
        return row;
    }

    public static Token createToken(Type refType, String content, int line) {
        switch (refType) {
            case IDENFR: return new Ident(refType,line,content);
            case INTCON: return new IntConst(refType,line,content);
            case STRCON: return new FormatString(refType,line,content);
            default: return new OtherToken(refType,line,content);
        }
    }

    @Override
    public String toString() {
        return String.format("%s %s\n", refType, content);
    }
}
