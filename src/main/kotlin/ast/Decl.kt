package org.noze.ast

import org.noze.Loc
import org.noze.symbol.Name
import org.noze.symbol.TypeName

sealed class Decl(loc: Loc) : Ast(loc) {
	sealed class Val(loc: Loc, val name: Name) : Decl(loc) {
		class Fn(loc: Loc, name: Name, val sig: Signature, val body: Expr.Block) : Val(loc, name) {
			override fun show(): String =
				"Fn($name, $sig, $body)"
		}

		//class Const(loc: Loc, name: Name, val value: Expr) : Val(loc, name) {
		//}
	}

	sealed class Type(loc: Loc, val name: TypeName) : Decl(loc) {
		class Rec(loc: Loc, name: TypeName, val properties: List<Property>) : Type(loc, name) {
			override fun show(): String =
				"Rec($name, $properties)"
		}
	}

	fun<A> match(ifVal: (Val) -> A, ifType: (Type) -> A): A =
		when (this) {
			is Val -> ifVal(this)
			is Type -> ifType(this)
		}
}

class Property(loc: Loc, val name: Name, val type: TypeAst) : Ast(loc) {
	override fun show(): String =
		"Property($name, $type)"
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
