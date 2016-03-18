package org.noze.check

import org.noze.ast.Declaration
import org.noze.ast.Expr
import org.noze.ast.ExprVisitorUnit
import org.noze.ast.ModuleAst
import org.noze.ast.Parameter
import org.noze.CompileContext
import org.noze.symbol.Name

class Bindings(private val map: Map<Expr.Access, Binding>) {
	operator fun get(access: Expr.Access): Binding =
		map[access]!!

	override fun toString(): String =
		map.toString()
}

sealed class Binding {
	class Decl(val declaration: Declaration) : Binding()
	class Local(val declaration: Parameter): Binding()
}

fun getBindings(module: ModuleAst, ctx: CompileContext): Bindings =
	BindingContext(ctx).run {
		addBindings(module, this)
		useBindings(module, this)
		finish()
	}

private fun addBindings(module: ModuleAst, bindings: BindingContext) {
	for (decl in module.declarations)
		when (decl) {
			is Declaration.Fn ->
				bindings[decl.name] = Binding.Decl(decl)
		}
}

private fun useBindings(module: ModuleAst, bindings: BindingContext) {
	val e = ExpressionBinder(bindings)
	for (decl in module.declarations)
		when (decl) {
			is Declaration.Fn ->
				bindings.withLocals(decl.sig.args) {
					e.visit(decl.body, Unit)
				}
		}
}

private class BindingContext(private val ctx: CompileContext) {
	val names = mutableMapOf<Name, Binding>()
	//val typeNames = mutableMapOf<TypeName, Binding>()
	val accesses = mutableMapOf<Expr.Access, Binding>()
	//val typeAccesses = ...

	fun bind(a: Expr.Access) {
		val boundTo = names[a.name] ?: throw ctx.fail(a.loc) { it.cantBind(a.name) }
		accesses[a] = boundTo
	}

	operator fun set(name: Name, binding: Binding) {
		names[name] = binding
	}

	inline fun withLocal(local: Parameter, action: () -> Unit) {
		tempBindLocal(local)
		action()
		unBindLocal(local)
	}

	inline fun withLocals(locals: Iterable<Parameter>, action: () -> Unit) {
		for (local in locals)
			tempBindLocal(local)
		action()
		for (local in locals)
			unBindLocal(local)
	}

	private fun tempBindLocal(local: Parameter) {
		if (local.name in names)
			throw ctx.fail(local.loc, { it.shadow(local.name) })
		names[local.name] = Binding.Local(local)
	}
	private fun unBindLocal(local: Parameter) {
		names.remove(local.name)
	}

	fun finish(): Bindings =
		Bindings(accesses)
}

private class ExpressionBinder(val ctx: BindingContext) : ExprVisitorUnit() {
	override fun visit(a: Expr.Access) {
		ctx.bind(a)
	}
}
