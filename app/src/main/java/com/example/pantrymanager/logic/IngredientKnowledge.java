package com.example.pantrymanager.logic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Small lookup tables that let the matcher convert between dimensions for common
 * ingredients (e.g. a recipe asks for "1 cup rice" but the pantry has "2 kg rice",
 * or a recipe asks for "1 onion" but the pantry has "500 g onions").
 * Conversions are only allowed when the ingredient is in these tables. For anything
 * else, mass/volume/count are treated as incompatible and the ingredient is NOT
 * considered available - the strict rule errs on the side of not suggesting a recipe.
 */
public final class IngredientKnowledge {

    private IngredientKnowledge() {
    }

    /** Approximate density in grams per millilitre */
    private static final Map<String, Double> DENSITY = new HashMap<>();

    /** Approximate weight in grams of one piece */
    private static final Map<String, Double> PIECE_WEIGHT = new HashMap<>();

    /**
     * Generic names that may be satisfied by any more specific variety.
     * e.g. a recipe needing "oil" can use "olive oil" or "sunflower oil",
     * and a recipe needing "cheese" can use "cheddar cheese".
     */
    private static final Set<String> GENERIC_HEADS = new HashSet<>(Arrays.asList(
            "oil", "cheese", "rice", "flour", "vinegar", "pasta", "sugar", "bread", "stock"));

    static {
        DENSITY.put("water", 1.0);
        DENSITY.put("milk", 1.03);
        DENSITY.put("salt", 1.2);
        DENSITY.put("sugar", 0.85);
        DENSITY.put("brown sugar", 0.83);
        DENSITY.put("flour", 0.53);
        DENSITY.put("butter", 0.96);
        DENSITY.put("oil", 0.92);
        DENSITY.put("olive oil", 0.92);
        DENSITY.put("sunflower oil", 0.92);
        DENSITY.put("rice", 0.85);
        DENSITY.put("honey", 1.42);
        DENSITY.put("oats", 0.41);
        DENSITY.put("maize meal", 0.6);
        DENSITY.put("yoghurt", 1.03);
        DENSITY.put("mayonnaise", 0.95);
        DENSITY.put("soy sauce", 1.15);
        DENSITY.put("cheese", 0.45);        // grated
        DENSITY.put("peas", 0.6);
        DENSITY.put("baking powder", 0.9);
        DENSITY.put("cinnamon", 0.56);
        DENSITY.put("paprika", 0.46);
        DENSITY.put("curry powder", 0.45);
        DENSITY.put("cocoa powder", 0.42);
        DENSITY.put("tomato sauce", 1.1);
        DENSITY.put("lemon juice", 1.03);
        DENSITY.put("pasta", 0.4);

        PIECE_WEIGHT.put("egg", 50.0);
        PIECE_WEIGHT.put("onion", 150.0);
        PIECE_WEIGHT.put("red onion", 150.0);
        PIECE_WEIGHT.put("tomato", 120.0);
        PIECE_WEIGHT.put("potato", 170.0);
        PIECE_WEIGHT.put("carrot", 60.0);
        PIECE_WEIGHT.put("garlic", 5.0);
        PIECE_WEIGHT.put("banana", 120.0);
        PIECE_WEIGHT.put("apple", 180.0);
        PIECE_WEIGHT.put("orange", 150.0);
        PIECE_WEIGHT.put("lemon", 100.0);
        PIECE_WEIGHT.put("bell pepper", 150.0);
        PIECE_WEIGHT.put("cucumber", 300.0);
        PIECE_WEIGHT.put("avocado", 170.0);
        PIECE_WEIGHT.put("spring onion", 15.0);
        PIECE_WEIGHT.put("bread", 35.0);
        PIECE_WEIGHT.put("chicken breast", 170.0);
        PIECE_WEIGHT.put("sweet potato", 200.0);
        PIECE_WEIGHT.put("zucchini", 200.0);
        PIECE_WEIGHT.put("baked beans", 410.0);
        PIECE_WEIGHT.put("tuna", 170.0);
    }

    /** Density in g/ml for a canonical name, or null if unknown. */
    public static Double densityOf(String canonicalName) {
        Double d = DENSITY.get(canonicalName);
        if (d == null) {
            String head = headOf(canonicalName);
            if (head != null && GENERIC_HEADS.contains(head)) {
                d = DENSITY.get(head);
            }
        }
        return d;
    }

    /** Weight in grams of one piece for a canonical name, or null if unknown. */
    public static Double pieceWeightOf(String canonicalName) {
        return PIECE_WEIGHT.get(canonicalName);
    }

    /**
     * True if a pantry item called {@code pantryName} can be used where a recipe asks for
     * {@code recipeName}. Both names must already be normalised.
     */
    public static boolean satisfies(String pantryName, String recipeName) {
        if (pantryName.isEmpty() || recipeName.isEmpty()) {
            return false;
        }
        if (pantryName.equals(recipeName)) {
            return true;
        }
        // "olive oil" satisfies "oil", "cheddar cheese" satisfies "cheese"
        return GENERIC_HEADS.contains(recipeName) && pantryName.endsWith(" " + recipeName);
    }

    /** True if the recipe ingredient name is a generic category rather than a specific item. */
    public static boolean isGeneric(String canonicalName) {
        return GENERIC_HEADS.contains(canonicalName);
    }

    private static String headOf(String name) {
        int idx = name.lastIndexOf(' ');
        return idx < 0 ? null : name.substring(idx + 1);
    }
}

