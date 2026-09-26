package com.liaoliao.flighthelmet.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/** Changes only terrain visibility, never the player's actual spectator/game mode state. */
public final class GhostRenderTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] original) {
        if (original == null || !"net.minecraft.client.renderer.RenderGlobal".equals(transformedName)) return original;
        ClassNode node = new ClassNode();
        new ClassReader(original).accept(node, 0);
        for (MethodNode method : node.methods) {
            if (("setupTerrain".equals(method.name) || "func_174970_a".equals(method.name))
                    && "(Lnet/minecraft/entity/Entity;DLnet/minecraft/client/renderer/culling/ICamera;IZ)V".equals(method.desc)) {
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ILOAD, 6));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/liaoliao/flighthelmet/client/GhostClientEvents", "useUnrestrictedTerrain", "(Z)Z", false));
                hook.add(new VarInsnNode(Opcodes.ISTORE, 6));
                method.instructions.insert(hook);
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                node.accept(writer);
                return writer.toByteArray();
            }
        }
        throw new IllegalStateException("PigThings could not locate RenderGlobal.setupTerrain for ghost visibility");
    }
}
