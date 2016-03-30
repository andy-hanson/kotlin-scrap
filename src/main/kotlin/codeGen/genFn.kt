package codeGen

import org.noze.ast.Decl
import org.noze.ast.Expr
import org.noze.ast.Parameter
import org.noze.ast.Statement
import org.noze.check.Binding
import org.noze.check.CheckResult
import org.noze.codeGen.CodeGen
import org.noze.codeGen.invokeStatic
import org.noze.codeGen.ldc
import org.noze.codeGen.load
import org.noze.codeGen.pop
import org.noze.codeGen.returnValue
import org.noze.codeGen.signatureString
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

//TODO: cw is always this.functions...
fun CodeGen.genFn(cw: ClassWriter, f: Decl.Val.Fn) {
	cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, f.name.string, signatureString(f.sig), null, null).run {
		MethodWriter(this, this@genFn).write(f)
		visitMaxs(0, 0)
		visitEnd()
	}
}

private class MethodWriter(val mv: MethodVisitor, val codeGen: CodeGen) {
	val checks: CheckResult
		get() = codeGen.checks


	val localNumbers = mutableMapOf<Parameter, Int>()
	var nextLocalNumber = 0

	private fun addLocal(p: Parameter) {
		localNumbers[p] = nextLocalNumber
		nextLocalNumber = nextLocalNumber + 1
	}

	private fun localNumber(p: Parameter): Int =
		localNumbers[p]!!

	fun write(f: Decl.Val.Fn) {
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
					is Binding.Declared -> TODO()
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
					is Expr.Literal.Value.Bool -> mv.ldc(value.value)
					is Expr.Literal.Value.Int -> mv.ldc(value.value)
					is Expr.Literal.Value.Real -> mv.ldc(value.value)
				}
			}
		}

	private fun writeCall(call: Expr.Call) {
		for (arg in call.args)
			writeExpr(arg)

		val ast = call.called
		if (ast is Expr.Access) {
			val bound = checks.getBinding(ast)
			return when (bound) {
				is Binding.Builtin ->
					when (bound.kind) {
						Binding.Builtin.Kind.PLUS ->
							mv.visitInsn(Opcodes.IADD)
					}
				is Binding.Declared -> {
					val decl = bound.decl
					when (decl) {
						is Decl.Val.Fn ->
							mv.invokeStatic("hello/HelloWorld", decl.name.string, codeGen.signatureString(decl.sig))
					}
				}
				is Binding.Local ->
					TODO("call lambda")
			}
		} else
			TODO("call lambda")
	}
}
