package com.kotori316.scala_lib.asm;

import cpw.mods.modlauncher.TransformingClassLoader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;

@SuppressWarnings("SpellCheckingInspection")
public class CreateClass {
    // NOT USED

    public static final String CLASS_NAME = "com.kotori316.scala_lib.Context";
    public static final String EVENT_BUS_NAME = "L" + replace("net.minecraftforge.eventbus.api.IEventBus") + ";";
    public static final String PARAMETER_NAME = "L" + replace("net.minecraftforge.fml.javafmlmod.FMLModContainer") + ";";
    private static Class<?> aClass = null;

    public static Class<?> createClass(TransformingClassLoader classLoader) {
        if (aClass == null) {
            ClassWriter cw = new ClassWriter(0);
            cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER,
                CLASS_NAME.replace('.', '/'),
                null,
                replace("net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext"),
                null
            );
            // Field
            cw.visitField(ACC_PUBLIC, "instance", EVENT_BUS_NAME, null, null).visitEnd();
            //Method
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", String.format("(%s)V", EVENT_BUS_NAME), null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitInsn(ACONST_NULL);
                // This call is invalid because constructor of FMLJavaModLoadingContext is package-private.
                // The access modifier can't be changed via access transformer.
                mv.visitMethodInsn(INVOKESPECIAL, replace("net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext"),
                    "<init>", String.format("(%s)V", PARAMETER_NAME), false);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitFieldInsn(PUTFIELD, replace(CLASS_NAME), "instance", EVENT_BUS_NAME);
                mv.visitInsn(RETURN);
                mv.visitMaxs(2, 2);
                mv.visitEnd();
            }
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getModEventBus", "()" + EVENT_BUS_NAME, null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, replace(CLASS_NAME), "instance", EVENT_BUS_NAME);
                mv.visitInsn(ARETURN);
                mv.visitMaxs(2, 2);
                mv.visitEnd();
            }

            aClass = classLoader.getClass(CLASS_NAME, cw.toByteArray());
        }
        return aClass;
    }

    public static String replace(String s) {
        return s.replace('.', '/');
    }
}
