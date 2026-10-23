package com.example.pantrymanager.UI;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pantrymanager.R;
import com.example.pantrymanager.adapter.PantryAdapter;
import com.example.pantrymanager.data.DatabaseHelper;
import com.example.pantrymanager.data.SettingsManager;
import com.example.pantrymanager.model.PantryItem;
import com.example.pantrymanager.util.ExpiryUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Screen 1 - Pantry List (launcher screen
 * Shows every pantry item from SQLite in a RecyclerView. Tap to edit, use the bin icon
 * (or long-press) to delete, and the + button to add.
 */
public class MainActivity extends BaseNavActivity implements PantryAdapter.Listener {

    private DatabaseHelper db;
    private SettingsManager settings;
    private PantryAdapter adapter;
    private RecyclerView recycler;
    private View emptyView;
    private TextView expiryBanner;
    private TextView countText;

    @Override
    protected int getNavItemId() {
        return R.id.nav_pantry;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = DatabaseHelper.getInstance(this);
        settings = new SettingsManager(this);

        recycler = findViewById(R.id.recycler_pantry);
        emptyView = findViewById(R.id.empty_pantry);
        expiryBanner = findViewById(R.id.text_expiry_banner);
        countText = findViewById(R.id.text_pantry_count);

        adapter = new PantryAdapter(this);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recycler.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditIngredientActivity.class)));

        findViewById(R.id.button_empty_add).setOnClickListener(
                v -> startActivity(new Intent(this, AddEditIngredientActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    /** READ: reload from the database every time the screen becomes visible. */
    private void loadPantry() {
        List<PantryItem> items = db.getAllPantryItems();
        adapter.submit(items, settings.isImperial(), settings.isExpiryAlertsEnabled(), settings.getExpiryDays());

        boolean empty = items.isEmpty();
        recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        countText.setText(getResources().getQuantityString(R.plurals.pantry_count, items.size(), items.size()));
        countText.setVisibility(empty ? View.GONE : View.VISIBLE);

        updateExpiryBanner(items);
    }

    private void updateExpiryBanner(List<PantryItem> items) {
        if (!settings.isExpiryAlertsEnabled()) {
            expiryBanner.setVisibility(View.GONE);
            return;
        }
        int expired = 0;
        int soon = 0;
        for (PantryItem item : items) {
            if (ExpiryUtils.isExpired(item.getExpiryDate())) {
                expired++;
            } else if (ExpiryUtils.isExpiringSoon(item.getExpiryDate(), settings.getExpiryDays())) {
                soon++;
            }
        }
        if (expired == 0 && soon == 0) {
            expiryBanner.setVisibility(View.GONE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (soon > 0) {
            sb.append(getResources().getQuantityString(R.plurals.expiring_soon, soon, soon, settings.getExpiryDays()));
        }
        if (expired > 0) {
            if (sb.length() > 0) sb.append("  •  ");
            sb.append(getResources().getQuantityString(R.plurals.expired_count, expired, expired));
        }
        expiryBanner.setText(sb.toString());
        expiryBanner.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClicked(PantryItem item) {
        // Pass the row id to the edit screen through the Intent.
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClicked(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, item.getName()))
                .setPositiveButton(R.string.delete, (d, w) -> {
                    if (db.deletePantryItem(item.getId()) > 0) {
                        Toast.makeText(this, getString(R.string.deleted_item, item.getName()), Toast.LENGTH_SHORT).show();
                    }
                    loadPantry();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
