package com.example.pantrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.pantrymanager.model.PantryItem;
import com.example.pantrymanager.model.Recipe;
import com.example.pantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Local SQLite database for the Smart Pantry Manager, built on SQLiteOpenHelper.
  * Tables:
 *  pantry_items (the user's ingredients)
 *  recipes (the seeded recipe collection)
 *  recipe_ingredients  ( ingredients per recipe)
 *  recipe_steps (method steps per recipe)
  * The recipe tables are filled from {@link RecipeSeedData} in onCreate(), which Android
 * calls only once: the first time the database file is opened on the device.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_Name = "smart_pantry.db";
    private static final int DB_Version = 1;

    // pantry_items
    public static final String T_Pantry = "pantry_items";
    public static final String P_ID = "_id";
    public static final String P_Name = "name";
    public static final String P_Qty = "quantity";
    public static final String P_Unit = "unit";
    public static final String P_Expiry = "expiry_date";
    public static final String P_Created = "created_at";

    // recipes
    public static final String T_Recipes = "recipes";
    public static final String R_ID = "_id";
    public static final String R_Name = "name";
    public static final String R_Desc = "description";
    public static final String R_Minutes = "prep_minutes";

    // recipe ingredients
    public static final String T_Recipe_Ing = "recipe_ingredients";
    public static final String RI_ID = "_id";
    public static final String RI_Recipe = "recipe_id";
    public static final String RI_Name = "name";
    public static final String RI_Qty = "quantity";
    public static final String RI_Unit = "unit";

    // recipe steps
    public static final String T_Recipe_Steps = "recipe_steps";
    public static final String RS_ID = "_id";
    public static final String RS_Recipe = "recipe_id";
    public static final String RS_Number = "step_number";
    public static final String RS_Instruction = "instruction";

    private static DatabaseHelper instance;

    /** One shared helper for the whole app, so connections are not leaked between Activities. */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_Name, null, DB_Version);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_Pantry + " ("
                + P_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + P_Name + " TEXT NOT NULL, "
                + P_Qty + " REAL NOT NULL CHECK (" + P_Qty + " > 0), "
                + P_Unit + " TEXT NOT NULL, "
                + P_Expiry + " TEXT, "
                + P_Created + " INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE " + T_Recipes + " ("
                + R_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + R_Name + " TEXT NOT NULL UNIQUE, "
                + R_Desc + " TEXT, "
                + R_Minutes + " INTEGER NOT NULL DEFAULT 0)");

        db.execSQL("CREATE TABLE " + T_Recipe_Ing + " ("
                + RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + RI_Recipe + " INTEGER NOT NULL REFERENCES " + T_Recipes + "(" + R_ID + ") ON DELETE CASCADE, "
                + RI_Name + " TEXT NOT NULL, "
                + RI_Qty + " REAL NOT NULL, "
                + RI_Unit + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + T_Recipe_Steps + " ("
                + RS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + RS_Recipe + " INTEGER NOT NULL REFERENCES " + T_Recipes + "(" + R_ID + ") ON DELETE CASCADE, "
                + RS_Number + " INTEGER NOT NULL, "
                + RS_Instruction + " TEXT NOT NULL)");

        db.execSQL("CREATE INDEX idx_ing_recipe ON " + T_Recipe_Ing + "(" + RI_Recipe + ")");
        db.execSQL("CREATE INDEX idx_steps_recipe ON " + T_Recipe_Steps + "(" + RS_Recipe + ")");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 is the first release, so there is nothing to migrate yet.
    }

    /** Inserts the recipe collection in one transaction. */
    private void seedRecipes(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            for (Recipe recipe : RecipeSeedData.recipes()) {
                ContentValues rv = new ContentValues();
                rv.put(R_Name, recipe.getName());
                rv.put(R_Desc, recipe.getDescription());
                rv.put(R_Minutes, recipe.getPrepMinutes());
                long recipeId = db.insertOrThrow(T_Recipes, null, rv);

                for (RecipeIngredient ing : recipe.getIngredients()) {
                    ContentValues iv = new ContentValues();
                    iv.put(RI_Recipe, recipeId);
                    iv.put(RI_Name, ing.getName());
                    iv.put(RI_Qty, ing.getQuantity());
                    iv.put(RI_Unit, ing.getUnit());
                    db.insertOrThrow(T_Recipe_Ing, null, iv);
                }
                int n = 1;
                for (String step : recipe.getSteps()) {
                    ContentValues sv = new ContentValues();
                    sv.put(RS_Recipe, recipeId);
                    sv.put(RS_Number, n++);
                    sv.put(RS_Instruction, step);
                    db.insertOrThrow(T_Recipe_Steps, null, sv);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // Pantry CRUD


    /** CREATE - returns the new row id, or -1 on failure. */
    public long insertPantryItem(PantryItem item) {
        ContentValues v = toValues(item);
        v.put(P_Created, System.currentTimeMillis());
        long id = getWritableDatabase().insert(T_Pantry, null, v);
        item.setId(id);
        return id;
    }

    /** READ - all pantry items, alphabetically. */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(T_Pantry, null, null, null, null, null,
                P_Name + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                items.add(fromCursor(c));
            }
        }
        return items;
    }

    /** READ - a single pantry item, or null if it no longer exists. */
    public PantryItem getPantryItem(long id) {
        try (Cursor c = getReadableDatabase().query(T_Pantry, null, P_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? fromCursor(c) : null;
        }
    }

    /** UPDATE - returns the number of rows changed (1 on success). */
    public int updatePantryItem(PantryItem item) {
        return getWritableDatabase().update(T_Pantry, toValues(item), P_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
    }

    /** DELETE - returns the number of rows removed (1 on success). */
    public int deletePantryItem(long id) {
        return getWritableDatabase().delete(T_Pantry, P_ID + " = ?", new String[]{String.valueOf(id)});
    }

    /** DELETE - removes every pantry item (used by Settings > Clear pantry). */
    public int clearPantry() {
        return getWritableDatabase().delete(T_Pantry, null, null);
    }

    /**
     * Subtracts what a recipe used from the pantry, in a single transaction.
     * Items that drop to (almost) zero are deleted.
     *
     * @param consumption pantry item id -> amount used, in that item's own unit
     */
    public void consumeIngredients(Map<Long, Double> consumption) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (Map.Entry<Long, Double> e : consumption.entrySet()) {
                PantryItem item = getPantryItem(e.getKey());
                if (item == null) {
                    continue;
                }
                double left = item.getQuantity() - e.getValue();
                if (left <= item.getQuantity() * 1e-6 || left < 1e-6) {
                    deletePantryItem(item.getId());
                } else {
                    item.setQuantity(Math.round(left * 1000.0) / 1000.0);
                    updatePantryItem(item);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private static ContentValues toValues(PantryItem item) {
        ContentValues v = new ContentValues();
        v.put(P_Name, item.getName().trim());
        v.put(P_Qty, item.getQuantity());
        v.put(P_Unit, item.getUnit());
        if (item.hasExpiryDate()) {
            v.put(P_Expiry, item.getExpiryDate());
        } else {
            v.putNull(P_Expiry);
        }
        return v;
    }

    private static PantryItem fromCursor(Cursor c) {
        int expiryIdx = c.getColumnIndexOrThrow(P_Expiry);
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow(P_ID)),
                c.getString(c.getColumnIndexOrThrow(P_Name)),
                c.getDouble(c.getColumnIndexOrThrow(P_Qty)),
                c.getString(c.getColumnIndexOrThrow(P_Unit)),
                c.isNull(expiryIdx) ? null : c.getString(expiryIdx));
    }


    // Recipes (read-only for the user)


    /** All recipes with their ingredients and steps, loaded with three queries in total. */
    public List<Recipe> getAllRecipes() {
        SQLiteDatabase db = getReadableDatabase();
        Map<Long, Recipe> byId = new LinkedHashMap<>();

        try (Cursor c = db.query(T_Recipes, null, null, null, null, null, R_Name + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                Recipe r = recipeFromCursor(c);
                byId.put(r.getId(), r);
            }
        }
        try (Cursor c = db.query(T_Recipe_Ing, null, null, null, null, null, RI_ID + " ASC")) {
            while (c.moveToNext()) {
                Recipe r = byId.get(c.getLong(c.getColumnIndexOrThrow(RI_Recipe)));
                if (r != null) {
                    r.ingredient(c.getString(c.getColumnIndexOrThrow(RI_Name)),
                            c.getDouble(c.getColumnIndexOrThrow(RI_Qty)),
                            c.getString(c.getColumnIndexOrThrow(RI_Unit)));
                }
            }
        }
        try (Cursor c = db.query(T_Recipe_Steps, null, null, null, null, null,
                RS_Recipe + " ASC, " + RS_Number + " ASC")) {
            while (c.moveToNext()) {
                Recipe r = byId.get(c.getLong(c.getColumnIndexOrThrow(RS_Recipe)));
                if (r != null) {
                    r.step(c.getString(c.getColumnIndexOrThrow(RS_Instruction)));
                }
            }
        }
        return new ArrayList<>(byId.values());
    }

    /** One recipe with ingredients and steps, or null if not found. */
    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        String[] args = {String.valueOf(id)};
        Recipe recipe;
        try (Cursor c = db.query(T_Recipes, null, R_ID + " = ?", args, null, null, null)) {
            if (!c.moveToFirst()) {
                return null;
            }
            recipe = recipeFromCursor(c);
        }
        try (Cursor c = db.query(T_Recipe_Ing, null, RI_Recipe + " = ?", args, null, null, RI_ID + " ASC")) {
            while (c.moveToNext()) {
                recipe.ingredient(c.getString(c.getColumnIndexOrThrow(RI_Name)),
                        c.getDouble(c.getColumnIndexOrThrow(RI_Qty)),
                        c.getString(c.getColumnIndexOrThrow(RI_Unit)));
            }
        }
        try (Cursor c = db.query(T_Recipe_Steps, null, RS_Recipe + " = ?", args, null, null, RS_Number + " ASC")) {
            while (c.moveToNext()) {
                recipe.step(c.getString(c.getColumnIndexOrThrow(RS_Instruction)));
            }
        }
        return recipe;
    }

    public int getRecipeCount() {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + T_Recipes, null)) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    private static Recipe recipeFromCursor(Cursor c) {
        return new Recipe(
                c.getLong(c.getColumnIndexOrThrow(R_ID)),
                c.getString(c.getColumnIndexOrThrow(R_Name)),
                c.getString(c.getColumnIndexOrThrow(R_Desc)),
                c.getInt(c.getColumnIndexOrThrow(R_Minutes)));
    }
}

