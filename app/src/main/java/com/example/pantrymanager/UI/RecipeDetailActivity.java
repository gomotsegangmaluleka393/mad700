package com.example.pantrymanager.UI;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.pantrymanager.R;
import com.example.pantrymanager.data.DatabaseHelper;
import com.example.pantrymanager.data.SettingsManager;
import com.example.pantrymanager.logic.IngredientNormalizer;
import com.example.pantrymanager.logic.MatchResult;
import com.example.pantrymanager.logic.MissingIngredient;
import com.example.pantrymanager.logic.UnitConverter;
import com.example.pantrymanager.model.Recipe;
import com.example.pantrymanager.model.RecipeIngredient;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.HashMap;
import java.util.Map;

/**
 * Screen 4 - Recipe Detail.
 * Receives a recipe id through the Intent, shows the full ingredient list (each marked as
 * available or missing) and the method. When every ingredient is available the user can
 * tap "I cooked this" to subtract the used amounts from the pantry (an UPDATE/DELETE).
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "com.example.smartpantry.EXTRA_RECIPE_ID";

    private DatabaseHelper db;
    private SettingsManager settings;
    private long recipeId;

    private TextView title;
    private TextView description;
    private TextView meta;
    private TextView status;
    private LinearLayout ingredientsContainer;
    private LinearLayout stepsContainer;
    private Button cookButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = DatabaseHelper.getInstance(this);
        settings = new SettingsManager(this);
        recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);

        title = findViewById(R.id.text_title);
        description = findViewById(R.id.text_description);
        meta = findViewById(R.id.text_meta);
        status = findViewById(R.id.text_status);
        ingredientsContainer = findViewById(R.id.container_ingredients);
        stepsContainer = findViewById(R.id.container_steps);
        cookButton = findViewById(R.id.button_cook);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bind();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void bind() {
        Recipe recipe = db.getRecipe(recipeId);
        if (recipe == null) {
            Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setTitle(recipe.getName());
        title.setText(recipe.getName());
        description.setText(recipe.getDescription());
        meta.setText(getString(R.string.recipe_meta, recipe.getIngredients().size(), recipe.getPrepMinutes()));

        MatchResult match = settings.buildMatcher().evaluate(db.getAllPantryItems(), recipe);

        // Index missing ingredients by canonical name to mark each line.
        Map<String, MissingIngredient> missingByName = new HashMap<>();
        for (MissingIngredient m : match.getMissing()) {
            missingByName.put(IngredientNormalizer.normalize(m.getName()), m);
        }

        if (match.isStrictMatch()) {
            status.setText(R.string.status_can_make);
            status.setTextColor(ContextCompat.getColor(this, R.color.status_good));
        } else {
            status.setText(getResources().getQuantityString(R.plurals.status_missing,
                    match.getMissing().size(), match.getMissing().size()));
            status.setTextColor(ContextCompat.getColor(this, R.color.status_bad));
        }

        boolean imperial = settings.isImperial();
        LayoutInflater inflater = LayoutInflater.from(this);
        ingredientsContainer.removeAllViews();
        for (RecipeIngredient ing : recipe.getIngredients()) {
            View row = inflater.inflate(R.layout.item_ingredient_line, ingredientsContainer, false);
            TextView icon = row.findViewById(R.id.text_icon);
            TextView text = row.findViewById(R.id.text_line);
            TextView note = row.findViewById(R.id.text_note);

            text.setText(getString(R.string.ingredient_line,
                    UnitConverter.format(ing.getQuantity(), ing.getUnit(), imperial), ing.getName()));

            String canonical = IngredientNormalizer.normalize(ing.getName());
            MissingIngredient missing = missingByName.get(canonical);
            boolean staple = canonical.equals("water") && settings.isWaterStaple();
            if (missing != null) {
                icon.setText("✗");
                icon.setTextColor(ContextCompat.getColor(this, R.color.status_bad));
                note.setText(missing.describe());
                note.setVisibility(View.VISIBLE);
            } else {
                icon.setText("✓");
                icon.setTextColor(ContextCompat.getColor(this, R.color.status_good));
                if (staple) {
                    note.setText(R.string.water_staple_note);
                    note.setVisibility(View.VISIBLE);
                } else {
                    note.setVisibility(View.GONE);
                }
            }
            ingredientsContainer.addView(row);
        }

        stepsContainer.removeAllViews();
        int n = 1;
        for (String step : recipe.getSteps()) {
            View row = inflater.inflate(R.layout.item_step, stepsContainer, false);
            ((TextView) row.findViewById(R.id.text_step_number)).setText(String.valueOf(n++));
            ((TextView) row.findViewById(R.id.text_step)).setText(step);
            stepsContainer.addView(row);
        }

        cookButton.setEnabled(match.isStrictMatch());
        cookButton.setOnClickListener(v -> confirmCook(recipe, match));
    }

    private void confirmCook(Recipe recipe, MatchResult match) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.cook_title)
                .setMessage(getString(R.string.cook_message, recipe.getName()))
                .setPositiveButton(R.string.cook_confirm, (d, w) -> {
                    db.consumeIngredients(match.getConsumption());
                    Toast.makeText(this, R.string.cook_done, Toast.LENGTH_LONG).show();
                    bind();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
