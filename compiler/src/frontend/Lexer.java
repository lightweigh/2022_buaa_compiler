package frontend;

import frontend.token.Ident;
import frontend.token.Token;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private List<String> lines = new ArrayList<>();

    private int raw = 0;
    private int col = 0;

    public Lexer(String file) {
        try {
            BufferedReader reader = new BufferedReader((new FileReader(file)));
            String tmp;
            while ((tmp = reader.readLine()) != null) {
                // tmp = tmp.trim();
                lines.add(tmp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getRaw() {
        return raw + 1;
    }

    public int getCol() {
        return col + 1;
    }

    public boolean isFileEnd() {
        return raw >= lines.size();
    }

    public String getCurLine() {
        if (!isFileEnd()) {
            return lines.get(raw);
        } else {
            return "";
        }
    }

    public boolean isLineEnd() {
        return col >= getCurLine().length();
    }

    public void nextLine() {
        if (!isFileEnd()) {
            raw++;
            col = 0;
        }
    }

    public void skip(int paces) {
        if (col + paces > getCurLine().length()) {
            raw++;
            col = 0;
            System.out.println("lexer:stepForward");
        } else {
            col += paces;
        }
    }

    public char getCurChar() {
        if (isFileEnd()) {
            return '\n';
        }
        if (isLineEnd()) {
            return 0;
        }
        return getCurLine().charAt(col);
    }

    public void skipBlanks() {
        while (!isFileEnd() && Character.isWhitespace(getCurChar())) {
            skip(1);
        }
    }

    public String peakString(int length) {
        if (isLineEnd()) {
            return "";
        }
        if (col + length > getCurLine().length()) {
            return getCurLine().substring(col);
        }
        return getCurLine().substring(col, col + length);
    }

    public TokenList GenTokenList() {
        TokenList tokenList = new TokenList();

        while(!isFileEnd()) {
            skipBlanks();
            if (peakString(2).equals("//")) {
                nextLine();
                continue;
            }
            if (peakString(2).equals("/*")) {
                skip(2);
                while(!isFileEnd() && !peakString(2).equals("*/")) {
                    skip(1);
                    if(isLineEnd()) {
                        nextLine();
                    }
                }
                if (peakString(2).equals("*/")) {
                    skip(2);
                    continue;
                }
            }
            if (!isLineEnd()) {
                Token token = getToken();
                if (token != null) {
                    tokenList.addToken(token);
                    skip(token.getContent().length());
                }
            } else {
                nextLine();
            }
        }
        return tokenList;
    }

    public Token getToken() {
        Token token = null;
        boolean getToken = false;
        for (Token.Type type : Token.Type.values()) {
            // System.out.println("type: " + type + " pattern: " +type.getPattern());
            Matcher matcher = type.getPattern().matcher(getCurLine().substring(col));
            if (matcher.find()) {
                // System.out.println(matcher.group(0) + " type: " + type);
                token = Token.createToken(type, matcher.group(0),getRaw());
                getToken = true;
                break;
            }
        }
        return token;
    }

}
