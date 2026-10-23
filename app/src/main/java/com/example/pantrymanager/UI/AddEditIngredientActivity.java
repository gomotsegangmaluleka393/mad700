package com.example.pantrymanager.UI;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pantrymanager.R;
import com.example.pantrymanager.data.DatabaseHelper;
import com.example.pantrymanager.logic.Unit;
import com.example.pantrymanager.model.PantryItem;
import com.example.pantrymanager.model.Recipe;
import com.example.pantrymanager.model.RecipeIngredient;
import com.example.pantrymanager.util.ExpiryUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

/**
 * Screen 2 - Add / Edit Ingredient.
 * Opened with no extras to CREATE a new item, or with {@link #EXTRA_ITEM_ID} to UPDATE
 * (and optionally DELETE) an existing one. All input is validated before saving.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "com.example.smartpantry.EXTRA_ITEM_ID";
    private static final String STATE_EXPIRY = "state_expiry";

    private static final int MAX_NAME_LENGTH = 50;
    private static final double MAX_QUANTITY = 100000;

    private DatabaseHelper db;
    private PantryItem editing;          // null when adding

    private TextInputLayout nameLayout;
    private TextInputLayout quantityLayout;
    private TextInputLayout expiryLayout;
    private MaterialAutoCompleteTextView nameInput;
    private TextInputEditText quantityInput;
    private TextInputEditText expiryInput;
    private Spinner unitSpinner;

    /** Selected expiry as yyyy-MM-dd, or null. */
    private String selectedExpiry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = DatabaseHelper.getInstance(this);

        nameLayout = findViewById(R.id.layout_name);
        quantityLayout = findViewById(R.id.layout_quantity);
        expiryLayout = findViewById(R.id.layout_expiry);
        nameInput = findViewById(R.id.input_name);
        quantityInput = findViewById(R.id.input_quantity);
        expiryInput = findViewById(R.id.input_expiry);
        unitSpinner = findViewById(R.id.spinner_unit);
        Button saveButton = findViewById(R.id.button_save);
        Button deleteButton = findViewById(R.id.button_delete);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Unit.symbols());
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(unitAdapter);
        unitSpinner.setSelection(Arrays.asList(Unit.symbols()).indexOf(Unit.PCS.getSymbol()));

        // Suggest ingredient names used by the recipes so the user's spelling lines up with them.
        nameInput.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, knownIngredientNames()));
        nameInput.setThreshold(1);

        clearErrorOnEdit(nameInput, nameLayout);
        clearErrorOnEdit(quantityInput, quantityLayout);

        expiryInput.setOnClickListener(v -> showDatePicker());
        expiryLayout.setEndIconOnClickListener(v -> setExpiry(null));
        setExpiry(null);   // start with no date (hides the clear icon)

        long id = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (id != -1) {
            editing = db.getPantryItem(id);
            if (editing == null) {
                Toast.makeText(this, R.string.item_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            setTitle(R.string.title_edit_ingredient);
            deleteButton.setVisibility(View.VISIBLE);
            if (savedInstanceState == null) {
                populate(editing);
            }
        } else {
            setTitle(R.string.title_add_ingredient);
            deleteButton.setVisibility(View.GONE);
        }

        if (savedInstanceState != null) {
            setExpiry(savedInstanceState.getString(STATE_EXPIRY));
        }

        saveButton.setOnClickListener(v -> save());
        deleteButton.setOnClickListener(v -> confirmDelete());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_EXPIRY, selectedExpiry);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void populate(PantryItem item) {
        nameInput.setText(item.getName(), false);
        quantityInput.setText(trimNumber(item.getQuantity()));
        int idx = Arrays.asList(Unit.symbols()).indexOf(item.getUnit());
        if (idx >= 0) {
            unitSpinner.setSelection(idx);
        }
        setExpiry(item.getExpiryDate());
    }

    private void showDatePicker() {
        LocalDate initial = ExpiryUtils.parse(selectedExpiry);
        if (initial == null) {
            initial = LocalDate.now().plusDays(7);
        }
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, day) -> setExpiry(ExpiryUtils.toIso(year, month, day)),
                initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth());
        dialog.show();
    }

    private void setExpiry(String iso) {
        selectedExpiry = (iso == null || iso.isEmpty()) ? null : iso;
        expiryInput.setText(selectedExpiry == null ? "" : ExpiryUtils.formatForDisplay(selectedExpiry));
        expiryLayout.setEndIconVisible(selectedExpiry != null);
        expiryLayout.setError(null);
    }


    // Validation + save


    private void save() {
        String name = nameInput.getText() == null ? "" : nameInput.getText().toString().trim().replaceAll("\\s+", " ");
        String qtyText = quantityInput.getText() == null ? "" : quantityInput.getText().toString().trim();
        String unit = (String) unitSpinner.getSelectedItem();

        boolean valid = true;

        // Name: required, sensible length, must contain letters, no digits/symbols.
        if (TextUtils.isEmpty(name)) {
            nameLayout.setError(getString(R.string.error_name_required));
            valid = false;
        } else if (name.length() < 2) {
            nameLayout.setError(getString(R.string.error_name_short));
            valid = false;
        } else if (name.length() > MAX_NAME_LENGTH) {
            nameLayout.setError(getString(R.string.error_name_long, MAX_NAME_LENGTH));
            valid = false;
        } else if (!name.matches("[\\p{L} '\\-(),&]+")) {
            nameLayout.setError(getString(R.string.error_name_chars));
            valid = false;
        }

        // Quantity: required, numeric, greater than zero, within a sane maximum.
        double quantity = 0;
        if (TextUtils.isEmpty(qtyText)) {
            quantityLayout.setError(getString(R.string.error_qty_required));
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(qtyText.replace(',', '.'));
                if (Double.isNaN(quantity) || Double.isInfinite(quantity) || quantity <= 0) {
                    quantityLayout.setError(getString(R.string.error_qty_positive));
                    valid = false;
                } else if (quantity > MAX_QUANTITY) {
                    quantityLayout.setError(getString(R.string.error_qty_large));
                    valid = false;
                }
            } catch (NumberFormatException e) {
                quantityLayout.setError(getString(R.string.error_qty_number));
                valid = false;
            }
        }

        // Unit: must be one of the supported units.
        if (unit == null || Unit.parse(unit) == null) {
            Toast.makeText(this, R.string.error_unit, Toast.LENGTH_SHORT).show();
            valid = false;
        }

        // Expiry: optional, but a newly chosen date may not be in the past.
        boolean expiryChanged = editing == null
                ? selectedExpiry != null
                : !TextUtils.equals(selectedExpiry, editing.getExpiryDate());
        if (expiryChanged && ExpiryUtils.isExpired(selectedExpiry)) {
            expiryLayout.setError(getString(R.string.error_expiry_past));
            valid = false;
        }

        if (!valid) {
            return;
        }

        if (editing == null) {
            PantryItem item = new PantryItem(0, name, quantity, unit, selectedExpiry);
            if (db.insertPantryItem(item) == -1) {
                Toast.makeText(this, R.string.error_save, Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, getString(R.string.added_item, name), Toast.LENGTH_SHORT).show();
        } else {
            editing.setName(name);
            editing.setQuantity(quantity);
            editing.setUnit(unit);
            editing.setExpiryDate(selectedExpiry);
            if (db.updatePantryItem(editing) != 1) {
                Toast.makeText(this, R.string.error_save, Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, getString(R.string.updated_item, name), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void confirmDelete() {
        if (editing == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, editing.getName()))
                .setPositiveButton(R.string.delete, (d, w) -> {
                    db.deletePantryItem(editing.getId());
                    Toast.makeText(this, getString(R.string.deleted_item, editing.getName()), Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }


    // Helpers

    private String[] knownIngredientNames() {
        TreeSet<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Recipe r : db.getAllRecipes()) {
            for (RecipeIngredient i : r.getIngredients()) {
                if (!"water".equalsIgnoreCase(i.getName())) {
                    names.add(i.getName());
                }
            }
        }
        List<String> list = new ArrayList<>(names);
        return list.toArray(new String[0]);
    }

    private static void clearErrorOnEdit(android.widget.EditText input, TextInputLayout layout) {
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout.setError(null);
            }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private static String trimNumber(double d) {
        if (d == Math.rint(d)) {
            return String.valueOf((long) d);
        }
        return String.valueOf(d);
    }
}
