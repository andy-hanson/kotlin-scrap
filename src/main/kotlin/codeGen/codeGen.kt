package org.noze.codeGen

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.signature.SignatureWriter

import org.noze.ast.Declaration
import org.noze.ast.Expr
import org.noze.ast.ModuleAst
import org.noze.ast.Parameter
import org.noze.ast.Signature
import org.noze.ast.Statement
import org.noze.check.Binding
import org.noze.check.CheckResult
import org.noze.type.Type

//TODO
//class CompiledModule {
// val functions: List<CompiledFn>
// val types: Array<CompiledType>
// private val functionsClass: Class<?>
//class CompiledFn() {
//	operator fun invoke() ...
//}

fun moduleToBytecode(m: ModuleAst, checks: CheckResult): ByteArray =
	CodeGen(checks).moduleToBytecode(m)

private class CodeGen(val checks: CheckResult) {
	fun moduleToBytecode(m: ModuleAst): ByteArray =
		ClassWriter(ClassWriter.COMPUTE_FRAMES).run {
			visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, "hello/HelloWorld", null, "java/lang/Object", null)
			visitSource("HelloWorld.nz", null)

			for (decl in m.declarations)
				writeDeclaration(decl)

			visitEnd()
			toByteArray()
		}

	private fun ClassWriter.writeDeclaration(decl: Declaration) {
		when (decl) {
			is Declaration.Fn ->
				writeFn(decl)
		}
	}

	private fun ClassWriter.writeFn(f: Declaration.Fn) {
		visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, f.name.string, signatureString(f.sig), null, null).run {
			MethodWriter(this).write(f)
			visitMaxs(0, 0)
			visitEnd()
		}
	}

	inner class MethodWriter(val mv: MethodVisitor) {
		val localNumbers = mutableMapOf<Parameter, Int>()
		var nextLocalNumber = 0

		private fun addLocal(p: Parameter) {
			localNumbers[p] = nextLocalNumber
			nextLocalNumber = nextLocalNumber + 1
		}

		private fun localNumber(p: Parameter): Int =
			localNumbers[p]!!

		fun write(f: Declaration.Fn) {
			val sig = f.sig
			for (arg in sig.args)
				addLocal(arg)
			writeBlock(f.body)
			mv.returnValue(checks.getRealType(f.sig.returnType))
		}

		fun writeBlock(block: Expr.Block) {
			for (line in block.lines)
				writeStatement(line)
			writeExpr(block.returned)
		}

		private fun writeStatement(statement: Statement) {
			when (statement) {
				is Expr -> {
					writeExpr(statement)
					mv.pop()
				}
				else -> TODO("OOO")
			}
		}

		private fun writeExpr(expr: Expr): Unit =
			when (expr) {
				is Expr.Access -> {
					val binding = checks.getBinding(expr)
					when (binding) {
						is Binding.Builtin -> TODO()
						is Binding.Decl -> TODO()
						is Binding.Local -> {
							val param = binding.declaration
							mv.load(checks.getType(param), localNumber(param))
						}
					}
				}

				is Expr.Block ->
					writeBlock(expr)

				is Expr.Call ->
					writeCall(expr)

				is Expr.Cond -> {
					writeExpr(expr.condition)
					val l1 = Label()
					val l2 = Label()
					mv.visitJumpInsn(Opcodes.IFEQ, l1)

					writeExpr(expr.ifTrue)
					mv.visitJumpInsn(Opcodes.GOTO, l2)

					mv.visitLabel(l1)
					writeExpr(expr.ifFalse)

					mv.visitLabel(l2)
				}

				is Expr.Literal -> {
					val value = expr.value
					when (value) {
						is Expr.Literal.Value.Float -> mv.ldc(value.value)
						is Expr.Literal.Value.Int -> mv.ldc(value.value)
						is Expr.Literal.Value.Bool -> mv.ldc(value.value)
						else -> throw AssertionError()
					}
				}
			}

		private fun writeCall(call: Expr.Call) {
			for (arg in call.args)
				writeExpr(arg)

			val ast = call.called
			if (ast is Expr.Access) {
				val bound = checks.getBinding(ast)
				when (bound) {
					is Binding.Builtin ->
						when (bound.kind) {
							Binding.Builtin.Kind.PLUS ->
								mv.visitInsn(Opcodes.IADD)
						}
					is Binding.Decl ->
						TODO("mv.invokeStatic")
					is Binding.Local ->
						TODO("call lambda")
				}
			} else
				TODO("call lambda")
		}
	}

	private fun signatureString(sig: Signature): String =
		SignatureWriter().run {
			for (arg in sig.args) {
				visitParameterType()
				sigType(checks.getRealType(arg.type))
			}
			visitReturnType()
			sigType(checks.getRealType(sig.returnType))
			toString()
		}
}

//TODO:RENAME
private fun SignatureWriter.sigType(type: Type) {
	when (type) {
		is Type.Builtin -> when (type.kind) {
			Type.Builtin.Kind.BOOL -> visitBaseType('Z')
			Type.Builtin.Kind.FLOAT -> visitBaseType('D')
			Type.Builtin.Kind.INT -> visitBaseType('I')
		}
	}
}
