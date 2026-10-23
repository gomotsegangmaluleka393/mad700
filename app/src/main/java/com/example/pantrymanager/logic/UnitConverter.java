package com.example.pantrymanager.logic;

import java.util.Locale;

/**
 * Converts quantities between units, including across dimensions where the ingredient's
 * density or piece weight is known.
 */
public final class UnitConverter {

    private UnitConverter() {
    }

    /**
     * Converts quantity of canonicalName from one unit to another.
     *
     * @return the converted amount, or null when the conversion is not possible
     */
    public static Double convert(double quantity, Unit from, Unit to, String canonicalName) {
        if (from == null || to == null) {
            return null;
        }
        if (from.getDimension() == to.getDimension()) {
            return quantity * from.getToBase() / to.getToBase();
        }
        Double grams = toGrams(quantity, from, canonicalName);
        if (grams == null) {
            return null;
        }
        return fromGrams(grams, to, canonicalName);
    }

    private static Double toGrams(double quantity, Unit from, String name) {
        double base = quantity * from.getToBase();
        switch (from.getDimension()) {
            case MASS:
                return base;
            case VOLUME: {
                Double density = IngredientKnowledge.densityOf(name);
                return density == null ? null : base * density;
            }
            case COUNT: {
                Double weight = IngredientKnowledge.pieceWeightOf(name);
                return weight == null ? null : base * weight;
            }
            default:
                return null;
        }
    }

    private static Double fromGrams(double grams, Unit to, String name) {
        switch (to.getDimension()) {
            case MASS:
                return grams / to.getToBase();
            case VOLUME: {
                Double density = IngredientKnowledge.densityOf(name);
                return density == null ? null : grams / density / to.getToBase();
            }
            case COUNT: {
                Double weight = IngredientKnowledge.pieceWeightOf(name);
                return weight == null ? null : grams / weight / to.getToBase();
            }
            default:
                return null;
        }
    }

    /**
     * Formats a quantity for display, optionally converting metric mass/volume to imperial.
     * Counts and spoon measures are left as they are.
     *
     * @param imperial true when the user chose imperial units in Settings
     */
    public static String format(double quantity, String unitText, boolean imperial) {
        Unit unit = Unit.parse(unitText);
        if (unit == null) {
            return trim(quantity) + " " + unitText;
        }
        if (imperial) {
            if (unit == Unit.G || unit == Unit.KG) {
                double oz = quantity * unit.getToBase() / Unit.OZ.getToBase();
                return oz >= 16 ? trim(oz / 16.0) + " lb" : trim(oz) + " oz";
            }
            if (unit == Unit.ML || unit == Unit.L) {
                double ml = quantity * unit.getToBase();
                double cups = ml / 236.6;                      // US cup for imperial display
                if (cups >= 0.25) {
                    return trim(cups) + (cups == 1 ? " cup" : " cups");
                }
                double tbsp = ml / Unit.TBSP.getToBase();
                return tbsp >= 1 ? trim(tbsp) + " tbsp" : trim(ml / Unit.TSP.getToBase()) + " tsp";
            }
        } else {
            if (unit == Unit.OZ || unit == Unit.LB) {
                double g = quantity * unit.getToBase();
                return g >= 1000 ? trim(g / 1000.0) + " kg" : trim(g) + " g";
            }
        }
        if (unit == Unit.CUP && quantity != 1) {
            return trim(quantity) + " cups";
        }
        return trim(quantity) + " " + unit.getSymbol();
    }

    /** 2.0 -> "2", 0.5 -> "0.5", 1.333 -> "1.33". */
    public static String trim(double value) {
        double rounded = Math.round(value * 100.0) / 100.0;
        if (rounded == Math.rint(rounded)) {
            return String.valueOf((long) rounded);
        }
        return String.format(Locale.US, "%.2f", rounded).replaceAll("0+$", "");
    }
}
