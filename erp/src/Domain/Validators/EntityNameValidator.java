package Domain.Validators;

import org.jetbrains.annotations.NotNull;

public class EntityNameValidator {

    public static boolean isValid(@NotNull String what) {
        // all non-empty strings considered to be valid for names
        // return if string = string - space (from left and right)
        return what.equals(what.trim());
    }

    public static void onValidation(@NotNull String what) {
        // FIXME: Function present for consistency among validator
        // FIXME: it is not required for any tracking of value[IT]
        // FIXME: maybe package these functions into one and remove
    }

    public static void onClaimation(@NotNull String what) {
        // FIXME: Function is present for consistency no use of any
        // FIXME: on Claim of value is required of this validator
        // FIXME: maybe package these functions into one and remove
    }
}
