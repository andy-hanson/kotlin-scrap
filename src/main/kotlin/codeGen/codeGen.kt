package org.noze.codeGen

import codeGen.genFn
import codeGen.genRec
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type as AsmType

import org.noze.ast.Decl
import org.noze.ast.ModuleAst
import org.noze.check.CheckResult
import org.noze.symbol.TypeName

data class ModuleBytecode(val functions: ByteArray, val types: Map<TypeName, ByteArray>)
fun moduleToBytecode(m: ModuleAst, checks: CheckResult): ModuleBytecode =
	CodeGen(checks).write(m)

class CodeGen(val checks: CheckResult) {
	val functions = ClassWriter(ClassWriter.COMPUTE_FRAMES)
	val types = mutableMapOf<TypeName, ByteArray>()

	fun write(m: ModuleAst): ModuleBytecode {
		functions.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, "hello/HelloWorld", null, "java/lang/Object", null)
		functions.visitSource("HelloWorld.nz", null)

		for (decl in m.decls)
			writeDeclaration(decl)

		functions.visitEnd()

		return ModuleBytecode(functions.toByteArray(), types)
	}

	private fun writeDeclaration(decl: Decl) {
		decl.match(
			{ decl -> when (decl) {
				is Decl.Val.Fn ->
					genFn(functions, decl)
			}},
			{ decl -> when (decl) {
				is Decl.Type.Rec ->
					genRec(decl)
			}}
		)
	}
}
