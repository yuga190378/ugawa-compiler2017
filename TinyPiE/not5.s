	.section .data
	@ 大域変数の定義
_Pi_var_x:
	.word 1
_Pi_var_y:
	.word 10
_Pi_var_z:
	.word -1
	.section .text
	.global _start
_start:
	@ 式をコンパイルした命令列
	ldr r0, =#5
	mvn r0, r0
	@ EXIT システムコール
	mov r7, #1
	swi #0
