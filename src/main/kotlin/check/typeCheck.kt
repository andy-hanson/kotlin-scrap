package org.noze.check

import org.noze.ast.Declaration
import org.noze.ast.Expr
import org.noze.ast.ExprVisitor
import org.noze.ast.ModuleAst
import org.noze.ast.TypeAst
import org.noze.CompileContext
import org.noze.type.BuiltinType
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
					getter.type(decl.body)
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

	override fun visit(a: Expr.Access, p: Unit): Type {
		val binding = ctx.bindings[a]
		val typeAst = when (binding) {
			is Binding.Decl->
				throw Exception("TODO")
			is Binding.Local ->
				binding.declaration.type
		}
		return declaredType(typeAst)
	}

	override fun visit(a: Expr.Block, p: Unit): Type =
		// TODO: lines
		type(a.returned)

	override fun visit(a: Expr.Call, p: Unit): Type =
		TODO()

	override fun visit(a: Expr.Cond, p: Unit): Type =
		type(a.ifTrue).combine(type(a.ifFalse))

	override fun visit(a: Expr.Literal, p: Unit): Type =
		Type.Builtin(when (a.value) {
			is Expr.Literal.Value.Bool ->
				BuiltinType.BOOL
			is Expr.Literal.Value.Int ->
				BuiltinType.INT
			is Expr.Literal.Value.Float ->
				BuiltinType.FLOAT
		})
}

private fun declaredType(ast: TypeAst): Type =
	when (ast) {
		is TypeAst.Builtin ->
			Type.Builtin(ast.kind)
		is TypeAst.Named ->
			TODO()
	}
