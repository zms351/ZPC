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

    private List<List<String>> types;
    private List<Object> codes;
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
        {
            types=new ArrayList<>();
            types.add(KeyType);
            if(!("void".equals(param) || "ignore".equals(param))) {
                for (String tok : param.split(",")) {
                    tok=tok.trim();
                    if(tok.length()>0) {
                        List<String> one=new ArrayList<>();
                        one.add(tok);
                        types.add(one);
                    }
                }
            }
        }
        {
            codes=new ArrayList<>();
            for (String tok : code.substring(1, code.length() - 1).split("\\s")) {
                tok=tok.trim();
                if(tok.length()>0) {
                    Object o=tok;
                    if(tok.matches("[0-9a-fA-F]{2}") && tok.length()==2) {
                        o=Integer.parseInt(tok,16);
                    }
                    codes.add(o);
                }
            }
        }
    }

    public static List<String> KeyType;
    static {
        KeyType=new ArrayList<>();
        KeyType.add("KEY");
    }

    public String getKey() {
        return key;
    }

    public String getFlag() {
        return flag;
    }

    public boolean isSys() {
        return "SYS".equals(flag);
    }

    public String getParam() {
        return param;
    }

    public String getCode() {
        return code;
    }

    public List<List<String>> getTypes() {
        return types;
    }

    public int match(Instru instru) {
        int size=this.types.size();
        List<List<String>> t1 = instru.getTypes();
        if(t1.size()==size) {
            out:for(int i=1;i<size;i++) {
                List<String> t2 = this.types.get(i);
                for (String t3 : t1.get(i)) {
                    if(t2.contains(t3)) {
                        continue out;
                    }
                }
                return 0;
            }
            return 1;
        }
        return 0;
    }

    public List<Object> getCodes() {
        return codes;
    }

}
