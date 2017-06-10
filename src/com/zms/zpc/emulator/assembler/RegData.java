package com.zms.zpc.emulator.assembler;

/**
 * Created by 张小美 on 17/六月/10.
 * Copyright 2002-2016
 */
public class RegData {

    private String name;
    private String klass;
    private String klass2;
    private int num;
    private String flag;

    public RegData(String name,String line) {
        this.name=name.toUpperCase();
        int index=line.indexOf(' ');
        assert index>0;
        String[] toks=line.substring(index+1).trim().split(" ");
        assert toks.length==3 || toks.length==4;
        num=Integer.parseInt(toks[2]);
        klass=toks[0];
        klass2=toks[1];
        if(toks.length>3) {
            flag=toks[3];
        }
    }

    public String getName() {
        return name;
    }

    public String getKlass() {
        return klass;
    }

    public String getKlass2() {
        return klass2;
    }

    public int getNum() {
        return num;
    }

    public String getFlag() {
        return flag;
    }

}
