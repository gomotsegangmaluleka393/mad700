package com.example.pantrymanager.data;

import com.example.pantrymanager.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * The 20 recipes inserted into the database the first time the app runs.
 * NB "water" is listed where a recipe needs it; the matcher treats water as
 * always available while the "Assume tap water is available" setting is on.
 */
public final class RecipeSeedData {

    private RecipeSeedData() {
    }

    public static List<Recipe> recipes() {
        List<Recipe> list = new ArrayList<>();

        list.add(new Recipe(0, "Scrambled Eggs", "Soft, creamy eggs ready in minutes.", 10)
                .ingredient("egg", 3, "pcs")
                .ingredient("milk", 30, "ml")
                .ingredient("butter", 15, "g")
                .ingredient("salt", 0.25, "tsp")
                .step("Whisk the eggs, milk and salt together in a bowl.")
                .step("Melt the butter in a pan over low heat.")
                .step("Pour in the eggs and stir slowly until just set.")
                .step("Serve immediately."));

        list.add(new Recipe(0, "French Toast", "Golden, cinnamon-spiced toast for breakfast.", 15)
                .ingredient("bread", 4, "pcs")
                .ingredient("egg", 2, "pcs")
                .ingredient("milk", 60, "ml")
                .ingredient("sugar", 1, "tbsp")
                .ingredient("cinnamon", 0.5, "tsp")
                .ingredient("butter", 20, "g")
                .step("Whisk the eggs, milk, sugar and cinnamon in a shallow dish.")
                .step("Dip each bread slice in the mixture, coating both sides.")
                .step("Fry in butter over medium heat for 2-3 minutes per side until golden."));

        list.add(new Recipe(0, "Pancakes", "Fluffy pancakes from basic baking staples.", 25)
                .ingredient("flour", 150, "g")
                .ingredient("milk", 250, "ml")
                .ingredient("egg", 1, "pcs")
                .ingredient("sugar", 2, "tbsp")
                .ingredient("baking powder", 2, "tsp")
                .ingredient("butter", 20, "g")
                .step("Mix the flour, sugar and baking powder in a bowl.")
                .step("Whisk in the milk and egg until smooth.")
                .step("Melt a little butter in a pan and pour in a ladle of batter.")
                .step("Cook until bubbles form, flip and cook for another minute."));

        list.add(new Recipe(0, "Tomato Garlic Pasta", "A simple fresh tomato pasta.", 25)
                .ingredient("pasta", 200, "g")
                .ingredient("tomato", 4, "pcs")
                .ingredient("garlic", 2, "pcs")
                .ingredient("olive oil", 2, "tbsp")
                .ingredient("salt", 0.5, "tsp")
                .ingredient("water", 2, "l")
                .step("Boil the pasta in salted water until al dente, then drain.")
                .step("Chop the tomatoes and slice the garlic.")
                .step("Fry the garlic in olive oil for 1 minute, add the tomatoes and cook for 8 minutes.")
                .step("Toss the pasta through the sauce and season."));

        list.add(new Recipe(0, "Garlic Butter Rice", "Fragrant rice that goes with anything.", 25)
                .ingredient("rice", 1, "cup")
                .ingredient("butter", 30, "g")
                .ingredient("garlic", 3, "pcs")
                .ingredient("salt", 0.5, "tsp")
                .ingredient("water", 500, "ml")
                .step("Melt the butter in a pot and fry the chopped garlic for 1 minute.")
                .step("Add the rice and stir to coat.")
                .step("Add the water and salt, bring to the boil, cover and simmer for 15 minutes.")
                .step("Rest for 5 minutes, then fluff with a fork."));

        list.add(new Recipe(0, "Vegetable Stir-Fry", "Quick, colourful vegetables with soy sauce.", 20)
                .ingredient("carrot", 2, "pcs")
                .ingredient("bell pepper", 1, "pcs")
                .ingredient("onion", 1, "pcs")
                .ingredient("garlic", 2, "pcs")
                .ingredient("soy sauce", 2, "tbsp")
                .ingredient("oil", 1, "tbsp")
                .step("Slice the carrots, pepper and onion into thin strips.")
                .step("Heat the oil in a pan or wok until very hot.")
                .step("Stir-fry the vegetables and garlic for 5-6 minutes.")
                .step("Add the soy sauce, toss and serve."));

        list.add(new Recipe(0, "Cheese Omelette", "A classic folded omelette.", 10)
                .ingredient("egg", 3, "pcs")
                .ingredient("cheese", 40, "g")
                .ingredient("milk", 2, "tbsp")
                .ingredient("butter", 10, "g")
                .ingredient("salt", 0.25, "tsp")
                .step("Whisk the eggs with the milk and salt.")
                .step("Melt the butter in a pan and pour in the eggs.")
                .step("When almost set, sprinkle the grated cheese over one half.")
                .step("Fold the omelette over and slide onto a plate."));

        list.add(new Recipe(0, "Cheese Toastie", "Crispy, melty toasted sandwich.", 10)
                .ingredient("bread", 2, "pcs")
                .ingredient("cheese", 50, "g")
                .ingredient("butter", 15, "g")
                .step("Butter the outside of both slices of bread.")
                .step("Place the cheese between the slices, buttered sides out.")
                .step("Toast in a pan for 2-3 minutes per side until golden and melted."));

        list.add(new Recipe(0, "Oven Potato Wedges", "Crispy paprika wedges.", 45)
                .ingredient("potato", 3, "pcs")
                .ingredient("oil", 2, "tbsp")
                .ingredient("salt", 1, "tsp")
                .ingredient("paprika", 1, "tsp")
                .step("Preheat the oven to 200 degrees C.")
                .step("Cut the potatoes into wedges and toss with oil, salt and paprika.")
                .step("Spread on a baking tray and roast for 35-40 minutes, turning halfway."));

        list.add(new Recipe(0, "Chakalaka", "Spicy South African vegetable relish.", 30)
                .ingredient("onion", 1, "pcs")
                .ingredient("carrot", 2, "pcs")
                .ingredient("bell pepper", 1, "pcs")
                .ingredient("tomato", 2, "pcs")
                .ingredient("baked beans", 410, "g")
                .ingredient("curry powder", 1, "tbsp")
                .ingredient("oil", 2, "tbsp")
                .step("Fry the chopped onion in oil until soft.")
                .step("Add the curry powder and cook for 1 minute.")
                .step("Add the grated carrot and chopped pepper and cook for 5 minutes.")
                .step("Stir in the chopped tomatoes and baked beans and simmer for 10 minutes."));

        list.add(new Recipe(0, "Stywe Pap", "Firm maize-meal porridge, a South African staple.", 30)
                .ingredient("maize meal", 2, "cup")
                .ingredient("salt", 1, "tsp")
                .ingredient("water", 750, "ml")
                .step("Bring the salted water to the boil.")
                .step("Add the maize meal all at once and do not stir for 5 minutes.")
                .step("Stir well with a wooden spoon until stiff and smooth.")
                .step("Cover and steam on low heat for 20 minutes, stirring now and then."));

        list.add(new Recipe(0, "Banana Smoothie", "Creamy breakfast smoothie.", 5)
                .ingredient("banana", 2, "pcs")
                .ingredient("milk", 250, "ml")
                .ingredient("yoghurt", 125, "ml")
                .ingredient("honey", 1, "tbsp")
                .step("Peel and slice the bananas.")
                .step("Blend everything until smooth.")
                .step("Serve cold."));

        list.add(new Recipe(0, "Oats Porridge", "Warm, filling oats with honey and cinnamon.", 10)
                .ingredient("oats", 1, "cup")
                .ingredient("milk", 500, "ml")
                .ingredient("honey", 1, "tbsp")
                .ingredient("cinnamon", 0.25, "tsp")
                .step("Bring the milk to a simmer in a pot.")
                .step("Stir in the oats and cook for 5 minutes, stirring often.")
                .step("Top with honey and a pinch of cinnamon."));

        list.add(new Recipe(0, "Chicken Curry", "A mild, comforting home-style curry.", 45)
                .ingredient("chicken breast", 500, "g")
                .ingredient("onion", 1, "pcs")
                .ingredient("tomato", 2, "pcs")
                .ingredient("garlic", 3, "pcs")
                .ingredient("curry powder", 2, "tbsp")
                .ingredient("oil", 2, "tbsp")
                .ingredient("salt", 1, "tsp")
                .step("Fry the chopped onion and garlic in oil until soft.")
                .step("Add the curry powder and cook for 1 minute.")
                .step("Add the cubed chicken and brown on all sides.")
                .step("Add the chopped tomatoes and salt, cover and simmer for 25 minutes."));

        list.add(new Recipe(0, "Tuna Pasta Salad", "Cold pasta salad for lunch boxes.", 20)
                .ingredient("pasta", 200, "g")
                .ingredient("tuna", 1, "pcs")
                .ingredient("mayonnaise", 3, "tbsp")
                .ingredient("spring onion", 2, "pcs")
                .ingredient("water", 2, "l")
                .step("Cook the pasta in boiling water, drain and cool under cold water.")
                .step("Drain the tuna and slice the spring onions.")
                .step("Mix everything with the mayonnaise and chill before serving."));

        list.add(new Recipe(0, "Greek Salad", "Fresh salad with feta and olive oil.", 10)
                .ingredient("tomato", 3, "pcs")
                .ingredient("cucumber", 1, "pcs")
                .ingredient("onion", 0.5, "pcs")
                .ingredient("feta cheese", 100, "g")
                .ingredient("olive oil", 2, "tbsp")
                .step("Chop the tomatoes and cucumber into chunks.")
                .step("Thinly slice the onion.")
                .step("Combine in a bowl, crumble over the feta and drizzle with olive oil."));

        list.add(new Recipe(0, "Spaghetti Bolognese", "Rich mince and tomato sauce over pasta.", 40)
                .ingredient("ground beef", 500, "g")
                .ingredient("pasta", 250, "g")
                .ingredient("onion", 1, "pcs")
                .ingredient("garlic", 2, "pcs")
                .ingredient("tomato", 4, "pcs")
                .ingredient("oil", 1, "tbsp")
                .ingredient("water", 2, "l")
                .step("Fry the chopped onion and garlic in oil until soft.")
                .step("Add the mince and cook until browned, breaking it up.")
                .step("Add the chopped tomatoes and simmer for 20 minutes.")
                .step("Meanwhile cook the pasta in boiling water, drain and serve with the sauce."));

        list.add(new Recipe(0, "Egg Fried Rice", "Great way to use leftover rice.", 20)
                .ingredient("rice", 1, "cup")
                .ingredient("egg", 2, "pcs")
                .ingredient("spring onion", 2, "pcs")
                .ingredient("peas", 100, "g")
                .ingredient("soy sauce", 2, "tbsp")
                .ingredient("oil", 1, "tbsp")
                .ingredient("water", 500, "ml")
                .step("Cook the rice in the water and let it cool (or use leftover rice).")
                .step("Scramble the eggs in hot oil, then push to one side.")
                .step("Add the rice and peas and stir-fry for 4 minutes.")
                .step("Add the soy sauce and sliced spring onion and toss together."));

        list.add(new Recipe(0, "Creamy Mashed Potatoes", "Smooth, buttery mash.", 30)
                .ingredient("potato", 4, "pcs")
                .ingredient("butter", 40, "g")
                .ingredient("milk", 100, "ml")
                .ingredient("salt", 0.5, "tsp")
                .ingredient("water", 1.5, "l")
                .step("Peel and cube the potatoes.")
                .step("Boil in salted water for 15-20 minutes until tender, then drain.")
                .step("Mash with the butter and warm milk until smooth."));

        list.add(new Recipe(0, "Fruit Salad", "Fresh fruit with yoghurt and honey.", 10)
                .ingredient("apple", 1, "pcs")
                .ingredient("banana", 2, "pcs")
                .ingredient("orange", 1, "pcs")
                .ingredient("yoghurt", 125, "ml")
                .ingredient("honey", 1, "tbsp")
                .step("Peel and chop all the fruit into bite-size pieces.")
                .step("Mix in a bowl.")
                .step("Serve topped with yoghurt and a drizzle of honey."));

        return list;
    }
}
