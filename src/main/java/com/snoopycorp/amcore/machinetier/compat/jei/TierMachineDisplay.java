package com.snoopycorp.amcore.machinetier.compat.jei;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

import com.snoopycorp.amcore.AMCore;
import com.snoopycorp.amcore.machinetier.MachineTier;
import com.snoopycorp.amcore.machinetier.recipe.AMRecipeTypes;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.fml.ModList;

/**
 * Machine drawn by Create's animated JEI machines while one of amcore's categories draws a recipe. Create's animations
 * always draw Create's own block; the mixins in {@code com.snoopycorp.amcore.mixin.client} ask this class for the tier
 * machine instead.
 * <p>
 * Create Encased gives some variants their own moving parts (mixer head, spout bottom), which it picks from the block
 * state; they are looked up by reflection so that amcore does not depend on Create Encased.
 */
public final class TierMachineDisplay {
    private static AMRecipeTypes current;

    private TierMachineDisplay() {
    }

    /** Called around a category draw; {@code null} outside amcore's categories. */
    public static void begin(ResourceLocation categoryId) {
        current = null;
        if (AMCore.MODID.equals(categoryId.getNamespace())) {
            for (AMRecipeTypes type : AMRecipeTypes.values()) {
                if (type.getId().equals(categoryId)) {
                    current = type;
                    return;
                }
            }
        }
    }

    public static void end() {
        current = null;
    }

    /** Machine of the tier being drawn, or {@code null} when Create's own should be drawn. */
    private static Block machine() {
        return current == null ? null : MachineTier.displayMachine(current.process(), current.tier());
    }

    /**
     * Replaces Create's machine block state by the tier machine's. Other blocks (shafts, basin, depot) are kept. The
     * swap only happens if the tier block has every property Create sets afterwards.
     */
    public static BlockState swap(BlockState state) {
        if (current == null) {
            return state;
        }
        Block base = BuiltInRegistries.BLOCK.get(current.process().baseMachine());
        if (!state.is(base)) {
            return state;
        }
        Block machine = machine();
        if (machine == null) {
            return state;
        }
        BlockState swapped = machine.defaultBlockState();
        List<Property<?>> needed = List.copyOf(state.getProperties());
        return swapped.getProperties().containsAll(needed) ? swapped : state;
    }

    public static PartialModel mixerHead(PartialModel original) {
        Block machine = machine();
        if (machine == null || !Encased.LOADED) {
            return original;
        }
        PartialModel head = Encased.mixerHead(machine.defaultBlockState());
        return head != null ? head : original;
    }

    public static PartialModel spoutBottom(PartialModel original) {
        Block machine = machine();
        if (machine == null || !Encased.LOADED) {
            return original;
        }
        PartialModel bottom = Encased.spoutBottom(machine);
        return bottom != null ? bottom : original;
    }

    /** Create Encased lookups, resolved once; any failure falls back to Create's models. */
    private static final class Encased {
        static final boolean LOADED = ModList.get().isLoaded("createcasing");
        private static MethodHandle mixerHead;
        private static MethodHandle fluidSets;
        private static MethodHandle isInSet;
        private static MethodHandle spoutBottom;
        private static boolean failed;

        static {
            if (LOADED) {
                try {
                    MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                    Class<?> partials = Class.forName("fr.iglee42.createcasing.registries.EncasedPartialModels");
                    mixerHead = lookup.findStatic(partials, "getMixerHead", MethodType.methodType(PartialModel.class, BlockState.class));
                    Class<?> sets = Class.forName("fr.iglee42.createcasing.sets.fluids.FluidSets");
                    Class<?> set = Class.forName("fr.iglee42.createcasing.sets.fluids.FluidSet");
                    fluidSets = lookup.findStatic(sets, "getSets", MethodType.methodType(List.class));
                    isInSet = lookup.findVirtual(set, "isInSet", MethodType.methodType(boolean.class, Block.class));
                    spoutBottom = lookup.findVirtual(set, "getSpoutBottomModel", MethodType.methodType(PartialModel.class));
                } catch (ReflectiveOperationException | RuntimeException e) {
                    fail(e);
                }
            }
        }

        private static void fail(Throwable e) {
            if (!failed) {
                failed = true;
                AMCore.LOGGER.warn("Create Encased models unavailable in JEI, using Create's", e);
            }
        }

        static PartialModel mixerHead(BlockState state) {
            if (failed) {
                return null;
            }
            try {
                return (PartialModel) mixerHead.invoke(state);
            } catch (Throwable e) {
                fail(e);
                return null;
            }
        }

        static PartialModel spoutBottom(Block block) {
            if (failed) {
                return null;
            }
            try {
                for (Object set : (List<?>) fluidSets.invoke()) {
                    if ((boolean) isInSet.invoke(set, block)) {
                        return (PartialModel) spoutBottom.invoke(set);
                    }
                }
                return null;
            } catch (Throwable e) {
                fail(e);
                return null;
            }
        }
    }
}
