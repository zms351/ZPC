package com.zms.zpc.execute;

import com.zms.zpc.emulator.PC;
import com.zms.zpc.emulator.processor.Regs;
import com.zms.zpc.support.NotImplException;

/**
 * Created by 张小美 on 17/六月/25.
 * Copyright 2002-2016
 */
public class CodeExecutor {

    private int bits = 16;
    private byte[] previewBuffer;
    private InstructionExecutor instruction;
    private Regs regs;

    public CodeExecutor() {
        previewBuffer = new byte[16];
        instruction = new InstructionExecutor();
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
    }

    public int execute(PC pc, CodeStream input) {
        pre(pc);
        instruction.setStartPos(input.getPos());
        instruction.parse1(input, bits);
        boolean jump = false;
        ModRMSIB mrs = instruction.mrs;
        int op = instruction.getOpcode();
        switch (op) {

            case 0x06:
                //PUSH		reg_es				[-:	06]					8086,NOLONG
                instruction.executePush(pc, regs.es, instruction.getOpWidth(getBits()));
                break;

            case 0x07:
                //POP		reg_es				[-:	07]					8086,NOLONG
                instruction.executePop(pc, regs.es, instruction.getOpWidth(getBits()));
                break;

            case 0x0e:
                //PUSH		reg_cs				[-:	0e]					8086,NOLONG
                instruction.executePush(pc, regs.cs, instruction.getOpWidth(getBits()));
                break;

            case 0x0f:
                //POP		reg_cs				[-:	0f]					8086,UNDOC,ND,OBSOLETE  ： use RET

                instruction.readNextOp(input);
                op = instruction.getOpcode(1);
                switch (op) {
                    case 0xa0:
                        //PUSH		reg_fs				[-:	0f a0]					386
                        instruction.executePush(pc, regs.fs, instruction.getOpWidth(getBits()));
                        break;

                    case 0xa1:
                        //POP		reg_fs				[-:	0f a1]					386
                        instruction.executePop(pc, regs.fs, instruction.getOpWidth(getBits()));
                        break;

                    case 0xa8:
                        //PUSH		reg_gs				[-:	0f a8]					386
                        instruction.executePush(pc, regs.gs, instruction.getOpWidth(getBits()));
                        break;

                    case 0xa9:
                        //POP		reg_gs				[-:	0f a9]					386
                        instruction.executePop(pc, regs.gs, instruction.getOpWidth(getBits()));
                        break;
                    default:
                        throw new NotImplException("op2: " + op);
                }
                break;

            case 0x16:
                //PUSH		reg_ss				[-:	16]					8086,NOLONG
                instruction.executePush(pc, regs.ss, instruction.getOpWidth(getBits()));
                break;

            case 0x17:
                //POP		reg_ss				[-:	17]					8086,NOLONG
                instruction.executePop(pc, regs.ss, instruction.getOpWidth(getBits()));
                break;

            case 0x1e:
                //PUSH		reg_ds				[-:	1e]					8086,NOLONG
                instruction.executePush(pc, regs.ds, instruction.getOpWidth(getBits()));
                break;

            case 0x1f:
                //POP		reg_ds				[-:	1f]					8086,NOLONG
                instruction.executePop(pc, regs.ds, instruction.getOpWidth(getBits()));
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

                instruction.parse2(input, bits);
                instruction.executeXor30313233(this, input, pc, false);
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

                instruction.parse2(input, bits);
                instruction.executeXor30313233(this, input, pc, true);
                break;

            case 0x34:
                //XOR		reg_al,imm			[-i:	34 ib]					8086,SM

                mrs.reg8 = true;
            case 0x35:
                //XOR		reg_ax,imm			[-i:	o16 35 iw]				8086,SM
                //XOR		reg_eax,imm			[-i:	o32 35 id]				386,SM
                //XOR		reg_rax,imm			[-i:	o64 35 id,s]				X64,SM

                instruction.executeXor3435(this, input, pc);
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

                instruction.parse2(input, bits);
                instruction.executeCmp_rm_mr(this, input, pc, false);
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

                instruction.parse2(input, bits);
                instruction.executeCmp_rm_mr(this, input, pc, true);
                break;

            case 0x3c:
                //CMP		reg_al,imm			[-i:	3c ib]					8086,SM
                mrs.reg8 = true;
            case 0x3d:
                //CMP		reg_ax,imm			[-i:	o16 3d iw]				8086,SM
                //CMP		reg_eax,imm			[-i:	o32 3d id]				386,SM
                //CMP		reg_rax,imm			[-i:	o64 3d id,s]				X64,SM

                instruction.executeCmp3c3d(this, input, pc);
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

                instruction.executePush50(this, input, pc, 0x50);
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

                instruction.executePop58(this, input, pc, 0x58);
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
                jump = instruction.executeJcc(this, input, pc);
                break;

            case 0x83:
                instruction.parse2(input, bits);
                switch (mrs.regIndex) {
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

                        instruction.executeCmp83(this, input, pc);
                        break;

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

                        instruction.executeAdd83(this, input, pc);
                        break;

                    default:
                        throw new NotImplException();
                }
                break;

            case 0x80:
                //CMP		rm8,imm				[mi:	80 /7 ib]				8086,SM
                //CMP		mem,imm8			[mi:	80 /7 ib]				8086,SM
            case 0x82:
                //CMP		rm8,imm				[mi:	82 /7 ib]				8086,SM,ND,NOLONG
                mrs.reg8 = true;
            case 0x81:
                //CMP		rm16,imm			[mi:	o16 81 /7 iw]				8086,SM
                //CMP		rm32,imm			[mi:	o32 81 /7 id]				386,SM
                //CMP		rm64,imm			[mi:	o64 81 /7 id,s]				X64,SM
                //CMP		mem,imm16			[mi:	o16 81 /7 iw]				8086,SM
                //CMP		mem,imm32			[mi:	o32 81 /7 id]				386,SM
                instruction.parse2(input, bits);
                switch (mrs.regIndex) {
                    case 7:
                        instruction.executeCmp82(this, input, pc);
                        break;
                    default:
                        throw new NotImplException();
                }
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

                instruction.parse2(input, bits);
                instruction.executeMovMR(this, input, pc);
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

                instruction.parse2(input, bits);
                instruction.executeMovRM(this, input, pc);
                break;

            case 0x8c:
                //MOV		mem,reg_sreg			[mr:	8c /r]					8086,SW
                //MOV		reg16,reg_sreg			[mr:	o16 8c /r]				8086
                //MOV		reg32,reg_sreg			[mr:	o32 8c /r]				386
                //MOV		reg64,reg_sreg			[mr:	o64nw 8c /r]				X64,OPT,ND
                //MOV		rm64,reg_sreg			[mr:	o64 8c /r]				X64

                instruction.mrs.regType = 1;
                instruction.parse2(input, bits);
                instruction.executeMovMR(this, input, pc);
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
                instruction.parse2(input, bits);
                instruction.executeMovRM(this, input, pc);
                break;

            case 0x8f:
                instruction.parse2(input, bits);
                switch (mrs.regIndex) {
                    case 0:
                        //POP rm16				[m:	o16 8f /0]				8086
                        //POP		rm32				[m:	o32 8f /0]				386,NOLONG
                        //POP		rm64				[m:	o64nw 8f /0]				X64

                        instruction.executePop8f(this, input, pc);
                        break;
                    default:
                        throw new NotImplException();
                }
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

                instruction.executeSTOS(this, input, pc);
                break;

            case 0xac:
                //LODSB		void				[	ac]					8086
                mrs.reg8 = true;
            case 0xad:
                //LODSD		void				[	o32 ad]					386
                //LODSQ		void				[	o64 ad]					X64
                //LODSW		void				[	o16 ad]					8086

                instruction.executeLODS(this, input, pc);
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
                instruction.executeMovri(this, input, pc, 0xb0);
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

                instruction.executeMovri(this, input, pc, 0xb8);
                break;

            case 0xc3:
                //RET		void				[	c3]					8086,BND
                //RETN		void				[	c3]					8086,BND

                instruction.executeRetNear(this, input, pc);
                jump = true;
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

                instruction.executeJumpFar(this, input, pc);
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

                instruction.executeIn(this, input, pc);
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

                instruction.executeOut(this, input, pc);
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

                instruction.executeCallNear(this, input, pc);
                jump = true;
                break;

            case 0xf5:
                //CMC		void				[	f5]					8086
                instruction.executeCMC(this, input, pc);
                break;

            case 0xf8:
                //CLC		void				[	f8]					8086
                instruction.executeCF_(this, input, pc, false);
                break;

            case 0xf9:
                //STC		void				[	f9]					8086
                instruction.executeCF_(this, input, pc, true);
                break;

            case 0xfa:
                //CLI		void				[	fa]					8086
                instruction.executeIF_(this, input, pc, false);
                break;

            case 0xfc:
                //CLD		void				[	fc]					8086
                instruction.executeDF_(this, input, pc, false);
                break;

            case 0xff:
                instruction.parse2(input, bits);
                switch (mrs.regIndex) {
                    case 6:
                        //PUSH rm16				[m:	o16 ff /6]				8086
                        //PUSH		rm32				[m:	o32 ff /6]				386,NOLONG
                        //PUSH		rm64				[m:	o64nw ff /6]				X64

                        instruction.executePushff(this, input, pc);
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
        regs.bits.opWidth = mrs.opWidth;
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
