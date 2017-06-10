package com.zms.zpc.emulator.assembler;

import java.util.*;

/**
 * Created by 张小美 on 17/六月/10.
 * Copyright 2002-2016
 */
public class InstruData {

    private String key;
    private String param;
    private String code;
    private String flag;

    public final static Collection<String> Flags=new LinkedHashSet<>();

    public InstruData(String line) {
        parse(line);
    }

    protected void parse(String line) {
        line=line.replaceAll("\\s+"," ").trim();
        assert line.length()>0;
        {
            char[] cs = line.toCharArray();
            for (int i = 0; i < cs.length; i++) {
                if (!((cs[i] >= 'A' && cs[i] <= 'Z') || cs[i] == '_' || (cs[i] >= '0' && cs[i] <= '9') || (cs[i] >= 'a' && cs[i] <= 'z'))) {
                    assert i > 0;
                    assert cs[i] == ' ';
                    key = line.substring(0, i);
                    break;
                }
            }
        }
        {
            line = line.substring(key.length() + 1);
            int index = line.lastIndexOf(' ');
            assert index > 0;
            String flag = line.substring(index + 1).trim();
            line=line.substring(0,index).trim();
            this.flag=flag;
            String[] flags = flag.split(",");
            assert flags.length>0;
            for (String s : flags) {
                if(s.equals("ignore")) {
                    continue;
                }
                assert s.length()>0;
                String s1=s.toUpperCase();
                assert s1.equals(s);
                synchronized (Flags) {
                    Flags.add(s);
                }
            }
        }
        {
            int index=line.indexOf(' ');
            assert index>0;
            param=line.substring(0,index).trim();
            code=line.substring(index+1).trim();
            if(!"ignore".equals(code)) {
                assert code.startsWith("[") && code.endsWith("]");
            }
        }
    }

    public String getKey() {
        return key;
    }

    public String getFlag() {
        return flag;
    }

}
