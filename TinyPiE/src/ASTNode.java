public class ASTNode {}

class ASTBinaryExprNode extends ASTNode {
	String op;
	ASTNode lhs;
	ASTNode rhs;
	ASTBinaryExprNode(String op, ASTNode lhs, ASTNode rhs) {
		this.op = op;
		this.lhs = lhs;
		this.rhs = rhs;
	}
	@Override
	public String toString() {
		return "(BinExpr "+op+" "+lhs+" "+rhs+")";
	}
}

class ASTNumberNode extends ASTNode {
	int value;
	ASTNumberNode(int value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return "(Number "+value+")";
	}
}

class ASTVarRefNode extends ASTNode {
	String varName;
	ASTVarRefNode(String varName) {
		this.varName = varName;
	}
	@Override
	public String toString() {
		return "(VarRef "+varName+")";
	}
}

class ASTUnaryExprNode extends ASTNode {
	String op;
	int value = -1;
	String varName = null;
	ASTNode operand;
	ASTUnaryExprNode(String op, int value) {
		this.op = op;
		this.value = value;
	}
	ASTUnaryExprNode(String op, String varName) {
		this.op = op;
		this.varName = varName;
	}
	ASTUnaryExprNode(String op, ASTNode operand) {
		this.op = op;
		this.operand = operand;
	}
	@Override
	public String toString() {
		if (value != -1) {
			return "(UnExpr "+op+" "+value+")";
		} else if (varName != null) {
			return "(UnExpr "+op+" "+varName+")";
		} else {
			return "(UnExpr "+op+" "+operand+")";
		}
	}
}