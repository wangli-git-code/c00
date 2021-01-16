package miniplc0java.util;

import miniplc0java.analyser.SymbolEntry;
import miniplc0java.error.AnalyzeError;
import miniplc0java.error.ErrorCode;
import miniplc0java.instruction.Instruction;
import miniplc0java.struct.functionDef;
import miniplc0java.struct.globalDef;
import miniplc0java.struct.parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Fuzhu {
    /**
     * 设置符号为已赋值
     *
     * @param name   符号名称
     * @param curPos 当前位置（报错用）
     * @throws AnalyzeError 如果未定义则抛异常
     */
    public static void initializeSymbol(List<SymbolEntry> symbolTable ,String name, int level,Pos curPos) throws AnalyzeError {
        /* 从后向前查找该符号 */
        SymbolEntry entry = CanBeUsed(symbolTable, level, name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            entry.setInitialized(true);
        }
    }



    /**
     * 将函数名或全局变量名加入全局变量表中
     * */
    public static void addGlobalTable(List<globalDef> GlobalTable
            , int isConst ,String name , Pos curPos) throws AnalyzeError {
        for(globalDef globalDef:GlobalTable){
            if(globalDef.getName().equals(name)){
                throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
            }
        }
        GlobalTable.add(new globalDef(name,isConst,ChangeToBinary(name)));
    }


    /**
     * 判断该函数是否为库函数
     * */
    public static boolean isLibraryFunction(String name) {
        if (name.equals("getint") || name.equals("getdouble") || name.equals("getchar") ||
                name.equals("putint") || name.equals("putdouble") || name.equals("putchar") ||
                name.equals("putstr") || name.equals("putln"))
            return true;
        return false;
    }

    /**
     * 添加该函数，判断该函数名是否已被定义
     * 函数名存入全局，直接在全局表查找即可
     * 已被定义则抛出错误
     * */
    public static void addFunction(HashMap<String, functionDef> FunctionTable  , List<globalDef> GlobalTable
            , int offset , int return_slots , int param_slots , int loc_slots , List<Instruction> Instructions , int FunctionId , String type , String name , List<parameter> parameters, Pos curPos) throws AnalyzeError {
        /* 在全局表中存在 */
        for(globalDef globalDef:GlobalTable){
            if(globalDef.getName().equals(name)){
                throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
            }
        }

        /* 为库函数 */
        if (isLibraryFunction(name) ) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
        } else {
            FunctionTable.put(name, new functionDef(offset, return_slots, param_slots,loc_slots, Instructions, FunctionId ,type, name, ChangeToBinary(name) ,parameters));
        }
    }

    /**
     * 查找该函数(不能再库函数中且不能在全局变量中存在)
     * */
    public static boolean isFunction(List<globalDef> GlobalTable,String name){
        for(globalDef GlobalDef:GlobalTable){
            if(GlobalDef.getName().equals(name)){
                return true;
            }
        }
        return isLibraryFunction(name);
    }



    /**
     * 将命名转化为一个个char
     */
    public static List<Character> ChangeToBinary(String str) {
        char[] arr = str.toCharArray();
        List<Character> items = new ArrayList<>();
        for (char c : arr) {
            items.add(c);
        }
        return items;
    }


    /**
     * 判断是否为命名重复的参数
     * */
    public static boolean isRepeatParam(List<parameter> params, parameter param){
        for (parameter parameter1 : params) {
            if (parameter1.getName().equals(param.getName())) {
                return true;
            }
        }
        return false;
    }


    /**
     * 当前模块结束后，将该模块的所有局部变量删除
     * */
    public static void clearSymbolTable(List<SymbolEntry>symbolTable, int level){
        symbolTable.removeIf(tmp -> tmp.getLevel() == level);
    }

    /**
     * 查找某个符号是否已经在当前层被定义
     * 同时判断全局符号是否和函数名重复
     *
     * 当符号在该层已经被定义，或者全局符号与函数名相同时返回true
     * */
    public static boolean isDefinedSymbol (HashMap<String, functionDef> FunctionTable, List<SymbolEntry>symbolTable, int level, String name){
        if(level == 0 && FunctionTable.get(name)!=null){
            return true;
        }

        for (SymbolEntry symbol : symbolTable) {
            if (symbol.getLevel()==level&&symbol.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断某个symbol能否被使用
     * 从当前层开始逐层向前查找
     *
     * 需要判断使用时的类型问题
     * */
    public static SymbolEntry CanBeUsed (List<SymbolEntry>symbolTable, int level, String name) {
        for(int i=level;i>=0;i--){
            for (SymbolEntry symbol : symbolTable) {
                if (symbol.getLevel()==i&&symbol.getName().equals(name)) {
                    return symbol;
                }
            }
        }
        return null;
    }



    /**
     * 判断某个symbol是否为函数的参数
     * */
    public static parameter isParameter(List<parameter> parameters , String name){
        for(parameter parameter1:parameters){
            if(parameter1.getName().equals(name)){
                return parameter1;
            }
        }
        return null;
    }

    /**
     * 查找参数在栈上的偏移
     * */
    public static int getParamOffset(String name, List<parameter> params) {
        for (int i = 0; i < params.size(); ++i) {
            if (params.get(i).getName().equals(name))
                return i;
        }
        return -1;
    }

    /**
     * 判断函数是否有返回值
     * */
    public static boolean hasReturn(String name, HashMap<String,functionDef>FunctionTable ){
        functionDef functionDef = FunctionTable.get(name);
        if (name.equals("getint") || name.equals("getdouble") || name.equals("getchar")) {
            return true;
        }else if(functionDef!=null){
            return functionDef.getType().equals("int") || functionDef.getType().equals("double");
        }

        return false;
    }

    /**
     * 返回函数的参数类型
     * */
    public static List<String> TypeReturn(String name, HashMap<String,functionDef>FunctionTable ){
        List<String> TypeList = new ArrayList<>();
        functionDef functionDef = FunctionTable.get(name);
        if (name.equals("putint") || name.equals("putchar") || name.equals("putstr")) {
            TypeList.add("int");
            return TypeList;
        }else if(name.equals("putdouble")){
            TypeList.add("double");
            return TypeList;
        }

        if(functionDef!=null){
            List<parameter> parameters = functionDef.getParameters();
            for (parameter parameter : parameters) {
                TypeList.add(parameter.getType());
            }
            return TypeList;
        }
        return TypeList;
    }


    /**
     * 获取库函数的返回类型
     * */
    public static String TypeReturnOfLibrary(String name){

        if (name.equals("getint") || name.equals("getchar")) {
            return "int";
        }else if(name.equals("getdouble")){
            return "double";
        }else if(name.equals("putint")||name.equals("putdouble")||
                name.equals("putchar")||name.equals("putstr")||
                name.equals("putln")){
            return "void";
        }
        return null;
    }
}
