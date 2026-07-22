package com.quickeats;

import com.quickeats.model.SpiceLevel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.quickeats.model.Restaurant;
import com.quickeats.model.Menu;

@SpringBootApplication
@EnableTransactionManagement
@org.springframework.scheduling.annotation.EnableScheduling
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
                // 1. Punjab Dhaba (North Indian)
                Restaurant r1 = new Restaurant("Punjab Dhaba", "12 Lal Chowk, Delhi", "North Indian");
                restaurantRepository.save(r1);

                Menu m1 = new Menu(r1, "Butter Chicken", 14.99, "Rich tomato & creamy butter gravy with tender chicken", false, SpiceLevel.MEDIUM, "curry,chicken,creamy,popular,punjabi,north indian");
                menuRepository.save(m1);

                Menu m2 = new Menu(r1, "Dal Makhani", 9.99, "Slow-cooked black lentils with butter and fresh cream", true, SpiceLevel.MILD, "veg,dal,creamy,popular,north indian");
                menuRepository.save(m2);

                Menu m3 = new Menu(r1, "Paneer Butter Masala", 11.99, "Cottage cheese cubes in rich tomato cashew gravy", true, SpiceLevel.MEDIUM, "veg,paneer,curry,popular,north indian");
                menuRepository.save(m3);

                Menu m4 = new Menu(r1, "Amritsari Stuffed Kulcha", 4.99, "Crispy tandoori flatbread stuffed with spiced potato & chole", true, SpiceLevel.MEDIUM, "veg,bread,snack,starter,punjabi");
                menuRepository.save(m4);

                Menu m5 = new Menu(r1, "Garlic Naan", 2.99, "Tandoori flatbread brushed with garlic butter", true, SpiceLevel.MILD, "veg,bread,tandoori");
                menuRepository.save(m5);

                // 2. Pind Balluchi (North Indian & Tandoori)
                Restaurant r2 = new Restaurant("Pind Balluchi", "45 Mall Road, Chandigarh", "North Indian");
                restaurantRepository.save(r2);

                Menu m6 = new Menu(r2, "Tandoori Chicken", 13.99, "Whole chicken marinated in yogurt & spices roasted in clay oven", false, SpiceLevel.HOT, "chicken,tandoori,spicy,starter,popular,north indian");
                menuRepository.save(m6);

                Menu m7 = new Menu(r2, "Malai Kofta", 12.99, "Soft paneer & potato dumplings in rich white cashew cream", true, SpiceLevel.MILD, "veg,paneer,creamy,popular");
                menuRepository.save(m7);

                Menu m8 = new Menu(r2, "Chicken Tikka Masala", 14.49, "Char-grilled chicken tikka cooked in spicy onion gravy", false, SpiceLevel.HOT, "chicken,spicy,curry,tandoori");
                menuRepository.save(m8);

                Menu m9 = new Menu(r2, "Sweet Punjabi Lassi", 3.49, "Chilled thick yogurt drink topped with malai", true, SpiceLevel.MILD, "beverage,sweet,cold,punjabi");
                menuRepository.save(m9);

                // 3. Haldiram's Express (North Indian Snacks & Street Food)
                Restaurant r3 = new Restaurant("Haldiram's Express", "78 Connaught Place, New Delhi", "North Indian Street Food");
                restaurantRepository.save(r3);

                Menu m10 = new Menu(r3, "Chole Bhature", 8.99, "Spicy Punjabi chickpea curry served with hot fluffy bhaturas", true, SpiceLevel.MEDIUM, "veg,chole,popular,streetfood,breakfast,north indian");
                menuRepository.save(m10);

                Menu m11 = new Menu(r3, "Rajma Chawal", 7.99, "Classic North Indian red kidney bean curry with steamed basmati rice", true, SpiceLevel.MEDIUM, "veg,rice,curry,homefood,popular");
                menuRepository.save(m11);

                Menu m12 = new Menu(r3, "Samosa Chaat", 5.99, "Crispy samosas crushed and topped with tangy chole & chutney", true, SpiceLevel.MEDIUM, "veg,snack,starter,chaat,streetfood");
                menuRepository.save(m12);

                Menu m13 = new Menu(r3, "Gulab Jamun", 3.99, "Warm golden milk dumplings soaked in rose cardamom syrup", true, SpiceLevel.MILD, "dessert,sweet,popular");
                menuRepository.save(m13);

                // 4. Royal Mughlai Kitchen (Mughlai & Biryani)
                Restaurant r4 = new Restaurant("Royal Mughlai Kitchen", "101 Aminabad, Lucknow", "Mughlai & Biryani");
                restaurantRepository.save(r4);

                Menu m14 = new Menu(r4, "Lucknowi Chicken Dum Biryani", 13.99, "Aromatic basmati rice layered with marinated chicken & Mughlai spices", false, SpiceLevel.HOT, "biryani,rice,spicy,chicken,popular,mughlai");
                menuRepository.save(m14);

                Menu m15 = new Menu(r4, "Mutton Rogan Josh", 16.99, "Authentic Kashmiri tender lamb curry infused with aromatic herbs", false, SpiceLevel.HOT, "lamb,mutton,spicy,curry,mughlai");
                menuRepository.save(m15);

                Menu m16 = new Menu(r4, "Shahi Paneer", 12.49, "Cottage cheese cooked in royal Mughlai saffron and nut gravy", true, SpiceLevel.MILD, "veg,paneer,creamy,shahi,mughlai");
                menuRepository.save(m16);

                System.out.println("Data seeded with authentic North Indian restaurants and dishes!");
            }
        };
    }
}
