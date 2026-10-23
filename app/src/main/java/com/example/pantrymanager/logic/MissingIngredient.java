package com.example.pantrymanager.logic;

/**
 * Describes why one recipe ingredient could not be satisfied by the pantry.
 */
public class MissingIngredient {

    public enum Reason {
        /** Nothing with this name is in the pantry. */
        NOT_IN_PANTRY,
        /** The pantry has it, but not enough of it. */
        NOT_ENOUGH,
        /** The pantry has it, but in a unit that cannot be converted (e.g. pcs vs ml). */
        UNIT_MISMATCH,
        /** The pantry has it, but every matching item is past its expiry date. */
        EXPIRED
    }

    private final String name;
    private final double required;
    private final double available;
    private final String unit;
    private final Reason reason;

    public MissingIngredient(String name, double required, double available, String unit, Reason reason) {
        this.name = name;
        this.required = required;
        this.available = available;
        this.unit = unit;
        this.reason = reason;
    }

    public String getName() { return name; }
    public double getRequired() { return required; }
    /** Amount found in the pantry, expressed in the recipe's unit (0 when absent). */
    public double getAvailable() { return available; }
    public String getUnit() { return unit; }
    public Reason getReason() { return reason; }

    /** Short human-readable explanation, e.g. "need 3 pcs, have 2". */
    public String describe() {
        String u = unit == null ? "" : " " + unit;
        switch (reason) {
            case NOT_ENOUGH:
                return "need " + UnitConverter.trim(required) + u
                        + ", have " + UnitConverter.trim(available) + u;
            case UNIT_MISMATCH:
                return "in pantry, but in a unit that can't be compared";
            case EXPIRED:
                return "in pantry, but expired";
            case NOT_IN_PANTRY:
            default:
                return "not in pantry";
        }
    }
}
