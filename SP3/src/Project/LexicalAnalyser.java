package Project;

import java.util.*;


public class LexicalAnalyser {

    private static final List<String> keywords = List.of(
            "and","and_eq", "auto", "bitand","bitor","bool","break","case","catch","char",
             "class","compl","const", "const_cast","continue","default", "delete","do","double",
            "dynamic_cast", "else","enum", "explicit", "export", "extern", "false","float","for",
            "friend","goto","if", "inline","int","long", "mutable","namespace", "new", "not", "not_eq",
            "operator", "or", "or_eq","private","protected","public", "register", "reinterpret_cast",
            "return","short", "signed","sizeof","static", "static_cast", "struct", "switch", "template",
            "this","throw", "true","try", "typedef", "typeid", "typename", "union", "unsigned", "using",
            "virtual","void","volatile", "wchar_t","while","xor", "xor_eq"

    );

    private static int lineNumber;
    private static boolean ManyLinesComment;

    private String line;
    private Integer currentIndex;
    private char peek;

    public LexicalAnalyser() {
        this.peek = ' ';
        lineNumber = 0;
        ManyLinesComment = false;
    }

    public void setLine(String lineOfCode) {
        this.currentIndex = 0;
        this.line = lineOfCode;
        this.peek = ' ';
        line += "\n";
        ++lineNumber;
    }

    public Token scan() {
        if (ManyLinesComment)
            return processManyLinesComment();

        for (;;getNextChar()) {
            if (this.peek == ' ' || this.peek == '\t')
                continue;
            else
            if (this.peek == '\n')
                ++lineNumber;
            else
                break;
        }

        Token token;

        switch (this.peek) {
            case '|' -> {
                if (getNextChar('|')) {
                    token = new Token(LexemeType.OR, "||");
                    move();
                    return token;
                }
            }
            case '&' -> {
                if (getNextChar('&')) {
                    token = new Token(LexemeType.AND, "&&");
                    move();
                    return token;
                }
            }
            case '{' -> {
                token = new Token(LexemeType.OPEN_CURLY_BRACE, peek);
                move();
                return token;
            }
            case '}' -> {
                token = new Token(LexemeType.CLOSE_CURLY_BRACE, peek);
                move();
                return token;
            }
            case '[' -> {
                token = new Token(LexemeType.OPEN_SQUARE_BRACE, peek);
                move();
                return token;
            }
            case ']' -> {
                token = new Token(LexemeType.CLOSE_SQUARE_BRACE, peek);
                move();
                return token;
            }
            case '(' -> {
                token = new Token(LexemeType.OPEN_PARENTHESIS, peek);
                move();
                return token;
            }
            case ')' -> {
                token = new Token(LexemeType.CLOSE_PARENTHESIS, peek);
                move();
                return token;
            }
            case '<' -> {
                if (getNextChar('=')) {
                    token = new Token(LexemeType.LESS_OR_EQUAL, "<=");
                } else {
                    token = new Token(LexemeType.LESS, '<');
                }
                //move();
                return token;
            }
            case '>' -> {
                if (getNextChar('=')) {
                    token = new Token(LexemeType.GREATER_OR_EQUAL, ">=");
                } else {
                    token = new Token(LexemeType.GREATER, '>');
                }
                move();
                return token;
            }
            case '=' -> {
                if (getNextChar('=')) {
                    token = new Token(LexemeType.EQUAL, "==");
                } else {
                    token = new Token(LexemeType.ASSIGNMENT, '=');
                }
                move();
                return token;
            }
            case ':' -> {
                token = new Token(LexemeType.COLON, ':');
                move();
                return token;
            }
            case ';' -> {
                token = new Token(LexemeType.SEMICOLON, peek);
                move();
                return token;
            }
            case ',' -> {
                token = new Token(LexemeType.COMA, peek);
                move();
                return token;
            }
            case '.' -> {
                token = new Token(LexemeType.DOT, peek);
                move();
                return token;
            }
            case '+' -> {
                token = new Token(LexemeType.PLUS, peek);
                move();
                return token;
            }
            case '@' -> {
                token = new Token(LexemeType.DECORATOR, peek);
                move();
                return token;
            }
            case '#' -> {
               // StringBuilder builder = new StringBuilder();
                token = new Token(LexemeType.PREPROCESSOR_DIRECTIVE, peek);
                if (getNextChar ('i'))
                {
                    if (getNextChar ('n'))
                    {
                        if (getNextChar ('c'))
                        {
                            if (getNextChar ('l'))
                            {if (getNextChar ('u'))
                                { if (getNextChar ('d'))
                                    if (getNextChar ('e'))
                                    {
                                        token = new Token(LexemeType.PREPROCESSOR_DIRECTIVE, "#include");
                                    }
                                }}}}}
                if(getNextChar('d'))
                {
                    if(getNextChar('e'))
                    {
                        if(getNextChar('f'))
                        {
                            if(getNextChar('i'))
                            {
                                if(getNextChar('n'))
                            {
                                if(getNextChar('e'))
                            {
                                token = new Token(LexemeType.PREPROCESSOR_DIRECTIVE, "#define");}}}

                        }
                    }
                }
                move();
                return token;
            }
            case '-' -> {
                token = new Token(LexemeType.MINUS, peek);
                move();
                return token;
            }
            case '*' -> {
                token = new Token(LexemeType.MULTIPLICATION, peek);
                move();
                return token;
            }
            case '/' -> {
                if (getNextChar('/' )) {
                    token = new Token(LexemeType.MANY_LINE_COMMENT, "//" );
                processManyLinesComment();}

                else {token = new Token(LexemeType.SLASH, '/');}
                move();
                return token;
            }
            case '\"' -> {
                token = processStringLiteral('\"');
                move();
                return token;
            }
            case '\'' -> {
                token = processStringLiteral('\'');
                move();
                return token;
            }
            case '!' -> {
                if (getNextChar('=')) {
                    token = new Token(LexemeType.NOT_EQUAL, "!=");
                } else {
                    token = new Token(LexemeType.NOT, '!');
                }
                move();
                return token;
            }
        }
        if (Character.isDigit(peek)) {
            return processNumber();
        }
        if (Character.isLetter(peek)) {
            return processWord();
        }
        return processError("");
    }

    private Token processError(String initialError) {
        StringBuilder error = new StringBuilder(initialError);
        while(!Character.isWhitespace(peek) || peek != '\n') {
            error.append(peek);
            getNextChar();
        }
        return new Token(LexemeType.LEXICAL_ERROR, error.toString());
    }

    private void getNextChar() {
        if (currentIndex < line.length()) {
            this.peek = line.charAt(currentIndex);
        }
        ++currentIndex;
    }

    private boolean getNextChar(char c) {
        getNextChar();
        return this.peek == c;
    }

    private Token processNumber() {
        int integerLiteral = 0;
        StringBuilder hexOrByLiteral = new StringBuilder();
        do {
            if (peek == 'x' || peek == 'b') {
                hexOrByLiteral.append(integerLiteral);
                do {
                    hexOrByLiteral.append(peek);
                    getNextChar();
                } while (Character.isLetterOrDigit(peek));
                return new Token(LexemeType.INTEGER, hexOrByLiteral.toString());
            } else if ((peek != 'x' | peek != 'b') && Character.isLetter(peek)) {
                return processError(Integer.toString(integerLiteral));
            }
            integerLiteral = 10 * integerLiteral + Character.digit(peek, 10);
            getNextChar();
        } while (Character.isLetterOrDigit(peek));

        if (peek != '.')
            return new Token(LexemeType.INTEGER, integerLiteral);

        double floatingPointLiteral = (double) integerLiteral;
        int divisor = 10;

        getNextChar();

        while (Character.isDigit(peek)) {
            floatingPointLiteral += Double.parseDouble(String.valueOf(peek)) / divisor;
            divisor *= 10;
            getNextChar();
        }
        return new Token(LexemeType.FLOATING_POINT_LITERAL, floatingPointLiteral);
    }

    private Token processWord() {
        StringBuilder word = new StringBuilder();
        do {
            word.append(peek);
            getNextChar();
        } while (Character.isLetterOrDigit(peek) || peek == '_');
        if (keywords.contains(word.toString())) {
            return new Token(LexemeType.KEYWORD, word.toString());
        } else {
            return new Token(LexemeType.IDENTIFIER, word.toString());
        }
    }

    public boolean reachEndOfTheLine() {
        return currentIndex >= line.length();
    }

    private void move() {
        getNextChar();
    }

    private Token processStringLiteral(char sep) {
        StringBuilder literal = new StringBuilder();
        literal.append(sep);
        do {
            getNextChar();
            literal.append(this.peek);
        } while (this.peek != sep);
        getNextChar();
        return new Token(LexemeType.STRING, literal.toString());
    }

    private Token processManyLinesComment() {
        line += "\n";
        StringBuilder builder = new StringBuilder();
        do {
            getNextChar();
            builder.append(this.peek);
            if (this.peek == '*' && getNextChar('/')) {
                ManyLinesComment = false;
                break;
            }
        } while (this.peek != '\n');
        currentIndex = line.length() + 1;
        return new Token(LexemeType.MANY_LINE_COMMENT, builder.toString());
    }

}
