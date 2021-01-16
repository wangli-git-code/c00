package miniplc0java.instruction;

public enum Operation {
    push(0x01),
    popn(0x03),
    loca(0x0a),
    arga(0x0b),
    globa(0x0c),
    load(0x13),
    store(0x17),
    stackalloc(0x1a),
    add(0x20),
    addf(0x24),
    sub(0x21),
    subf(0x25),
    mul(0x22),
    mulf(0x26),
    div(0x23),
    divf(0x27),
    not(0x2e),
    cmp(0x30),
    cmpf(0x32),
    neg(0x34),
    negf(0x35),
    itof(0x36),
    ftoi(0x37),
    setLt(0x39),
    setGt(0x3a),
    br(0x41),
    brTrue(0x43),
    call(0x48),
    ret(0x49),
    callname(0x4a);

    private int num;

    Operation(int num) {
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}

