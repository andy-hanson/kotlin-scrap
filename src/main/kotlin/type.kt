package org.noze.type

sealed class Type {
	fun combine(other: Type): Type =
		when (other) {
			is Builtin -> combine(other)
			is Union -> combine(other)
		}

	abstract fun combine(other: Builtin): Type
	abstract fun combine(other: Union): Type

	class Builtin(val kind: BuiltinType) : Type() {
		override fun combine(other: Builtin): Type =
			if (this == other) this else Type.Union(this, other)

		override fun combine(other: Union): Type =
			TODO()
	}

	class Union(vararg types: Type) : Type() {
		override fun combine(other: Builtin) =
			TODO()

		override fun combine(other: Union): Type =
			TODO()
	}
}

enum class BuiltinType {
	BOOL,
	INT,
	FLOAT
}
