package codeGen

import org.noze.ast.Decl
import org.noze.codeGen.CodeGen
import org.noze.codeGen.dup
import org.noze.codeGen.ldc
import org.noze.codeGen.load
import org.noze.codeGen.loadThis
import org.noze.codeGen.new
import org.noze.codeGen.typeDesc
import org.noze.codeGen.typeDescSig
import org.noze.codeGen.voidSignatureString
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type as AsmType

fun CodeGen.genRec(rec: Decl.Type.Rec) {
	types[rec.name] = ClassWriter(ClassWriter.COMPUTE_FRAMES).run {
		// TODO: include package name (= module name)
		val name = rec.name.string
		// TODO: first null: we may have a signature
		// TODO: second null: list of interfaces
		visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, name, null, "java/lang/Object", null)
		visitSource("HelloWorld.nz", null) //TODO

		for (prop in rec.properties) {
			val (desc, signature) = typeDescSig(prop.type)
			val fv = visitField(Opcodes.ACC_PUBLIC, prop.name.string, desc, signature, null)
			fv.visitEnd()
		}

		genCtr(this, rec)
		genToString(this, rec)

		visitEnd()
		toByteArray()
	}
}

private fun CodeGen.genCtr(cw: ClassWriter, rec: Decl.Type.Rec) {
	val sig = voidSignatureString(rec.properties.map { realType(it.type) })
	cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", sig, null, null).apply {
		visitCode()
		val l0 = Label()

		//TODO: include package in name
		val name = rec.name.string

		// super.init()
		loadThis()
		visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)

		// set fields
		for ((idx, prop) in rec.properties.withIndex()) {
			loadThis()
			load(idx + 1, realType(prop.type))
			//TODO: clean up all uses of checks.getRealType
			//TODO: mv.putField helper
			visitFieldInsn(Opcodes.PUTFIELD, name, prop.name.string, typeStr(prop.type))
		}

		visitInsn(Opcodes.RETURN)

		val l1 = Label()
		visitLocalVariable("this", "L$name;", null, l0, l1, 0)
		//TODO: get names, types from args
		visitLocalVariable("x", "F", null, l0, l1, 1)
		visitLocalVariable("y", "F", null, l0, l1, 2)

		visitMaxs(0, 0)
		visitEnd()
	}
}

private fun CodeGen.genToString(cw: ClassWriter, rec: Decl.Type.Rec) {
	cw.visitMethod(Opcodes.ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null).apply {
		visitCode()
		val l0 = Label()

		//TODO: include package in name
		val name = rec.name.string

		val sb = AsmType.getInternalName(StringBuilder::class.java)
		val sbInit = AsmType.getConstructorDescriptor(StringBuilder::class.java.getConstructor(String::class.java))
		val sbToString = AsmType.getMethodDescriptor(StringBuilder::class.java.getMethod("toString"))
		val sbAppendStr = AsmType.getMethodDescriptor(StringBuilder::class.java.getMethod("append", String::class.java))

		fun appendStr(str: String) {
			ldc(str)
			visitMethodInsn(Opcodes.INVOKEVIRTUAL, sb, "append", sbAppendStr, false)
		}

		new(sb)
		dup()
		ldc("(Point ")
		visitMethodInsn(Opcodes.INVOKESPECIAL, sb, "<init>", sbInit, false)

		for ((idx, prop) in rec.properties.withIndex()) {
			dup() // The string builder
			loadThis()
			visitFieldInsn(Opcodes.GETFIELD, name, prop.name.string, typeStr(prop.type))
			TODO()
			val desc = "(F)Ljava/lang/StringBuilder;" //TODO
			visitMethodInsn(Opcodes.INVOKEVIRTUAL, sb, "append", desc, false)

			if (idx != rec.properties.size - 1)
				appendStr(" ")
		}

		appendStr(")")

		visitMethodInsn(Opcodes.INVOKEVIRTUAL, sb, "toString", sbToString, false)
		visitInsn(Opcodes.ARETURN)

		val l1 = Label()
		visitLabel(l1)
		visitLocalVariable("this", "L$name;", null, l0, l1, 0)
		visitMaxs(0, 0)
		visitEnd()
	}
}
