import backend.CodeGen;
import backend.mipsCode.MipsCode;
import frontend.Error;
import frontend.Lexer;
import frontend.TokenList;
import frontend.parser.CompUnitParser;
import frontend.visitor.Visitor;
import middle.BasicBlock;
import middle.FuncDefBlock;
import middle.quartercode.operand.MiddleCode;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) {
        Lexer lexer = new Lexer("testfile.txt");
        TokenList tokenList = lexer.GenTokenList();
        // tokenList.print();
        CompUnitParser compUnitParser = new CompUnitParser();
        compUnitParser.parser();
        Visitor visitor = new Visitor();
        visitor.visitCompUnit(compUnitParser.getCompUnit());
        /*try {
            BufferedWriter output = new BufferedWriter(new FileWriter("output.txt"));
            compUnitParser.print(output);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter("error.txt"));
            while (!Error.errorTable.isEmpty()) {
                Error error = Error.errorTable.poll();
                output.write(error.toString());
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 中间代码生成
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter("ir.txt"));
            ArrayList<MiddleCode> constStr = new ArrayList<>(visitor.getConstStr().values());
            for (MiddleCode middleCode : constStr) {
                output.write(middleCode.toString());
            }
            visitor.getGlobalBlock().output(output);
            for (FuncDefBlock funcDefBlock : visitor.getFuncDefBlocks()) {
                funcDefBlock.output(output);
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedWriter output = new BufferedWriter(new FileWriter("mips.txt"));

            CodeGen codeGen = new CodeGen(visitor.getConstStr(), visitor.getGlobalBlock(),
                    visitor.getFuncDefBBlocksMap(), visitor.getFuncDefBlocks());
            codeGen.mipsGen();
            for (MipsCode mipsCode : codeGen.getMipsCodes()) {
                output.write(mipsCode.toString());
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
