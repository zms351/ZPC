package com.zms.zpc.emulator.assembler;

import java.util.*;

/**
 * Created by 张小美 on 17/六月/10.
 * Copyright 2002-2016
 */
public class Instru {

    private String line;
    private List<String> tokens=new ArrayList<>();
    private List<List<String>> types=new ArrayList<>();
    
    public void parse(Assembler assembler,String line) {
        this.line=line;
        tokens.clear();
        int index=line.indexOf(' ');
        if(index>0) {
            tokens.add(line.substring(0,index));
            for (String tok : line.substring(index + 1).split(",")) {
                if((tok=tok.trim()).length()>0) {
                    tokens.add(tok);
                }
            }
        } else {
            tokens.add(line);
        }
        tokens.set(0,tokens.get(0).toUpperCase());
        types.clear();
        types.add(new ArrayList<>());
        types.get(0).add("KEY");
        for(int i=1;i<tokens.size();i++) {
            types.add(assembler.parseTokenTypes(tokens.get(i)));
        }
    }

    public List<String> getTokens() {
        return tokens;
    }

    public List<List<String>> getTypes() {
        return types;
    }

    public String getKey() {
        return tokens.get(0);
    }

    public String getLine() {
        return line;
    }

}
