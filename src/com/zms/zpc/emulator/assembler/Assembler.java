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

    public static Map<String, Collection<InstruData>> InstruMap;
    public static Map<String, RegData> RegMap;

    private static void _add(Map<String, Collection<InstruData>> map, InstruData instru) {
        if (map == null) {
            map = InstruMap;
        }
        String key = instru.getKey();
        Collection<InstruData> list = map.computeIfAbsent(key, k -> new ArrayList<>());
        list.add(instru);
    }

    private static void _add(Map<String, RegData> map, RegData reg) {
        if (map == null) {
            map = RegMap;
        }
        assert !map.containsKey(reg.getName());
        map.put(reg.getName(), reg);
    }

    public static void Init() {
        {
            try {
                Class<Assembler> klass = Assembler.class;
                String name = klass.getName();
                name = name.substring(0, name.lastIndexOf('.')).replace('.', '/') + "/insns.dat";
                Map<String, Collection<InstruData>> map = new LinkedHashMap<>();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(klass.getClassLoader().getResourceAsStream(name)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        int index = line.indexOf(';');
                        if (index >= 0) {
                            line = line.substring(0, index);
                        }
                        if ((line = line.trim()).length() > 0) {
                            //System.out.println(line);
                            InstruData instru = new InstruData(line);
                            _add(map, instru);
                        }
                    }
                }
                for (String key : new ArrayList<>(map.keySet())) {
                    if (!key.toUpperCase().equals(key)) {
                        map.put(key.toUpperCase(), map.get(key));
                    }
                }
                InstruMap = map;
                //System.out.println(map.size());
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            _add(null, new InstruData("BITS imm [] SYS"));
        }
        {
            try {
                Class<Assembler> klass = Assembler.class;
                String name = klass.getName();
                name = name.substring(0, name.lastIndexOf('.')).replace('.', '/') + "/regs.dat";
                Map<String, RegData> map = new LinkedHashMap<>();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(klass.getClassLoader().getResourceAsStream(name)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        int index = line.indexOf('#');
                        if (index >= 0) {
                            line = line.substring(0, index);
                        }
                        line = line.replaceAll("\\s+", " ");
                        if ((line = line.trim()).length() > 0) {
                            System.out.println(line);
                            index = line.indexOf(' ');
                            assert index > 0;
                            String name1 = line.substring(0, index);
                            index = name1.indexOf('-');
                            RegData reg;
                            if (index > 0) {
                                int index3=index+1;
                                while(index3<name1.length() && Character.isDigit(name1.charAt(index3))) {
                                    index3++;
                                }
                                int to = Integer.parseInt(name1.substring(index + 1,index3));
                                int index2 = index - 1;
                                while (Character.isDigit(name1.charAt(index2))) {
                                    index2--;
                                }
                                int from = Integer.parseInt(name1.substring(index2 + 1, index));
                                for (int i = from; i <= to; i++) {
                                    name=name1.substring(0, index2 + 1)+i+name1.substring(index3);
                                    reg = new RegData(name, line);
                                    _add(map, reg);
                                }
                            } else {
                                reg = new RegData(name1, line);
                                _add(map, reg);
                            }
                        }
                    }
                }
                RegMap=map;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    public Assembler() {
        this(32);
    }

    public Assembler(int bits) {
        this.bits=bits;
    }

    private ByteArrayOutputStream output;
    private StringWriter writer1;
    private PrintWriter writer;

    private int bits;

    public Object assemble(String asm) {
        try {
            if (output == null) {
                output = new ByteArrayOutputStream();
            } else {
                output.reset();
            }
            if (writer1 == null) {
                writer1 = new StringWriter();
            } else {
                writer1.getBuffer().setLength(0);
            }
            writer = new PrintWriter(writer1);
            BufferedReader reader = new BufferedReader(new StringReader(asm));
            String line;
            int n1 = 0;
            String s;
            while ((line = reader.readLine()) != null) {
                n1++;
                s = line;
                int index = line.indexOf(';');
                if (index >= 0) {
                    line = line.substring(0, index).trim();
                }
                writer.print(s);
                if ((line = line.trim()).length() > 0) {
                    process(line);
                }
                writer.println();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        return writer1;
    }

    protected void process(String line) {
        line = line.replaceAll("\\s+", " ").trim();
        System.out.println(line);
        InstruData instru = findInstruData(line);
        if (instru.isSys()) {
            processSysInstruData(instru, line);
        }
    }

    protected InstruData findInstruData(String line) {
        int index = line.indexOf(' ');
        String key;
        if (index > 0) {
            key = line.substring(0, index);
        } else {
            key = line;
        }
        key = key.trim().toUpperCase();
        Collection<InstruData> list = InstruMap.get(key);
        if (list != null) {
            if (list.size() == 1) {
                return list.iterator().next();
            }
        }
        return null;
    }

    protected void processSysInstruData(InstruData instru, String line) {
        System.out.println("hehe");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("test");
        System.out.println(InstruData.Flags);
    }

}
