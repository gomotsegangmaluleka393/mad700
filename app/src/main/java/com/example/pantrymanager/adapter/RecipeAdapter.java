package com.example.pantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pantrymanager.R;
import com.example.pantrymanager.logic.MatchResult;
import com.example.pantrymanager.logic.MissingIngredient;
import com.example.pantrymanager.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom RecyclerView adapter for recipe match results. The same adapter is used for the
 * strict "You can make" list and the separate "Almost there" list; the latter also shows
 * which ingredient is missing.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface Listener {
        void onRecipeClicked(Recipe recipe);
    }

    private final List<MatchResult> results = new ArrayList<>();
    private final Listener listener;
    private final boolean showMissing;

    public RecipeAdapter(Listener listener, boolean showMissing) {
        this.listener = listener;
        this.showMissing = showMissing;
    }

    public void submit(List<MatchResult> newResults) {
        results.clear();
        results.addAll(newResults);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        MatchResult result = results.get(position);
        Recipe recipe = result.getRecipe();
        h.name.setText(recipe.getName());
        h.meta.setText(h.itemView.getContext().getString(R.string.recipe_meta,
                recipe.getIngredients().size(), recipe.getPrepMinutes()));
        h.description.setText(recipe.getDescription());

        if (showMissing && !result.getMissing().isEmpty()) {
            MissingIngredient m = result.getMissing().get(0);
            h.status.setText(h.itemView.getContext().getString(R.string.missing_one, m.getName(), m.describe()));
            h.status.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.status_warn));
        } else {
            h.status.setText(R.string.have_everything);
            h.status.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.status_good));
        }

        h.itemView.setOnClickListener(v -> listener.onRecipeClicked(recipe));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView meta;
        final TextView description;
        final TextView status;

        ViewHolder(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.text_recipe_name);
            meta = v.findViewById(R.id.text_recipe_meta);
            description = v.findViewById(R.id.text_recipe_description);
            status = v.findViewById(R.id.text_recipe_status);
        }
    }
}
