package org.noze

import org.noze.codeGen.moduleToBytecode
import org.noze.check.CheckResult
import org.noze.check.getBindings
import org.noze.check.typeCheck
import org.noze.lex.lex
import org.noze.parse.parse

fun compile(source: String): ByteArray {
	val ctx = CompileContext()

	val token = lex(source, ctx)

	val ast = parse(token, ctx)

	val bindings = getBindings(ast, ctx)

	val types = typeCheck(ast, bindings, ctx)

	val checks = CheckResult(bindings, types)

	return moduleToBytecode(ast, checks)
}
