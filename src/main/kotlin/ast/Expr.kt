package org.noze.ast

import org.noze.Loc
import org.noze.symbol.Name
import org.noze.token.Token

// Every Expression has a type.
sealed class Expr(loc: Loc) : Statement(loc) {
	abstract fun<P, R> accept(v: ExprVisitor<P, R>, p: P): R

	class Access(loc: Loc, val name: Name) : Expr(loc) {
		override fun<P, R> accept(v: ExprVisitor<P, R>, p: P): R =
			v.visit(this, p)

		override fun show(): String =
			"Access($name)"
	}

	class Block(loc: Loc, val lines: List<Statement>, val returned: Expr) : Expr(loc) {
		override fun<P, R> accept(v: ExprVisitor<P, R>, p: P): R =
			v.visit(this, p)

		override fun show(): String =
			"Block($lines, $returned)"
	}

	class Call(loc: Loc, val called: Expr, val args: List<Expr>) : Expr(loc) {
		override fun<P, R> accept(v: ExprVisitor<P, R>, p: P): R =
			v.visit(this, p)

		override fun show(): String =
			"Call($called, $args)"
	}

	class Cond(loc: Loc, val condition: Expr, val ifTrue: Expr, val ifFalse: Expr) : Expr(loc) {
		override fun<P, R> accept(v: ExprVisitor<P, R>, p: P): R =
			v.visit(this, p)

		override fun show(): String =
			"Cond($condition, $ifTrue, $ifFalse)"
	}

	class Literal(loc: Loc, val value: Literal.Value) : Expr(loc) {
		override fun<P, R> accept(v: ExprVisitor<P, R>, p: P): R =
			v.visit(this, p)

		override fun show(): String =
			"Literal($value)"

		sealed class Value {
			class Bool(val value: Boolean): Value()
			class Int(val value: kotlin.Int) : Value()
			class Real(val value: kotlin.Double) : Value()
		}
	}
}

abstract class ExprVisitor<P, R> {
	abstract fun visit(a: Expr.Block, p: P): R
	abstract fun visit(a: Expr.Call, p: P): R
	abstract fun visit(a: Expr.Cond, p: P): R
	abstract fun visit(a: Expr.Literal, p: P): R
	abstract fun visit(a: Expr.Access, p: P): R

	fun visit(ast: Expr, p: P): R =
		ast.accept(this, p)
}

abstract class ExprVisitorUnit : ExprVisitor<Unit, Unit>() {
	override fun visit(a: Expr.Access, p: Unit) {
		visit(a)
	}

	open fun visit(a: Expr.Access) {}

	override fun visit(a: Expr.Block, p: Unit) {
		visit(a)
	}
	open fun visit(a: Expr.Block) {
		//TODO
		//for (line in a.lines)
		//	visit(line, Unit)
		visit(a.returned)
	}

	override fun visit(a: Expr.Call, p: Unit) {
		visit(a)
	}
	open fun visit(a: Expr.Call) {
		visit(a.called)
		for (arg in a.args)
			visit(arg)
	}

	override fun visit(a: Expr.Cond, p: Unit) {
		visit(a)
	}
	open fun visit(a: Expr.Cond) {
		visit(a.condition)
		visit(a.ifTrue)
		visit(a.ifFalse)
	}

	override fun visit(a: Expr.Literal, p: Unit) {
		visit(a)
	}
	open fun visit(a: Expr.Literal) {}

	fun visit(ast: Expr) {
		ast.accept(this, Unit)
	}
}
