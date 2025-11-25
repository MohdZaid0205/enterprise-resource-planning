package Domain.Validators;


import org.jetbrains.annotations.NotNull;

public class EntityIdentityValidator {

    public static boolean isValid(@NotNull String what) {
        // TODO: Query databases to check if identity id valid.
        return what.equals(what.trim());
    }

    public static void onValidation(@NotNull String what) {
        // TODO: Query ADD into list of all identity. DATABASE
    }

    public static void onClaimation(@NotNull String what) {
        // TODO: Query REM from list of all identity. DATABASE
    }
}
