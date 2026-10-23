package com.example.pantrymanager.UI;

import android.content.Intent;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pantrymanager.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Shared bottom-navigation behaviour for the three top-level screens
 * (Pantry, Recipes, Settings). Navigation uses explicit Intents.
 */
public abstract class BaseNavActivity extends AppCompatActivity {

    /** The bottom-nav item that represents this screen. */
    @IdRes
    protected abstract int getNavItemId();

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        if (nav == null) {
            return;
        }
        // Clear the listener while re-selecting, so we don't trigger navigation to ourselves.
        nav.setOnItemSelectedListener(null);
        nav.setSelectedItemId(getNavItemId());
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == getNavItemId()) {
                return true;
            }
            Class<?> target;
            if (id == R.id.nav_pantry) {
                target = MainActivity.class;
            } else if (id == R.id.nav_recipes) {
                target = SuggestedRecipesActivity.class;
            } else if (id == R.id.nav_settings) {
                target = SettingsActivity.class;
            } else {
                return false;
            }
            Intent intent = new Intent(this, target);
            // Bring an existing instance forward instead of stacking duplicates.
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(0, 0);
            return true;
        });
    }
}
