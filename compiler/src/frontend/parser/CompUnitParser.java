package frontend.parser;

import frontend.Error;
import frontend.Lexer;
import frontend.grammar.CompUnit;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class CompUnitParser {
    private CompUnit compUnit;
    private boolean declParser = true;
    private boolean funcParser = true;

    public CompUnit getCompUnit() {
        return compUnit;
    }

    public void parser() {
        this.compUnit = new CompUnit();
        Token token0;
        Token token1;
        Token token2;

        //  ConstDecl   →   'const'     BType   Ident                   ...
        //  VarDecl     →   BType       Ident   { '[' ConstExp ']' }    ...
        //  FuncDef     →   FuncType    Ident   '('                     ...
        //  MainFuncDef →   'int'       'main'  '('                     ...

        while (declParser) {
            token0 = Lexer.tokenList.peek(0);
            token1 = Lexer.tokenList.peek(1);
            token2 = Lexer.tokenList.peek(2);
            if (token0.getRefType() == Token.Type.CONSTTK) {
                compUnit.addDecl();
            } else if (token0.getRefType() == Token.Type.INTTK) {
                if (token1.getRefType() == Token.Type.MAINTK) {
                    declParser = false;
                    funcParser = false;
                } else {
                    if (token2.getRefType() != Token.Type.LPARENT) {
                        compUnit.addDecl();
                    } else {
                        declParser = false;
                    }
                }
            } else {    // token0.getRefType() == Token.Type.VOIDTK
                declParser = false;
            }
        }

        while (funcParser) {
            token0 = Lexer.tokenList.peek(0);
            token1 = Lexer.tokenList.peek(1);
            if (token0.getRefType() == Token.Type.VOIDTK) {
                compUnit.addFuncDef();
            } else if (token0.getRefType() == Token.Type.INTTK) {
                if (token1.getRefType() == Token.Type.MAINTK) {
                    funcParser = false;
                } else {
                    compUnit.addFuncDef();
                }
            }
        }

        compUnit.mainParser();

        //  递归下降过程中的方法命名问题，不产生歧义和冲突

        //  逐个取Token，判断属于哪一个非终结符的产生式
        //  不好之处：Token被取出来了，再进入到对应的语法分析单元中后，还需判断从当前文法的哪一个位置开始分析，耦合度高。
        /*while (Lexer.tokenList.peek(0) != null) {
            Token token = Lexer.tokenList.poll();
            if (token.getRefType() == Token.Type.CONSTTK) {
                compUnit.addDecl();
            } else if (token.getRefType() == Token.Type.VOIDTK) {
                compUnit.addFuncDef();
            } else if (token.getRefType() == Token.Type.INTTK) {
                token = Lexer.tokenList.poll();
                if (token.getRefType() == Token.Type.MAINTK) {
                    compUnit.mainParser();
                } else {
                    Ident ident = (Ident) token;
                    token = Lexer.tokenList.peek();
                    if (token.getRefType() == Token.Type.LPARENT) {
                        compUnit.addFuncDef(ident);
                    } else {
                        compUnit.addDecl(ident);
                    }
                }
            }
        }*/
    }

    public void print(BufferedWriter output) throws IOException {
        compUnit.print(output);
        output.write("<CompUnit>\n");
    }
}
