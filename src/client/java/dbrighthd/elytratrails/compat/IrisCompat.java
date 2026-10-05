package dbrighthd.elytratrails.compat;
public class IrisCompat {
    private static boolean query(String method) {
        try {
            Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object instance = api.getMethod("getInstance").invoke(null);
            return (boolean) api.getMethod(method).invoke(instance);
        } catch (ReflectiveOperationException | LinkageError e) { return false; }
    }
    public static boolean isShadowPassing() { return query("isRenderingShadowPass"); }
    public static boolean isUsingShaders() { return query("isShaderPackInUse"); }
}
