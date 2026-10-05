package dbrighthd.elytratrails.compat;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

public class ModStatuses {
    public static final boolean EMF_LOADED = ModList.get().isLoaded("entity_model_features");
    public static final boolean IRIS_LOADED = ModList.get().isLoaded("iris");
    public static final boolean FLASHBACK_LOADED = ModList.get().isLoaded("flashback");
    public static final boolean CLOTH_LOADED = ModList.get().isLoaded("cloth_config");
    public static final boolean CPM_LOADED = ModList.get().isLoaded("cpm");
}
