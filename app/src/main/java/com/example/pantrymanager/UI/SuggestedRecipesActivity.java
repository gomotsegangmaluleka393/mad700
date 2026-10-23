package com.example.pantrymanager.UI;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pantrymanager.R;
import com.example.pantrymanager.adapter.RecipeAdapter;
import com.example.pantrymanager.data.DatabaseHelper;
import com.example.pantrymanager.data.SettingsManager;
import com.example.pantrymanager.logic.MatchResult;
import com.example.pantrymanager.logic.RecipeMatcher;
import com.example.pantrymanager.model.PantryItem;
import com.example.pantrymanager.model.Recipe;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen 3 - Suggested Recipes.
 * Runs the strict-matching rule against the current pantry. Only recipes for which EVERY
 * ingredient is available in sufficient quantity appear under "You can make these now".
 * Recipes missing exactly one ingredient are shown in a separate "Almost there" section
 * (optional, can be turned off in Settings).
 */
public class SuggestedRecipesActivity extends BaseNavActivity implements RecipeAdapter.Listener {

    private DatabaseHelper db;
    private SettingsManager settings;
    private RecipeAdapter strictAdapter;
    private RecipeAdapter almostAdapter;

    private TextView summary;
    private RecyclerView strictList;
    private View emptyState;
    private TextView emptyMessage;
    private View almostSection;

    @Override
    protected int getNavItemId() {
        return R.id.nav_recipes;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = DatabaseHelper.getInstance(this);
        settings = new SettingsManager(this);

        summary = findViewById(R.id.text_summary);
        strictList = findViewById(R.id.recycler_strict);
        emptyState = findViewById(R.id.empty_recipes);
        emptyMessage = findViewById(R.id.text_empty_recipes);
        almostSection = findViewById(R.id.section_almost);

        strictAdapter = new RecipeAdapter(this, false);
        strictList.setLayoutManager(new LinearLayoutManager(this));
        strictList.setNestedScrollingEnabled(false);
        strictList.setAdapter(strictAdapter);

        almostAdapter = new RecipeAdapter(this, true);
        RecyclerView almostList = findViewById(R.id.recycler_almost);
        almostList.setLayoutManager(new LinearLayoutManager(this));
        almostList.setNestedScrollingEnabled(false);
        almostList.setAdapter(almostAdapter);

        findViewById(R.id.button_add_ingredients).setOnClickListener(
                v -> startActivity(new Intent(this, AddEditIngredientActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        runMatching();
    }

    /** Re-run the strict match every time the screen is shown, so pantry edits are reflected. */
    private void runMatching() {
        List<PantryItem> pantry = db.getAllPantryItems();
        List<Recipe> recipes = db.getAllRecipes();
        RecipeMatcher matcher = settings.buildMatcher();

        List<MatchResult> all = matcher.evaluateAll(pantry, recipes);
        List<MatchResult> strict = new ArrayList<>();
        List<MatchResult> almost = new ArrayList<>();
        for (MatchResult r : all) {
            if (r.isStrictMatch()) {
                strict.add(r);
            } else if (r.isAlmostThere()) {
                almost.add(r);
            }
        }

        strictAdapter.submit(strict);
        summary.setText(getString(R.string.match_summary, strict.size(), recipes.size()));

        if (strict.isEmpty()) {
            strictList.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            emptyMessage.setText(pantry.isEmpty()
                    ? R.string.empty_recipes_no_pantry
                    : R.string.empty_recipes_no_match);
        } else {
            strictList.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }

        boolean showAlmost = settings.isShowAlmostThere() && !almost.isEmpty();
        almostSection.setVisibility(showAlmost ? View.VISIBLE : View.GONE);
        almostAdapter.submit(almost);
    }

    @Override
    public void onRecipeClicked(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
