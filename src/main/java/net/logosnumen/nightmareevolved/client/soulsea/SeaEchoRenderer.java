package net.logosnumen.nightmareevolved.client.soulsea;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.logosnumen.nightmareevolved.entity.soulsea.SeaEchoEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;

/**
 * Draws a still figure on the water as the creature it stands for: a resting Echo, frozen and dim; a Shadow, darker;
 * and the shadows of the slain in Sunny's sea, black as the dark itself. An Echo that has been called out leaves its
 * place empty. A creature whose renderer can't cope with a stand-in is skipped rather than crashing.
 */
public class SeaEchoRenderer extends EntityRenderer<SeaEchoEntity> {
    private final Map<EntityType<?>, Entity> standIns = new HashMap<>();
    private final Set<EntityType<?>> unrenderable = new HashSet<>();
    private Level standInLevel;

    public SeaEchoRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(SeaEchoEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        EntityType<?> type = entity.kind();
        if (!entity.out() && type != null && !this.unrenderable.contains(type)) {
            Entity standIn = this.standIn(type, entity.level());
            if (standIn != null) {
                float facing = entity.getYRot();
                standIn.setYRot(facing);
                standIn.yRotO = facing;
                if (standIn instanceof LivingEntity living) {
                    living.yBodyRot = facing;
                    living.yBodyRotO = facing;
                    living.yHeadRot = facing;
                    living.yHeadRotO = facing;
                }

                MultiBufferSource drawn = switch (entity.style()) {
                    case SeaEchoEntity.SHADOW_STYLE -> new Silhouette(buffers, 0x2A2040);
                    case SeaEchoEntity.SLAIN_STYLE -> new Silhouette(buffers, 0x08080C);
                    default -> buffers;
                };
                int shade = entity.style() == SeaEchoEntity.ECHO_STYLE ? LightTexture.pack(6, 3) : LightTexture.FULL_BRIGHT;
                try {
                    EntityRenderer<? super Entity> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(standIn);
                    renderer.render(standIn, facing, 0.0F, pose, drawn, shade);
                } catch (RuntimeException e) {
                    this.unrenderable.add(type);
                }
            }
        }

        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    private Entity standIn(EntityType<?> type, Level level) {
        if (level != this.standInLevel) {
            this.standIns.clear();
            this.standInLevel = level;
        }

        return this.standIns.computeIfAbsent(type, t -> {
            Entity created = t.create(level);
            if (created == null) {
                this.unrenderable.add(t);
            }

            return created;
        });
    }

    @Override
    protected boolean shouldShowName(SeaEchoEntity entity) {
        return false;
    }

    @Override
    public ResourceLocation getTextureLocation(SeaEchoEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
