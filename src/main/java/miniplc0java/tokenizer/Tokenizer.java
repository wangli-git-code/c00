package miniplc0java.tokenizer;

//import jdk.nashorn.internal.ir.TernaryNode;
import miniplc0java.error.TokenizeError;
import miniplc0java.error.ErrorCode;
import miniplc0java.util.Pos;

public class Tokenizer {

    private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了
    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError 如果解析有异常则抛出
     */
    public Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char peek = it.peekChar();
        if (Character.isDigit(peek)) {
            return lexUInt();
        } else if (Character.isAlphabetic(peek)||peek=='_') {
            return lexIdentOrKeyword();
        } else if (peek=='\"'){
            return lexString();
        } else {
            Token token=lexOperatorOrUnknown();
            if (token==null) return nextToken();
            return token;
        }
    }
    private Token lexString() throws  TokenizeError{
        Pos begin=it.currentPos();
        it.nextChar();
        char ch;
        String temp=new String();
        while ((ch=it.peekChar())!='"'){
            if (it.isEOF()){
                throw new TokenizeError(ErrorCode.IncompleteString, begin);
            }
            if (ch=='\\'){
                it.nextChar();
                if ((ch=it.peekChar())=='\\'){
                    temp+='\\';
                }
                else if (ch=='\'') temp+='\'';
                else if (ch == '\"') temp += '\"';
                else if (ch == 'n') temp += '\n';
                else if (ch == 't') temp += '\t';
                else if (ch == 'r') temp += '\r';
                else throw new TokenizeError(ErrorCode.InvalidEscapeSequence, it.previousPos());
            }
            else {
                temp+=ch;
            }
            it.nextChar();
        }
        it.nextChar();
        return new Token(TokenType.STRING,temp,begin,it.currentPos());
    }
    private Token lexUInt() throws TokenizeError {
        // 请填空：
        // 直到查看下一个字符不是数字为止:
        Pos begin=it.currentPos();
        String temp=new String();
        temp+=it.nextChar();
        while(Character.isDigit(it.peekChar())){
            temp+=it.nextChar();
        }
        long a=0;
        a=Long.parseLong(temp);

        return new Token(TokenType.UINT, a, begin, it.currentPos());
        // -- 前进一个字符，并存储这个字符
        //
        // 解析存储的字符串为无符号整数
        // 解析成功则返回无符号整数类型的token，否则返回编译错误
        //
        // Token 的 Value 应填写数字的值

    }

    private Token lexIdentOrKeyword() throws TokenizeError {
        // 请填空：
        Pos begin=it.currentPos();
        String temp=new String();
        boolean is_ident=false;
        if (it.peekChar()=='_') is_ident=true;
        temp+=it.nextChar();
        if (Character.isDigit(it.peekChar())&&is_ident){
            throw new TokenizeError(ErrorCode.InvalidIdentifier,begin);
        }
        while (true){
            if (Character.isLetter(it.peekChar())){
                temp+=it.nextChar();
            }
            else if (Character.isDigit(it.peekChar())||it.peekChar()=='_'){
                is_ident=true;
                temp+=it.nextChar();
            }
            else break;
        }

        if (is_ident){
            return new Token(TokenType.IDENT,temp,begin,it.currentPos());
        }
        switch (temp){
            case "fn":
                return new Token(TokenType.FN_KW,temp,begin,it.currentPos());
            case "let":
                return new Token(TokenType.LET_KW,temp,begin,it.currentPos());
            case "const" :
                return new Token(TokenType.CONST_KW,temp,begin,it.currentPos());
            case "as" :
                return new Token(TokenType.AS_KW,temp,begin,it.currentPos());
            case "while" :
                return new Token(TokenType.WHILE_KW,temp,begin,it.currentPos());
            case "if" :
                return new Token(TokenType.IF_KW,temp,begin,it.currentPos());
            case "else" :
                return new Token(TokenType.ELSE_KW,temp,begin,it.currentPos());
            case "return" :
                return new Token(TokenType.RETURN_KW,temp,begin,it.currentPos());
            default:
                return new Token(TokenType.IDENT,temp,begin,it.currentPos());
        }
        // 直到查看下一个字符不是数字或字母为止:
        // -- 前进一个字符，并存储这个字符
        //
        // 尝试将存储的字符串解释为关键字
        // -- 如果是关键字，则返回关键字类型的 token
        // -- 否则，返回标识符
        //
        // Token 的 Value 应填写标识符或关键字的字符串
    }

    private Token lexOperatorOrUnknown() throws TokenizeError {
        switch (it.nextChar()) {
            case '+':
                return new Token(TokenType.PLUS, "+", it.previousPos(), it.currentPos());
            case '-':
                if (it.peekChar() == '>') {
                    it.nextChar();
                    return new Token(TokenType.ARROW, "->", it.previousPos(), it.currentPos());
                } else {
                    return new Token(TokenType.MINUS, "-", it.previousPos(), it.currentPos());
                }
            case '*':
                return new Token(TokenType.MUL, "*", it.previousPos(), it.currentPos());
            case '=':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.EQ, "==", it.previousPos(), it.currentPos());
                } else {
                    return new Token(TokenType.ASSIGN, "=", it.previousPos(), it.currentPos());
                }
            case '/':
                if (it.peekChar() == '/') {
                    skipComment();
                    return null;
                } else {
                    return new Token(TokenType.DIV, "/", it.previousPos(), it.currentPos());
                }
            case '!':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.NEQ, "!=", it.previousPos(), it.currentPos());
                } else {
                    throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
                }
            case '<':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.LE, "<=", it.previousPos(), it.currentPos());
                } else {
                    return new Token(TokenType.LT, "<", it.previousPos(), it.currentPos());
                }
            case '>':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.GE, ">=", it.previousPos(), it.currentPos());
                } else {
                    return new Token(TokenType.GT, ">", it.previousPos(), it.currentPos());
                }
            case '(':
                return new Token(TokenType.L_PAREN, "(", it.previousPos(), it.currentPos());
            case ')':
                return new Token(TokenType.R_PAREN, ")", it.previousPos(), it.currentPos());
            case '{':
                return new Token(TokenType.L_BRACE, "{", it.previousPos(), it.currentPos());
            case '}':
                return new Token(TokenType.R_BRACE, "}", it.previousPos(), it.currentPos());
            case ',':
                return new Token(TokenType.COMMA, ",", it.previousPos(), it.currentPos());
            case ':':
                return new Token(TokenType.COLON, ":", it.previousPos(), it.currentPos());
            case ';':
                return new Token(TokenType.SEMICOLON, ";", it.previousPos(), it.currentPos());
            default:
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }

    private void skipComment() {
        it.nextChar();
        while (it.peekChar() != '\n') it.nextChar();
    }
}