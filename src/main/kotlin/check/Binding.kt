package org.noze.check

import org.noze.ast.Declaration
import org.noze.ast.Parameter
import org.noze.symbol.Name
import org.noze.type.Type

sealed class Binding {
	// Binds to a builtin.
	class Builtin(val kind: Kind) : Binding() {
		fun type(): Type =
			kind.type

		enum class Kind {
			PLUS {
				override val type =
					Type.Fn(Type.Builtin.Int, listOf(Type.Builtin.Int, Type.Builtin.Int))
			};

			abstract val type: Type
		}

		companion object {
			val map = mapOf<String, Builtin>(
				"+" to Builtin(Kind.PLUS))
			fun forName(name: Name): Builtin? =
				map[name.string]
			fun isNameBuiltin(name: Name): Boolean =
				map.contains(name.string)
		}
	}
	// Binds to some declaration.
	class Decl(val declaration: Declaration) : Binding()
	// Binds to a local variable.
	class Local(val declaration: Parameter): Binding()
}
