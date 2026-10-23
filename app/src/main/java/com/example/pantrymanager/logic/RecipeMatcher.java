package com.example.pantrymanager.logic;

import com.example.pantrymanager.model.PantryItem;
import com.example.pantrymanager.model.Recipe;
import com.example.pantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The core business logic of the app: the STRICT matching rule.
 * A recipe is suggested if every ingredient it requires is present in the pantry
 * in at least the required quantity. Partial matches are never returned by
 * {@link #suggest}; they are only available through {@link #almostThere}, which the UI
 * shows in a separate, clearly labelled section.
 * Robustness to real-world messiness:
 *  - names are normalised (case, plurals, synonyms, descriptors) by {@link IngredientNormalizer}
 *  - units are converted (g/kg/oz/lb, ml/l/tsp/tbsp/cup, and mass/volume/count where the
 *    ingredient's density or piece weight is known) by {@link UnitConverter}
 *  - several pantry rows of the same ingredient are added together
 *  - a generic recipe ingredient ("oil") can be satisfied by a specific pantry item ("olive oil")
 *  - one pantry item is never counted twice for two ingredients of the same recipe
 *  - expired items can optionally be ignored
 */
public class RecipeMatcher {

    private static final double ABS_EPSILON = 1e-9;
    private static final double REL_EPSILON = 1e-6;

    private final Set<String> alwaysAvailable;
    private final boolean ignoreExpired;
    private final String todayIso;

    /**
     * @param alwaysAvailable ingredient names assumed to be on hand, may be empty
     * @param ignoreExpired   if true, pantry items whose expiry date is before today are skipped
     * @param todayIso        today's date as yyyy-MM-dd (used only when ignoreExpired is true)
     */
    public RecipeMatcher(Set<String> alwaysAvailable, boolean ignoreExpired, String todayIso) {
        this.alwaysAvailable = new HashSet<>();
        if (alwaysAvailable != null) {
            for (String s : alwaysAvailable) {
                this.alwaysAvailable.add(IngredientNormalizer.normalize(s));
            }
        }
        this.ignoreExpired = ignoreExpired;
        this.todayIso = todayIso;
    }

    /** Recipes the user can make currently (strict rule), sorted by name. */
    public List<MatchResult> suggest(List<PantryItem> pantry, List<Recipe> recipes) {
        List<MatchResult> out = new ArrayList<>();
        for (MatchResult r : evaluateAll(pantry, recipes)) {
            if (r.isStrictMatch()) {
                out.add(r);
            }
        }
        return out;
    }

    /** Recipes missing exactly one ingredient (bonus feature), sorted by name. */
    public List<MatchResult> almostThere(List<PantryItem> pantry, List<Recipe> recipes) {
        List<MatchResult> out = new ArrayList<>();
        for (MatchResult r : evaluateAll(pantry, recipes)) {
            if (r.isAlmostThere()) {
                out.add(r);
            }
        }
        return out;
    }

    /** Evaluates every recipe and returns the results sorted by recipe name. */
    public List<MatchResult> evaluateAll(List<PantryItem> pantry, List<Recipe> recipes) {
        List<MatchResult> results = new ArrayList<>();
        for (Recipe recipe : recipes) {
            results.add(evaluate(pantry, recipe));
        }
        Collections.sort(results, new Comparator<MatchResult>() {
            @Override
            public int compare(MatchResult a, MatchResult b) {
                return a.getRecipe().getName().compareToIgnoreCase(b.getRecipe().getName());
            }
        });
        return results;
    }

    /** Checks a single recipe against the pantry. */
    public MatchResult evaluate(List<PantryItem> pantry, Recipe recipe) {
        // Prepare usable pantry items and how much of each is still "unclaimed".
        List<PantryItem> usable = new ArrayList<>();
        List<PantryItem> expired = new ArrayList<>();
        Map<Long, String> canonical = new HashMap<>();
        Map<Long, Double> remaining = new HashMap<>();
        for (PantryItem item : pantry) {
            if (item.getQuantity() <= 0) {
                continue;
            }
            canonical.put(item.getId(), IngredientNormalizer.normalize(item.getName()));
            if (isExpired(item)) {
                expired.add(item);
            } else {
                usable.add(item);
                remaining.put(item.getId(), item.getQuantity());
            }
        }

        // Handle specific ingredients before generic ones so "olive oil" is claimed
        // by an "olive oil" line before a generic "oil" line can use it up.
        List<RecipeIngredient> ordered = new ArrayList<>(recipe.getIngredients());
        Collections.sort(ordered, new Comparator<RecipeIngredient>() {
            @Override
            public int compare(RecipeIngredient a, RecipeIngredient b) {
                boolean ga = IngredientKnowledge.isGeneric(IngredientNormalizer.normalize(a.getName()));
                boolean gb = IngredientKnowledge.isGeneric(IngredientNormalizer.normalize(b.getName()));
                return Boolean.compare(ga, gb);
            }
        });

        List<MissingIngredient> missing = new ArrayList<>();
        Map<Long, Double> consumption = new HashMap<>();

        for (RecipeIngredient req : ordered) {
            String reqName = IngredientNormalizer.normalize(req.getName());
            if (alwaysAvailable.contains(reqName)) {
                continue;
            }
            Unit reqUnit = Unit.parse(req.getUnit());
            double required = req.getQuantity();
            double need = required;
            boolean anyCandidate = false;
            boolean anyConvertible = false;

            for (PantryItem item : usable) {
                String itemName = canonical.get(item.getId());
                if (!IngredientKnowledge.satisfies(itemName, reqName)) {
                    continue;
                }
                anyCandidate = true;
                Unit itemUnit = Unit.parse(item.getUnit());
                if (!canConvert(itemUnit, reqUnit, reqName, itemName)) {
                    continue;
                }
                anyConvertible = true;

                double left = remaining.get(item.getId());
                if (left <= ABS_EPSILON || need <= ABS_EPSILON) {
                    continue;
                }
                Double leftInReqUnit = convertEither(left, itemUnit, reqUnit, reqName, itemName);
                if (leftInReqUnit == null || leftInReqUnit <= ABS_EPSILON) {
                    continue;
                }
                double take = Math.min(leftInReqUnit, need);
                need -= take;
                // Conversion is linear, so scale back to the item's own unit proportionally.
                double usedInItemUnit = take * left / leftInReqUnit;
                remaining.put(item.getId(), left - usedInItemUnit);
                Double prev = consumption.get(item.getId());
                consumption.put(item.getId(), (prev == null ? 0 : prev) + usedInItemUnit);
            }

            if (need > ABS_EPSILON + REL_EPSILON * required) {
                MissingIngredient.Reason reason;
                if (anyConvertible) {
                    reason = MissingIngredient.Reason.NOT_ENOUGH;
                } else if (anyCandidate) {
                    reason = MissingIngredient.Reason.UNIT_MISMATCH;
                } else if (hasMatchingName(expired, canonical, reqName)) {
                    reason = MissingIngredient.Reason.EXPIRED;
                } else {
                    reason = MissingIngredient.Reason.NOT_IN_PANTRY;
                }
                double available = anyConvertible ? Math.max(0, required - need) : 0;
                missing.add(new MissingIngredient(req.getName(), required, available, req.getUnit(), reason));
            }
        }
        return new MatchResult(recipe, missing, consumption);
    }

    private boolean isExpired(PantryItem item) {
        if (!ignoreExpired || todayIso == null || !item.hasExpiryDate()) {
            return false;
        }
        // ISO dates (yyyy-MM-dd) sort correctly as plain strings.
        return item.getExpiryDate().compareTo(todayIso) < 0;
    }

    private static boolean hasMatchingName(List<PantryItem> items, Map<Long, String> canonical, String reqName) {
        for (PantryItem item : items) {
            if (IngredientKnowledge.satisfies(canonical.get(item.getId()), reqName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean canConvert(Unit from, Unit to, String reqName, String itemName) {
        return convertEither(1.0, from, to, reqName, itemName) != null;
    }

    /** Tries the recipe's ingredient name first, then the pantry item's name, for density/weight lookup. */
    private static Double convertEither(double qty, Unit from, Unit to, String reqName, String itemName) {
        Double v = UnitConverter.convert(qty, from, to, reqName);
        if (v == null && !reqName.equals(itemName)) {
            v = UnitConverter.convert(qty, from, to, itemName);
        }
        return v;
    }
}
