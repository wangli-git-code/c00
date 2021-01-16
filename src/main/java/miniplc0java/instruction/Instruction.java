package miniplc0java.instruction;

import miniplc0java.tokenizer.TokenType;

import java.util.List;
import java.util.Objects;

public class Instruction {
    private Operation opt;
    long x;
    int ByteNum; //字节数，int为4，double为8


    public Instruction(Operation opt) {
        this.opt = opt;
        this.x = 0;
        ByteNum = 0;
    }

    public Instruction(Operation opt, long x, int ByteNum) {
        this.opt = opt;
        this.x = x;
        this.ByteNum = ByteNum;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Instruction that = (Instruction) o;
        return opt == that.opt && Objects.equals(x, that.x);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opt, x);
    }

    public int getOpt() {
        return opt.getNum();
    }

    public void setOpt(Operation opt) {
        this.opt = opt;
    }

    public long getX() {
        return x;
    }

    public void setX(long x) {
        this.x = x;
    }

    public int getByteNum() {
        return ByteNum;
    }

    @Override
    public String toString() {
        return ""+opt +'(' + x +')'+ '\n';
    }

    public static void AddToInstructionListInt(TokenType type, List<Instruction> instructionsList){
        switch (type) {
            case LT:
                instructionsList.add(new Instruction(Operation.cmp));
                instructionsList.add(new Instruction(Operation.setLt));
                break;
            case LE:
                instructionsList.add(new Instruction(Operation.cmp));
                instructionsList.add(new Instruction(Operation.setGt));
                instructionsList.add(new Instruction(Operation.not));
                break;
            case GT:
                instructionsList.add(new Instruction(Operation.cmp));
                instructionsList.add(new Instruction(Operation.setGt));
                break;
            case GE:
                instructionsList.add(new Instruction(Operation.cmp));
                instructionsList.add(new Instruction(Operation.setLt));
                instructionsList.add(new Instruction(Operation.not));
                break;
            case PLUS:
                instructionsList.add(new Instruction(Operation.add));
                break;
            case MINUS:
                instructionsList.add(new Instruction(Operation.sub));
                break;
            case MUL:
                instructionsList.add(new Instruction(Operation.mul));
                break;
            case DIV:
                instructionsList.add(new Instruction(Operation.div));
                break;
            case EQ:
                instructionsList.add(new Instruction(Operation.cmp));
                instructionsList.add(new Instruction(Operation.not));
                break;
            case NEQ:
                instructionsList.add(new Instruction(Operation.cmp));
                break;
            default:
                break;
        }
    }
}
