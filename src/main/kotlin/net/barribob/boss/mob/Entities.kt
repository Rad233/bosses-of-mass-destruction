package net.barribob.boss.mob

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig
import net.barribob.boss.Mod
import net.barribob.boss.animation.IAnimationTimer
import net.barribob.boss.animation.PauseAnimationTimer
import net.barribob.boss.config.ModConfig
import net.barribob.boss.mob.mobs.lich.*
import net.barribob.boss.mob.utils.SimpleLivingGeoRenderer
import net.barribob.boss.particle.ParticleFactories
import net.barribob.boss.projectile.MagicMissileProjectile
import net.barribob.boss.projectile.comet.CometCodeAnimations
import net.barribob.boss.projectile.comet.CometProjectile
import net.barribob.boss.render.*
import net.barribob.maelstrom.general.data.WeakHashPredicate
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.util.GlfwUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

object Entities {
    private val mobConfig = AutoConfig.getConfigHolder(ModConfig::class.java).config

    val LICH: EntityType<LichEntity> = registerConfiguredMob("lich",
        { type, world -> LichEntity(type, world, mobConfig.lichConfig) })
    { it.dimensions(EntityDimensions.fixed(1.8f, 3.0f)) }

    val MAGIC_MISSILE: EntityType<MagicMissileProjectile> = Registry.register(
        Registry.ENTITY_TYPE,
        Mod.identifier("blue_fireball"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::MagicMissileProjectile)
            .dimensions(EntityDimensions.fixed(0.25f, 0.25f)).build()
    )

    val COMET: EntityType<CometProjectile> = Registry.register(
        Registry.ENTITY_TYPE,
        Mod.identifier("comet"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::CometProjectile)
            .dimensions(EntityDimensions.fixed(0.25f, 0.25f)).build()
    )

    private fun <T : Entity> registerConfiguredMob(
        name: String,
        factory: (EntityType<T>, World) -> T,
        augment: (FabricEntityTypeBuilder<T>) -> FabricEntityTypeBuilder<T>,
    ): EntityType<T> {
        val builder = FabricEntityTypeBuilder.create(SpawnGroup.MONSTER)
        { type: EntityType<T>, world -> factory(type, world) }
        return Registry.register(Registry.ENTITY_TYPE,  Mod.identifier(name), augment(builder).build())
    }

    fun init() {
        FabricDefaultAttributeRegistry.register(LICH,
            HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 6.0)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, mobConfig.lichConfig.health)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, mobConfig.lichConfig.missile.damage))
    }

    fun clientInit(animationTimer: IAnimationTimer) {
        val pauseSecondTimer = PauseAnimationTimer({ GlfwUtil.getTime() }, { MinecraftClient.getInstance().isPaused })

        EntityRendererRegistry.INSTANCE.register(LICH) { entityRenderDispatcher, _ ->
            SimpleLivingGeoRenderer(
                entityRenderDispatcher, GeoModel(
                    Mod.identifier("geo/lich.geo.json"),
                    Mod.identifier("textures/entity/lich.png"),
                    Mod.identifier("animations/lich.animation.json"),
                    animationTimer,
                    LichCodeAnimations()
                ),
                BoundedLighting(7),
                LichBoneLight(),
                EternalNightRenderer()
            )
        }

        val missileTexture = Mod.identifier("textures/entity/blue_magic_missile.png")
        val magicMissileRenderLayer = RenderLayer.getEntityCutoutNoCull(missileTexture)
        EntityRendererRegistry.INSTANCE.register(MAGIC_MISSILE) { entityRenderDispatcher, _ ->
            SimpleEntityRenderer(
                entityRenderDispatcher,
                CompositeRenderer(listOf(
                    BillboardRenderer(entityRenderDispatcher, magicMissileRenderLayer) { 0.5f },
                    ConditionalRenderer(
                        WeakHashPredicate<MagicMissileProjectile> { FrameLimiter(20f, pauseSecondTimer)::canDoFrame },
                        LerpedPosRenderer {
                            ParticleFactories.soulFlame().build(it.add(RandomUtils.randVec().multiply(0.25)))
                        })
                )),
                { missileTexture },
                FullRenderLight()
            )
        }

        EntityRendererRegistry.INSTANCE.register(COMET) { entityRenderDispatcher, _ ->
            ModGeoRenderer(entityRenderDispatcher, GeoModel(
                Mod.identifier("geo/comet.geo.json"),
                Mod.identifier("textures/entity/comet.png"),
                Mod.identifier("animations/comet.animation.json"),
                animationTimer,
                CometCodeAnimations()
            ),
                ConditionalRenderer(
                    WeakHashPredicate { FrameLimiter(60f, pauseSecondTimer)::canDoFrame },
                    LerpedPosRenderer {
                        ParticleFactories.cometTrail().build(it.add(RandomUtils.randVec().multiply(0.5)))
                    }),
                FullRenderLight()
            )
        }
    }
}