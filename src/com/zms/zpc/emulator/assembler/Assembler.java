package com.zms.zpc.emulator.assembler;

import java.io.*;
import java.util.*;

/**
 * Created by 张小美 on 17/六月/10.
 * Copyright 2002-2016
 */
public class Assembler {

    static {
        Init();
    }

    public static Map<String,Collection<InstruData>> InstruMap;

    public static void Init() {
        try {
            Class<Assembler> klass = Assembler.class;
            String name= klass.getName();
            name=name.substring(0,name.lastIndexOf('.')).replace('.','/')+"/insns.dat";
            Map<String,Collection<InstruData>> map=new LinkedHashMap<>();
            try(BufferedReader reader=new BufferedReader(new InputStreamReader(klass.getClassLoader().getResourceAsStream(name)))) {
                String line;
                while((line=reader.readLine())!=null) {
                    int index=line.indexOf(';');
                    if(index>=0) {
                        line = line.substring(0, index);
                    }
                    if((line=line.trim()).length()>0) {
                        //System.out.println(line);
                        InstruData instru=new InstruData(line);
                        String key=instru.getKey();
                        Collection<InstruData> list = map.computeIfAbsent(key, k -> new ArrayList<>());
                        list.add(instru);
                    }
                }
            }
            for (String key : new ArrayList<>(map.keySet())) {
                if(!key.toUpperCase().equals(key)) {
                    map.put(key.toUpperCase(),map.get(key));
                }
            }
            InstruMap=map;
            //System.out.println(map.size());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void assemble(String s) {
        try {
            BufferedReader reader=new BufferedReader(new StringReader(s));
            String line;
            int n1=0;
            while((line=reader.readLine())!=null) {
                n1++;
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("test");
        System.out.println(InstruData.Flags);
    }

}
