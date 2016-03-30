package org.noze.type

sealed class Type {
	fun combine(other: Type): Type =
		when (other) {
			is Builtin -> combine(other)
			is Fn -> combine(other)
			is Union -> combine(other)
		}

	abstract fun combine(other: Builtin): Type
	abstract fun combine(other: Fn): Type
	abstract fun combine(other: Union): Type

	class Builtin private constructor(val kind: Kind) : Type() {
		enum class Kind {
			BOOL,
			INT,
			FLOAT
		}

		companion object {
			//fun of(kind: Kind): Builtin = when (kind) {
			//	BOOL -> Bool
			//}
			val Bool = Builtin(Kind.BOOL)
			val Int = Builtin(Kind.INT)
			val Float = Builtin(Kind.FLOAT)
		}

		override fun combine(other: Builtin): Type =
			if (this == other) this else Type.Union(this, other)
		override fun combine(other: Fn) = TODO()
		override fun combine(other: Union): Type = TODO()

		override fun toString() =
			"$kind"
	}

	class Fn(val ret: Type, val args: List<Type>) : Type() {
		override fun combine(other: Builtin) = TODO()
		override fun combine(other: Fn) = TODO()
		override fun combine(other: Union) = TODO()
	}

	class Union(vararg types: Type) : Type() {
		override fun combine(other: Builtin) = TODO()
		override fun combine(other: Fn) = TODO()
		override fun combine(other: Union): Type = TODO()
	}
}
