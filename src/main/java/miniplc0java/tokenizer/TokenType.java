package miniplc0java.tokenizer;

public enum TokenType {
    //空
    None,

    //关键字
    FN_KW,      // fn
    LET_KW,     // let
    CONST_KW,   // const
    AS_KW,      // as
    WHILE_KW,   // while
    IF_KW,      // if
    ELSE_KW,    // else
    RETURN_KW,  // return
    BREAK_KW,   // break
    CONTINUE_KW,// continue

    //字面量
    UINT,   // digit+
    STRING, // '"' (string_regular_char | escape_sequence)* '"'


    //标识符
    IDENT,      // [_a-zA-Z] [_a-zA-Z0-9]*

    //运算符
    PLUS,     // +
    MINUS,    // -
    MUL,      // *
    DIV,      // /
    ASSIGN,   // =
    EQ,       // ==
    NEQ,      // !=
    LT,       // <
    GT,       // >
    LE,       // <=
    GE,       // >=
    L_PAREN,  // (
    R_PAREN,  // )
    L_BRACE,  // {
    R_BRACE,  // }
    ARROW,    // ->
    COMMA,    // ,
    COLON,    // :
    SEMICOLON,// ;

    //注释
    COMMENT,  // '//' regex(.*) '\n'

    //文件尾
    EOF;

    @Override
    public String toString() {
        switch (this) {
            case None:
                return "NullToken";
            case FN_KW:
                return "FN";
            case LET_KW:
                return "LET";
            case CONST_KW:
                return "CONST";
            case AS_KW:
                return "AS";
            case WHILE_KW:
                return "WHILE";
            case IF_KW:
                return "IF";
            case ELSE_KW:
                return "ELSE";
            case RETURN_KW:
                return "RETURN";
            case UINT:
                return "Uint";
            case STRING:
                return "STRING";
            case IDENT:
                return "IDENT";
            case PLUS:
                return "PLUS";
            case MINUS:
                return "MINUS";
            case MUL:
                return "MUL";
            case DIV:
                return "DIV";
            case ASSIGN:
                return "ASSIGN";
            case EQ:
                return "EQ";
            case NEQ:
                return "NEQ";
            case LT:
                return "LT";
            case GT:
                return "GT";
            case LE:
                return "LE";
            case GE:
                return "GE";
            case L_PAREN:
                return "LPAREN";
            case R_PAREN:
                return "RPAREN";
            case L_BRACE:
                return "LBRACE";
            case R_BRACE:
                return "RBRACE";
            case ARROW:
                return "ARROW";
            case COMMA:
                return "COMMA";
            case COLON:
                return "COLON";
            case SEMICOLON:
                return "SEMICOLON";
            case COMMENT:
                return "COMMENT";
            default:
                return "InvalidToken";
        }
    }
}
