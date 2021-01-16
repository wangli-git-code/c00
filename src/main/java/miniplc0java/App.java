package miniplc0java;

import miniplc0java.analyser.Analyser;
import miniplc0java.analyser.produce;
import miniplc0java.error.CompileError;
import miniplc0java.tokenizer.StringIter;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.struct.functionDef;
import miniplc0java.struct.globalDef;

import java.io.*;
import java.util.*;

public class App {
    public static void main(String[] args) throws IOException, CompileError {
//        var argparse = buildArgparse();
//
//        Namespace result;
//        try {
//            result = argparse.parseArgs(args);
//        } catch (ArgumentParserException e1) {
//            argparse.handleError(e1);
//            return;
//        }
//
//        var inputFileName = result.getString("input");
//        var outputFileName = result.getString("output");
//
//        InputStream input;
//        if (inputFileName.equals("-")) {
//            input = System.in;
//        } else {
//            try {
//                input = new FileInputStream(inputFileName);
//            } catch (FileNotFoundException e) {
//                System.err.println("Cannot find input file.");
//                e.printStackTrace();
//                System.exit(2);
//                return;
//            }
//        }
//
//        PrintStream output;
//        if (outputFileName.equals("-")) {
//            output = System.out;
//        } else {
//            try {
//                output = new PrintStream(new FileOutputStream(outputFileName));
//            } catch (FileNotFoundException e) {
//                System.err.println("Cannot open output file.");
//                e.printStackTrace();
//                System.exit(2);
//                return;
//            }
//        }
//
//        Scanner scanner;
//        scanner = new Scanner(input);
//        var iter = new StringIter(scanner);
//        var tokenizer = tokenize(iter);
//
//        if (result.getBoolean("tokenize")) {
//            // tokenize
//            var tokens = new ArrayList<Token>();
//            try {
//                while (true) {
//                    var token = tokenizer.nextToken();
//                    if (token.getTokenType().equals(TokenType.EOF)) {
//                        break;
//                    }
//                    tokens.add(token);
//                }
//            } catch (Exception e) {
//                // 遇到错误不输出，直接退出
//                System.err.println(e);
//                System.exit(0);
//                return;
//            }
//            for (Token token : tokens) {
//                output.println(token.toString());
//            }
//        } else if (result.getBoolean("analyse")) {
//            // analyze
//            var analyzer = new Analyser(tokenizer);
//            List<Instruction> instructions;
//            try {
//                instructions = analyzer.analyse();
//            } catch (Exception e) {
//                // 遇到错误不输出，直接退出
//                System.err.println(e);
//                System.exit(0);
//                return;
//            }
//            for (Instruction instruction : instructions) {
//                output.println(instruction.toString());
//            }
//        } else {
//            System.err.println("Please specify either '--analyse' or '--tokenize'.");
//            System.exit(3);
//        }
//    }
//
//    private static ArgumentParser buildArgparse() {
//        var builder = ArgumentParsers.newFor("miniplc0-java");
//        var parser = builder.build();
//        parser.addArgument("-t", "--tokenize").help("Tokenize the input").action(Arguments.storeTrue());
//        parser.addArgument("-l", "--analyse").help("Analyze the input").action(Arguments.storeTrue());
//        parser.addArgument("-o", "--output").help("Set the output file").required(true).dest("output")
//                .action(Arguments.store());
//        parser.addArgument("file").required(true).dest("input").action(Arguments.store()).help("Input file");
//        return parser;
//    }
//
//    private static Tokenizer tokenize(StringIter iter) {
//        var tokenizer = new Tokenizer(iter);
//        return tokenizer;
        try{
            InputStream inputStream=new FileInputStream(args[0]);
            Scanner scanner=new Scanner(inputStream);
            StringIter iter=new StringIter(scanner);
            iter.readAll();
            System.out.println(iter.getLinesBuffer());
            Analyser temp=new Analyser(new Tokenizer(iter));
            temp.analyseProgram();
            for (globalDef globalDef:temp.getGlobalTable()){
                System.out.println(globalDef);
            }
            List<Map.Entry<String, functionDef>> FunctionList = new ArrayList<Map.Entry<String, functionDef>>(temp.getFunctionTable().entrySet());
            Collections.sort(FunctionList, new Comparator<Map.Entry<String, functionDef>>() {
                public int compare(Map.Entry<String, functionDef> o1, Map.Entry<String, functionDef> o2) {
                    return (o1.getValue().getFunction_id() - o2.getValue().getFunction_id());
                }
            });
            for (Map.Entry<String, functionDef> functionDef : FunctionList) {
                System.out.println(functionDef.getValue().getName());
                System.out.println(functionDef);
            }
            produce output = new produce(temp.getGlobalTable(), FunctionList);
            System.out.println();
            DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(args[1])));
            List<Byte> bytes = output.getproduceOut();
            byte[] resultBytes = new byte[bytes.size()];
            for (int i = 0; i < bytes.size(); ++i) {
                resultBytes[i] = bytes.get(i);
            }
            out.write(resultBytes);
            System.exit(0);
        }
        catch (Exception s){
            System.out.println(s);
            throw s;
        }
    }
}
