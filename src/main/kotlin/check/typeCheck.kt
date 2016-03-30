package org.noze.check

import org.noze.ast.Declaration
import org.noze.ast.Expr
import org.noze.ast.ExprVisitor
import org.noze.ast.ModuleAst
import org.noze.ast.TypeAst
import org.noze.CompileContext
import org.noze.Loc
import org.noze.type.Type

class Types(private val map: Map<Expr, Type>) {
	fun getType(ast: Expr): Type  =
		map[ast]!!
}

fun typeCheck(module: ModuleAst, bindings: Bindings, ctx: CompileContext): Types =
	TypeContext(bindings, ctx).run {
		val getter = TypeGetter(this)
		for (decl in module.declarations)
			when (decl) {
				is Declaration.Fn ->
					getter.assertType(declaredType(decl.sig.returnType), decl.body)
				else -> throw Error()
			}
		finish()
	}

private class TypeContext(val bindings: Bindings, val ctx: CompileContext) {
	private val types = mutableMapOf<Expr, Type>()

	fun set(expr: Expr, type: Type) {
		types.set(expr, type)
	}

	fun finish(): Types =
		Types(types)
}

private class TypeGetter(val ctx: TypeContext) : ExprVisitor<Unit, Type>() {
	fun<A : Expr> type(a: A): Type =
		visit(a, Unit).apply {
			ctx.set(a, this)
		}

	fun<A : Expr> assertType(expected: Type, a: A) {
		assertAssignable(a.loc, expected, type(a))
	}

	// actual should be assignable to expected.
	// e.g. assertAssignable(Or Int Float, Int) should work
	fun assertAssignable(loc: Loc, expected: Type, actual: Type) {
		ctx.ctx.check(expected == actual, loc) { it.typeNotAssignable(expected, actual) }
	}

	override fun visit(a: Expr.Access, p: Unit): Type {
		val binding = ctx.bindings[a]
		return when (binding) {
			is Binding.Builtin ->
				binding.type()
			is Binding.Decl->
				TODO("ZOO")
			is Binding.Local ->
				declaredType(binding.declaration.type)
		}
	}

	override fun visit(a: Expr.Block, p: Unit): Type =
		// TODO: lines
		type(a.returned)

	override fun visit(a: Expr.Call, p: Unit): Type {
		val fn = type(a.called) as? Type.Fn ?: throw ctx.ctx.fail(a.loc) { it.expectedFnType() }

		val argTypes = fn.args
		val argAsts = a.args
		ctx.ctx.check(argTypes.size == argAsts.size, a.loc) { it.numArgs(argTypes.size, argAsts.size) }

		for ((expectedType, ast) in argTypes.zip(argAsts))
			assertAssignable(ast.loc, expectedType, type(ast))

		return fn.ret
	}

	override fun visit(a: Expr.Cond, p: Unit): Type =
		type(a.ifTrue).combine(type(a.ifFalse))

	override fun visit(a: Expr.Literal, p: Unit): Type =
		when (a.value) {
			is Expr.Literal.Value.Bool ->
				Type.Builtin.Bool
			is Expr.Literal.Value.Int ->
				Type.Builtin.Int
			is Expr.Literal.Value.Float ->
				Type.Builtin.Float
		}
}

private fun declaredType(ast: TypeAst): Type =
	when (ast) {
		is TypeAst.Builtin ->
			ast.kind
		is TypeAst.Named ->
			TODO("FOO")
	}
