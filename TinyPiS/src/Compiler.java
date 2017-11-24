import java.io.IOException;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import parser.TinyPiSLexer;
import parser.TinyPiSParser;

public class Compiler extends CompilerBase {
	boolean printflg = false;
	void compileExpr(ASTNode ndx, Environment env) {
		if (ndx instanceof ASTBinaryExprNode) {
			ASTBinaryExprNode nd = (ASTBinaryExprNode) ndx;
			compileExpr(nd.lhs, env);
			emitPUSH(REG_R1);
			emitRR("mov", REG_R1, REG_DST);
			compileExpr(nd.rhs, env);
			if (nd.op.equals("+"))
				emitRRR("add", REG_DST, REG_R1, REG_DST);
			else if (nd.op.equals("-"))
				emitRRR("sub", REG_DST, REG_R1, REG_DST);
			else if (nd.op.equals("*"))
				emitRRR("mul", REG_DST, REG_R1, REG_DST);
			else if (nd.op.equals("/"))
				emitRRR("udiv", REG_DST, REG_R1, REG_DST);
			else if (nd.op.equals("&"))
				emitRRR("and", REG_DST, REG_R1, REG_DST);
			else if (nd.op.equals("|"))
				emitRRR("orr", REG_DST, REG_R1, REG_DST);
			else 
				throw new Error("Unknwon operator: "+nd.op);
			emitPOP(REG_R1);
		} else if (ndx instanceof ASTUnaryExprNode) {
			ASTUnaryExprNode nd = (ASTUnaryExprNode) ndx;
			compileExpr(nd.operand, env);
			if (nd.op.equals("~"))
				emitRR("mvn", REG_DST, REG_DST);
			else if(nd.op.equals("-")) {
				emitRR("mvn", REG_DST, REG_DST);
				emitRRI("add", REG_DST, REG_DST, 1);
			} else
				throw new Error("Unknown operator: "+nd.op);
		} else if (ndx instanceof ASTNumberNode) {
			ASTNumberNode nd = (ASTNumberNode) ndx;
			emitLDC(REG_DST, nd.value);
		} else if (ndx instanceof ASTVarRefNode) {
			ASTVarRefNode nd = (ASTVarRefNode) ndx;
			Variable var = env.lookup(nd.varName);
			if (var == null)
				throw new Error("Undefined variable: "+nd.varName);
			if (var instanceof GlobalVariable) {
				GlobalVariable globalVar = (GlobalVariable) var;
				emitLDC(REG_DST, globalVar.getLabel());
				emitLDR(REG_DST, REG_DST, 0);
			} else
				throw new Error("Not a global variable: "+nd.varName);
		} else 
			throw new Error("Unknown expression: "+ndx);
	}
	
	void compileStmt(ASTNode ndx, Environment env) {
		if (ndx instanceof ASTCompoundStmtNode) {
			ASTCompoundStmtNode nd = (ASTCompoundStmtNode) ndx;
			for (ASTNode stmt:nd.stmts)
				compileStmt(stmt, env);
		} else if (ndx instanceof ASTAssignStmtNode) {
			ASTAssignStmtNode nd = (ASTAssignStmtNode) ndx;
			Variable var = env.lookup(nd.var);
			if (var == null)
				throw new Error("undefined variable: "+nd.var);
			compileExpr(nd.expr, env);
			if (var instanceof GlobalVariable) {
				GlobalVariable globalVar = (GlobalVariable) var;
				emitLDC(REG_R1, globalVar.getLabel());
				emitSTR(REG_DST, REG_R1, 0);
			} else
				throw new Error("Not a global variable: "+nd.var);
		} else if (ndx instanceof ASTIfStmtNode) {
			ASTIfStmtNode nd = (ASTIfStmtNode) ndx;
			String elseLabel = freshLabel();
			String endifLabel = freshLabel();
			compileExpr(nd.cond, env);
			emitRI("cmp", REG_DST, 0);
			emitJMP("beq", elseLabel);
			compileStmt(nd.thenClause, env);
			emitJMP("b", endifLabel);
			emitLabel(elseLabel);
			compileStmt(nd.elseClause, env);
			emitLabel(endifLabel);
		} else if (ndx instanceof ASTWhileStmtNode) {
			ASTWhileStmtNode nd = (ASTWhileStmtNode) ndx;
			String startwhileLabel = freshLabel();
			String endwhileLabel = freshLabel();
			emitLabel(startwhileLabel);
			compileExpr(nd.cond, env);
			emitRI("cmp", REG_DST, 0);
			emitJMP("beq", endwhileLabel);
			compileStmt(nd.stmt, env);
			emitJMP("b", startwhileLabel);
			emitLabel(endwhileLabel);
		} else if (ndx instanceof ASTPrintStmtNode) {
			ASTPrintStmtNode nd = (ASTPrintStmtNode) ndx;
			printflg = true;
			String printLabel = freshLabel();
			String endprintLabel = freshLabel();
			emitRR("ldr", REG_R1, "=buf+8");
			emitRI("mov", REG_R4, 8);
			emitRRI("add", REG_R2, REG_R1, 1);
			compileExpr(nd.expr, env);
			emitLabel(printLabel);
			emitRRR("mov", REG_R6, REG_DST, "lsr #4");
			System.out.println("\t"+"eor"+" "+REG_R7+", "+REG_DST+", "+REG_R6+", "+"lsl #4");
			emitRI("cmp", REG_R7, 10);
			emitRRI("addcc", REG_R7, REG_R7, 48);
			emitRRI("addge", REG_R7, REG_R7, 87);
			emitRR("mov", REG_DST, REG_R6);
			System.out.println("\tstrb "+REG_R7+", ["+REG_R1+", #"+-1+"]!");
			emitRRI("subs", REG_R4, REG_R4, 1);
			emitJMP("bne", printLabel);
			emitRRR("sub", REG_R2, REG_R2, REG_R1);
			emitLabel(endprintLabel);
			System.out.println("\t@ WRITEシステムコール");
			emitRI("mov", REG_R7, 4);   // WRITE のシステムコール番号
			emitRI("mov", REG_DST, 1);	//標準出力
			emitI("swi", 0);
		} else
			throw new Error("Unknown expression: "+ndx);
	}
	
	void compile(ASTNode ast) {
		Environment env = new Environment();
		ASTProgNode prog = (ASTProgNode) ast;
		System.out.println("\t.section .data");
		System.out.println("\t@ 大域変数の定義");
		for (String varName: prog.varDecls) {
			if (env.lookup(varName) != null)
				throw new Error("Variable redefined: "+varName);
			GlobalVariable v = addGlobalVariable(env, varName);
			emitLabel(v.getLabel());
			System.out.println("\t.word 0");
		}
		if (env.lookup("answer") == null) {
			GlobalVariable v = addGlobalVariable(env, "answer");
			emitLabel(v.getLabel());
			System.out.println("\t.word 0");
		}
		System.out.println("\t.section .text");
		System.out.println("\t.global _start");
		System.out.println("_start:");
		System.out.println("\t@ 式をコンパイルした命令列");
		compileStmt(prog.stmt, env);
		System.out.println("\t@ EXITシステムコール");
		if (printflg){
			emitRI("mov", "r7", 1);   // EXIT のシステムコール番号
			emitRI("mov", REG_DST, 0);
			emitI("swi", 0);
			System.out.println("\t.section .data");
			emitLabel("buf");
			System.out.println("\t"+".space"+" "+"8");
			System.out.println("\t"+".byte"+" "+"10");
		} else {
			GlobalVariable v = (GlobalVariable) env.lookup("answer");
			emitLDC(REG_DST, v.getLabel()); // 変数answerの値をr0(終了コード)に入れる
			emitLDR("r0", REG_DST, 0);
			emitRI("mov", "r7", 1);   // EXIT のシステムコール番号
			emitI("swi", 0);
		}
	}

	public static void main(String[] args) throws IOException {
		ANTLRInputStream input = new ANTLRInputStream(System.in);
		TinyPiSLexer lexer = new TinyPiSLexer(input);
		CommonTokenStream token = new CommonTokenStream(lexer);
		TinyPiSParser parser = new TinyPiSParser(token);
		ParseTree tree = parser.prog();
		ASTGenerator astgen = new ASTGenerator();
		ASTNode ast = astgen.translate(tree);
		Compiler compiler = new Compiler();
		compiler.compile(ast);
	}
}
