package foundation.esoteric.fireworkwarscore.util

import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import org.bukkit.World
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.entity.Player
import java.util.function.Consumer
import java.util.function.Function

@Suppress("UNCHECKED_CAST", "unused", "MemberVisibilityCanBePrivate")
object NMSUtil {
    fun <T : Entity> toNMSEntity(bukkit: org.bukkit.entity.Entity): T {
        return (bukkit as CraftEntity).handle as T
    }

    fun toNMSWorld(world: World): Level {
        return (world as org.bukkit.craftbukkit.CraftWorld).handle
    }

    fun sendPacket(player: Player, packetCreator: Function<ServerPlayer?, Packet<ClientGamePacketListener?>?>) {
        val serverPlayer: ServerPlayer = toNMSEntity(player)
        serverPlayer.connection.send(packetCreator.apply(serverPlayer)!!)
    }

    fun sendEntityAdd(player: Player, entity: org.bukkit.entity.Entity) {
        val nmsEntity: Entity = toNMSEntity(entity)

        sendPacket(player) {
            ClientboundAddEntityPacket(
                nmsEntity, nmsEntity.id, BlockPos(nmsEntity.blockX, nmsEntity.blockY, nmsEntity.blockZ))
        }
    }

    fun getCollidingEntities(entity: Entity, hitboxModifier: Consumer<AABB?>): List<org.bukkit.entity.Entity> {
        val world: Level = entity.level()
        val entityBoundingBox: AABB = entity.boundingBox

        hitboxModifier.accept(entityBoundingBox)

        val result: MutableList<org.bukkit.entity.Entity> = ArrayList()

        for (otherEntity in world.entities.all) {
            if (otherEntity !== entity) {
                val otherEntityBoundingBox: AABB = otherEntity.boundingBox

                if (entityBoundingBox.intersects(otherEntityBoundingBox)) {
                    result.add(otherEntity.bukkitEntity)
                }
            }
        }

        return result
    }

    fun getCollidingLivingEntities(entity: Entity, hitboxModifier: Consumer<AABB?>): List<LivingEntity> {
        return getCollidingEntities(entity, hitboxModifier).stream()
            .filter { LivingEntity::class.java.isInstance(it) }
            .map { LivingEntity::class.java.cast(it) }
            .toList()
    }
}