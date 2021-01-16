package miniplc0java.struct;

import miniplc0java.instruction.Instruction;

import java.util.List;

public class functionDef {
    int offset; //偏移量
    int return_slots;
    int param_slots;
    int loc_slots;
    List<Instruction> instructions;

    int function_id;
    String type;
    String name;
    List<Character> names;
    List<parameter> parameters;

    public functionDef(int offset, int return_slots, int param_slots, int loc_slots, List<Instruction> instructions, int function_id, String type, String name, List<Character> names, List<parameter> parameters) {
        this.offset = offset;
        this.return_slots = return_slots;
        this.param_slots = param_slots;
        this.loc_slots = loc_slots;
        this.instructions = instructions;
        this.function_id = function_id;
        this.type = type;
        this.name = name;
        this.names = names;
        this.parameters = parameters;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getReturn_slots() {
        return return_slots;
    }

    public void setReturn_slots(int return_slots) {
        this.return_slots = return_slots;
    }

    public int getParam_slots() {
        return param_slots;
    }

    public void setParam_slots(int param_slots) {
        this.param_slots = param_slots;
    }

    public int getLoc_slots() {
        return loc_slots;
    }

    public void setLoc_slots(int loc_slots) {
        this.loc_slots = loc_slots;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public int getFunction_id() {
        return function_id;
    }

    public void setFunction_id(int function_id) {
        this.function_id = function_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Character> getNames() {
        return names;
    }

    public void setNames(List<Character> names) {
        this.names = names;
    }

    public List<parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<parameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "function{" +
                "offset=" + offset +
                ", return_slots=" + return_slots +
                ", param_slots=" + param_slots +
                ", loc_slots=" + loc_slots +
                ", instructions=" + instructions +
                ", function_id=" + function_id +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", names=" + names +
                ", parameters=" + parameters +
                '}';
    }
}
