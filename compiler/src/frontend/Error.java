package frontend;

import frontend.token.Token;

public class Error {
    private char type;

    public static Token errorDetect(char type) {
        switch (type) {
            case ';':
                if (!Lexer.tokenList.equalPeekType(0, Token.Type.SEMICN)) {
                    //  TODO Error i
                    //  缺少分号
                    //  报错行号为分号前一个非终结符所在行号。
                    System.out.println("Error occurs!");
                    // assert false;
                    return null;
                } else {
                    return Lexer.tokenList.poll();   //  ';'
                }
            case ']':
                if (!Lexer.tokenList.equalPeekType(0, Token.Type.RBRACK)) {
                    // TODO Error k
                    // 缺少右中括号’]’
                    // 报错行号为右中括号前一个非终结符所在行号。
                    System.out.println("Error occurs!");
                    // return null;
                } else {
                    return Lexer.tokenList.poll();   // ']'
                }
            case 'b':
                // TODO Error b
                // 函数名或者变量名在当前作用域下重复定义。
                // 报错行号为<Ident>所在行数。
                break;

        }
        return null;
    }
}
