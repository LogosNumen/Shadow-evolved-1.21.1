package net.logosnumen.nightmareevolved.soulsea;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Aspects;
import net.logosnumen.nightmareevolved.echo.EchoRecord;
import net.logosnumen.nightmareevolved.echo.Echoes;
import net.logosnumen.nightmareevolved.entity.soulsea.SeaEchoEntity;
import net.logosnumen.nightmareevolved.entity.soulsea.SeaEntities;
import net.logosnumen.nightmareevolved.entity.soulsea.SeaFigure;
import net.logosnumen.nightmareevolved.entity.soulsea.SeaHoardEntity;
import net.logosnumen.nightmareevolved.entity.soulsea.SeaMemoryEntity;
import net.logosnumen.nightmareevolved.entity.soulsea.SeaShadowEntity;
import net.logosnumen.nightmareevolved.entity.soulsea.SeaSunEntity;
import net.logosnumen.nightmareevolved.memory.MemoryDef;
import net.logosnumen.nightmareevolved.memory.MemoryRecord;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.soul.SoulRules;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

/**
 * What a soul sees in its sea, kept in step with the soul itself:
 * <ul>
 * <li>its cores, burning high over the water like suns, one for each;</li>
 * <li>its Memories, circling the heart of the sea as little lights;</li>
 * <li>its Echoes (and Shadows), standing in a ring around them;</li>
 * <li>in Sunny's sea, his own shadows at his side, and the shadows of everything he has slain, standing guard in a wide
 * ring around the rest;</li>
 * <li>for a Sovereign, the store of things the soul keeps.</li>
 * </ul>
 * A Citadel's likeness and Effie's island are built of blocks, by {@link SeaBuilder}. This class only adds, updates and
 * removes figures to match the soul.
 */
public final class SeaPresence {
    private static final double SEARCH = 64.0;

    private SeaPresence() {
    }

    /** Rebuilds the sea from scratch around its owner. */
    public static void populate(ServerPlayer player) {
        clear(player);
        refresh(player);
    }

    public static void clear(ServerPlayer player) {
        ServerLevel sea = player.server.getLevel(SoulSea.DIMENSION);
        if (sea != null) {
            figures(player, sea).forEach(Entity::discard);
        }
    }

    /** Brings the figures in line with the soul. */
    public static void refresh(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel sea) || !SoulSea.in(player)) {
            return;
        }

        int region = SeaRegions.get(player.server).regionOf(player.getUUID());
        double cx = SeaRegions.centerX(region);
        double cz = SeaRegions.centerZ(region);
        SoulData soul = Soul.of(player);
        List<SeaFigure> existing = figures(player, sea);
        suns(player, soul, sea, existing, cx, cz);
        memories(player, soul, sea, existing, cx, cz);
        echoes(player, soul, sea, existing, cx, cz);
        shadows(player, soul, sea, existing, cx, cz);
        hoard(player, soul, sea, existing, cx, cz);
    }

    private static List<SeaFigure> figures(ServerPlayer player, ServerLevel sea) {
        int region = SeaRegions.get(player.server).regionOf(player.getUUID());
        double cx = SeaRegions.centerX(region);
        double cz = SeaRegions.centerZ(region);
        AABB area = new AABB(cx - SEARCH, 0, cz - SEARCH, cx + SEARCH, 128, cz + SEARCH);
        return sea.getEntitiesOfClass(SeaFigure.class, area, figure -> figure.ownedBy(player));
    }

    private static <T extends SeaFigure> T spawn(ServerLevel sea, T figure, ServerPlayer owner, double x, double y, double z, float yaw) {
        figure.moveTo(x, y, z, yaw, 0.0F);
        figure.setOwner(owner.getUUID());
        sea.addFreshEntity(figure);
        return figure;
    }

    /** How a soul's suns look: Sunny's black Shadow Core, Nephis's blazing white stars, Mordret's pale marked sun. */
    private static String look(SoulData soul) {
        return switch (soul.aspect()) {
            case "shadow_slave" -> "shadow";
            case "light_bringer" -> "light";
            case "prince_of_nothing" -> "nothing";
            default -> "";
        };
    }

    private static void suns(ServerPlayer player, SoulData soul, ServerLevel sea, List<SeaFigure> existing, double cx, double cz) {
        int count = soul.cores();
        Aspect aspect = Aspects.byId(soul.aspect());
        int colour = aspect == null ? 0xFFE6B0 : aspect.palette().main;
        String look = look(soul);
        SeaSunEntity[] shown = new SeaSunEntity[count];
        for (SeaFigure figure : existing) {
            if (figure instanceof SeaSunEntity sun) {
                int index = sun.index();
                if (index < 0 || index >= count || shown[index] != null || sun.count() != count) {
                    sun.discard();
                } else {
                    shown[index] = sun;
                }
            }
        }

        for (int i = 0; i < count; i++) {
            float fill = (float)Math.max(0.0, Math.min(1.0, (soul.fragments() - i * (double)SoulRules.FRAGMENTS_PER_CORE) / SoulRules.FRAGMENTS_PER_CORE));
            SeaSunEntity sun = shown[i];
            if (sun == null) {
                SeaSunEntity made = SeaEntities.SUN.get().create(sea);
                if (made == null) {
                    continue;
                }

                double spread = (i - (count - 1) / 2.0) * 6.0;
                double z = cz - 6.0 - Math.abs(spread) * 0.4;
                sun = spawn(sea, made, player, cx + spread, SoulSea.SURFACE_Y + 18.0 + (i % 2) * 2.5, z, 0.0F);
            }

            if (Math.abs(sun.fill() - fill) > 0.001F || !look.equals(sun.look()) || sun.colour() != colour) {
                sun.set(i, count, fill, look, colour);
            }
        }
    }

    private static void memories(ServerPlayer player, SoulData soul, ServerLevel sea, List<SeaFigure> existing, double cx, double cz) {
        Map<UUID, MemoryRecord> records = new LinkedHashMap<>();
        soul.memories().forEach(record -> records.put(record.id, record));
        Map<UUID, SeaMemoryEntity> shown = new HashMap<>();
        for (SeaFigure figure : existing) {
            if (figure instanceof SeaMemoryEntity memory) {
                UUID id = memory.memory();
                if (id == null || !records.containsKey(id) || shown.containsKey(id)) {
                    memory.discard();
                } else {
                    shown.put(id, memory);
                }
            }
        }

        int count = records.size();
        int index = 0;
        for (MemoryRecord record : records.values()) {
            MemoryDef def = record.def();
            boolean out = record.anyOut();
            int piece = def != null && def.armor() && record.pieces.size() > 1 ? 1 : 0;
            SeaMemoryEntity figure = shown.get(record.id);
            if (figure == null) {
                SeaMemoryEntity made = SeaEntities.MEMORY.get().create(sea);
                if (made == null) {
                    continue;
                }

                // Three rings of lights, each Memory spaced evenly around its ring.
                int ring = index % 3;
                double radius = 6.0 + ring * 1.8;
                double phase = Math.PI * 2.0 * (index / 3) / Math.max(1, (count + 2 - ring) / 3) + ring * 0.7;
                figure = spawn(sea, made, player, cx + Math.cos(phase) * radius, SoulSea.SURFACE_Y + 1.6, cz + Math.sin(phase) * radius, 0.0F);
                figure.orbit(cx, cz, radius, phase);
                figure.set(record.id, record.pieces.get(piece).stack, out);
            } else if (figure.out() != out) {
                figure.set(record.id, record.pieces.get(piece).stack, out);
            }

            index++;
        }
    }

    private static void echoes(ServerPlayer player, SoulData soul, ServerLevel sea, List<SeaFigure> existing, double cx, double cz) {
        Map<UUID, EchoRecord> records = new LinkedHashMap<>();
        soul.echoes().forEach(record -> records.put(record.id, record));
        Map<UUID, SeaEchoEntity> shown = new HashMap<>();
        ListTag standing = "shadow_slave".equals(soul.aspect()) ? Echoes.standing(soul) : new ListTag();
        Map<Integer, SeaEchoEntity> slain = new HashMap<>();
        for (SeaFigure figure : existing) {
            if (figure instanceof SeaEchoEntity echo) {
                if (echo.style() == SeaEchoEntity.SLAIN_STYLE) {
                    // Each shadow of the slain keeps its place in the guard by the number it was given.
                    int place = echo.echo() == null ? -1 : (int)echo.echo().getLeastSignificantBits();
                    if (place < 0 || place >= standing.size() || slain.containsKey(place) || !echo.kindId().equals(standing.getString(place))) {
                        echo.discard();
                    } else {
                        slain.put(place, echo);
                    }

                    continue;
                }

                UUID id = echo.echo();
                EchoRecord record = id == null ? null : records.get(id);
                int style = record != null && record.shadow ? SeaEchoEntity.SHADOW_STYLE : SeaEchoEntity.ECHO_STYLE;
                if (record == null || shown.containsKey(id) || echo.style() != style) {
                    echo.discard();
                } else {
                    shown.put(id, echo);
                }
            }
        }

        int count = records.size();
        int index = 0;
        for (EchoRecord record : records.values()) {
            int style = record.shadow ? SeaEchoEntity.SHADOW_STYLE : SeaEchoEntity.ECHO_STYLE;
            SeaEchoEntity figure = shown.get(record.id);
            if (figure == null) {
                SeaEchoEntity made = SeaEntities.ECHO.get().create(sea);
                if (made == null) {
                    continue;
                }

                // A wide ring, each turned toward the heart of the sea.
                double radius = Math.min(16.0, 12.0 + count * 0.12);
                double angle = Math.PI * 2.0 * index / Math.max(1, count) + Math.PI / Math.max(1, count);
                double x = cx + Math.cos(angle) * radius;
                double z = cz + Math.sin(angle) * radius;
                float yaw = (float)(Math.toDegrees(Math.atan2(cz - z, cx - x)) - 90.0);
                figure = spawn(sea, made, player, x, SoulSea.SURFACE_Y, z, yaw);
                figure.setYHeadRot(yaw);
                figure.set(record.type, record.id, record.out, style);
            } else if (figure.out() != record.out) {
                figure.set(record.type, record.id, record.out, style);
            }

            index++;
        }

        // The shadows of the slain, in Sunny's sea: a guard of black figures in a wide ring around everything else.
        for (int i = 0; i < standing.size(); i++) {
            if (slain.containsKey(i)) {
                continue;
            }

            SeaEchoEntity made = SeaEntities.ECHO.get().create(sea);
            if (made == null) {
                continue;
            }

            int ring = i / 24;
            double radius = 22.0 + ring * 3.0;
            double angle = Math.PI * 2.0 * (i % 24) / 24.0 + ring * 0.13;
            double x = cx + Math.cos(angle) * radius;
            double z = cz + Math.sin(angle) * radius;
            float yaw = (float)(Math.toDegrees(Math.atan2(z - cz, x - cx)) - 90.0);
            spawn(sea, made, player, x, SoulSea.SURFACE_Y, z, yaw).set(standing.getString(i), new UUID(0L, i), false, SeaEchoEntity.SLAIN_STYLE);
        }
    }

    /** How many shadows Sunny has of his own by his Rank: one at first, and another at each Ascension from Master. */
    public static int ownShadows(SoulData soul) {
        if (!"shadow_slave".equals(soul.aspect()) || soul.rank() == Rank.ASPIRANT) {
            return 0;
        }

        return 1 + (soul.rank().atLeast(Rank.MASTER) ? 1 : 0) + (soul.rank().atLeast(Rank.SAINT) ? 1 : 0) + (soul.rank().atLeast(Rank.SOVEREIGN) ? 1 : 0);
    }

    private static void shadows(ServerPlayer player, SoulData soul, ServerLevel sea, List<SeaFigure> existing, double cx, double cz) {
        int wanted = ownShadows(soul);
        int have = 0;
        for (SeaFigure figure : existing) {
            if (figure instanceof SeaShadowEntity) {
                if (have < wanted) {
                    have++;
                } else {
                    figure.discard();
                }
            }
        }

        for (int i = have; i < wanted; i++) {
            SeaShadowEntity made = SeaEntities.SHADOW.get().create(sea);
            if (made != null) {
                double x = cx + (i % 2 == 0 ? -1 : 1) * (2.0 + (i / 2) * 1.6);
                spawn(sea, made, player, x, SoulSea.SURFACE_Y, cz + SoulSea.ARRIVAL - 1.5, 180.0F);
            }
        }
    }

    private static void hoard(ServerPlayer player, SoulData soul, ServerLevel sea, List<SeaFigure> existing, double cx, double cz) {
        boolean wanted = soul.rank().atLeast(Rank.SOVEREIGN);
        boolean have = false;
        for (SeaFigure figure : existing) {
            if (figure instanceof SeaHoardEntity) {
                if (wanted && !have) {
                    have = true;
                } else {
                    figure.discard();
                }
            }
        }

        if (wanted && !have) {
            SeaHoardEntity made = SeaEntities.HOARD.get().create(sea);
            if (made != null) {
                spawn(sea, made, player, cx + 5.0, SoulSea.SURFACE_Y + 0.5, cz + SoulSea.ARRIVAL - 4.0, 200.0F);
            }
        }
    }
}
