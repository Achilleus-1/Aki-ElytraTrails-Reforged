package dbrighthd.elytratrails.util;

//import dbrighthd.elytratrails.compat.IrisCompat;
import dbrighthd.elytratrails.compat.IrisCompat;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

public final class ShaderChecksUtil {
    private ShaderChecksUtil() {
    }

    private static final boolean IRIS_LOADED = ModList.get().isLoaded("iris");

    public static boolean isShadowPass() {
        if (!IRIS_LOADED) return false;
        return IrisCompat.isShadowPassing();
    }

    @SuppressWarnings("unused")
    public static boolean isUsingShaders() {
        if (!IRIS_LOADED) return false;
        return IrisCompat.isUsingShaders();
    }
}
