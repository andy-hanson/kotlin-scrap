package org.noze.type

import org.noze.ast.Decl

sealed class Type {
	class Builtin private constructor(val kind: Kind) : Type() {
		enum class Kind {
			BOOL,
			CHAR,
			INT,
			INT8,
			INT16,
			INT64,
			REAL,
			REAL32,
			STRING
		}

		companion object {
			val Bool = Builtin(Kind.BOOL)
			val Char = Builtin(Kind.CHAR)
			val Int = Builtin(Kind.INT)
			val Int8 = Builtin(Kind.INT8)
			val Int16 = Builtin(Kind.INT16)
			val Int64 = Builtin(Kind.INT64)
			val Real = Builtin(Kind.REAL)
			val Real32 = Builtin(Kind.REAL32)
			val String = Builtin(Kind.STRING)

			val map = mapOf<String, Type.Builtin>(
				"Bool" to Bool,
				"Char" to Char,
				"Int" to Int,
				"Int8" to Int8,
				"Int16" to Int16,
				"Int64" to Int64,
				"Real" to Real,
				"Real32" to Real32,
				"String" to String)
		}

		override fun toString() =
			"$kind"
	}

	class Declared(val decl: Decl.Type) : Type()

	class Fn(val ret: Type, val args: List<Type>) : Type()
}
