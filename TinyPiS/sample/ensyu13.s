	.section .data
	@ 大域変数の定義
_Pi_var_answer:
	.word 0
	.section .text
	.global _start
_start:
	@ 式をコンパイルした命令列
	ldr r1, =buf+8
	mov r4, #8
	add r2, r1, #1
	ldr r0, =#1
	str r1, [sp, #-4]!
	mov r1, r0
	ldr r0, =#10
	add r0, r1, r0
	ldr r1, [sp], #4
L0:
	mov r6, r0, lsr #4
	eor r7, r0, r6, lsl #4
	cmp r7, #10
	addcc r7, r7, #48
	addge r7, r7, #55
	mov r0, r6
	strb r7, [r1, #-1]!
	subs r4, r4, #1
	bne L0
	sub r2, r2, r1
L1:
	@ WRITEシステムコール
	mov r7, #4
	mov r0, #1
	swi #0
	@ EXITシステムコール
	mov r7, #1
	mov r0, #0
	swi #0
	.section .data
buf:
	.space 8
	.byte 10
