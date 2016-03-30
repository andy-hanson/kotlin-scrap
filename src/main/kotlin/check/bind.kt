package org.noze.check

import org.noze.ast.Decl
import org.noze.ast.Expr
import org.noze.ast.ExprVisitorUnit
import org.noze.ast.ModuleAst
import org.noze.ast.Parameter
import org.noze.CompileContext
import org.noze.Loc
import org.noze.ast.TypeAst
import org.noze.symbol.Name
import org.noze.symbol.TypeName
import org.noze.type.Type
import org.noze.util.noElse

class Bindings(private val vals: Map<Expr.Access, Binding>, private val types: Map<TypeAst.Access, Type>) {
	operator fun get(access: Expr.Access): Binding =
		vals[access]!!

	operator fun get(access: TypeAst.Access): Type =
		types[access]!!

	override fun toString(): String =
		vals.toString() + " -- " + types.toString()
}

fun getBindings(module: ModuleAst, ctx: CompileContext): Bindings =
	BindingContext(ctx).run {
		addDeclarationBindings(module, this)
		useBindings(module, this)
		finish()
	}

private fun addDeclarationBindings(module: ModuleAst, bindings: BindingContext) {
	for (decl in module.decls)
		decl.match(
			{ decl -> when (decl) {
				is Decl.Val.Fn ->
					bindings.set(decl.loc, decl.name, Binding.Declared(decl))
			}},
			{ decl -> when (decl) {
				is Decl.Type.Rec ->
					bindings.set(decl.loc, decl.name, Type.Declared(decl))
			}})
}

private fun useBindings(module: ModuleAst, ctx: BindingContext) {
	val e = ExpressionBinder(ctx)
	for (decl in module.decls)
		decl.match(
			{ decl -> when (decl) {
				is Decl.Val.Fn -> {
					ctx.bindType(decl.sig.returnType)
					for (arg in decl.sig.args)
						ctx.bindType(arg.type)
					ctx.withLocals(decl.sig.args) {
						e.visit(decl.body)
					}
				}
			}},
			{ decl -> when (decl) {
				is Decl.Type.Rec ->
					for (prop in decl.properties)
						ctx.bindType(prop.type)
			}})
}

//TODO:RENAME
private fun BindingContext.bindType(type: TypeAst) {
	noElse(when (type) {
		is TypeAst.Access ->
			bind(type)
	})
}

private class BindingContext(private val ctx: CompileContext) {
	val names = mutableMapOf<String, Binding>().apply { putAll(Binding.Builtin.map) }
	// TODO: builtin types can be handled same as builtin names
	val typeNames = mutableMapOf<String, Type>().apply { putAll(Type.Builtin.map) }

	val accesses = mutableMapOf<Expr.Access, Binding>()
	val typeAccesses = mutableMapOf<TypeAst.Access, Type>()

	//TODO:RENAME
	fun bind(a: Expr.Access) {
		val boundTo = names[a.name.string] ?: throw ctx.fail(a.loc) { it.cantBind(a.name) }
		accesses[a] = boundTo
	}

	//TODO:RENAME
	fun bind(a: TypeAst.Access) {
		val boundTo = typeNames[a.name.string] ?: throw ctx.fail(a.loc) { it.cantBind(a.name) }
		typeAccesses[a] = boundTo
	}

	operator fun set(loc: Loc, name: Name, binding: Binding) {
		val bound = names.getOrPut(name.string) { binding }
		if (bound !== binding)
			ctx.fail(loc) { it.nameAlreadyAssigned(name) }
	}

	operator fun set(loc: Loc, name: TypeName, binding: Type) {
		val bound = typeNames.getOrPut(name.string) { binding }
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
		Bindings(accesses, typeAccesses)
}

private class ExpressionBinder(val ctx: BindingContext) : ExprVisitorUnit() {
	override fun visit(a: Expr.Access) {
		ctx.bind(a)
	}
}
