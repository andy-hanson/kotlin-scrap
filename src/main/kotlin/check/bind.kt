package org.noze.check

import org.noze.ast.Declaration
import org.noze.ast.Expr
import org.noze.ast.ExprVisitorUnit
import org.noze.ast.ModuleAst
import org.noze.ast.Parameter
import org.noze.CompileContext
import org.noze.Loc
import org.noze.symbol.Name

class Bindings(private val map: Map<Expr.Access, Binding>) {
	operator fun get(access: Expr.Access): Binding =
		map[access]!!

	override fun toString(): String =
		map.toString()
}

fun getBindings(module: ModuleAst, ctx: CompileContext): Bindings =
	BindingContext(ctx).run {
		addDeclarationBindings(module, this)
		useBindings(module, this)
		finish()
	}

private fun addDeclarationBindings(module: ModuleAst, bindings: BindingContext) {
	for (decl in module.declarations)
		when (decl) {
			is Declaration.Fn ->
				bindings.set(decl.loc, decl.name, Binding.Decl(decl))
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
	//val names = mutableMapOf<String, Binding>(*Binding.Builtin.map.entries.map { entry -> entry.key to entry.value }.toTypedArray())
	val names = mutableMapOf<String, Binding>().apply { putAll(Binding.Builtin.map) }

	//for ((key, value) in Binding.Builtin.map)
	//	names[key] = value

	//val typeNames = mutableMapOf<TypeName, Binding>()
	val accesses = mutableMapOf<Expr.Access, Binding>()
	//val typeAccesses = ...

	//TODO:RENAME
	fun bind(a: Expr.Access) {
		val boundTo = names[a.name.string] ?: throw ctx.fail(a.loc) { it.cantBind(a.name) }
		accesses[a] = boundTo
	}

	operator fun set(loc: Loc, name: Name, binding: Binding) {
		val bound = names.getOrPut(name.string) { binding }
		if (bound !== binding)
			ctx.fail(loc) { it.nameAlreadyAssigned(name) }
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
		if (local.name.string in names)
			throw ctx.fail(local.loc, { it.shadow(local.name) })
		names[local.name.string] = Binding.Local(local)
	}
	private fun unBindLocal(local: Parameter) {
		names.remove(local.name.string)
	}

	fun finish(): Bindings =
		Bindings(accesses)
}

private class ExpressionBinder(val ctx: BindingContext) : ExprVisitorUnit() {
	override fun visit(a: Expr.Access) {
		ctx.bind(a)
	}
}
