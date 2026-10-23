package com.example.pantrymanager.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A recipe with its required ingredients and preparation steps.
 */
public class Recipe {

    private long id;
    private final String name;
    private final String description;
    private final int prepMinutes;
    private final List<RecipeIngredient> ingredients = new ArrayList<>();
    private final List<String> steps = new ArrayList<>();

    public Recipe(long id, String name, String description, int prepMinutes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.prepMinutes = prepMinutes;
    }

    /** Fluent helper used by the seeder and the tests. */
    public Recipe ingredient(String name, double quantity, String unit) {
        ingredients.add(new RecipeIngredient(name, quantity, unit));
        return this;
    }

    /** Fluent helper used by the seeder and the tests. */
    public Recipe step(String text) {
        steps.add(text);
        return this;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPrepMinutes() { return prepMinutes; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public List<String> getSteps() { return steps; }
}
