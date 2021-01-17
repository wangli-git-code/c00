package miniplc0java.analyser;



import miniplc0java.error.*;
import miniplc0java.instruction.Instruction;
import miniplc0java.instruction.Operation;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.util.Fuzhu;
import miniplc0java.util.OperatorPrecedence;
import miniplc0java.struct.functionDef;
import miniplc0java.struct.globalDef;
import miniplc0java.struct.parameter;

import java.util.*;

public final class Analyser {

    Tokenizer tokenizer;

    /** 当前偷看的 token */
    Token peekedToken = null;

    /** 符号表 */
    List<SymbolEntry> symbolTable = new ArrayList<>();

    /** 函数表 */
    HashMap<String, functionDef> functionTable=new HashMap<>();

    /**全局表*/
    List<globalDef> globalTable = new ArrayList<>();

    /**指令集*/
    List<Instruction> instructionList;

    /**全局指令集*/
    List<Instruction> global_instructionList=new ArrayList<>();

    /** 全局变量偏移 */
    int global_offset=0;

    /** 函数ID */
    int function_id=0;

    /**参数偏移*/
    int parameter_offset=0;

    /**当前函数参数列表*/
    List<parameter> parameters=new ArrayList<>();
    /** 是否返回值为void */
    boolean is_void=false;

    /**函数是否有返回 */
    boolean has_return;

    /**返回值的类型*/
    String return_type= "";

    /**
     *  用于存储函数的局部变量大小
     *  以及同时用于表达局部变量在栈中的偏移
     * */
    int loc_slots = 0;


    /** 用于存储函数的返回值大小 */
    int return_slots = 0;

    /** 创建一个符号栈 */
    Stack<TokenType> stack = new Stack<>();

    /**
     *  语句、表达式、函数当前的层次
     *  0层即为全局变量
     * */
    int level = 0;
    public List<globalDef> getGlobalTable() {
        return globalTable;
    }
    public HashMap<String, functionDef> getFunctionTable() {
        return functionTable;
    }
    int[][] priority = OperatorPrecedence.getPriority();
    /**
     * 查看下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = tokenizer.nextToken();
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            Token token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            return tokenizer.nextToken();
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     *
     * @param tt
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        Token token = peek();
        return ((Token) token).getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     *
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        Token token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     *
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        Token token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructionList = new ArrayList<>();
    }

    public List<Instruction> analyse() throws CompileError {
        analyseProgram();
        return instructionList;
    }

    public void analyseProgram() throws CompileError {
        while(check(TokenType.FN_KW)||check(TokenType.LET_KW)||check(TokenType.CONST_KW)){
            analyseItem();
        }
        /* 查找main函数 */
        functionDef main = functionTable.get("main");
        if(main==null){
            throw new AnalyzeError(ErrorCode.NoMain);
        }

        globalTable.add(new globalDef("_start", 1, Fuzhu.ChangeToBinary("_start")));
        Instruction tmp = new Instruction(Operation.stackalloc, 0,4);
        global_instructionList.add(tmp);
        if(main.getType().equals("int") ||main.getType()=="double"){
            tmp.setX(1);
            global_instructionList.add(new Instruction(Operation.call, function_id - 1,4));
            global_instructionList.add(new Instruction(Operation.popn, 1,4));
        }else{
            global_instructionList.add(new Instruction(Operation.call, function_id - 1,4));
        }
        functionTable.put("_start", new functionDef(global_offset, 0, 0, 0, global_instructionList,
                0, "void","_start", Fuzhu.ChangeToBinary("_start"), null));
        global_offset++;
    }

    private void analyseItem() throws CompileError {
        Token var = peek();
        if(check(TokenType.FN_KW)){
            analyseFunction();
        }else if(check(TokenType.LET_KW)||check(TokenType.CONST_KW)){
            analyseDeclStmt();
        }else{
            throw new AnalyzeError(ErrorCode.InvalidInput, var.getStartPos());
        }
    }

    private void analyseDeclStmt() throws CompileError{
        if (check(TokenType.LET_KW)){
            expect(TokenType.LET_KW);
            Token temp =expect(TokenType.IDENT);
            if (Fuzhu.isDefinedSymbol(functionTable,symbolTable,level,temp.getValueString())|| Fuzhu.isParameter(parameters,temp.getValueString())!=null){
                throw new AnalyzeError(ErrorCode.DuplicateDeclaration, temp.getStartPos());
            }
            expect(TokenType.COLON);
            String ty=analyseTy();
            if (level==0){
                symbolTable.add(new SymbolEntry(false, false, temp.getValueString(), level, ty, global_offset));
                globalTable.add( new globalDef(temp.getValueString(), 0));
            }
            else{
                symbolTable.add(new SymbolEntry(false,false,temp.getValueString(),level,ty,loc_slots));
            }
            if (check(TokenType.ASSIGN)){
                expect(TokenType.ASSIGN);
                Fuzhu.initializeSymbol(symbolTable,temp.getValueString(),level,temp.getStartPos());
                if (level==0) {global_instructionList.add(new Instruction(Operation.globa,global_offset,4));}
                else {
                    instructionList.add(new Instruction(Operation.loca,loc_slots,4));
                }
                String type=analyseExpression();
                if (!type.equals(ty)){
                    throw  new AnalyzeError(ErrorCode.InvalidAssignment,temp.getStartPos());
                }
                while (!stack.empty()){

                    Instruction.AddToInstructionListInt(stack.pop(),instructionList);
                }
                if (level==0){
                    global_instructionList.add(new Instruction(Operation.store));
                }
                else {
                    instructionList.add(new Instruction(Operation.store));
                }
            }
            expect(TokenType.SEMICOLON);
        }
        else if (check(TokenType.CONST_KW)){
            expect(TokenType.CONST_KW);
            Token temp = expect(TokenType.IDENT);
            if (Fuzhu.isDefinedSymbol(functionTable,symbolTable,level,temp.getValueString())){
                throw new AnalyzeError(ErrorCode.DuplicateDeclaration,temp.getStartPos());
            }
            expect(TokenType.COLON);
            String ty=analyseTy();
            expect(TokenType.ASSIGN);
            if (level==0){
                symbolTable.add(new SymbolEntry(true, true, temp.getValueString(), level, ty, global_offset));
                globalTable.add(new globalDef(temp.getValueString(),1));
                global_instructionList.add(new Instruction(Operation.globa,global_offset,4));
            }
            else
            {
                symbolTable.add(new SymbolEntry(true,true,temp.getValueString(),level,ty,loc_slots));
                instructionList.add(new Instruction(Operation.loca,loc_slots,4));
            }
            String type = analyseExpression();
            if (!type.equals(ty)){
                throw  new AnalyzeError(ErrorCode.InvalidAssignment,temp.getStartPos());
            }
            while (!stack.empty()){

                Instruction.AddToInstructionListInt(stack.pop(),instructionList);
            }
            if (level==0){
                global_instructionList.add(new Instruction(Operation.store));
            }
            else {
                instructionList.add(new Instruction(Operation.store));
            }
            expect(TokenType.SEMICOLON);
        }
        if (level==0){
            global_offset++;
        }
        else{
            loc_slots++;
        }
    }

    private String analyseExpression() throws CompileError{
        String type="";
        if (check(TokenType.IDENT)){
            type=analyseAssign_Call_IdentExpression();
        }
        else if (check(TokenType.MINUS)){
            type=analyseNegativeExpression();
        }
        else if (check(TokenType.L_PAREN)){
            type=analyseGroupExpression();
        }
        else if (isLiteralExpr()){
            Token temp=next();
            if (temp.getTokenType()==TokenType.UINT){
                if (level==0){
                    global_instructionList.add(new Instruction(Operation.push,(Long)temp.getValue(),8));
                }else {
                    instructionList.add(new Instruction(Operation.push,(Long)temp.getValue(),8));
                }

                type="int";
            }
            else if (temp.getTokenType()==TokenType.STRING){
                globalTable.add(new globalDef(temp.getValueString(),1, Fuzhu.ChangeToBinary(temp.getValueString())));
                if (level==0){
                    global_instructionList.add(new Instruction(Operation.push,global_offset,8));
                }else {
                    instructionList.add(new Instruction(Operation.push,global_offset,8));
                }
                global_offset++;
                type="int";
            }
        }
        while (isBinaryOperator()||check(TokenType.AS_KW)){
            if (isBinaryOperator()){
                Token temp=next();
                if (!stack.empty()){
                    int pre = OperatorPrecedence.getOrder(stack.peek());
                    int next = OperatorPrecedence.getOrder(temp.getTokenType());
                    if (priority[pre][next]>0){
                        TokenType type1 = stack.pop();
                        Instruction.AddToInstructionListInt(type1,instructionList);
                    }
                }
                stack.push(temp.getTokenType());
                String type2=analyseExpression();
                if (!type.equals(type2)){
                    throw  new AnalyzeError(ErrorCode.TypeError,temp.getStartPos());
                }
            }
            else{
                Token temp=peek();
                if (!type.equals("int")){
                    throw new AnalyzeError(ErrorCode.TypeError,temp.getStartPos());
                }
                expect(TokenType.AS_KW);
                analyseTy();
            }
        }
        return type;
    }
    private String analyseGroupExpression() throws CompileError{
        expect(TokenType.L_PAREN);
        String type="";
        stack.push(TokenType.L_PAREN);
        type=analyseExpression();
        expect(TokenType.R_PAREN);
        while (stack.peek() != TokenType.L_PAREN) {

            Instruction.AddToInstructionListInt(stack.pop(), instructionList);
        }
        stack.pop();
        return type;
    }
    private String analyseNegativeExpression() throws CompileError{
        expect(TokenType.MINUS);
        String type="";
        Token temp=peek();
        type=analyseExpression();
        if (type.equalsIgnoreCase("int")){
            if (level==0){
                global_instructionList.add(new Instruction(Operation.neg));
            }else {
                instructionList.add(new Instruction(Operation.neg));
            }
        }
        else {
            throw new AnalyzeError(ErrorCode.TypeError, temp.getStartPos());

        }
        return type;
    }
    private String analyseAssign_Call_IdentExpression()throws CompileError{
        String Type1 = "";
        Token tmp = expect(TokenType.IDENT);

        //偷偷查看下一个token
        Token var = peek();
        if(check(TokenType.ASSIGN)){
            expect(TokenType.ASSIGN);
            String type = "";
            /* 在当层或之前层查找该变量 */
            SymbolEntry symbol = Fuzhu.CanBeUsed(symbolTable, level, tmp.getValueString());
            /* 在函数列表中查找该变量 */
            parameter param = Fuzhu.isParameter(parameters, tmp.getValueString());
            /* 找不到该变量 */
            if(symbol==null&&param==null){
                throw new AnalyzeError(ErrorCode.NotDeclared, tmp.getStartPos());
            }
            else {
                //为符号(首先查找局部变量)
                if(symbol!=null&&symbol.getLevel()>0) {
                    type = symbol.getType();
                    /* 该变量类型与表达式类型不同,或l_expr的类型为void */
                    if (type.equals("void")) {
                        throw new AnalyzeError(ErrorCode.InvalidAssignment, tmp.getStartPos());
                    }
                    /* 找到一个常量 */
                    else if (symbol.isConstant() == true) {
                        throw new AnalyzeError(ErrorCode.AssignToConstant, tmp.getStartPos());
                    }
                    /* 都不是 */
                    else {
                        instructionList.add(new Instruction(Operation.loca, symbol.getStackOffset(), 4));
                    }
                }
                //为参数
                else if (param!=null){
                    type = param.getType();
                    if (type.equals("void")) {
                        throw new AnalyzeError(ErrorCode.InvalidAssignment, tmp.getStartPos());
                    }
                    int offset = Fuzhu.getParamOffset(param.getName(), parameters);
                    instructionList.add(new Instruction(Operation.arga,parameter_offset+offset, 4));
                }else if(symbol.getLevel() == 0){
                    type = symbol.getType();
                    /* 该变量类型与表达式类型不同,或l_expr的类型为void */
                    if (type.equals("void")) {
                        throw new AnalyzeError(ErrorCode.InvalidAssignment, tmp.getStartPos());
                    }
                    /* 找到一个常量 */
                    else if (symbol.isConstant() == true) {
                        throw new AnalyzeError(ErrorCode.AssignToConstant, tmp.getStartPos());
                    }
                    /* 都不是 */
                    else {
                        instructionList.add(new Instruction(Operation.globa, symbol.getStackOffset(), 4));
                    }
                }
            }

            String Type2 = "";
            if(check(TokenType.MINUS)||check(TokenType.IDENT)||check(TokenType.L_PAREN)||isLiteralExpr()) {
                Type2 = analyseExpression();
            }

            if(!type.equals(Type2)){
                throw new AnalyzeError(ErrorCode.InvalidAssignment, tmp.getStartPos());
            }

            while (!stack.empty()) {
                if(type.equals("int")) {
                    Instruction.AddToInstructionListInt(stack.pop(), instructionList);
                }
            }

            instructionList.add(new Instruction(Operation.store));

            Type1 = "void";

        }

        else if(check(TokenType.L_PAREN)){
            expect(TokenType.L_PAREN);

            stack.push(TokenType.L_PAREN);
            /* 判断是否为已经定义的函数或库函数 */
            functionDef function = functionTable.get(tmp.getValueString());
            Instruction instruction;

            if(function!=null||Fuzhu.isLibraryFunction(tmp.getValueString())){
                int offset;

                /* 库函数 */
                if(Fuzhu.isLibraryFunction(tmp.getValueString())){
                    offset=global_offset;
                    //库函数允许重复，直接添加进全局
                    globalTable.add(new globalDef(tmp.getValueString(),1, Fuzhu.ChangeToBinary(tmp.getValueString())));
                    global_offset++;
                    instruction = new Instruction(Operation.callname, offset, 4);

                    Type1 = Fuzhu.TypeReturnOfLibrary(tmp.getValueString());
                }
                /* 为自己创建的函数 */
                /* 非库函数才需要返回值空间 */
                else{
                    offset = function.getFunction_id();
                    instruction = new Instruction(Operation.call,offset, 4);

                    Type1 = function.getType();
                }
            }else{
                throw new AnalyzeError(ErrorCode.NotDeclared, tmp.getStartPos());
            }

            if(Fuzhu.hasReturn(tmp.getValueString(), functionTable)){
                instructionList.add(new Instruction(Operation.stackalloc,1, 4));
            }else{
                instructionList.add(new Instruction(Operation.stackalloc,0, 4));
            }


            if(check(TokenType.MINUS)||check(TokenType.IDENT)||check(TokenType.L_PAREN)||isLiteralExpr()) {
                /* 函数参数均正确 */
                analyseCallParamList(tmp.getValueString());
            }
            expect(TokenType.R_PAREN);


            //弹栈
            while (stack.peek() != TokenType.L_PAREN) {
                TokenType tokenType = stack.pop();
                Instruction.AddToInstructionListInt(tokenType, instructionList);
            }
            stack.pop();

            instructionList.add(instruction);

        }

        else{
            SymbolEntry symbol = Fuzhu.CanBeUsed(symbolTable,level,tmp.getValueString());
            parameter parameter = Fuzhu.isParameter(parameters, tmp.getValueString());
            if (symbol==null&&parameter==null)
                throw new AnalyzeError(ErrorCode.NotDeclared,tmp.getStartPos());
            Instruction instruction;

            int id;
            //参数
            if (parameter!=null) {
                id = Fuzhu.getParamOffset(parameter.getName(), parameters);
                instruction = new Instruction(Operation.arga, parameter_offset + id,4);
                instructionList.add(instruction);
                Type1 = parameter.getType();
            }
            //变量
            else {
                /* 全局 */
                if(symbol.getLevel()>0){
                    id = symbol.getStackOffset();
                    instruction = new Instruction(Operation.loca, id,4);
                    instructionList.add(instruction);
                }
                /* 局部 */
                else {
                    id = symbol.getStackOffset();
                    instruction = new Instruction(Operation.globa, id,4);
                    instructionList.add(instruction);
                }
                Type1 = symbol.getType();
            }
            instructionList.add(new Instruction(Operation.load));
        }
        return Type1;
    }

    private int analyseCallParamList(String name) throws CompileError{
        List<String> list = new ArrayList<>();
        int num=0;
        String type=analyseExpression();
        list.add(type);
        while (!stack.empty()&&stack.peek()!=TokenType.L_PAREN){
            Instruction.AddToInstructionListInt(stack.pop(),instructionList);
        }
        num++;
        while (nextIf(TokenType.COMMA)!=null){
            type=analyseExpression();
            list.add(type);
            while (!stack.empty()&&stack.peek()!=TokenType.L_PAREN){
                Instruction.AddToInstructionListInt(stack.pop(),instructionList);
            }
            num++;
        }
        List<String> plist= Fuzhu.TypeReturn(name,functionTable);
        if (plist.size()==list.size()){
            for (int i=0;i<list.size();i++){
                if (!list.get(i).equals(plist.get(i))){
                    throw new AnalyzeError(ErrorCode.ParamError);
                }
            }
        }
        else {
            throw new AnalyzeError(ErrorCode.ParamError);
        }
        return num;
    }
    private  void analyseFunction() throws CompileError{
        initJudgeVar();
        expect(TokenType.FN_KW);
        Token temp=expect(TokenType.IDENT);
        expect(TokenType.L_PAREN);
        if (check(TokenType.CONST_KW)||check(TokenType.IDENT)){
            parameters=analyseFunctionParamList();
        }
        expect(TokenType.R_PAREN);
        expect(TokenType.ARROW);
        String type=analyseTy();
        if (type.equals("void")) is_void=true;

        if (!is_void){
            return_slots=1;
            parameter_offset=1;
        }
        else  {
            return_slots=0;
        }
        if (Fuzhu.isFunction(globalTable,temp.getValueString())){
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, temp.getStartPos());
        }
        functionDef function = new functionDef(0,return_slots,parameters.size(),0,null, function_id, type, temp.getValueString(), Fuzhu.ChangeToBinary(temp.getValueString()), parameters);

        functionTable.put(function.getName(),function);

        analyseBlockStmt(false,0,type);
        if ((!return_type.equals(type)) || is_void&&has_return || ((!is_void)&&(!has_return))){
            throw new AnalyzeError(ErrorCode.NotValidReturn, temp.getEndPos());
        }

        if (type.equals("void")){
            instructionList.add(new Instruction(Operation.ret));
        }

        function.setOffset(global_offset);
        function.setLoc_slots(loc_slots);
        function.setInstructions(instructionList);

        Fuzhu.addGlobalTable(globalTable,1,temp.getValueString(),temp.getStartPos());
        global_offset++;
        function_id++;

        initJudgeVar();
    }

    private void analyseBlockStmt(Boolean isWhile, int startOfWhile ,String type) throws CompileError{
        expect(TokenType.L_BRACE);
        level++;
        while(check(TokenType.IDENT)||check(TokenType.MINUS)||isLiteralExpr()||
                check(TokenType.LET_KW)||check(TokenType.CONST_KW)||check(TokenType.L_PAREN)||check(TokenType.IF_KW)||check(TokenType.WHILE_KW)
                ||check(TokenType.BREAK_KW)||check(TokenType.CONTINUE_KW)||check(TokenType.RETURN_KW)||check(TokenType.L_BRACE)||
                check(TokenType.SEMICOLON)){
            analyseStatement(isWhile,startOfWhile,type);
        }
        expect(TokenType.R_BRACE);
        Fuzhu.clearSymbolTable(symbolTable, level);
        level--;
    }
    private void analyseStatement(Boolean isWhile, int startOfWhile ,String type) throws CompileError {
        Token temp = peek();
        if (check(TokenType.IDENT) || check(TokenType.MINUS) || check(TokenType.L_PAREN) || isLiteralExpr()) {
            String type1=analyseExpression();
            while (!stack.empty()) {
                TokenType tokenType = stack.pop();
                Instruction.AddToInstructionListInt(tokenType, instructionList);
            }
            expect(TokenType.SEMICOLON);
        } else if (check(TokenType.LET_KW) || check(TokenType.CONST_KW)) {
            analyseDeclStmt();
        } else if (check(TokenType.RETURN_KW)) {
            analyseReturnStmt(type);
        } else if (check(TokenType.L_BRACE)) {
            analyseBlockStmt(isWhile, startOfWhile,type);
        } else if (check(TokenType.SEMICOLON)) {
            expect(TokenType.SEMICOLON);
        } else if (check(TokenType.IF_KW)) {
            analyseIfStmt(isWhile,startOfWhile,type);
        } else if (check(TokenType.WHILE_KW)) {
            analyseWhileStmt(type);
        } else if (check(TokenType.BREAK_KW)) {
            expect(TokenType.BREAK_KW);
            expect(TokenType.SEMICOLON);
        } else if (check(TokenType.CONTINUE_KW)) {
            expect(TokenType.CONTINUE_KW);
            expect(TokenType.SEMICOLON);
        }
        else {
            throw  new AnalyzeError(ErrorCode.InvalidInput,temp.getStartPos());
        }
    }
    private void analyseReturnStmt(String type) throws CompileError{
        expect(TokenType.RETURN_KW);
        Token temp=peek();
        if (check(TokenType.MINUS)||check(TokenType.IDENT)||check(TokenType.L_PAREN)||isLiteralExpr()){
            if (type.equals("int")){
                instructionList.add(new Instruction(Operation.arga,0,4));
                return_type=analyseExpression();
                if (!type.equals(return_type)){
                    throw new AnalyzeError(ErrorCode.NotValidReturn,temp.getStartPos());
                }
                while (!stack.empty()){
                    Instruction.AddToInstructionListInt(stack.pop(),instructionList);
                }
                instructionList.add(new Instruction(Operation.store));
                has_return=true;
            }
            else {
                throw new AnalyzeError(ErrorCode.NotValidReturn,temp.getStartPos());
            }
        }
        expect(TokenType.SEMICOLON);
        while (!stack.empty()){
            Instruction.AddToInstructionListInt(stack.pop(),instructionList);
        }
        instructionList.add(new Instruction(Operation.ret));
    }
    private void analyseIfStmt(Boolean isWhile, int startOfWhile,String type) throws CompileError{
        expect(TokenType.IF_KW);
        String type1=analyseExpression();
        while (!stack.empty()){
            TokenType tokenType=stack.pop();
            Instruction.AddToInstructionListInt(tokenType, instructionList);
        }
        instructionList.add(new Instruction(Operation.brTrue,1,4));
        Instruction jmp_if=new Instruction(Operation.br,0,4);
        instructionList.add(jmp_if);
        int if_begin=instructionList.size();
        analyseBlockStmt(isWhile, startOfWhile,type);
        Instruction jmp_else=new Instruction(Operation.br,0,4);
        instructionList.add(jmp_else);
        int else_begin=instructionList.size();
        int jmp=instructionList.size()-if_begin;
        jmp_if.setX(jmp);
        if (check(TokenType.ELSE_KW)){
            expect(TokenType.ELSE_KW);
            if (check(TokenType.IF_KW)){
                analyseIfStmt(isWhile,startOfWhile,type);
            }
            else {
                analyseBlockStmt(isWhile,startOfWhile,type);
                instructionList.add(new Instruction(Operation.br,0,4));
            }
        }
        jmp=instructionList.size()-else_begin;
        jmp_else.setX(jmp);
    }
    private void analyseWhileStmt(String type) throws CompileError{
        expect(TokenType.WHILE_KW);
        int a=instructionList.size();
        String type1=analyseExpression();
        while (!stack.empty()){
            TokenType tokenType=stack.pop();
            Instruction.AddToInstructionListInt(tokenType,instructionList);
        }
        instructionList.add(new Instruction(Operation.brTrue,1,4));
        Instruction br = new Instruction(Operation.br,0,4);
        instructionList.add(br);
        int b=instructionList.size();
        boolean iswhile = true;
        analyseBlockStmt(iswhile,a,type);
        Instruction back=new Instruction(Operation.br,0,4);
        instructionList.add(back);
        int c=instructionList.size();
        int Back=a-c;
        int jmp=c-b;
        back.setX(Back);
        br.setX(jmp);
    }
    private List<parameter> analyseFunctionParamList() throws CompileError{
        List<parameter> temp=new ArrayList<>();
        temp.add(analyseFunctionParam());
        while (check(TokenType.COMMA)){
            Token temp1=expect(TokenType.COMMA);
            parameter parameter=analyseFunctionParam();
            if (Fuzhu.isRepeatParam(temp,parameter)){
                throw new AnalyzeError(ErrorCode.DuplicateDeclaration, temp1.getEndPos());
            }
            else temp.add(parameter);
        }
        return temp;
    }

    private parameter analyseFunctionParam() throws CompileError{
        if (check(TokenType.CONST_KW)){
            expect(TokenType.CONST_KW);
        }
        Token name=expect(TokenType.IDENT);
        expect(TokenType.COLON);
        String ty=analyseTy();
        return new parameter(ty,name.getValueString());
    }

    private String analyseTy() throws CompileError{

        Token ty=expect(TokenType.IDENT);
        if (ty.getValueString().equals("int")||ty.getValueString().equals("void")){
            return ty.getValueString();
        }
        else {
            throw new AnalyzeError(ErrorCode.InvalidInput, ty.getStartPos());
        }
    }

    private boolean isLiteralExpr() throws TokenizeError{
        if (check(TokenType.UINT)||check(TokenType.STRING)){
            return true;
        }
        return false;
    }
    private void initJudgeVar(){
        is_void = false;
        has_return = false;
        return_type = "void";
        instructionList = new ArrayList<>();
        parameter_offset = 0;
        loc_slots = 0;
        return_slots = 0;
        parameters = new ArrayList<>();
    }
    private boolean isBinaryOperator() throws TokenizeError{
        if(check(TokenType.PLUS)||check(TokenType.MINUS)||
                check(TokenType.MUL)||check(TokenType.DIV)||
                check(TokenType.ASSIGN)||check(TokenType.EQ)||
                check(TokenType.NEQ)||check(TokenType.LT)||check(TokenType.GT)
                ||check(TokenType.LE)||check(TokenType.GE)) {
            return true;
        }
        return false;
    }
}
