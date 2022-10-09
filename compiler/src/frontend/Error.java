package frontend;

import frontend.grammar.Component;
import frontend.grammar.MainFuncDef;
import frontend.grammar.funcDef.FuncDef;
import frontend.grammar.stmt.PrintfStmt;
import frontend.token.FormatString;
import frontend.token.Token;

import java.util.PriorityQueue;

public class Error implements Comparable {
    private ErrorType errorType;
    private int rowNum;
    public static PriorityQueue<Error> errorTable = new PriorityQueue<>();

    public Error(ErrorType errorType, int rowNum) {
        this.errorType = errorType;
        this.rowNum = rowNum;
    }

    /*@Override
    public int compare(Object o1, Object o2) {
        return ((Error) o1).rowNum - ((Error) o2).rowNum;
    }*/

    @Override
    public int compareTo(Object o) {
        return this.rowNum-((Error)o).rowNum;
    }


    public enum ErrorType {
        ILLEGAL_SYM('a'),   // 格式字符串中出现非法字符报错行号为<FormatString>所在行数。
        NAME_REDEF('b'),
        NAME_UNDEF('c'),
        WRONG_PARA_NUM('d'),
        WRONG_PARA_TYPE('e'),
        WRONG_VOID_RET('f'),
        MISS_INT_RET('g'),
        MODIFY_CONST('h'),
        MISS_SEM('i'),
        MISS_RPARENT('j'),
        MISS_RBACKET('k'),
        WRONG_PRINTF_EXP('l'),
        WRONG_BREAK_CONTINUE('m'),
        ;
        private char errorCode;

        ErrorType(char errorCode) {
            this.errorCode = errorCode;
        }

        public char getErrorCode() {
            return errorCode;
        }
    }

    public static void errorDetect(Component component, String type) {
        switch (type) {
            // printf, formatString 相关
            case "printfStmt":
                PrintfStmt printfStmt = (PrintfStmt) component;
                FormatString formatString = printfStmt.getFormatString();
                if (formatString.getFormedCharNum() != printfStmt.getExpNum()) {
                    errorTable.add(new Error(ErrorType.WRONG_PRINTF_EXP, printfStmt.getPrintfRow()));
                }
                if (printfStmt.getFormatString().isHasIllegalSym()) {
                    errorTable.add(new Error(ErrorType.ILLEGAL_SYM, printfStmt.getFormatString().getRow()));
                }
                break;

            case "funcDef":
                FuncDef funcDef = (FuncDef) component;
                if (!funcDef.needRet() && funcDef.hasRet()) {
                    errorTable.add(new Error(ErrorType.WRONG_VOID_RET, funcDef.getRetRow()));
                } else if (funcDef.needRet() && !funcDef.hasRet()) {
                    errorTable.add(new Error(ErrorType.MISS_INT_RET, funcDef.getRBraceRow()));
                }
                break;
            case "main":
                MainFuncDef mainFuncDef = (MainFuncDef) component;
                if (!mainFuncDef.hasRet()) {
                    errorTable.add(new Error(ErrorType.MISS_INT_RET, mainFuncDef.getRBraceRow()));
                }
            }
    }

    public static Token errorDetect(Token.Type type) {
        // 在获取所需 Token 之前进行检测, 若无错误则成功返回, 否则记录错误
        // i,j,k
        if (!Lexer.tokenList.equalPeekType(0, type)) {
            //  Error i,j,k
            Token prev = Lexer.tokenList.peek(-1);
            ErrorType errorType = type == Token.Type.SEMICN ? ErrorType.MISS_SEM :
                    type == Token.Type.RPARENT ? ErrorType.MISS_RPARENT : ErrorType.MISS_RBACKET;
            errorTable.add(new Error(errorType, prev.getRow()));
            // System.out.println("Error occurs!");
            return null;
        } else {
            return Lexer.tokenList.poll();   //  ; ) ]
        }
    }

    @Override
    public String toString() {
        return  rowNum+ " " +
                errorType.errorCode + "\n";
    }
}
