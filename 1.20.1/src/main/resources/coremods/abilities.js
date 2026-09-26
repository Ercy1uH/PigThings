function initializeCoreMod() {
    var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
    var Opcodes = Java.type('org.objectweb.asm.Opcodes');
    var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
    var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
    var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
    return {
        'ghost_terrain': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.renderer.LevelRenderer',
                'methodName': ASMAPI.mapMethod('m_194338_'),
                'methodDesc': '(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V'
            },
            'transformer': function(method) {
                var hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ILOAD, 4));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    'com/liaoliao/flighthelmet/client/AbilityClientEvents', 'unrestrictedTerrain', '(Z)Z', false));
                hook.add(new VarInsnNode(Opcodes.ISTORE, 4));
                method.instructions.insert(hook);
                return method;
            }
        },
        'carrot_travel_staff': {
            'target': {
                'type': 'METHOD',
                'class': 'com.enderio.base.common.handler.TravelHandler',
                'methodName': 'canItemTeleport',
                'methodDesc': '(Lnet/minecraft/world/entity/player/Player;)Z'
            },
            'transformer': function(method) {
                var instructions = method.instructions.toArray();
                for (var i = 0; i < instructions.length; i++) {
                    if (instructions[i].getOpcode() === Opcodes.IRETURN) {
                        var hook = new InsnList();
                        hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            'com/liaoliao/flighthelmet/CarrotSaberTeleport', 'canItemTeleport',
                            '(ZLnet/minecraft/world/entity/player/Player;)Z', false));
                        method.instructions.insertBefore(instructions[i], hook);
                    }
                }
                return method;
            }
        }
    };
}
