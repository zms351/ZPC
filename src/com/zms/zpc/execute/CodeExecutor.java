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
        instruction.bits.status = 0;
    }

    public int execute(PC pc, CodeStream input) {
        pre(pc);
        instruction.setStartPos(input.getPos());
        instruction.parse1(input, bits);
        boolean jump = false;
        ModRMSIB mrs = instruction.mrs;
        int op = instruction.getOpcode();
        switch (op) {
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
                instruction.parse2(input, bits);
                instruction.executeMov8rm(this, input, pc);
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
            default:
                throw new NotImplException(String.valueOf(op));
        }
        if (!jump) {
            reLoc(input);
        }
        regs.bits.opWidth = mrs.opWidth;
        return 0;
    }

    private void reLoc(CodeStream input) {
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

}
