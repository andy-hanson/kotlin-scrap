package org.noze.ast

import org.noze.Loc
import org.noze.symbol.TypeName
import org.noze.type.Type

abstract class Ast(val loc: Loc) {
	abstract fun show(): String

	override fun toString(): String =
		show()
}

class ModuleAst(loc: Loc, val declarations: List<Declaration>) : Ast(loc) {
	//override fun<P, R> accept(v: AstVisitor<P, R>, p: P): R =
	//	v.visit(this, p)
	override fun show(): String =
		"ModuleAst($declarations)"
}

// This is a local declare or an Expression that does something
abstract class Statement(loc: Loc) : Ast(loc)

sealed class TypeAst(loc: Loc) : Ast(loc) {
	class Builtin(loc: Loc, val kind: Type.Builtin) : TypeAst(loc) {
		override fun show(): String =
			"TypeAst.Builtin($kind)"
	}
	class Named(loc: Loc, val name: TypeName) : TypeAst(loc) {
		override fun show(): String =
			"TypeAst.Named($name)"
	}
}
