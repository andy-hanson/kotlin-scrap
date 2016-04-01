package org.noze.codeGen

import org.noze.ast.Signature
import org.noze.type.Type
import org.noze.util.noElse
import org.objectweb.asm.signature.SignatureWriter

fun CodeGen.voidSignatureString(args: List<Type>): String =
	SignatureWriter().run {
		for (arg in args) {
			visitParameterType()
			sigType(arg)
		}
		visitReturnType()
		visitBaseType('V')
		toString()
	}

fun CodeGen.signatureString(sig: Signature): String =
	SignatureWriter().run {
		//visitFormalTypeParameter
		//	(then use visitTypeVariable later)
		for (arg in sig.args) {
			visitParameterType()
			sigType(realType(arg.type))
		}
		visitReturnType()
		sigType(realType(sig.returnType))
		toString()
	}

fun typeDesc(type: Type): String =
	typeDescSig(type).first

fun typeDescSig(type: Type): Pair<String, String?> =
	when (type) {
		is Type.Builtin -> {
			val desc = when (type.kind) {
				Type.Builtin.Kind.BOOL -> org.objectweb.asm.Type.BOOLEAN_TYPE
				Type.Builtin.Kind.CHAR -> org.objectweb.asm.Type.CHAR_TYPE
				Type.Builtin.Kind.INT -> org.objectweb.asm.Type.INT_TYPE
				Type.Builtin.Kind.INT8 -> org.objectweb.asm.Type.BYTE_TYPE
				Type.Builtin.Kind.INT16 -> org.objectweb.asm.Type.SHORT_TYPE
				Type.Builtin.Kind.INT64 -> org.objectweb.asm.Type.LONG_TYPE
				Type.Builtin.Kind.REAL -> org.objectweb.asm.Type.DOUBLE_TYPE
				Type.Builtin.Kind.REAL32 -> org.objectweb.asm.Type.FLOAT_TYPE
				Type.Builtin.Kind.STRING -> org.objectweb.asm.Type.getType(String::class.java)
			// -> AsmType.VOID_TYPE
			}
			Pair(desc.descriptor, null)
		}
		is Type.Declared -> {
			val desc = org.objectweb.asm.Type.getObjectType(type.decl.name.string)
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
fun SignatureWriter.sigType(type: Type) {
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
