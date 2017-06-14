package com.zms.zpc.emulator.assembler;

import com.zms.zpc.support.*;

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
                            //System.out.println(line);
                            index = line.indexOf(' ');
                            assert index > 0;
                            String name1 = line.substring(0, index);
                            index = name1.indexOf('-');
                            RegData reg;
                            if (index > 0) {
                                int index3 = index + 1;
                                while (index3 < name1.length() && Character.isDigit(name1.charAt(index3))) {
                                    index3++;
                                }
                                int to = Integer.parseInt(name1.substring(index + 1, index3));
                                int index2 = index - 1;
                                while (Character.isDigit(name1.charAt(index2))) {
                                    index2--;
                                }
                                int from = Integer.parseInt(name1.substring(index2 + 1, index));
                                for (int i = from; i <= to; i++) {
                                    name = name1.substring(0, index2 + 1) + i + name1.substring(index3);
                                    reg = new RegData(name, line);
                                    reg.setNum(i);
                                    _add(map, reg);
                                }
                            } else {
                                reg = new RegData(name1, line);
                                _add(map, reg);
                            }
                        }
                    }
                }
                RegMap = map;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    public Assembler() {
        this(32);
    }

    public Assembler(int bits) {
        this.bits = bits;
    }

    private AssemblerOutput output;
    private StringWriter writer1;
    private PrintWriter writer;

    private int bits;

    public Object assemble(String asm) {
        try {
            if (output == null) {
                output = new AssemblerOutput();
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
            Instru instru = new Instru();
            while ((line = reader.readLine()) != null) {
                n1++;
                s = line;
                int index = line.indexOf(';');
                if (index >= 0) {
                    line = line.substring(0, index).trim();
                }
                writer.print(s);
                line = line.replaceAll("\\s+", " ");
                if ((line = line.trim()).length() > 0) {
                    instru.parse(this, line);
                    process(instru);
                }
                writer.println();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        return writer1;
    }

    protected void write8(int n) {
        output.write(n);
    }

    private static Object[][][] ModData = new Object[][][]{
            {
                {"AL", "AX", "EAX", "MM0", "XMM0" },
                {"CL", "CX", "ECX", "MM1", "XMM1" },
                {"DL", "DX", "EDX", "MM2", "XMM2" },
                {"BL", "BX", "EBX", "MM3", "XMM3" },
                {"AH", "SP", "ESP", "MM4", "XMM4" },
                {"CH", "BP", "EBP", "MM5", "XMM5" },
                {"DH", "SI", "ESI", "MM6", "XMM6" },
                {"BH", "DI", "EDI", "MM7", "XMM7" }
            },
            {
                {"AL", "AX" ,"EAX", "MM0", "XMM0"},
                {"CL", "CX" ,"ECX", "MM1", "XMM1"},
                {"DL", "DX" ,"EDX", "MM2", "XMM2"},
                {"BL", "BX" ,"EBX" ,"MM3", "XMM3"},
                {"AH", "SP" ,"ESP", "MM4", "XMM4"},
                {"CH", "BP" ,"EBP", "MM5", "XMM5"},
                {"DH", "SI", "ESI", "MM6", "XMM6"},
                {"BH", "DI", "EDI", "MM7", "XMM7"}
            },
            {
                    {"EAX","AX","AL","MM0","XMM0"},
                    {"ECX","CX","CL","MM1","XMM1"},
                    {"EDX","DX","DL","MM2","XMM2"},
                    {"EBX","BX","BL","MM3","XMM3"},
                    {"ESP","SP","AH","MM4","XMM4"},
                    {"EBP","BP","CH","MM5","XMM5"},
                    {"ESI","SI","DH","MM6","XMM6"},
                    {"EDI","DI","BH","MM7","XMM7"}
            },
            {
                    {"BX+SI","BX+DI","BP+SI","BP+DI","SI","DI","BP","BX"},  //BP  ....
                    {"EAX","ECX","EDX","EBX", "ESP", "EBP" ,"ESI", "EDI"}  //ESP  EBP ....
            }
    };
    protected void processModRMSIB(Instru instru, InstruData data) {
        String pr=data.getPr1();
        assert "mr".equals(pr) || "rm".equals(pr);
        String r2;
        if(pr.charAt(0)=='r') {
            r2=instru.getTokens().get(1);
        } else {
            r2=instru.getTokens().get(2);
        }
        r2=r2.toUpperCase();
        assert RegMap.containsKey(r2);

        String r2c=null;
        if(bits==16) {

        } else if(bits==32) {

        }
    }

    protected void write(Instru instru, InstruData data) {
        for (Object o : data.getCodes()) {
            if(o instanceof Number) {
                int n = ((Number) o).intValue();
                write8(n);
            } else if("ib".equals(o) || "ib,u".equals(o)) {
                write8(getImm(instru).intValue());
            } else if("/r".equals(o)) {
                processModRMSIB(instru,data);
            } else {
                if(!("hle".equals(o))) {
                    throw new RuntimeException("impl this");
                }
            }
        }
    }

    protected void process(Instru instru, InstruData data) {
        int position=output.getPosition();
        output.setMark(position);

        write(instru,data);

        int size=output.getPosition();
        byte[] buffer=output.getBuffer();
        writer.print("\t\t;#");
        for(int i=position;i<size;i++) {
            writer.print(' ');
            writer.print(NumberUtils.byte2Hex(buffer[i]));
        }
    }

    protected Number getImm(Instru instru) {
        List<List<String>> types = instru.getTypes();
        int n=0;
        Number r=null;
        for(int i=0;i<types.size();i++) {
            for (String s : types.get(i)) {
                if(s.startsWith("imm")) {
                    r=parseImm(instru.getTokens().get(i));
                    n++;
                }
            }
        }
        assert n==1;
        return r;
    }

    protected void process(Instru instru) {
        System.out.println(instru.getLine());
        InstruData data = findInstruData(instru);
        if (data.isSys()) {
            processSysInstruData(instru, data);
        } else {
            process(instru,data);
        }
    }

    protected InstruData findInstruData(Instru instru) {
        Collection<InstruData> list = InstruMap.get(instru.getKey());
        if (list != null) {
            for (InstruData data : list) {
                if (data.match(instru) > 0) {
                    return data;
                }
            }
        }
        return null;
    }

    protected void processSysInstruData(Instru instru, InstruData data) {
        if ("BITS".equals(instru.getKey())) {
            this.bits = parseImm(instru.getTokens().get(1)).intValue();
        }
    }

    public List<String> parseTokenTypes(String token) {
        List<String> list = new ArrayList<>();
        if (token.matches("^\\d+$")) {
            list.add("imm");
        }
        if(token.matches("^-\\d+$")) {
            list.add("imm");
        }
        if(token.startsWith("[") && token.endsWith("]")) {
            list.add("mem");
        }
        if(Character.isLetter(token.charAt(0))) {
            token = token.toUpperCase();
            RegData reg = RegMap.get(token);
            if (reg != null) {
                if (reg.getKlasses2().contains("reg8")) {
                    list.add("reg8");
                }
            }
        }
        return list;
    }

    public Number parseImm(String token) {
        return Long.parseLong(token);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("test stub");
        System.out.println(InstruData.Flags);
    }

    private static File tempNativeInput, tempNativeOutput,tempNativeError;
    private static File Nasm;

    public static synchronized Object nativeAssemble(String text) {
        try {
            if (tempNativeInput == null) {
                tempNativeInput = File.createTempFile("zpc", ".asm");
                tempNativeOutput = File.createTempFile("zpc", ".bin");
                tempNativeError = File.createTempFile("zpc", ".log");
                Nasm = new File("/Users/zms/workspace/api/jpc/gnu/nasm/nasm");
            }
            GarUtils.saveFile(tempNativeInput, text);
            ProcessBuilder pb = new ProcessBuilder(Nasm.getPath(), "-o", tempNativeOutput.getPath(), tempNativeInput.getPath());
            pb.redirectErrorStream(true);
            pb.directory(Nasm.getParentFile());
            pb.redirectOutput(tempNativeError);
            int code = pb.start().waitFor();
            if (code == 0) {
                byte[] bytes = GarUtils.readFile(tempNativeOutput);
                if (bytes != null && bytes.length > 0) {
                    return GarUtils.bytes2Plain(bytes);
                }
            }
            return GarUtils.loadFile(tempNativeError);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
