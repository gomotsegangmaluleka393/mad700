package com.example.pantrymanager.UI;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.pantrymanager.R;
import com.example.pantrymanager.data.DatabaseHelper;
import com.example.pantrymanager.data.SettingsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;

/**
 * Screen 5 - Settings.
 * Expiring-soon alerts, display units, matching options, and a "clear pantry" action.
 * Every change is saved to SharedPreferences immediately.
 */
public class SettingsActivity extends BaseNavActivity {

    private static final Integer[] DAY_OPTIONS = {1, 2, 3, 5, 7, 14};

    private SettingsManager settings;
    private DatabaseHelper db;
    private Spinner daysSpinner;
    private TextView aboutText;

    @Override
    protected int getNavItemId() {
        return R.id.nav_settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        settings = new SettingsManager(this);
        db = DatabaseHelper.getInstance(this);

        MaterialSwitch alerts = findViewById(R.id.switch_expiry_alerts);
        alerts.setChecked(settings.isExpiryAlertsEnabled());

        daysSpinner = findViewById(R.id.spinner_expiry_days);
        ArrayAdapter<Integer> daysAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, DAY_OPTIONS);
        daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daysSpinner.setAdapter(daysAdapter);
        daysSpinner.setSelection(indexOfDays(settings.getExpiryDays()));
        daysSpinner.setEnabled(alerts.isChecked());
        daysSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                settings.setExpiryDays(DAY_OPTIONS[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        alerts.setOnCheckedChangeListener((b, checked) -> {
            settings.setExpiryAlertsEnabled(checked);
            daysSpinner.setEnabled(checked);
        });

        RadioGroup units = findViewById(R.id.radio_units);
        units.check(settings.isImperial() ? R.id.radio_imperial : R.id.radio_metric);
        units.setOnCheckedChangeListener((group, checkedId) ->
                settings.setImperial(checkedId == R.id.radio_imperial));

        MaterialSwitch ignoreExpired = findViewById(R.id.switch_ignore_expired);
        ignoreExpired.setChecked(settings.isIgnoreExpired());
        ignoreExpired.setOnCheckedChangeListener((b, checked) -> settings.setIgnoreExpired(checked));

        MaterialSwitch water = findViewById(R.id.switch_water);
        water.setChecked(settings.isWaterStaple());
        water.setOnCheckedChangeListener((b, checked) -> settings.setWaterStaple(checked));

        MaterialSwitch almost = findViewById(R.id.switch_almost);
        almost.setChecked(settings.isShowAlmostThere());
        almost.setOnCheckedChangeListener((b, checked) -> settings.setShowAlmostThere(checked));

        findViewById(R.id.button_clear_pantry).setOnClickListener(v -> confirmClear());

        aboutText = findViewById(R.id.text_about);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAbout();
    }

    private void refreshAbout() {
        aboutText.setText(getString(R.string.about_text, db.getRecipeCount(), db.getAllPantryItems().size()));
    }

    private void confirmClear() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_title)
                .setMessage(R.string.clear_message)
                .setPositiveButton(R.string.clear_confirm, (d, w) -> {
                    int removed = db.clearPantry();
                    Toast.makeText(this, getResources().getQuantityString(R.plurals.cleared_count, removed, removed),
                            Toast.LENGTH_SHORT).show();
                    refreshAbout();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private static int indexOfDays(int days) {
        for (int i = 0; i < DAY_OPTIONS.length; i++) {
            if (DAY_OPTIONS[i] == days) {
                return i;
            }
        }
        return 2;
    }
}
