/*
package check

import ast.Declaration
import ast.ModuleAst
import ast.Parameter

class LocalNumbering(private val numbers: Map<Parameter, Int>) {
	//TODO: support all locals, not just parameters!
	fun getNumber(local: Parameter): Int =
		numbers[local]!!
}

fun numberLocals(module: ModuleAst): LocalNumbering {
	val numbers = mutableMapOf<Parameter, Int>()

	for (decl in module.declarations)
		when (decl) {
			is Declaration.Fn -> {
				val params = decl.sig.args
				for (i in 0..(params.size - 1))
					numbers[params[i]] = i
			}
		}

	return LocalNumbering(numbers)
}
*/
