package miniplc0java.struct;

import java.util.List;

public class globalDef {
    String name;
    int is_const;
    int value_count;
    List<Character> values;

    public globalDef(String name, int is_const) {
        this.name = name;
        this.is_const = is_const;
        this.value_count = 0;
        this.values = null;
    }

    public globalDef(String name, int is_const,  List<Character> values) {
        this.name = name;
        this.is_const = is_const;
        this.value_count = values.size();
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIs_const() {
        return is_const;
    }

    public void setIs_const(int is_const) {
        this.is_const = is_const;
    }

    public int getValue_count() {
        return value_count;
    }

    public void setValue_count(int value_count) {
        this.value_count = value_count;
    }

    public List<Character> getValues() {
        return values;
    }

    public void setValues(List<Character> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "global{" +
                "name='" + name + '\'' +
                ", is_const=" + is_const +
                ", value_count=" + value_count +
                ", values=" + values +
                '}';
    }
}
