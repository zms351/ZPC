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
        Regs regs = pc.cpu.regs;
        if (regs.bits.pe.get()) {
            throw new NotImplException();
        } else {
            bits = 16;
        }
        instruction.mrs.reg8=false;
        regs = pc.cpu.regs;
        instruction.bits = regs.bits;
        instruction.bits.status=0;
    }

    public int execute(PC pc, CodeStream input) {
        pre(pc);
        instruction.setStartPos(input.getPos());
        instruction.parse1(input, bits);
        boolean jump = false;
        int op = instruction.getOpcode();
        switch (op) {
            case 0x30:
                //XOR		mem,reg8			[mr:	hle 30 /r]				8086,SM,LOCK
                //XOR		reg8,reg8			[mr:	30 /r]					8086

                //org.jpc.emulator.execution.opcodes.rm.xor_Eb_Gb_mem
                //org.jpc.emulator.execution.opcodes.rm.xor_Ew_Gw

                instruction.mrs.reg8=true;
                instruction.parse2(input, bits);
                instruction.executeXor30313233(this, input, pc, false);
                break;
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

                instruction.mrs.reg8=true;
                instruction.parse2(input, bits);
                instruction.executeXor30313233(this, input, pc, true);
                break;
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

                instruction.mrs.reg8=true;
            case 0x35:
                //XOR		reg_ax,imm			[-i:	o16 35 iw]				8086,SM
                //XOR		reg_eax,imm			[-i:	o32 35 id]				386,SM
                //XOR		reg_rax,imm			[-i:	o64 35 id,s]				X64,SM

                instruction.executeXor3435(this,input,pc);
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

                instruction.mrs.reg8=true;
                instruction.executeMov8ri(this,input,pc);
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
            case 0xe6:
                //OUT		imm,reg_al			[i-:	e6 ib,u]				8086,SB
            case 0xee:
                //OUT		reg_dx,reg_al			[--:	ee]					8086

                instruction.mrs.reg8=true;
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
