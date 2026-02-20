package com.quickeats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.quickeats.model.Restaurant;
import com.quickeats.model.Menu;

@SpringBootApplication
@EnableTransactionManagement
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
                Restaurant r1 = new Restaurant("Burger King", "123 Main St", "American");
                restaurantRepository.save(r1);

                Menu m1 = new Menu("Whopper", 5.99, "Flame-grilled beef patty");
                m1.setRestaurant(r1);
                menuRepository.save(m1);
                
                Menu m2 = new Menu("Chicken Fries", 3.99, "Crispy chicken strips");
                m2.setRestaurant(r1);
                menuRepository.save(m2);
                
                Menu m3 = new Menu("Coke", 1.99, "Chilled soft drink");
                m3.setRestaurant(r1);
                menuRepository.save(m3);

                // 2. Pizza Hut (Italian)
                Restaurant r2 = new Restaurant("Pizza Hut", "456 Oak Ave", "Italian");
                restaurantRepository.save(r2);

                Menu m4 = new Menu("Pepperoni Pizza", 12.99, "Classic pepperoni pizza");
                m4.setRestaurant(r2);
                menuRepository.save(m4);
                
                Menu m5 = new Menu("Garlic Bread", 4.99, "Baked with garlic butter");
                m5.setRestaurant(r2);
                menuRepository.save(m5);
                
                Menu m6 = new Menu("Pasta Alfredo", 8.99, "Creamy white sauce pasta");
                m6.setRestaurant(r2);
                menuRepository.save(m6);

                // 3. Spice Villa (Indian)
                Restaurant r3 = new Restaurant("Spice Villa", "789 Curry Lane", "Indian");
                restaurantRepository.save(r3);

                Menu m7 = new Menu("Butter Chicken", 14.99, "Rich tomato and butter gravy");
                m7.setRestaurant(r3);
                menuRepository.save(m7);
                
                Menu m8 = new Menu("Paneer Tikka", 11.99, "Grilled cottage cheese cubes");
                m8.setRestaurant(r3);
                menuRepository.save(m8);
                
                Menu m9 = new Menu("Garlic Naan", 2.99, "Indian flatbread with garlic");
                m9.setRestaurant(r3);
                menuRepository.save(m9);
                
                Menu m10 = new Menu("Biryani", 13.99, "Aromatic rice dish with spices");
                m10.setRestaurant(r3);
                menuRepository.save(m10);

                // 4. Dragon Wok (Chinese)
                Restaurant r4 = new Restaurant("Dragon Wok", "321 Dim Sum St", "Chinese");
                restaurantRepository.save(r4);

                Menu m11 = new Menu("Kung Pao Chicken", 10.99, "Spicy stir-fry with peanuts");
                m11.setRestaurant(r4);
                menuRepository.save(m11);
                
                Menu m12 = new Menu("Spring Rolls", 5.99, "Crispy vegetable rolls");
                m12.setRestaurant(r4);
                menuRepository.save(m12);
                
                Menu m13 = new Menu("Fried Rice", 8.99, "Classic vegetable fried rice");
                m13.setRestaurant(r4);
                menuRepository.save(m13);

                // 5. Sushi Spot (Japanese)
                Restaurant r5 = new Restaurant("Sushi Spot", "555 Fish Market Rd", "Japanese");
                restaurantRepository.save(r5);

                Menu m14 = new Menu("California Roll", 7.99, "Crab and avocado roll");
                m14.setRestaurant(r5);
                menuRepository.save(m14);
                
                Menu m15 = new Menu("Spicy Tuna Roll", 8.99, "Fresh tuna with spicy mayo");
                m15.setRestaurant(r5);
                menuRepository.save(m15);
                
                Menu m16 = new Menu("Miso Soup", 2.99, "Traditional soybean soup");
                m16.setRestaurant(r5);
                menuRepository.save(m16);

                System.out.println("Data seeded with diverse restaurants!");
            }
        };
    }
}
