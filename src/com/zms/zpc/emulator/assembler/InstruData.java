package com.zms.zpc.emulator.assembler;

/**
 * Created by 张小美 on 17/六月/10.
 * Copyright 2002-2016
 */
public class InstruData {

    private String key;

    public InstruData(String line) {
        parse(line);
    }

    protected void parse(String line) {
        line=line.replaceAll("\\s+"," ").trim();
        assert line.length()>0;
        char[] cs=line.toCharArray();
        for(int i=0;i<cs.length;i++) {
            if(!((cs[i]>='A' && cs[i]<='Z') || cs[i]=='_' || (cs[i]>='0' && cs[i]<='9') || (cs[i]>='a' && cs[i]<='z'))) {
                assert i>0;
                assert cs[i]==' ';
                key=line.substring(0,i);
                break;
            }
        }
    }

    public String getKey() {
        return key;
    }

}
