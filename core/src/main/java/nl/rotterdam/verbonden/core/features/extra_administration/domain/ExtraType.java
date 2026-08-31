package nl.rotterdam.verbonden.core.features.extra_administration.domain;

public enum ExtraType {
    TROUWBOEKJE("Trouwboekje", "trouwboekje"),
    INTERNATIONALE_AKTE("Internationale akte", "internationaleAkte");

    private final String label;
    private final String dbValue;

    ExtraType(String label, String dbValue) {
        this.label = label;
        this.dbValue = dbValue;
    }

    public String getLabel() {
        return label;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static ExtraType fromDbValue(String dbValue) {
        for (ExtraType type : values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Onbekende ExtraType db-waarde: " + dbValue);
    }
}
