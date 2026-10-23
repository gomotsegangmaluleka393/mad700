package com.example.pantrymanager.logic;

import com.example.pantrymanager.model.Recipe;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The outcome of checking one recipe against the pantry.
 */
public class MatchResult {

    private final Recipe recipe;
    private final List<MissingIngredient> missing;
    /** Pantry item id -> amount (in that item's own unit) the recipe would use. */
    private final Map<Long, Double> consumption;

    public MatchResult(Recipe recipe, List<MissingIngredient> missing, Map<Long, Double> consumption) {
        this.recipe = recipe;
        this.missing = Collections.unmodifiableList(missing);
        this.consumption = Collections.unmodifiableMap(consumption);
    }

    public Recipe getRecipe() { return recipe; }
    public List<MissingIngredient> getMissing() { return missing; }
    public Map<Long, Double> getConsumption() { return consumption; }

    /** Strict rule: every ingredient present in at least the required quantity. */
    public boolean isStrictMatch() {
        return missing.isEmpty();
    }

    /** Stretch goal: exactly one ingredient is missing or short. */
    public boolean isAlmostThere() {
        return missing.size() == 1;
    }
}
