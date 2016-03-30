package org.noze.codeGen

import org.noze.Module
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.signature.SignatureWriter
import org.objectweb.asm.Type as AsmType

import org.noze.ast.Decl
import org.noze.ast.Expr
import org.noze.ast.ModuleAst
import org.noze.ast.Parameter
import org.noze.ast.Signature
import org.noze.ast.Statement
import org.noze.check.Binding
import org.noze.check.CheckResult
import org.noze.symbol.TypeName
import org.noze.type.Type
import org.noze.util.noElse

data class ModuleBytecode(val functions: ByteArray, val types: Map<TypeName, ByteArray>)
fun moduleToBytecode(m: ModuleAst, checks: CheckResult): ModuleBytecode =
	CodeGen(checks).write(m)

private class CodeGen(val checks: CheckResult) {
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

	private fun record(rec: Decl.Type.Rec) {
		/*
		types[rec.name] = ClassWriter(ClassWriter.COMPUTE_FRAMES).run {
			// TODO: include package name (= module name)
			val name = rec.name.string
			visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, name, null, "java/lang/Object", null)
			visitSource("HelloWorld.nz", null) //TODO

			for (prop in rec.properties) {
				val (desc, signature) = typeDescSig(checks.getRealType(prop.type))
				val fv = visitField(Opcodes.ACC_PUBLIC, prop.name.string, desc, signature, null)
				fv.visitEnd()
			}

			// TODO: Create a constructor

			visitEnd()
			toByteArray()
		}
		*/
	}

	private fun writeDeclaration(decl: Decl) {
		decl.match(
			{ decl -> when (decl) {
				is Decl.Val.Fn ->
					functions.writeFn(decl)
			}},
			{ decl -> when (decl) {
				is Decl.Type.Rec ->
					record(decl)
			}}
		)
	}

	private fun ClassWriter.writeFn(f: Decl.Val.Fn) {
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
								mv.invokeStatic("hello/HelloWorld", decl.name.string, signatureString(decl.sig))
						}
					}
					is Binding.Local ->
						TODO("call lambda")
				}
			} else
				TODO("call lambda")
		}
	}

	private fun signatureString(sig: Signature): String =
		SignatureWriter().run {
			//visitFormalTypeParameter
			//	(then use visitTypeVariable later)
			for (arg in sig.args) {
				visitParameterType()
				sigType(checks.getRealType(arg.type))
			}
			visitReturnType()
			sigType(checks.getRealType(sig.returnType))
			toString()
		}
}

//move
private fun typeDescSig(type: Type): Pair<String, String?> =
	when (type) {
		is Type.Builtin -> {
			val desc = when (type.kind) {
				Type.Builtin.Kind.BOOL -> AsmType.BOOLEAN_TYPE
				Type.Builtin.Kind.CHAR -> AsmType.CHAR_TYPE
				Type.Builtin.Kind.INT -> AsmType.INT_TYPE
				Type.Builtin.Kind.INT8 -> AsmType.BYTE_TYPE
				Type.Builtin.Kind.INT16 -> AsmType.SHORT_TYPE
				Type.Builtin.Kind.INT64 -> AsmType.LONG_TYPE
				Type.Builtin.Kind.REAL -> AsmType.DOUBLE_TYPE
				Type.Builtin.Kind.REAL32 -> AsmType.FLOAT_TYPE
				Type.Builtin.Kind.STRING -> AsmType.getType(String::class.java)
				// -> AsmType.VOID_TYPE
			}
			Pair(desc.descriptor, null)
		}
		is Type.Declared -> {
			val desc = AsmType.getObjectType(type.decl.name.string)
			val sig = null
			Pair(desc.descriptor, sig)
		}
		is Type.Fn -> {
			val desc = "kotlin/lang/Function"
			val sig = SignatureWriter().run {
				TODO()
				// TODO: Function/1? Function/2?
				visitClassType(Function::class.java.name)
				for (arg in type.args) {
					visitTypeArgument()
					sigType(arg)
				}
				visitTypeArgument()
				sigType(type.ret)
				toString()
			}
			Pair(desc, sig)
		}
	}

//TODO:RENAME
private fun SignatureWriter.sigType(type: Type) {
	when (type) {
		is Type.Builtin -> noElse(when (type.kind) {
			Type.Builtin.Kind.BOOL -> visitBaseType('Z')
			Type.Builtin.Kind.CHAR -> visitBaseType('C')
			Type.Builtin.Kind.INT -> visitBaseType('I')
			Type.Builtin.Kind.INT8 -> visitBaseType('B')
			Type.Builtin.Kind.INT16 -> visitBaseType('S')
			Type.Builtin.Kind.INT64 -> visitBaseType('J')
			Type.Builtin.Kind.REAL -> visitBaseType('D')
			Type.Builtin.Kind.REAL32 -> visitBaseType('F')
			Type.Builtin.Kind.STRING -> visitClassType("java/lang/String")
		})
		/*
		"LClassname;" : some class
		"[": an array
		*/
	}
}
