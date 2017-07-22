package com.zms.zpc.execute;

import com.zms.zpc.emulator.PC;
import com.zms.zpc.emulator.processor.Regs;
import com.zms.zpc.support.NotImplException;

import static com.zms.zpc.support.Constants.*;

/**
 * Created by 张小美 on 17/六月/25.
 * Copyright 2002-2016
 */
public class CodeExecutor {

    private int bits = 16;
    private byte[] previewBuffer;
    private InstructionExecutor2 instruction;
    private Regs regs;

    public CodeExecutor() {
        previewBuffer = new byte[128];
        instruction = new InstructionExecutor2();
        instruction.executor = this;
    }

    protected void pre(PC pc) {
        regs = pc.cpu.regs;
        if (regs.bits.pe.get()) {
            throw new NotImplException();
        } else {
            bits = 16;
        }
        instruction.mrs.reg8 = false;
        instruction.bits = regs.bits;
        instruction.mrs.regType = 0;
        instruction.segBase = "DS";
        instruction.mrs.opWidth = -1;
        instruction.pc = pc;
    }

    public int execute(PC pc, CodeStream input) {
        pre(pc);
        instruction.input = input;
        instruction.setStartPos(input.getPos());
        instruction.parse1(bits);
        boolean jump = false;
        ModRMSIB mrs = instruction.mrs;
        int op = instruction.getOpcode();
        switch (op) {
            case 0x00:
                //ADD		mem,reg8			[mr:	hle 00 /r]				8086,SM,LOCK
                //ADD		reg8,reg8			[mr:	00 /r]					8086
                mrs.reg8 = true;
            case 0x01:
                //ADD		mem,reg16			[mr:	hle o16 01 /r]				8086,SM,LOCK
                //ADD		reg16,reg16			[mr:	o16 01 /r]				8086
                //ADD		mem,reg32			[mr:	hle o32 01 /r]				386,SM,LOCK
                //ADD		reg32,reg32			[mr:	o32 01 /r]				386
                //ADD		mem,reg64			[mr:	hle o64 01 /r]				X64,SM,LOCK
                //ADD		reg64,reg64			[mr:	o64 01 /r]				X64
                instruction.parse2(bits);
                instruction.executeCal1(ADD, false);
                break;

            case 0x02:
                //ADD		reg8,mem			[rm:	02 /r]					8086,SM
                //ADD		reg8,reg8			[rm:	02 /r]					8086
                mrs.reg8 = true;
            case 0x03:
                //ADD		reg16,mem			[rm:	o16 03 /r]				8086,SM
                //ADD		reg16,reg16			[rm:	o16 03 /r]				8086
                //ADD		reg32,mem			[rm:	o32 03 /r]				386,SM
                //ADD		reg32,reg32			[rm:	o32 03 /r]				386
                //ADD		reg64,mem			[rm:	o64 03 /r]				X64,SM
                //ADD		reg64,reg64			[rm:	o64 03 /r]				X64
                instruction.parse2(bits);
                instruction.executeCal1(ADD, true);
                break;

            case 0x04:
                //ADD		reg_al,imm			[-i:	04 ib]					8086,SM
                mrs.reg8 = true;
            case 0x05:
                //ADD		reg_ax,imm			[-i:	o16 05 iw]				8086,SM
                //ADD		reg_eax,imm			[-i:	o32 05 id]				386,SM
                //ADD		reg_rax,imm			[-i:	o64 05 id,s]				X64,SM
                instruction.executeCal2(ADD);
                break;

            case 0x06:
                //PUSH		reg_es				[-:	06]					8086,NOLONG
                instruction.executePush(regs.es, instruction.getOpWidth(getBits()));
                break;

            case 0x07:
                //POP		reg_es				[-:	07]					8086,NOLONG
                instruction.executePop(regs.es, instruction.getOpWidth(getBits()));
                break;

            case 0x08:
                //OR		mem,reg8			[mr:	hle 08 /r]				8086,SM,LOCK
                //OR		reg8,reg8			[mr:	08 /r]					8086
                mrs.reg8 = true;
            case 0x09:
                //OR		mem,reg16			[mr:	hle o16 09 /r]				8086,SM,LOCK
                //OR		reg16,reg16			[mr:	o16 09 /r]				8086
                //OR		mem,reg32			[mr:	hle o32 09 /r]				386,SM,LOCK
                //OR		reg32,reg32			[mr:	o32 09 /r]				386
                //OR		mem,reg64			[mr:	hle o64 09 /r]				X64,SM,LOCK
                //OR		reg64,reg64			[mr:	o64 09 /r]				X64

                instruction.parse2(bits);
                instruction.executeCal1(OR, false);
                break;

            case 0x0a:
                //OR		reg8,mem			[rm:	0a /r]					8086,SM
                //OR		reg8,reg8			[rm:	0a /r]					8086
                mrs.reg8 = true;
            case 0x0b:
                //OR		reg16,mem			[rm:	o16 0b /r]				8086,SM
                //OR		reg16,reg16			[rm:	o16 0b /r]				8086
                //OR		reg32,mem			[rm:	o32 0b /r]				386,SM
                //OR		reg32,reg32			[rm:	o32 0b /r]				386
                //OR		reg64,mem			[rm:	o64 0b /r]				X64,SM
                //OR		reg64,reg64			[rm:	o64 0b /r]				X64

                instruction.parse2(bits);
                instruction.executeCal1(OR, true);
                break;

            case 0x0c:
                //OR		reg_al,imm			[-i:	0c ib]					8086,SM
                mrs.reg8 = true;
            case 0x0d:
                //OR		reg_ax,imm			[-i:	o16 0d iw]				8086,SM
                //OR		reg_eax,imm			[-i:	o32 0d id]				386,SM
                //OR		reg_rax,imm			[-i:	o64 0d id,s]				X64,SM

                instruction.executeCal2(OR);
                break;

            case 0x0e:
                //PUSH		reg_cs				[-:	0e]					8086,NOLONG
                instruction.executePush(regs.cs, instruction.getOpWidth(getBits()));
                break;

            case 0x0f:
                //POP		reg_cs				[-:	0f]					8086,UNDOC,ND,OBSOLETE  ： use RET

                instruction.readNextOp();
                op = instruction.getOpcode(1);
                switch (op) {
                    case 0xa0:
                        //PUSH		reg_fs				[-:	0f a0]					386
                        instruction.executePush(regs.fs, instruction.getOpWidth(getBits()));
                        break;

                    case 0xa1:
                        //POP		reg_fs				[-:	0f a1]					386
                        instruction.executePop(regs.fs, instruction.getOpWidth(getBits()));
                        break;

                    case 0xa8:
                        //PUSH		reg_gs				[-:	0f a8]					386
                        instruction.executePush(regs.gs, instruction.getOpWidth(getBits()));
                        break;

                    case 0xa9:
                        //POP		reg_gs				[-:	0f a9]					386
                        instruction.executePop(regs.gs, instruction.getOpWidth(getBits()));
                        break;
                    default:
                        throw new NotImplException("op2: " + op);
                }
                break;

            case 0x10:
                //ADC		mem,reg8			[mr:	hle 10 /r]				8086,SM,LOCK
                //ADC		reg8,reg8			[mr:	10 /r]					8086
                mrs.reg8 = true;
            case 0x11:
                //ADC		mem,reg16			[mr:	hle o16 11 /r]				8086,SM,LOCK
                //ADC		reg16,reg16			[mr:	o16 11 /r]				8086
                //ADC		mem,reg32			[mr:	hle o32 11 /r]				386,SM,LOCK
                //ADC		reg32,reg32			[mr:	o32 11 /r]				386
                //ADC		mem,reg64			[mr:	hle o64 11 /r]				X64,SM,LOCK
                //ADC		reg64,reg64			[mr:	o64 11 /r]				X64

                instruction.parse2(bits);
                instruction.executeCal1(ADC, false);
                break;

            case 0x12:
                //ADC		reg8,mem			[rm:	12 /r]					8086,SM
                //ADC		reg8,reg8			[rm:	12 /r]					8086
                mrs.reg8 = true;
            case 0x13:
                //ADC		reg16,mem			[rm:	o16 13 /r]				8086,SM
                //ADC		reg16,reg16			[rm:	o16 13 /r]				8086
                //ADC		reg32,mem			[rm:	o32 13 /r]				386,SM
                //ADC		reg32,reg32			[rm:	o32 13 /r]				386
                //ADC		reg64,mem			[rm:	o64 13 /r]				X64,SM
                //ADC		reg64,reg64			[rm:	o64 13 /r]				X64

                instruction.parse2(bits);
                instruction.executeCal1(ADC, true);
                break;

            case 0x14:
                //ADC		reg_al,imm			[-i:	14 ib]					8086,SM
                mrs.reg8 = true;
            case 0x15:
                //ADC		reg_ax,imm			[-i:	o16 15 iw]				8086,SM
                //ADC		reg_eax,imm			[-i:	o32 15 id]				386,SM
                //ADC		reg_rax,imm			[-i:	o64 15 id,s]				X64,SM
                instruction.executeCal2(ADC);
                break;

            case 0x16:
                //PUSH		reg_ss				[-:	16]					8086,NOLONG
                instruction.executePush(regs.ss, instruction.getOpWidth(getBits()));
                break;

            case 0x17:
                //POP		reg_ss				[-:	17]					8086,NOLONG
                instruction.executePop(regs.ss, instruction.getOpWidth(getBits()));
                break;

            case 0x18:
                //SBB		mem,reg8			[mr:	hle 18 /r]				8086,SM,LOCK
                //SBB		reg8,reg8			[mr:	18 /r]					8086
                mrs.reg8 = true;
            case 0x19:
                //SBB		mem,reg16			[mr:	hle o16 19 /r]				8086,SM,LOCK
                //SBB		reg16,reg16			[mr:	o16 19 /r]				8086
                //SBB		mem,reg32			[mr:	hle o32 19 /r]				386,SM,LOCK
                //SBB		reg32,reg32			[mr:	o32 19 /r]				386
                //SBB		mem,reg64			[mr:	hle o64 19 /r]				X64,SM,LOCK
                //SBB		reg64,reg64			[mr:	o64 19 /r]				X64

                instruction.parse2(bits);
                instruction.executeCal1(SBB, false);
                break;

            case 0x1a:
                //SBB		reg8,mem			[rm:	1a /r]					8086,SM
                //SBB		reg8,reg8			[rm:	1a /r]					8086
                mrs.reg8 = true;
            case 0x1b:
                //SBB		reg16,mem			[rm:	o16 1b /r]				8086,SM
                //SBB		reg16,reg16			[rm:	o16 1b /r]				8086
                //SBB		reg32,mem			[rm:	o32 1b /r]				386,SM
                //SBB		reg32,reg32			[rm:	o32 1b /r]				386
                //SBB		reg64,mem			[rm:	o64 1b /r]				X64,SM
                //SBB		reg64,reg64			[rm:	o64 1b /r]				X64

                instruction.parse2(bits);
                instruction.executeCal1(SBB, true);
                break;

            case 0x1c:
                //SBB		reg_al,imm			[-i:	1c ib]					8086,SM
                mrs.reg8 = true;
            case 0x1d:
                //SBB		reg_ax,imm			[-i:	o16 1d iw]				8086,SM
                //SBB		reg_eax,imm			[-i:	o32 1d id]				386,SM
                //SBB		reg_rax,imm			[-i:	o64 1d id,s]				X64,SM

                instruction.executeCal2(SBB);
                break;

            case 0x1e:
                //PUSH		reg_ds				[-:	1e]					8086,NOLONG
                instruction.executePush(regs.ds, instruction.getOpWidth(getBits()));
                break;

            case 0x1f:
                //POP		reg_ds				[-:	1f]					8086,NOLONG
                instruction.executePop(regs.ds, instruction.getOpWidth(getBits()));
                break;

            case 0x20:
                //AND		mem,reg8			[mr:	hle 20 /r]				8086,SM,LOCK
                //AND		reg8,reg8			[mr:	20 /r]					8086

                mrs.reg8 = true;
            case 0x21:
                //AND		mem,reg16			[mr:	hle o16 21 /r]				8086,SM,LOCK
                //AND		reg16,reg16			[mr:	o16 21 /r]				8086
                //AND		mem,reg32			[mr:	hle o32 21 /r]				386,SM,LOCK
                //AND		reg32,reg32			[mr:	o32 21 /r]				386
                //AND		mem,reg64			[mr:	hle o64 21 /r]				X64,SM,LOCK
                //AND		reg64,reg64			[mr:	o64 21 /r]				X64

                instruction.parse2(bits);
                instruction.executeCal1(AND, false);
                break;
            case 0x22:
                //AND		reg8,mem			[rm:	22 /r]					8086,SM
                //AND		reg8,reg8			[rm:	22 /r]					8086
                mrs.reg8 = true;
            case 0x23:
                //AND		reg16,mem			[rm:	o16 23 /r]				8086,SM
                //AND		reg16,reg16			[rm:	o16 23 /r]				8086
                //AND		reg32,mem			[rm:	o32 23 /r]				386,SM
                //AND		reg32,reg32			[rm:	o32 23 /r]				386
                //AND		reg64,mem			[rm:	o64 23 /r]				X64,SM
                //AND		reg64,reg64			[rm:	o64 23 /r]				X64

                instruction.parse2(bits);
                instruction.executeCal1(AND, true);
                break;

            case 0x24:
                //AND		reg_al,imm			[-i:	24 ib]					8086,SM
                mrs.reg8 = true;
            case 0x25:
                //AND		reg_ax,imm			[-i:	o16 25 iw]				8086,SM
                //AND		reg_eax,imm			[-i:	o32 25 id]				386,SM
                //AND		reg_rax,imm			[-i:	o64 25 id,s]				X64,SM

                instruction.executeCal2(AND);
                break;

            case 0x28:
                //SUB		mem,reg8			[mr:	hle 28 /r]				8086,SM,LOCK
                //SUB		reg8,reg8			[mr:	28 /r]					8086
                mrs.reg8 = true;
            case 0x29:
                //SUB		mem,reg16			[mr:	hle o16 29 /r]				8086,SM,LOCK
                //SUB		reg16,reg16			[mr:	o16 29 /r]				8086
                //SUB		mem,reg32			[mr:	hle o32 29 /r]				386,SM,LOCK
                //SUB		reg32,reg32			[mr:	o32 29 /r]				386
                //SUB		mem,reg64			[mr:	hle o64 29 /r]				X64,SM,LOCK
                //SUB		reg64,reg64			[mr:	o64 29 /r]				X64

                instruction.parse2(bits);
                instruction.executeCal1(SUB, false);
                break;

            case 0x2a:
                //SUB		reg8,mem			[rm:	2a /r]					8086,SM
                //SUB		reg8,reg8			[rm:	2a /r]					8086
                mrs.reg8 = true;
            case 0x2b:
                //SUB		reg16,mem			[rm:	o16 2b /r]				8086,SM
                //SUB		reg16,reg16			[rm:	o16 2b /r]				8086
                //SUB		reg32,mem			[rm:	o32 2b /r]				386,SM
                //SUB		reg32,reg32			[rm:	o32 2b /r]				386
                //SUB		reg64,mem			[rm:	o64 2b /r]				X64,SM
                //SUB		reg64,reg64			[rm:	o64 2b /r]				X64

                instruction.parse2(bits);
                instruction.executeCal1(SUB, true);
                break;

            case 0x2c:
                //SUB		reg_al,imm			[-i:	2c ib]					8086,SM
                mrs.reg8 = true;
            case 0x2d:
                //SUB		reg_ax,imm			[-i:	o16 2d iw]				8086,SM
                //SUB		reg_eax,imm			[-i:	o32 2d id]				386,SM
                //SUB		reg_rax,imm			[-i:	o64 2d id,s]				X64,SM

                instruction.executeCal2(SUB);
                break;

            case 0x30:
                //XOR		mem,reg8			[mr:	hle 30 /r]				8086,SM,LOCK
                //XOR		reg8,reg8			[mr:	30 /r]					8086

                //org.jpc.emulator.execution.opcodes.rm.xor_Eb_Gb_mem
                //org.jpc.emulator.execution.opcodes.rm.xor_Ew_Gw

                mrs.reg8 = true;
            case 0x31:
                //XOR		mem,reg16			[mr:	hle o16 31 /r]				8086,SM,LOCK
                //XOR		reg16,reg16			[mr:	o16 31 /r]				8086
                //XOR		mem,reg32			[mr:	hle o32 31 /r]				386,SM,LOCK
                //XOR		reg32,reg32			[mr:	o32 31 /r]				386
                //XOR		mem,reg64			[mr:	hle o64 31 /r]				X64,SM,LOCK
                //XOR		reg64,reg64			[mr:	o64 31 /r]				X64

                //org.jpc.emulator.execution.opcodes.rm.xor_Ew_Gw_mem
                //org.jpc.emulator.execution.opcodes.rm.xor_Ew_Gw

                instruction.parse2(bits);
                instruction.executeCal1(XOR, false);
                break;

            case 0x32:
                //XOR		reg8,mem			[rm:	32 /r]					8086,SM
                //XOR		reg8,reg8			[rm:	32 /r]					8086

                mrs.reg8 = true;
            case 0x33:
                //XOR		reg16,mem			[rm:	o16 33 /r]				8086,SM
                //XOR		reg16,reg16			[rm:	o16 33 /r]				8086
                //XOR		reg32,mem			[rm:	o32 33 /r]				386,SM
                //XOR		reg32,reg32			[rm:	o32 33 /r]				386
                //XOR		reg64,mem			[rm:	o64 33 /r]				X64,SM
                //XOR		reg64,reg64			[rm:	o64 33 /r]				X64

                instruction.parse2(bits);
                instruction.executeCal1(XOR, true);
                break;

            case 0x34:
                //XOR		reg_al,imm			[-i:	34 ib]					8086,SM

                mrs.reg8 = true;
            case 0x35:
                //XOR		reg_ax,imm			[-i:	o16 35 iw]				8086,SM
                //XOR		reg_eax,imm			[-i:	o32 35 id]				386,SM
                //XOR		reg_rax,imm			[-i:	o64 35 id,s]				X64,SM

                instruction.executeCal2(XOR);
                break;

            case 0x38:
                //CMP		mem,reg8			[mr:	38 /r]					8086,SM
                //CMP		reg8,reg8			[mr:	38 /r]					8086

                //org.jpc.emulator.execution.opcodes.rm.cmp_Eb_Gb_mem

                mrs.reg8 = true;
            case 0x39:
                //CMP		mem,reg16			[mr:	o16 39 /r]				8086,SM
                //CMP		reg16,reg16			[mr:	o16 39 /r]				8086
                //CMP		mem,reg32			[mr:	o32 39 /r]				386,SM
                //CMP		reg32,reg32			[mr:	o32 39 /r]				386
                //CMP		mem,reg64			[mr:	o64 39 /r]				X64,SM
                //CMP		reg64,reg64			[mr:	o64 39 /r]				X64

                instruction.parse2(bits);
                instruction.executeCal1(CMP, false);
                break;

            case 0x3a:
                //CMP		reg8,mem			[rm:	3a /r]					8086,SM
                //CMP		reg8,reg8			[rm:	3a /r]					8086

                mrs.reg8 = true;
            case 0x3b:
                //CMP		reg16,mem			[rm:	o16 3b /r]				8086,SM
                //CMP		reg16,reg16			[rm:	o16 3b /r]				8086
                //CMP		reg32,mem			[rm:	o32 3b /r]				386,SM
                //CMP		reg32,reg32			[rm:	o32 3b /r]				386
                //CMP		reg64,mem			[rm:	o64 3b /r]				X64,SM
                //CMP		reg64,reg64			[rm:	o64 3b /r]				X64

                instruction.parse2(bits);
                instruction.executeCal1(CMP, true);
                break;

            case 0x3c:
                //CMP		reg_al,imm			[-i:	3c ib]					8086,SM
                mrs.reg8 = true;
            case 0x3d:
                //CMP		reg_ax,imm			[-i:	o16 3d iw]				8086,SM
                //CMP		reg_eax,imm			[-i:	o32 3d id]				386,SM
                //CMP		reg_rax,imm			[-i:	o64 3d id,s]				X64,SM

                instruction.executeCal2(CMP);
                break;

            case 0x40:
            case 0x41:
            case 0x42:
            case 0x43:
            case 0x44:
            case 0x45:
            case 0x46:
            case 0x47:
                //INC		reg16				[r:	o16 40+r]				8086,NOLONG
                //INC		reg32				[r:	o32 40+r]				386,NOLONG

                instruction.executeIncReg();
                break;

            case 0x50:
            case 0x51:
            case 0x52:
            case 0x53:
            case 0x54:
            case 0x55:
            case 0x56:
            case 0x57:
                //PUSH		reg16				[r:	o16 50+r]				8086
                //PUSH reg32				[r:	o32 50+r]				386,NOLONG
                //PUSH		reg64				[r:	o64nw 50+r]				X64

                //org.jpc.emulator.execution.opcodes.rm.push_o16_rBXr11

                instruction.executePush50();
                break;

            case 0x58:
            case 0x59:
            case 0x5a:
            case 0x5b:
            case 0x5c:
            case 0x5d:
            case 0x5e:
            case 0x5f:
                //POP reg16				[r:	o16 58+r]				8086
                //POP		reg32				[r:	o32 58+r]				386,NOLONG
                //POP		reg64				[r:	o64nw 58+r]				X64

                instruction.executePop58();
                break;

            case 0x70:
            case 0x71:
            case 0x72:
            case 0x73:
            case 0x74:
            case 0x75:
            case 0x76:
            case 0x77:
            case 0x78:
            case 0x79:
            case 0x7a:
            case 0x7b:
            case 0x7c:
            case 0x7d:
            case 0x7e:
            case 0x7f:
                mrs.reg8 = true;
                jump = instruction.executeJcc();
                break;

            case 0x80:
                //ADD		rm8,imm				[mi:	hle 80 /0 ib]				8086,SM,LOCK
                //ADD		mem,imm8			[mi:	hle 80 /0 ib]				8086,SM,LOCK
                //OR		rm8,imm				[mi:	hle 80 /1 ib]				8086,SM,LOCK
                //OR		mem,imm8			[mi:	hle 80 /1 ib]				8086,SM,LOCK
                //ADC		rm8,imm				[mi:	hle 80 /2 ib]				8086,SM,LOCK
                //ADC		mem,imm8			[mi:	hle 80 /2 ib]				8086,SM,LOCK,ND
                //SBB		rm8,imm				[mi:	hle 80 /3 ib]				8086,SM,LOCK
                //SBB		mem,imm8			[mi:	hle 80 /3 ib]				8086,SM,LOCK
                //AND		rm8,imm				[mi:	hle 80 /4 ib]				8086,SM,LOCK
                //AND		mem,imm8			[mi:	hle 80 /4 ib]				8086,SM,LOCK
                //SUB		rm8,imm				[mi:	hle 80 /5 ib]				8086,SM,LOCK
                //SUB		mem,imm8			[mi:	hle 80 /5 ib]				8086,SM,LOCK
                //XOR		rm8,imm				[mi:	hle 80 /6 ib]				8086,SM,LOCK
                //XOR		mem,imm8			[mi:	hle 80 /6 ib]				8086,SM,LOCK
                //CMP		rm8,imm				[mi:	80 /7 ib]				8086,SM
                //CMP		mem,imm8			[mi:	80 /7 ib]				8086,SM
            case 0x82:
                //ADD		rm8,imm				[mi:	hle 82 /0 ib]				8086,SM,LOCK,ND,NOLONG
                //OR		rm8,imm				[mi:	hle 82 /1 ib]				8086,SM,LOCK,ND,NOLONG
                //ADC		rm8,imm				[mi:	hle 82 /2 ib]				8086,SM,LOCK,ND,NOLONG
                //SBB		rm8,imm				[mi:	hle 82 /3 ib]				8086,SM,LOCK,ND,NOLONG
                //AND		rm8,imm				[mi:	hle 82 /4 ib]				8086,SM,LOCK,ND,NOLONG
                //SUB		rm8,imm				[mi:	hle 82 /5 ib]				8086,SM,LOCK,ND,NOLONG
                //XOR		rm8,imm				[mi:	hle 82 /6 ib]				8086,SM,LOCK,ND,NOLONG
                //CMP		rm8,imm				[mi:	82 /7 ib]				8086,SM,ND,NOLONG
                mrs.reg8 = true;
            case 0x81:
                instruction.parse2(bits);
                /*
                switch (mrs.regIndex) {
                    case 0:
                        //ADD		rm16,imm			[mi:	hle o16 81 /0 iw]			8086,SM,LOCK
                        //ADD		rm32,imm			[mi:	hle o32 81 /0 id]			386,SM,LOCK
                        //ADD		rm64,imm			[mi:	hle o64 81 /0 id,s]			X64,SM,LOCK
                        //ADD		mem,imm16			[mi:	hle o16 81 /0 iw]			8086,SM,LOCK
                        //ADD		mem,imm32			[mi:	hle o32 81 /0 id]			386,SM,LOCK
                    case 1:
                        //OR		rm16,imm			[mi:	hle o16 81 /1 iw]			8086,SM,LOCK
                        //OR		rm32,imm			[mi:	hle o32 81 /1 id]			386,SM,LOCK
                        //OR		rm64,imm			[mi:	hle o64 81 /1 id,s]			X64,SM,LOCK
                        //OR		mem,imm16			[mi:	hle o16 81 /1 iw]			8086,SM,LOCK
                        //OR		mem,imm32			[mi:	hle o32 81 /1 id]			386,SM,LOCK
                    case 2:
                        //ADC		rm16,imm			[mi:	hle o16 81 /2 iw]			8086,SM,LOCK
                        //ADC		rm32,imm			[mi:	hle o32 81 /2 id]			386,SM,LOCK
                        //ADC		rm64,imm			[mi:	hle o64 81 /2 id,s]			X64,SM,LOCK
                        //ADC		mem,imm16			[mi:	hle o16 81 /2 iw]			8086,SM,LOCK
                        //ADC		mem,imm32			[mi:	hle o32 81 /2 id]			386,SM,LOCK
                    case 3:
                        //SBB		rm16,imm			[mi:	hle o16 81 /3 iw]			8086,SM,LOCK
                        //SBB		rm32,imm			[mi:	hle o32 81 /3 id]			386,SM,LOCK
                        //SBB		rm64,imm			[mi:	hle o64 81 /3 id,s]			X64,SM,LOCK
                        //SBB		mem,imm16			[mi:	hle o16 81 /3 iw]			8086,SM,LOCK
                        //SBB		mem,imm32			[mi:	hle o32 81 /3 id]			386,SM,LOCK
                    case 4:
                        //AND		rm16,imm			[mi:	hle o16 81 /4 iw]			8086,SM,LOCK
                        //AND		rm32,imm			[mi:	hle o32 81 /4 id]			386,SM,LOCK
                        //AND		rm64,imm			[mi:	hle o64 81 /4 id,s]			X64,SM,LOCK
                        //AND		mem,imm16			[mi:	hle o16 81 /4 iw]			8086,SM,LOCK
                        //AND		mem,imm32			[mi:	hle o32 81 /4 id]			386,SM,LOCK
                    case 5:
                        //SUB		rm16,imm			[mi:	hle o16 81 /5 iw]			8086,SM,LOCK
                        //SUB		rm32,imm			[mi:	hle o32 81 /5 id]			386,SM,LOCK
                        //SUB		rm64,imm			[mi:	hle o64 81 /5 id,s]			X64,SM,LOCK
                        //SUB		mem,imm16			[mi:	hle o16 81 /5 iw]			8086,SM,LOCK
                        //SUB		mem,imm32			[mi:	hle o32 81 /5 id]			386,SM,LOCK
                    case 6:
                        //XOR		rm16,imm			[mi:	hle o16 81 /6 iw]			8086,SM,LOCK
                        //XOR		rm32,imm			[mi:	hle o32 81 /6 id]			386,SM,LOCK
                        //XOR		rm64,imm			[mi:	hle o64 81 /6 id,s]			X64,SM,LOCK
                        //XOR		mem,imm16			[mi:	hle o16 81 /6 iw]			8086,SM,LOCK
                        //XOR		mem,imm32			[mi:	hle o32 81 /6 id]			386,SM,LOCK
                    case 7:
                        //CMP		rm16,imm			[mi:	o16 81 /7 iw]				8086,SM
                        //CMP		rm32,imm			[mi:	o32 81 /7 id]				386,SM
                        //CMP		rm64,imm			[mi:	o64 81 /7 id,s]				X64,SM
                        //CMP		mem,imm16			[mi:	o16 81 /7 iw]				8086,SM
                        //CMP		mem,imm32			[mi:	o32 81 /7 id]				386,SM
                        instruction.executeCmp82();
                        break;
                    default:
                        throw new NotImplException();
                }
                */
                instruction.executeCal3();
                break;

            case 0x83:
                instruction.parse2(bits);
                /*
                switch (mrs.regIndex) {

                    case 0:
                        //ADD		rm16,imm8			[mi:	hle o16 83 /0 ib,s]			8086,LOCK
                        //ADD		rm32,imm8			[mi:	hle o32 83 /0 ib,s]			386,LOCK
                        //ADD		rm64,imm8			[mi:	hle o64 83 /0 ib,s]			X64,LOCK
                        //ADD		reg_ax,sbyteword		[mi:	o16 83 /0 ib,s]				8086,SM,ND
                        //ADD		reg_eax,sbytedword		[mi:	o32 83 /0 ib,s]				386,SM,ND
                        //ADD		reg_rax,sbytedword		[mi:	o64 83 /0 ib,s]				X64,SM,ND
                        //ADD		rm16,sbyteword			[mi:	hle o16 83 /0 ib,s]			8086,SM,LOCK,ND
                        //ADD		rm32,sbytedword			[mi:	hle o32 83 /0 ib,s]			386,SM,LOCK,ND
                        //ADD		rm64,sbytedword			[mi:	hle o64 83 /0 ib,s]			X64,SM,LOCK,ND
                        //ADD		mem,sbyteword16			[mi:	hle o16 83 /0 ib,s]			8086,SM,LOCK,ND
                        //ADD		mem,sbytedword32		[mi:	hle o32 83 /0 ib,s]			386,SM,LOCK,ND

                        instruction.executeAdd2();
                        break;

                    case 1:
                        //OR		rm16,imm8			[mi:	hle o16 83 /1 ib,s]			8086,LOCK
                        //OR		rm32,imm8			[mi:	hle o32 83 /1 ib,s]			386,LOCK
                        //OR		rm64,imm8			[mi:	hle o64 83 /1 ib,s]			X64,LOCK
                        //OR		reg_ax,sbyteword		[mi:	o16 83 /1 ib,s]				8086,SM,ND
                        //OR		reg_eax,sbytedword		[mi:	o32 83 /1 ib,s]				386,SM,ND
                        //OR		reg_rax,sbytedword		[mi:	o64 83 /1 ib,s]				X64,SM,ND
                        //OR		rm16,sbyteword			[mi:	hle o16 83 /1 ib,s]			8086,SM,LOCK,ND
                        //OR		rm32,sbytedword			[mi:	hle o32 83 /1 ib,s]			386,SM,LOCK,ND
                        //OR		rm64,sbytedword			[mi:	hle o64 83 /1 ib,s]			X64,SM,LOCK,ND
                        //OR		mem,sbyteword16			[mi:	hle o16 83 /1 ib,s]			8086,SM,LOCK,ND
                        //OR		mem,sbytedword32		[mi:	hle o32 83 /1 ib,s]			386,SM,LOCK,ND

                        instruction.executeOr2();
                        break;

                    case 2:
                        //ADC		rm16,imm8			[mi:	hle o16 83 /2 ib,s]			8086,LOCK
                        //ADC		rm32,imm8			[mi:	hle o32 83 /2 ib,s]			386,LOCK
                        //ADC		rm64,imm8			[mi:	hle o64 83 /2 ib,s]			X64,LOCK
                        //ADC		reg_ax,sbyteword		[mi:	o16 83 /2 ib,s]				8086,SM,ND
                        //ADC		reg_eax,sbytedword		[mi:	o32 83 /2 ib,s]				386,SM,ND
                        //ADC		reg_rax,sbytedword		[mi:	o64 83 /2 ib,s]				X64,SM,ND
                        //ADC		rm16,sbyteword			[mi:	hle o16 83 /2 ib,s]			8086,SM,LOCK,ND
                        //ADC		rm32,sbytedword			[mi:	hle o32 83 /2 ib,s]			386,SM,LOCK,ND
                        //ADC		rm64,sbytedword			[mi:	hle o64 83 /2 ib,s]			X64,SM,LOCK,ND
                        //ADC		mem,sbyteword16			[mi:	hle o16 83 /2 ib,s]			8086,SM,LOCK,ND
                        //ADC		mem,sbytedword32		[mi:	hle o32 83 /2 ib,s]			386,SM,LOCK,ND

                        instruction.executeAdc2();
                        break;

                    case 3:
                        //SBB		rm16,imm8			[mi:	hle o16 83 /3 ib,s]			8086,LOCK
                        //SBB		rm32,imm8			[mi:	hle o32 83 /3 ib,s]			386,LOCK
                        //SBB		rm64,imm8			[mi:	hle o64 83 /3 ib,s]			X64,LOCK
                        //SBB		reg_ax,sbyteword		[mi:	o16 83 /3 ib,s]				8086,SM,ND
                        //SBB		reg_eax,sbytedword		[mi:	o32 83 /3 ib,s]				386,SM,ND
                        //SBB		reg_rax,sbytedword		[mi:	o64 83 /3 ib,s]				X64,SM,ND
                        //SBB		rm16,sbyteword			[mi:	hle o16 83 /3 ib,s]			8086,SM,LOCK,ND
                        //SBB		rm32,sbytedword			[mi:	hle o32 83 /3 ib,s]			386,SM,LOCK,ND
                        //SBB		rm64,sbytedword			[mi:	hle o64 83 /3 ib,s]			X64,SM,LOCK,ND
                        //SBB		mem,sbyteword16			[mi:	hle o16 83 /3 ib,s]			8086,SM,LOCK,ND
                        //SBB		mem,sbytedword32		[mi:	hle o32 83 /3 ib,s]			386,SM,LOCK,ND

                        instruction.executeSbb2();
                        break;

                    case 4:
                        //AND		rm16,imm8			[mi:	hle o16 83 /4 ib,s]			8086,LOCK
                        //AND		rm32,imm8			[mi:	hle o32 83 /4 ib,s]			386,LOCK
                        //AND		rm64,imm8			[mi:	hle o64 83 /4 ib,s]			X64,LOCK
                        //AND		reg_ax,sbyteword		[mi:	o16 83 /4 ib,s]				8086,SM,ND
                        //AND		reg_eax,sbytedword		[mi:	o32 83 /4 ib,s]				386,SM,ND
                        //AND		reg_rax,sbytedword		[mi:	o64 83 /4 ib,s]				X64,SM,ND
                        //AND		rm16,sbyteword			[mi:	hle o16 83 /4 ib,s]			8086,SM,LOCK,ND
                        //AND		rm32,sbytedword			[mi:	hle o32 83 /4 ib,s]			386,SM,LOCK,ND
                        //AND		rm64,sbytedword			[mi:	hle o64 83 /4 ib,s]			X64,SM,LOCK,ND
                        //AND		mem,sbyteword16			[mi:	hle o16 83 /4 ib,s]			8086,SM,LOCK,ND
                        //AND		mem,sbytedword32		[mi:	hle o32 83 /4 ib,s]			386,SM,LOCK,ND

                        instruction.executeAnd2();
                        break;

                    case 5:
                        //SUB		rm16,imm8			[mi:	hle o16 83 /5 ib,s]			8086,LOCK
                        //SUB		rm32,imm8			[mi:	hle o32 83 /5 ib,s]			386,LOCK
                        //SUB		rm64,imm8			[mi:	hle o64 83 /5 ib,s]			X64,LOCK
                        //SUB		reg_ax,sbyteword		[mi:	o16 83 /5 ib,s]				8086,SM,ND
                        //SUB		reg_eax,sbytedword		[mi:	o32 83 /5 ib,s]				386,SM,ND
                        //SUB		reg_rax,sbytedword		[mi:	o64 83 /5 ib,s]				X64,SM,ND
                        //SUB		rm16,sbyteword			[mi:	hle o16 83 /5 ib,s]			8086,SM,LOCK,ND
                        //SUB		rm32,sbytedword			[mi:	hle o32 83 /5 ib,s]			386,SM,LOCK,ND
                        //SUB		rm64,sbytedword			[mi:	hle o64 83 /5 ib,s]			X64,SM,LOCK,ND
                        //SUB		mem,sbyteword16			[mi:	hle o16 83 /5 ib,s]			8086,SM,LOCK,ND
                        //SUB		mem,sbytedword32		[mi:	hle o32 83 /5 ib,s]			386,SM,LOCK,ND

                        instruction.executeSub2();
                        break;

                    case 6:
                        //XOR		rm16,imm8			[mi:	hle o16 83 /6 ib,s]			8086,LOCK
                        //XOR		rm32,imm8			[mi:	hle o32 83 /6 ib,s]			386,LOCK
                        //XOR		rm64,imm8			[mi:	hle o64 83 /6 ib,s]			X64,LOCK
                        //XOR		reg_ax,sbyteword		[mi:	o16 83 /6 ib,s]				8086,SM,ND
                        //XOR		reg_eax,sbytedword		[mi:	o32 83 /6 ib,s]				386,SM,ND
                        //XOR		reg_rax,sbytedword		[mi:	o64 83 /6 ib,s]				X64,SM,ND
                        //XOR		rm16,sbyteword			[mi:	hle o16 83 /6 ib,s]			8086,SM,LOCK,ND
                        //XOR		rm32,sbytedword			[mi:	hle o32 83 /6 ib,s]			386,SM,LOCK,ND
                        //XOR		rm64,sbytedword			[mi:	hle o64 83 /6 ib,s]			X64,SM,LOCK,ND
                        //XOR		mem,sbyteword16			[mi:	hle o16 83 /6 ib,s]			8086,SM,LOCK,ND
                        //XOR		mem,sbytedword32		[mi:	hle o32 83 /6 ib,s]			386,SM,LOCK,ND

                        instruction.executeXor2();
                        break;

                    case 7:
                        //CMP		rm16,imm8			[mi:	o16 83 /7 ib,s]				8086
                        //CMP		rm32,imm8			[mi:	o32 83 /7 ib,s]				386
                        //CMP		rm64,imm8			[mi:	o64 83 /7 ib,s]				X64
                        //CMP		reg_ax,sbyteword		[mi:	o16 83 /7 ib,s]				8086,SM,ND
                        //CMP		reg_eax,sbytedword		[mi:	o32 83 /7 ib,s]				386,SM,ND
                        //CMP		reg_rax,sbytedword		[mi:	o64 83 /7 ib,s]				X64,SM,ND
                        //CMP		rm16,sbyteword			[mi:	o16 83 /7 ib,s]				8086,SM,ND
                        //CMP		rm32,sbytedword			[mi:	o32 83 /7 ib,s]				386,SM,ND
                        //CMP		rm64,sbytedword			[mi:	o64 83 /7 ib,s]				X64,SM,ND

                        //CMP		mem,sbyteword16			[mi:	o16 83 /7 ib,s]				8086,SM,ND
                        //CMP		mem,sbytedword32		[mi:	o32 83 /7 ib,s]				386,SM,ND

                        instruction.executeCmp2();
                        break;

                    default:
                        throw new NotImplException();
                }
                */
                instruction.execute83();
                break;

            case 0x84:
                //TEST		mem,reg8			[mr:	84 /r]					8086,SM
                //TEST		reg8,reg8			[mr:	84 /r]					8086
                //TEST		reg8,mem			[rm:	84 /r]					8086,SM
                mrs.reg8=true;
            case 0x85:
                //TEST		mem,reg16			[mr:	o16 85 /r]				8086,SM
                //TEST		reg16,reg16			[mr:	o16 85 /r]				8086
                //TEST		mem,reg32			[mr:	o32 85 /r]				386,SM
                //TEST		reg32,reg32			[mr:	o32 85 /r]				386
                //TEST		mem,reg64			[mr:	o64 85 /r]				X64,SM
                //TEST		reg64,reg64			[mr:	o64 85 /r]				X64
                //TEST		reg16,mem			[rm:	o16 85 /r]				8086,SM
                //TEST		reg32,mem			[rm:	o32 85 /r]				386,SM
                //TEST		reg64,mem			[rm:	o64 85 /r]				X64,SM

                instruction.parse2(bits);
                instruction.executeCal1(TEST,false);
                break;

            case 0x88:
                //MOV		mem,reg8			[mr:	hlexr 88 /r]				8086,SM
                //MOV		reg8,reg8			[mr:	88 /r]					8086

                //org.jpc.emulator.execution.opcodes.rm.mov_Eb_Gb_mem

                mrs.reg8 = true;

            case 0x89:
                //MOV		mem,reg16			[mr:	hlexr o16 89 /r]			8086,SM
                //MOV		reg16,reg16			[mr:	o16 89 /r]				8086
                //MOV		mem,reg32			[mr:	hlexr o32 89 /r]			386,SM
                //MOV		reg32,reg32			[mr:	o32 89 /r]				386
                //MOV		mem,reg64			[mr:	hlexr o64 89 /r]			X64,SM
                //MOV		reg64,reg64			[mr:	o64 89 /r]				X64

                instruction.parse2(bits);
                instruction.executeMovMR();
                break;

            case 0x8a:
                //MOV		reg8,mem			[rm:	8a /r]					8086,SM
                //MOV		reg8,reg8			[rm:	8a /r]					8086

                mrs.reg8 = true;

            case 0x8b:
                //MOV		reg16,mem			[rm:	o16 8b /r]				8086,SM
                //MOV		reg16,reg16			[rm:	o16 8b /r]				8086
                //MOV		reg32,mem			[rm:	o32 8b /r]				386,SM
                //MOV		reg32,reg32			[rm:	o32 8b /r]				386
                //MOV		reg64,mem			[rm:	o64 8b /r]				X64,SM
                //MOV		reg64,reg64			[rm:	o64 8b /r]				X64

                instruction.parse2(bits);
                instruction.executeMovRM();
                break;

            case 0x8c:
                //MOV		mem,reg_sreg			[mr:	8c /r]					8086,SW
                //MOV		reg16,reg_sreg			[mr:	o16 8c /r]				8086
                //MOV		reg32,reg_sreg			[mr:	o32 8c /r]				386
                //MOV		reg64,reg_sreg			[mr:	o64nw 8c /r]				X64,OPT,ND
                //MOV		rm64,reg_sreg			[mr:	o64 8c /r]				X64

                instruction.mrs.regType = 1;
                instruction.parse2(bits);
                instruction.executeMovMR();
                break;

            case 0x8d:
                //LEA		reg16,mem			[rm:	o16 8d /r]				8086
                //LEA		reg32,mem			[rm:	o32 8d /r]				386
                //LEA		reg64,mem			[rm:	o64 8d /r]				X64

                instruction.parse2(bits);
                instruction.executeLEA();
                break;

            case 0x8e:
                //MOV		reg_sreg,mem			[rm:	8e /r]					8086,SW
                //MOV		reg_sreg,reg16			[rm:	8e /r]					8086,OPT,ND
                //MOV		reg_sreg,reg32			[rm:	8e /r]					386,OPT,ND
                //MOV		reg_sreg,reg64			[rm:	o64nw 8e /r]				X64,OPT,ND
                //MOV		reg_sreg,reg16			[rm:	o16 8e /r]				8086
                //MOV		reg_sreg,reg32			[rm:	o32 8e /r]				386
                //MOV		reg_sreg,rm64			[rm:	o64 8e /r]				X64

                instruction.mrs.regType = 1;
                instruction.parse2(bits);
                instruction.executeMovRM();
                break;

            case 0x8f:
                instruction.parse2(bits);
                switch (mrs.regIndex) {
                    case 0:
                        //POP rm16				[m:	o16 8f /0]				8086
                        //POP		rm32				[m:	o32 8f /0]				386,NOLONG
                        //POP		rm64				[m:	o64nw 8f /0]				X64

                        instruction.executePop8f();
                        break;
                    default:
                        throw new NotImplException();
                }
                break;

            case 0xa8:
                //TEST		reg_al,imm			[-i:	a8 ib]					8086,SM
                mrs.reg8=true;
            case 0xa9:
                //TEST		reg_ax,imm			[-i:	o16 a9 iw]				8086,SM
                //TEST		reg_eax,imm			[-i:	o32 a9 id]				386,SM
                //TEST		reg_rax,imm			[-i:	o64 a9 id,s]				X64,SM
                instruction.executeCal2(TEST);
                break;

            case 0xaa:
                //STOSB		void				[	aa]					8086
                mrs.reg8 = true;
            case 0xab:
                //STOSD		void				[	o32 ab]					386
                //STOSQ		void				[	o64 ab]					X64
                //STOSW		void				[	o16 ab]					8086

                //org.jpc.emulator.execution.opcodes.rm.stosb_a16
                //org.jpc.emulator.execution.opcodes.rm.stosw_a16

                instruction.executeSTOS();
                break;

            case 0xac:
                //LODSB		void				[	ac]					8086
                mrs.reg8 = true;
            case 0xad:
                //LODSD		void				[	o32 ad]					386
                //LODSQ		void				[	o64 ad]					X64
                //LODSW		void				[	o16 ad]					8086

                instruction.executeLODS();
                break;

            case 0xb0:
            case 0xb1:
            case 0xb2:
            case 0xb3:
            case 0xb4:
            case 0xb5:
            case 0xb6:
            case 0xb7:
                //MOV		reg8,imm			[ri:	b0+r ib]				8086,SM

                //org.jpc.emulator.execution.opcodes.rm.mov_ALr8b_Ib

                mrs.reg8 = true;
                instruction.executeMovri(0xb0);
                break;

            case 0xb8:
            case 0xb9:
            case 0xba:
            case 0xbb:
            case 0xbc:
            case 0xbd:
            case 0xbe:
            case 0xbf:
                //MOV		reg16,imm			[ri:	o16 b8+r iw]				8086,SM
                //MOV		reg32,imm			[ri:	o32 b8+r id]				386,SM
                //MOV		reg64,udword			[ri:	o64nw b8+r id]				X64,SM,OPT,ND
                //MOV		reg64,imm			[ri:	o64 b8+r iq]				X64,SM

                //org.jpc.emulator.execution.opcodes.rm.mov_rAXr8_Iw

                instruction.executeMovri(0xb8);
                break;

            case 0xc3:
                //RET		void				[	c3]					8086,BND
                //RETN		void				[	c3]					8086,BND

                instruction.executeRetNear();
                jump = true;
                break;

            case 0xeb:
                //JMP		imm|short			[i:	eb rel8]				8086
                //JMP		imm				[i:	jmp8 eb rel8]				8086,ND
                mrs.reg8=true;
            case 0xe9:
                //JMP		imm				[i:	odf e9 rel]				8086,BND
                //JMP		imm|near			[i:	odf e9 rel]				8086,ND,BND
                //JMP		imm16				[i:	o16 e9 rel]				8086,NOLONG,BND
                //JMP		imm16|near			[i:	o16 e9 rel]				8086,ND,NOLONG,BND
                //JMP		imm32				[i:	o32 e9 rel]				386,NOLONG,BND
                //JMP		imm32|near			[i:	o32 e9 rel]				386,ND,NOLONG,BND
                //JMP		imm64				[i:	o64nw e9 rel]				X64,BND
                //JMP		imm64|near			[i:	o64nw e9 rel]				X64,ND,BND
                instruction.executeJumpNear();
                break;

            case 0xea:
                //JMP		imm|far				[i:	odf ea iwd seg]				8086,ND,NOLONG
                //JMP		imm16|far			[i:	o16 ea iwd seg]				8086,ND,NOLONG
                //JMP		imm32|far			[i:	o32 ea iwd seg]				386,ND,NOLONG
                //JMP		imm:imm				[ji:	odf ea iwd iw]				8086,NOLONG
                //JMP		imm16:imm			[ji:	o16 ea iw iw]				8086,NOLONG
                //JMP		imm:imm16			[ji:	o16 ea iw iw]				8086,NOLONG
                //JMP		imm32:imm			[ji:	o32 ea id iw]				386,NOLONG
                //JMP		imm:imm32			[ji:	o32 ea id iw]				386,NOLONG

                //org.jpc.emulator.execution.opcodes.rm.jmp_Ap

                instruction.executeJumpFar();
                jump = true;
                break;

            case 0xe4:
                //IN		reg_al,imm			[-i:	e4 ib,u]				8086,SB
            case 0xec:
                //IN		reg_al,reg_dx			[--:	ec]					8086

                mrs.reg8 = true;
            case 0xe5:
                //IN		reg_ax,imm			[-i:	o16 e5 ib,u]				8086,SB
                //IN		reg_eax,imm			[-i:	o32 e5 ib,u]				386,SB
            case 0xed:
                //IN		reg_ax,reg_dx			[--:	o16 ed]					8086
                //IN		reg_eax,reg_dx			[--:	o32 ed]					386

                //org.jpc.emulator.execution.opcodes.rm.in_o16_eAX_DX

                instruction.executeIn();
                break;

            case 0xe6:
                //OUT		imm,reg_al			[i-:	e6 ib,u]				8086,SB
            case 0xee:
                //OUT		reg_dx,reg_al			[--:	ee]					8086

                //org.jpc.emulator.execution.opcodes.rm.out_DX_AL

                mrs.reg8 = true;
            case 0xe7:
                //OUT		imm,reg_ax			[i-:	o16 e7 ib,u]				8086,SB
                //OUT		imm,reg_eax			[i-:	o32 e7 ib,u]				386,SB

            case 0xef:
                //OUT		reg_dx,reg_ax			[--:	o16 ef]					8086
                //OUT		reg_dx,reg_eax			[--:	o32 ef]					386

                instruction.executeOut();
                break;

            case 0xe8:
                //CALL		imm				[i:	odf e8 rel]				8086,BND
                //CALL		imm|near			[i:	odf e8 rel]				8086,ND,BND
                //CALL		imm16				[i:	o16 e8 rel]				8086,NOLONG,BND
                //CALL		imm16|near			[i:	o16 e8 rel]				8086,ND,NOLONG,BND
                //CALL		imm32				[i:	o32 e8 rel]				386,NOLONG,BND
                //CALL		imm32|near			[i:	o32 e8 rel]				386,ND,NOLONG,BND
                //CALL		imm64				[i:	o64nw e8 rel]				X64,BND
                //CALL		imm64|near			[i:	o64nw e8 rel]				X64,ND,BND

                instruction.executeCallNear();
                jump = true;
                break;

            case 0xf5:
                //CMC		void				[	f5]					8086
                instruction.executeCMC();
                break;

            case 0xf8:
                //CLC		void				[	f8]					8086
                instruction.executeCF_(false);
                break;

            case 0xf9:
                //STC		void				[	f9]					8086
                instruction.executeCF_(true);
                break;

            case 0xfa:
                //CLI		void				[	fa]					8086
                instruction.executeIF_(false);
                break;

            case 0xfc:
                //CLD		void				[	fc]					8086
                instruction.executeDF_(false);
                break;

            case 0xfe:
                //INC		rm8				[m:	hle fe /0]				8086,LOCK

                mrs.reg8 = true;
                instruction.executeIncRm();
                break;

            case 0xff:
                instruction.parse2(bits);
                switch (mrs.regIndex) {
                    case 0:
                        //INC		rm16				[m:	hle o16 ff /0]				8086,LOCK
                        //INC		rm32				[m:	hle o32 ff /0]				386,LOCK
                        //INC		rm64				[m:	hle o64 ff /0]				X64,LOCK

                        instruction.executeIncRm();
                        break;
                    case 6:
                        //PUSH rm16				[m:	o16 ff /6]				8086
                        //PUSH		rm32				[m:	o32 ff /6]				386,NOLONG
                        //PUSH		rm64				[m:	o64nw ff /6]				X64

                        instruction.executePushff();
                        break;
                    default:
                        throw new NotImplException();
                }
                break;

            default:
                throw new NotImplException(String.valueOf(op));
        }
        if (!jump) {
            reLoc(input);
        }
        checkIR();
        return 0;
    }

    public void reLoc(CodeStream input) {
        regs.rip.setValue64(regs.rip.getValue64() + (input.getPos() - instruction.getStartPos()));
    }

    public String decode(PC pc, CodeStream input) {
        pre(pc);
        long pos = input.getPos();
        try {
            input.readFully(previewBuffer);
        } finally {
            input.setPos(pos);
        }
        return Disassembler.nativeAssemble(previewBuffer, bits);
    }

    public int getBits() {
        return bits;
    }

    public void setBits(int bits) {
        this.bits = bits;
    }

    public void checkIR() {
    }

}
