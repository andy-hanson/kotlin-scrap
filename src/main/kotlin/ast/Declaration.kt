package org.noze.ast

import org.noze.Loc
import org.noze.symbol.Name

sealed class Declaration(loc: Loc) : Ast(loc) {
	class Fn(loc: Loc, val name: Name, val sig: Signature, val body: Expr.Block) : Declaration(loc) {
		override fun show(): String =
			"Fn($name, $sig, $body)"
	}
}

class Signature(loc: Loc, val returnType: TypeAst, val args: List<Parameter>) : Ast(loc) {
	//override fun<P, R> accept(v: AstVisitor<P, R>, p: P): R =
	//	v.visit(this, p)

	override fun show(): String =
		"Signature($returnType, $args)"
}

class Parameter(loc: Loc, val name: Name, val type: TypeAst) : Ast(loc) {
	//override fun<P, R> accept(v: AstVisitor<P, R>, p: P): R =
	//	v.visit(this, p)

	override fun show(): String =
		"Parameter($name, $type)"
}
