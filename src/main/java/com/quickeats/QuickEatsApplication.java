package com.quickeats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.quickeats.model.Restaurant;
import com.quickeats.model.Menu;

@SpringBootApplication
public class QuickEatsApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuickEatsApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.CommandLineRunner demo(
            com.quickeats.repository.RestaurantRepository restaurantRepository,
            com.quickeats.repository.MenuRepository menuRepository) {
        return (args) -> {
            if (restaurantRepository.count() == 0) {
                // 1. Burger King (American)
                Restaurant r1 = new Restaurant();
                r1.setName("Burger King");
                r1.setCuisineType("American");
                r1.setAddress("123 Main St");
                restaurantRepository.save(r1);

                menuRepository.save(new Menu(null, "Whopper", 5.99, "Flame-grilled beef patty", r1));
                menuRepository.save(new Menu(null, "Chicken Fries", 3.99, "Crispy chicken strips", r1));
                menuRepository.save(new Menu(null, "Coke", 1.99, "Chilled soft drink", r1));

                // 2. Pizza Hut (Italian)
                Restaurant r2 = new Restaurant();
                r2.setName("Pizza Hut");
                r2.setCuisineType("Italian");
                r2.setAddress("456 Oak Ave");
                restaurantRepository.save(r2);

                menuRepository.save(new Menu(null, "Pepperoni Pizza", 12.99, "Classic pepperoni pizza", r2));
                menuRepository.save(new Menu(null, "Garlic Bread", 4.99, "Baked with garlic butter", r2));
                menuRepository.save(new Menu(null, "Pasta Alfredo", 8.99, "Creamy white sauce pasta", r2));

                // 3. Spice Villa (Indian)
                Restaurant r3 = new Restaurant();
                r3.setName("Spice Villa");
                r3.setCuisineType("Indian");
                r3.setAddress("789 Curry Lane");
                restaurantRepository.save(r3);

                menuRepository.save(new Menu(null, "Butter Chicken", 14.99, "Rich tomato and butter gravy", r3));
                menuRepository.save(new Menu(null, "Paneer Tikka", 11.99, "Grilled cottage cheese cubes", r3));
                menuRepository.save(new Menu(null, "Garlic Naan", 2.99, "Indian flatbread with garlic", r3));
                menuRepository.save(new Menu(null, "Biryani", 13.99, "Aromatic rice dish with spices", r3));

                // 4. Dragon Wok (Chinese)
                Restaurant r4 = new Restaurant();
                r4.setName("Dragon Wok");
                r4.setCuisineType("Chinese");
                r4.setAddress("321 Dim Sum St");
                restaurantRepository.save(r4);

                menuRepository.save(new Menu(null, "Kung Pao Chicken", 10.99, "Spicy stir-fry with peanuts", r4));
                menuRepository.save(new Menu(null, "Spring Rolls", 5.99, "Crispy vegetable rolls", r4));
                menuRepository.save(new Menu(null, "Fried Rice", 8.99, "Classic vegetable fried rice", r4));

                // 5. Sushi Spot (Japanese)
                Restaurant r5 = new Restaurant();
                r5.setName("Sushi Spot");
                r5.setCuisineType("Japanese");
                r5.setAddress("555 Fish Market Rd");
                restaurantRepository.save(r5);

                menuRepository.save(new Menu(null, "California Roll", 7.99, "Crab and avocado roll", r5));
                menuRepository.save(new Menu(null, "Spicy Tuna Roll", 8.99, "Fresh tuna with spicy mayo", r5));
                menuRepository.save(new Menu(null, "Miso Soup", 2.99, "Traditional soybean soup", r5));

                System.out.println("Data seeded with diverse restaurants!");
            }
        };
    }
}
