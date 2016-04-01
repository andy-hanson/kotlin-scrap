package org.noze.codeGen

import org.noze.Module
import org.noze.ast.Decl
import org.objectweb.asm.Type as AsmType
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

import org.noze.type.Type

fun MethodVisitor.new(type: String) {
	visitTypeInsn(Opcodes.NEW, type)
}

fun MethodVisitor.dup() {
	visitInsn(Opcodes.DUP)
}

fun MethodVisitor.ldc(value: Boolean) {
	visitLdcInsn(value)
}

fun MethodVisitor.ldc(value: Double) {
	visitLdcInsn(value)
}

fun MethodVisitor.ldc(value: String) {
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
fun MethodVisitor.invokeStatic(className: String, name: String, signature: String) {
	//AsmType.getMethodDescriptor(StringBuilder::class.java.getMethod("append", String::class.java))
	visitMethodInsn(Opcodes.INVOKESTATIC, className, name, signature, false)
}

fun MethodVisitor.returnValue(type: Type) {
	visitInsn(returnOpcode(type))
}

fun MethodVisitor.load(index: Int, type: Type) {
	visitVarInsn(loadOpcode(type), index)
}

fun MethodVisitor.loadThis() {
	visitVarInsn(Opcodes.ALOAD, 0)
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
			Type.Builtin.Kind.CHAR -> TypeKind.CHAR
			Type.Builtin.Kind.INT, Type.Builtin.Kind.INT8, Type.Builtin.Kind.INT16, Type.Builtin.Kind.BOOL -> TypeKind.INT
			Type.Builtin.Kind.INT64 -> TypeKind.LONG
			Type.Builtin.Kind.REAL -> TypeKind.DOUBLE
			Type.Builtin.Kind.REAL32 -> TypeKind.FLOAT
			else -> TypeKind.OBJECT
		}
		is Type.Declared ->
			when (type.decl) {
				is Decl.Type.Rec ->
					TypeKind.OBJECT
			}
		is Type.Fn ->
			TypeKind.OBJECT
	}

private fun returnOpcode(type: Type): Int =
	returnOpcode(typeKind(type))

private fun returnOpcode(kind: TypeKind): Int =
	when (kind) {
		TypeKind.BOOL, TypeKind.CHAR, TypeKind.INT -> Opcodes.IRETURN
		TypeKind.LONG -> Opcodes.LRETURN
		TypeKind.FLOAT -> Opcodes.FRETURN
		TypeKind.DOUBLE -> Opcodes.DRETURN
		TypeKind.OBJECT -> Opcodes.ARETURN
	}

private fun loadOpcode(type: Type): Int =
	loadOpcode(typeKind(type))

private fun loadOpcode(kind: TypeKind): Int =
	when (kind) {
		TypeKind.BOOL, TypeKind.CHAR, TypeKind.INT -> Opcodes.ILOAD
		TypeKind.LONG -> Opcodes.LLOAD
		TypeKind.FLOAT -> Opcodes.FLOAD
		TypeKind.DOUBLE -> Opcodes.DLOAD
		TypeKind.OBJECT -> Opcodes.ALOAD
	}

private enum class TypeKind {
	CHAR,
	BOOL,
	INT,
	LONG,
	FLOAT,
	DOUBLE,
	OBJECT
}
