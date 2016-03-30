package org.noze.codeGen

import org.objectweb.asm.Type as AsmType
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

import org.noze.type.Type

fun MethodVisitor.ldc(value: Boolean) {
	visitLdcInsn(value)
}

fun MethodVisitor.ldc(value: Double) {
	visitLdcInsn(value)
}

fun MethodVisitor.ldc(value: Int) {
	when (value) {
		in -1..5 -> visitInsn(when (value) {
			-1 -> Opcodes.ICONST_M1
			0 -> Opcodes.ICONST_0
			1 -> Opcodes.ICONST_1
			2 -> Opcodes.ICONST_2
			3 -> Opcodes.ICONST_3
			4 -> Opcodes.ICONST_4
			else -> Opcodes.ICONST_5
		})
		else -> visitLdcInsn(value)
	}
}

// TODO:ARGS
fun MethodVisitor.invokeStatic() {
	AsmType.getMethodDescriptor(StringBuilder::class.java.getMethod("append", String::class.java))
	visitMethodInsn(Opcodes.INVOKESTATIC, "Builtins", "+", "", false)
}

fun MethodVisitor.returnValue(type: Type) {
	visitInsn(returnOpcode(type))
}

fun MethodVisitor.load(type: Type, index: Int) {
	visitVarInsn(loadOpcode(type), index)
}

fun MethodVisitor.loc(line: Int) {
	loc(Label(), line)
}

fun MethodVisitor.loc(label: Label, line: Int) {
	visitLabel(label)
	visitLineNumber(line, label)
}

fun MethodVisitor.pop() {
	visitInsn(Opcodes.POP)
}

private fun typeKind(type: Type): TypeKind =
	when (type) {
		is Type.Builtin -> when (type.kind) {
			Type.Builtin.Kind.BOOL -> TypeKind.BOOL
			Type.Builtin.Kind.INT -> TypeKind.INT
			Type.Builtin.Kind.FLOAT -> TypeKind.DOUBLE
		}
		is Type.Fn ->
			TypeKind.OBJECT
		is Type.Union ->
			TypeKind.OBJECT
	}

private fun returnOpcode(type: Type): Int =
	returnOpcode(typeKind(type))

private fun returnOpcode(kind: TypeKind): Int =
	when (kind) {
		TypeKind.BOOL -> Opcodes.IRETURN
		TypeKind.INT -> Opcodes.IRETURN
		TypeKind.LONG -> Opcodes.LRETURN
		TypeKind.FLOAT -> Opcodes.FRETURN
		TypeKind.DOUBLE -> Opcodes.DRETURN
		TypeKind.OBJECT -> Opcodes.ARETURN
		else -> throw Error()
	}

private fun loadOpcode(type: Type): Int =
	loadOpcode(typeKind(type))

private fun loadOpcode(kind: TypeKind): Int =
	when (kind) {
		TypeKind.BOOL -> Opcodes.ILOAD
		TypeKind.INT -> Opcodes.ILOAD
		TypeKind.LONG -> Opcodes.LLOAD
		TypeKind.FLOAT -> Opcodes.FLOAD
		TypeKind.DOUBLE -> Opcodes.DLOAD
		TypeKind.OBJECT -> Opcodes.ALOAD
	}

private enum class TypeKind {
	BOOL,
	INT,
	LONG,
	FLOAT,
	DOUBLE,
	OBJECT
}
