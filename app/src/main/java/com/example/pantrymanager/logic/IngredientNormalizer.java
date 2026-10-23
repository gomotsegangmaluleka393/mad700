package com.example.pantrymanager.logic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Turns messy, user-typed ingredient names into a canonical form so that
 * "Tomatoes", "tomato", "fresh tomatoes" and " TOMATO " all compare as equal.
 * Steps:
 *  1. lower-case, strip punctuation and extra spaces
 *  2. singularise every word (tomatoes -> tomato, berries -> berry, loaves -> loaf)
 *  3. map known synonyms to one name (scallion -> spring onion, mince -> ground beef)
 *  4. drop preparation/size words that do not change what the ingredient is
 *     (fresh, large, chopped, diced ,etc)
 */
public final class IngredientNormalizer {

    private IngredientNormalizer() {
    }

    /** Words that describe size, state or preparation rather than the ingredient itself. */
    private static final Set<String> DESCRIPTORS = new HashSet<>(Arrays.asList(
            "fresh", "freshly", "large", "small", "medium", "big", "ripe", "raw",
            "chopped", "diced", "sliced", "grated", "shredded", "minced", "crushed",
            "peeled", "whole", "frozen", "canned", "tinned", "dried", "organic",
            "of", "a", "an", "the", "some", "free", "range", "boneless", "skinless",
            "cooked", "uncooked", "plain", "full", "low", "fat", "extra", "virgin",
            "brand", "pack", "packet", "bag", "box", "bottle", "jar", "tin", "can",
            "clove", "slice", "piece", "head", "bunch", "stick", "fillet"));

    /** Words that end in "s" but are already singular (or have no plural form). */
    private static final Set<String> NO_SINGULAR = new HashSet<>(Arrays.asList(
            "hummus", "couscous", "asparagus", "molasses", "swiss", "brussels", "chickpeas",
            "oats", "lentils", "peas", "beans", "noodles", "grits", "greens"));

    /** Irregular plurals. */
    private static final Map<String, String> IRREGULAR = new HashMap<>();

    /** Whole-phrase synonyms, applied after singularising. Value is the canonical name. */
    private static final Map<String, String> SYNONYMS = new HashMap<>();

    static {
        IRREGULAR.put("leaves", "leaf");
        IRREGULAR.put("loaves", "loaf");
        IRREGULAR.put("knives", "knife");
        IRREGULAR.put("halves", "half");
        IRREGULAR.put("potatoes", "potato");
        IRREGULAR.put("tomatoes", "tomato");
        IRREGULAR.put("mangoes", "mango");
        IRREGULAR.put("avocadoes", "avocado");
        IRREGULAR.put("chillies", "chilli");
        IRREGULAR.put("cookies", "cookie");
        IRREGULAR.put("cheeses", "cheese");
        IRREGULAR.put("sauces", "sauce");
        IRREGULAR.put("spices", "spice");
        IRREGULAR.put("olives", "olive");
        IRREGULAR.put("chives", "chive");

        // Plural-only forms we keep as-is but still want to accept the singular.
        SYNONYMS.put("oat", "oats");
        SYNONYMS.put("rolled oats", "oats");
        SYNONYMS.put("oat flake", "oats");
        SYNONYMS.put("lentil", "lentils");
        SYNONYMS.put("pea", "peas");
        SYNONYMS.put("green peas", "peas");
        SYNONYMS.put("garden peas", "peas");
        SYNONYMS.put("noodle", "noodles");
        SYNONYMS.put("chickpea", "chickpeas");

        SYNONYMS.put("egg", "egg");
        SYNONYMS.put("hen egg", "egg");
        SYNONYMS.put("scallion", "spring onion");
        SYNONYMS.put("green onion", "spring onion");
        SYNONYMS.put("salad onion", "spring onion");
        SYNONYMS.put("mince", "ground beef");
        SYNONYMS.put("beef mince", "ground beef");
        SYNONYMS.put("minced beef", "ground beef");
        SYNONYMS.put("mince meat", "ground beef");
        SYNONYMS.put("mincemeat", "ground beef");
        SYNONYMS.put("capsicum", "bell pepper");
        SYNONYMS.put("green pepper", "bell pepper");
        SYNONYMS.put("red pepper", "bell pepper");
        SYNONYMS.put("yellow pepper", "bell pepper");
        SYNONYMS.put("sweet pepper", "bell pepper");
        SYNONYMS.put("mealie meal", "maize meal");
        SYNONYMS.put("mielie meal", "maize meal");
        SYNONYMS.put("mealiemeal", "maize meal");
        SYNONYMS.put("cornmeal", "maize meal");
        SYNONYMS.put("corn meal", "maize meal");
        SYNONYMS.put("yogurt", "yoghurt");
        SYNONYMS.put("yoghourt", "yoghurt");
        SYNONYMS.put("plain yoghurt", "yoghurt");
        SYNONYMS.put("greek yoghurt", "yoghurt");
        SYNONYMS.put("greek yogurt", "yoghurt");
        SYNONYMS.put("chilli", "chili");
        SYNONYMS.put("spaghetti", "pasta");
        SYNONYMS.put("penne", "pasta");
        SYNONYMS.put("macaroni", "pasta");
        SYNONYMS.put("fusilli", "pasta");
        SYNONYMS.put("linguine", "pasta");
        SYNONYMS.put("tagliatelle", "pasta");
        SYNONYMS.put("all purpose flour", "flour");
        SYNONYMS.put("plain flour", "flour");
        SYNONYMS.put("cake flour", "flour");
        SYNONYMS.put("wheat flour", "flour");
        SYNONYMS.put("white sugar", "sugar");
        SYNONYMS.put("castor sugar", "sugar");
        SYNONYMS.put("caster sugar", "sugar");
        SYNONYMS.put("granulated sugar", "sugar");
        SYNONYMS.put("table salt", "salt");
        SYNONYMS.put("sea salt", "salt");
        SYNONYMS.put("fine salt", "salt");
        SYNONYMS.put("white bread", "bread");
        SYNONYMS.put("brown bread", "bread");
        SYNONYMS.put("bread slice", "bread");
        SYNONYMS.put("toast", "bread");
        SYNONYMS.put("garlic clove", "garlic");
        SYNONYMS.put("chicken breast fillet", "chicken breast");
        SYNONYMS.put("chicken fillet", "chicken breast");
        SYNONYMS.put("canned tuna", "tuna");
        SYNONYMS.put("tuna chunk", "tuna");
        SYNONYMS.put("mayo", "mayonnaise");
        SYNONYMS.put("baked bean", "baked beans");
        SYNONYMS.put("bean in tomato sauce", "baked beans");
        SYNONYMS.put("bicarb", "baking soda");
        SYNONYMS.put("bicarbonate of soda", "baking soda");
        SYNONYMS.put("unsalted butter", "butter");
        SYNONYMS.put("salted butter", "butter");
        SYNONYMS.put("whole milk", "milk");
        SYNONYMS.put("fresh milk", "milk");
        SYNONYMS.put("lemon juice", "lemon juice");
        SYNONYMS.put("soya sauce", "soy sauce");
        SYNONYMS.put("vegetable oil", "sunflower oil");
        SYNONYMS.put("cooking oil", "oil");
        SYNONYMS.put("courgette", "zucchini");
        SYNONYMS.put("baby marrow", "zucchini");
        SYNONYMS.put("coriander", "cilantro");
        SYNONYMS.put("aubergine", "eggplant");
        SYNONYMS.put("brinjal", "eggplant");
    }

    /**
     * Returns the canonical form of an ingredient name, or an empty string for blank input.
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String text = raw.toLowerCase(Locale.ROOT)
                .replace('-', ' ')
                .replace('_', ' ')
                .replaceAll("\\(.*?\\)", " ") // drop "(optional)", "(400g)" etc.
                .replaceAll("[^a-z ]", " ")  // drop digits and punctuation
                .replaceAll("\\s+", " ")
                .trim();
        if (text.isEmpty()) {
            return "";
        }

        String singular = singularizePhrase(text);

        // A synonym of the full phrase wins first (e.g. "minced beef" before "minced" is dropped).
        String mapped = SYNONYMS.get(singular);
        if (mapped != null) {
            return mapped;
        }

        String stripped = stripDescriptors(singular);
        if (stripped.isEmpty()) {
            stripped = singular;                       // never strip a name down to nothing
        }
        mapped = SYNONYMS.get(stripped);
        return mapped != null ? mapped : stripped;
    }

    private static String singularizePhrase(String phrase) {
        String[] words = phrase.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(singularize(w));
        }
        return sb.toString();
    }

    private static String stripDescriptors(String phrase) {
        StringBuilder sb = new StringBuilder();
        for (String w : phrase.split(" ")) {
            if (DESCRIPTORS.contains(w)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(w);
        }
        return sb.toString();
    }

    /** English plural-to-singular rules good enough for grocery words. */
    static String singularize(String word) {
        if (word.length() <= 3 || NO_SINGULAR.contains(word)) {
            return word;
        }
        String irregular = IRREGULAR.get(word);
        if (irregular != null) {
            return irregular;
        }
        if (word.endsWith("ss") || word.endsWith("us") || word.endsWith("is")) {
            return word;                                 // glass, asparagus, aioli-ish words
        }
        if (word.endsWith("ies")) {
            return word.substring(0, word.length() - 3) + "y";   // berries -> berry
        }
        if (word.endsWith("oes")) {
            return word.substring(0, word.length() - 2);          // potatoes -> potato
        }
        if (word.endsWith("ches") || word.endsWith("shes") || word.endsWith("xes")
                || word.endsWith("zes") || word.endsWith("sses")) {
            return word.substring(0, word.length() - 2);          // peaches -> peach
        }
        if (word.endsWith("s")) {
            return word.substring(0, word.length() - 1);          // carrots -> carrot
        }
        return word;
    }
}

