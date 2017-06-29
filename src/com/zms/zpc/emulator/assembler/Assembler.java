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

    protected void write16(int n) {
        output.write(n);
        output.write(n >>> 8);
    }

    protected void write32(int n) {
        output.write(n);
        output.write(n >>> 8);
        output.write(n >>> 16);
        output.write(n >>> 24);
    }

    protected void writen(int n, int bits) {
        switch (bits) {
            case 8:
                write8(n);
                return;
            case 16:
                write16(n);
                return;
            case 32:
                write32(n);
                return;
            default:
                throw new RuntimeException("impl this");
        }
    }

    public static Object[][][] ModData = new Object[][][]{
            {
                    {"AL", "AX", "EAX", "MM0", "XMM0"},
                    {"CL", "CX", "ECX", "MM1", "XMM1"},
                    {"DL", "DX", "EDX", "MM2", "XMM2"},
                    {"BL", "BX", "EBX", "MM3", "XMM3"},
                    {"AH", "SP", "ESP", "MM4", "XMM4"},
                    {"CH", "BP", "EBP", "MM5", "XMM5"},
                    {"DH", "SI", "ESI", "MM6", "XMM6"},
                    {"BH", "DI", "EDI", "MM7", "XMM7"}
            },
            {
                    {"EAX", "AX", "AL", "MM0", "XMM0"},
                    {"ECX", "CX", "CL", "MM1", "XMM1"},
                    {"EDX", "DX", "DL", "MM2", "XMM2"},
                    {"EBX", "BX", "BL", "MM3", "XMM3"},
                    {"ESP", "SP", "AH", "MM4", "XMM4"},
                    {"EBP", "BP", "CH", "MM5", "XMM5"},
                    {"ESI", "SI", "DH", "MM6", "XMM6"},
                    {"EDI", "DI", "BH", "MM7", "XMM7"}
            },
            {
                    {"BX+SI", "BX+DI", "BP+SI", "BP+DI", "SI", "DI", "BP", "BX"},  //BP  ....
                    {"EAX", "ECX", "EDX", "EBX", "ESP", "EBP", "ESI", "EDI"}  //ESP  EBP ....
            },
            {
                    {"AL","CL","DL","BL","AH","CH","DH","BH"},
                    {"AX","CX","DX","BX","SP","BP","SI","DI"},
                    {"EAX","ECX","EDX","EBX","ESP","EBP","ESI","EDI"},
                    {"RAX","RCX","RDX","RBX","RSP","RBP","RSI","RDI"},
                    {"MMX0","MMX1","MMX2","MMX3","MMX4","MMX5","MMX6","MMX7"},
                    {"XMM0","XMM1","XMM2","XMM3","XMM4","XMM5","XMM6","XMM7"},
                    {"ES","CS","SS","DS","FS","GS",null,null},
                    {"CR0","CR1","CR2","CR3","CR4","CR5","CR6","CR7"},
                    {"DR0","DR1","DR2","DR3","DR4","DR5","DR6","DR7"},
                    {"AL","CL","DL","BL","SPL","BPL","SIL","DIL"}
            },
            {
                    {"R8B","R9B","R10B","R11B","R12B","R13B","R14B","R15B"},
                    {"R8W","R9W","R10W","R11W","R12W","R13W","R14W","R15W"},
                    {"R8D","R9D","R10D","R11D","R12D","R13D","R14D","R15D"},
                    {"R8","R9","R10","R11","R12","R13","R14","R15"},
                    {"MMX","MMX1","MMX2","MMX3","MMX4","MMX5","MMX6","MMX7"},
                    {"XMM8","XMM9","XMM10","XMM11","XMM12","XMM13","XMM14","XMM15"},
                    {"ES","CS","SS","DS","FS","GS",null,null},
                    {"CR8","CR9","CR10","CR11","CR12","CR13","CR14","CR15"},
                    {"DR8","DR9","DR10","DR11","DR12","DR13","DR14","DR15"}
            }
    };

    protected void processModRMSIB(Instru instru, InstruData data) {
        String pr = data.getPr1();
        assert "mr".equals(pr) || "rm".equals(pr);
        String r2;
        String r1;
        if (pr.charAt(0) == 'r') {
            r2 = instru.getTokens().get(1);
            r1 = instru.getTokens().get(2);
        } else {
            r2 = instru.getTokens().get(2);
            r1 = instru.getTokens().get(1);
        }
        r2 = r2.toUpperCase();
        r1 = r1.toUpperCase();
        assert RegMap.containsKey(r2);

        int sib = -1;
        boolean hasDisp = false;
        int disp = -1;
        int dispLen = 0;

        int r2c = -1;
        out:
        for (int i = 0; i < 8; i++) {
            for (Object o : ModData[0][i]) {
                if (r2.equals(o)) {
                    r2c = i;
                    break out;
                }
            }
        }
        assert r2c >= 0;
        int r0c = -1;
        int r1c = -1;
        int regBits = 0;
        int addressBits = bits;
        if (r1.startsWith("[") && r1.endsWith("]")) {
            r1 = r1.substring(1, r1.length() - 1).replaceAll("\\s+", "").toUpperCase();
            if ("BP".equals(r1) || "EBP".equals(r1)) {
                r1 = r1 + "+0";
            }
            if (NumberUtils.isNumber(r1)) {
                long n = parseImm(r1).longValue();
                if (bits == 16) {
                    dispLen = 16;
                }
                if (bits == 32) {
                    dispLen = 32;
                }
                r0c = 0;
                r1c = 6;
                hasDisp = true;
                disp = (int) n;
                addressBits = dispLen;
            } else {
                int index = r1.lastIndexOf('-');
                if (index < 0) {
                    index = r1.lastIndexOf('+');
                }
                String ra;
                long n;
                if (index > 0 && NumberUtils.isNumber(ra = r1.substring(index))) {
                    n = NumberUtils.parseNumber(ra);
                    r1 = r1.substring(0, index);
                } else {
                    ra = null;
                    n = 0;
                }
                //System.out.println(n);
                String pattern;
                for (int i = 0; i < 8; i++) {
                    pattern = (String) ModData[2][0][i];
                    if (eqModRMSIB(r1, pattern)) {
                        r1c = i;
                        regBits = 16;
                        break;
                    }
                    pattern = (String) ModData[2][1][i];
                    if (eqModRMSIB(r1, pattern)) {
                        r1c = i;
                        regBits = 32;
                        break;
                    }
                }
                if (r1c == 4 && regBits == 32) {
                    assert "ESP".equals(r1);
                }
                if (r1c < 0) {
                    regBits = 32;
                    r1c = 4;
                }
                assert regBits > 0;
                if (regBits == 16) {
                    if (ra == null) {
                        r0c = 0;
                    } else if (NumberUtils.isIn8Bits(n)) {
                        r0c = 1;
                        dispLen = 8;
                    } else {
                        assert NumberUtils.isIn16Bits(n);
                        r0c = 2;
                        dispLen = 16;
                    }
                }
                if (regBits == 32) {
                    if (ra == null) {
                        r0c = 0;
                    } else if (NumberUtils.isIn8Bits(n)) {
                        r0c = 1;
                        dispLen = 8;
                    } else {
                        assert NumberUtils.isIn32Bits(n);
                        r0c = 2;
                        dispLen = 32;
                    }
                }
                if (r1c == 4 && regBits == 32) {
                    //sib
                    int k1 = -1;
                    int k2 = -1;
                    int k;
                    index = r1.indexOf('+');
                    if (index > 0) {
                        String p1 = r1.substring(0, index).trim();
                        String p2 = r1.substring(index + 1).trim();
                        if (p1.contains("*")) {
                            String s = p1;
                            p1 = p2;
                            p2 = s;
                        }
                        if (!p2.contains("*")) {
                            if ("ESP".equals(p2)) {
                                String s = p1;
                                p1 = p2;
                                p2 = s;
                            }
                            p2 = p2 + "*1";
                        }
                        assert !p1.contains("*");
                        index = p2.indexOf('*');
                        assert index > 0;
                        if (index < 3) {
                            k = Integer.parseInt(p2.substring(0, index).trim());
                            p2 = p2.substring(index + 1).trim();
                        } else {
                            k = Integer.parseInt(p2.substring(index + 1).trim());
                            p2 = p2.substring(0, index).trim();
                        }
                        assert !"ESP".equals(p2);
                        for (int i = 0; i < 8; i++) {
                            pattern = (String) ModData[2][1][i];
                            if (p1.equals(pattern)) {
                                k1 = i;
                            }
                            if (p2.equals(pattern)) {
                                k2 = i;
                            }
                        }
                        if ("EBP".equals(p1) || k1 == 5) {
                            assert "EBP".equals(p1) && k1 == 5;
                            if (r0c == 0) {
                                r0c = 1;
                                ra = "+0";
                                n = 0;
                                dispLen = 8;
                            }
                        }
                    } else {
                        String p2 = r1;
                        if (!p2.contains("*")) {
                            p2 = p2 + "*1";
                        }
                        index = p2.indexOf('*');
                        assert index > 0;
                        if (index < 3) {
                            k = Integer.parseInt(p2.substring(0, index).trim());
                            p2 = p2.substring(index + 1).trim();
                        } else {
                            k = Integer.parseInt(p2.substring(index + 1).trim());
                            p2 = p2.substring(0, index).trim();
                        }
                        if ("ESP".equals(p2)) {
                            assert k == 1;
                            k1 = 4;
                            k2 = 4;
                        } else {
                            k1 = 5;
                            for (int i = 0; i < 8; i++) {
                                pattern = (String) ModData[2][1][i];
                                if (p2.equals(pattern)) {
                                    k2 = i;
                                    break;
                                }
                            }
                            if (r0c == 0) {
                                ra = "+0";
                                n = 0;
                            } else {
                                r0c = 0;
                            }
                            dispLen = 32;
                        }
                    }
                    assert k1 >= 0 && k2 >= 0;
                    assert k == 1 || k == 2 || k == 4 || k == 8;
                    if (k == 2) {
                        k = 1;
                    } else if (k == 4) {
                        k = 2;
                    } else if (k == 8) {
                        k = 3;
                    } else {
                        k = 0;
                    }
                    sib = 0x40 * k + k2 * 8 + k1;
                }
                hasDisp = ra != null;
                disp = (int) n;
                addressBits = regBits;
            }
        } else {
            assert RegMap.containsKey(r1);
            r0c = 3;
            out:
            for (int i = 0; i < 8; i++) {
                for (Object o : ModData[1][i]) {
                    if (r1.equals(o)) {
                        r1c = i;
                        break out;
                    }
                }
            }
        }
        assert r0c >= 0;
        assert r1c >= 0;
        if (bits != addressBits) {
            output.addInMark(0x67);
        }
        int modrm = 0x40 * r0c + r2c * 8 + r1c;
        write8(modrm);
        if (sib >= 0) {
            write8(sib);
        }
        if (hasDisp) {
            assert dispLen > 0;
            writen(disp, dispLen);
        }
    }

    private boolean eqModRMSIB(String r1, String pattern) {
        if (pattern.equals(r1)) {
            return true;
        }
        int index = pattern.indexOf('+');
        if (index > 0 && r1.contains("+")) {
            String p = pattern.substring(index + 1) + '+' + pattern.substring(0, index);
            return p.equals(r1);
        }
        return false;
    }

    protected void write(Instru instru, InstruData data) {
        for (Object o : data.getCodes()) {
            if (o instanceof Number) {
                int n = ((Number) o).intValue();
                write8(n);
            } else if ("ib".equals(o) || "ib,u".equals(o)) {
                write8(getImm(instru).intValue());
            } else if ("/r".equals(o)) {
                processModRMSIB(instru, data);
            } else if ("o16".equals(o)) {
                if (bits == 32) {
                    write8(0x66);
                }
            } else if ("o32".equals(o)) {
                if (bits == 16) {
                    write8(0x66);
                }
            } else {
                if (!("hle".equals(o))) {
                    throw new RuntimeException("impl this");
                }
            }
        }
    }

    protected void process(Instru instru, InstruData data) {
        int position = output.getPosition();
        output.setMark(position);

        write(instru, data);

        int size = output.getPosition();
        byte[] buffer = output.getBuffer();
        {
            if ((size - position) > 1) {
                if (buffer[position] == 0x67 && buffer[position + 1] == 0x66) {
                    buffer[position] = 0x66;
                    buffer[position + 1] = 0x67;
                }
            }
        }
        writer.print("\t\t;#");
        for (int i = position; i < size; i++) {
            writer.print(' ');
            writer.print(NumberUtils.byte2Hex(buffer[i]));
        }
    }

    protected Number getImm(Instru instru) {
        List<List<String>> types = instru.getTypes();
        int n = 0;
        Number r = null;
        for (int i = 0; i < types.size(); i++) {
            for (String s : types.get(i)) {
                if (s.startsWith("imm")) {
                    r = parseImm(instru.getTokens().get(i));
                    n++;
                }
            }
        }
        assert n == 1;
        return r;
    }

    protected void process(Instru instru) {
        //System.out.println(instru.getLine());
        InstruData data = findInstruData(instru);
        if (data.isSys()) {
            processSysInstruData(instru, data);
        } else {
            process(instru, data);
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
        if (NumberUtils.isNumber(token)) {
            list.add("imm");
        }
        if (token.startsWith("[") && token.endsWith("]")) {
            list.add("mem");
        }
        if (Character.isLetter(token.charAt(0))) {
            token = token.toUpperCase();
            RegData reg = RegMap.get(token);
            if (reg != null) {
                list.addAll(reg.getKlasses2());
            }
        }
        return list;
    }

    public Number parseImm(String token) {
        return NumberUtils.parseNumber(token);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("test stub");
        System.out.println(InstruData.Flags);
    }

    public static File tempNativeInput, tempNativeOutput, tempNativeError;
    public static File Nasm, Ndisasm;

    public static void initNative() throws Exception {
        if (tempNativeInput == null) {
            tempNativeInput = File.createTempFile("zpc", ".asm");
            tempNativeOutput = File.createTempFile("zpc", ".bin");
            tempNativeError = File.createTempFile("zpc", ".log");
            Nasm = new File("/Users/zms/workspace/api/jpc/gnu/nasm/nasm");
            Ndisasm = new File("/Users/zms/workspace/api/jpc/gnu/nasm/ndisasm");
        }
    }

    public static synchronized Object nativeAssemble(String text) {
        try {
            initNative();
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
