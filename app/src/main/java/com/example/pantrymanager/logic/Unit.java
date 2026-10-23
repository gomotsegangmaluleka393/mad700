package com.example.pantrymanager.logic;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Supported measurement units.
 */
public enum Unit {

    G("g", Dimension.MASS, 1.0),
    KG("kg", Dimension.MASS, 1000.0),
    OZ("oz", Dimension.MASS, 28.3495),
    LB("lb", Dimension.MASS, 453.592),
    ML("ml", Dimension.VOLUME, 1.0),
    L("l", Dimension.VOLUME, 1000.0),
    TSP("tsp", Dimension.VOLUME, 5.0),
    TBSP("tbsp", Dimension.VOLUME, 15.0),
    CUP("cup", Dimension.VOLUME, 250.0),
    PCS("pcs", Dimension.COUNT, 1.0);

    public enum Dimension { MASS, VOLUME, COUNT }

    private final String symbol;
    private final Dimension dimension;
    private final double toBase;

    Unit(String symbol, Dimension dimension, double toBase) {
        this.symbol = symbol;
        this.dimension = dimension;
        this.toBase = toBase;
    }

    public String getSymbol() { return symbol; }
    public Dimension getDimension() { return dimension; }
    /** Multiplier to grams (mass), millilitres (volume) or pieces (count). */
    public double getToBase() { return toBase; }

    private static final Map<String, Unit> ALIASES = new HashMap<>();

    static {
        put(G, "g", "gram", "grams", "gm", "gms", "gr");
        put(KG, "kg", "kgs", "kilo", "kilos", "kilogram", "kilograms");
        put(OZ, "oz", "ounce", "ounces");
        put(LB, "lb", "lbs", "pound", "pounds");
        put(ML, "ml", "millilitre", "millilitres", "milliliter", "milliliters", "mls");
        put(L, "l", "litre", "litres", "liter", "liters", "lt", "ltr");
        put(TSP, "tsp", "teaspoon", "teaspoons", "t");
        put(TBSP, "tbsp", "tablespoon", "tablespoons", "tbs", "tbl", "tbls");
        put(CUP, "cup", "cups", "c");
        put(PCS, "pcs", "pc", "piece", "pieces", "each", "ea", "whole", "item", "items",
                "clove", "cloves", "slice", "slices", "can", "cans", "tin", "tins", "unit", "units", "x", "");
    }

    private static void put(Unit unit, String... names) {
        for (String n : names) {
            ALIASES.put(n, unit);
        }
    }

    /**
     * Parses free text unit names ("Tablespoons", "kg.", "cloves") into a Unit.
     * Returns null when the unit is not recognised.
     */
    public static Unit parse(String raw) {
        if (raw == null) {
            return PCS;
        }
        String key = raw.trim().toLowerCase(Locale.ROOT).replace(".", "");
        return ALIASES.get(key);
    }

    /** Symbols shown in the unit drop-down on the Add/Edit screen. */
    public static String[] symbols() {
        Unit[] values = values();
        String[] out = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            out[i] = values[i].symbol;
        }
        return out;
    }
}
