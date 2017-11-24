// antlr4 -package parser -o antlr-generated  -no-listener parser/TinyPiS.g4
grammar TinyPiS;

prog: varDecls stmt
    ;
	
varDecls: ('var' IDENTIFIER ';')*
	;
	
stmt: '{' stmt* '}'								# compoundStmt
	| IDENTIFIER '=' expr ';'					# assignStmt
	| 'if' '(' expr ')' stmt 'else' stmt		# ifStmt
	| 'while' '(' expr ')'stmt					# whileStmt
	| 'print' expr ';'							# printStmt
	;

expr: orExpr
      ;

orExpr: orExpr OROP andExpr
	| andExpr
	;
	
andExpr: andExpr ANDOP addExpr
	| addExpr
	;
	
addExpr: addExpr (ADDOP|SUBOP) mulExpr
	| mulExpr
	;

mulExpr: mulExpr MULOP unaryExpr
	| unaryExpr
	;

unaryExpr: VALUE					# literalExpr
	| IDENTIFIER					# varExpr
	| '(' expr ')'				# parenExpr
	| (UNOP|SUBOP) unaryExpr				# unExpr
	;

ADDOP: '+';
SUBOP: '-';
MULOP: '*'|'/';
ANDOP: '&';
OROP:  '|';
UNOP: 	'~';

IDENTIFIER: [a-zA-Z_][a-zA-Z0-9_]*;
VALUE: '0'|[1-9][0-9]*;
WS: [ \t\r\n] -> skip;
