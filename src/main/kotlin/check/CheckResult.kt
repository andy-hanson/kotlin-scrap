package org.noze.check

import org.noze.ast.Expr
import org.noze.ast.Parameter
import org.noze.ast.TypeAst
import org.noze.type.Type

class CheckResult(private val bindings: Bindings, private val types: Types) {
	fun getType(access: Expr.Access): Type {
		val ld = bindings[access]
		return when (ld) {
			is Binding.Builtin -> when (ld.kind) {
				Binding.Builtin.Kind.PLUS -> TODO()
			}
			is Binding.Declared ->
				TODO()
			is Binding.Local ->
				getRealType(ld.declaration.type)
		}
	}

	fun getType(param: Parameter): Type =
		getRealType(param.type)

	fun getBinding(a: Expr.Access): Binding =
		bindings[a]

	fun getRealType(type: TypeAst): Type =
		when (type) {
			is TypeAst.Access ->
				bindings[type]
		}
}
