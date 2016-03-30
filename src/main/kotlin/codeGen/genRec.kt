package codeGen

import org.noze.ast.Decl
import org.noze.codeGen.CodeGen
import org.noze.codeGen.typeDescSig
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

fun CodeGen.genRec(rec: Decl.Type.Rec) {
	types[rec.name] = ClassWriter(ClassWriter.COMPUTE_FRAMES).run {
		// TODO: include package name (= module name)
		val name = rec.name.string
		// TODO: first null: we may have a signature
		// TODO: second null: list of interfaces
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
}
