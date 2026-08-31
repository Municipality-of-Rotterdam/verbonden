package nl.rotterdam.verbonden.core.identity;

/**
 * Rolconstanten die {@link AuthenticatedUser#getRoles()} kan teruggeven.
 */
public final class Roles {

    public static final String BURGER = "BURGER";
    public static final String SUPERUSER = "SUPERUSER";
    public static final String BABS = "BABS";

    private Roles() {
    }
}
