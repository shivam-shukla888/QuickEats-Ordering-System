package com.quickeats.repository;

import com.quickeats.dto.SearchFilterDTO;
import com.quickeats.model.Menu;
import com.quickeats.model.Restaurant;
import com.quickeats.model.SpiceLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MenuSpecificationTest {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @BeforeEach
    void setUp() {
        menuRepository.deleteAll();
        restaurantRepository.deleteAll();

        Restaurant r1 = new Restaurant("Dragon Wok", "321 Dim Sum St", "Chinese");
        restaurantRepository.save(r1);

        Menu m1 = new Menu(r1, "Kung Pao Chicken", 10.99, "Spicy stir-fry with peanuts", false, SpiceLevel.HOT, "chinese,spicy,hot");
        menuRepository.save(m1);

        Menu m2 = new Menu(r1, "Spring Rolls", 5.99, "Crispy vegetable rolls", true, SpiceLevel.MILD, "veg,crispy,chinese");
        menuRepository.save(m2);
    }

    @Test
    void buildSpecification_FiltersByVegAndPrice() {
        SearchFilterDTO filter = new SearchFilterDTO(null, "Chinese", null, 8.00, true, SpiceLevel.MILD, null, List.of("rolls"));
        var spec = MenuSpecification.buildSpecification(filter);

        Page<Menu> result = menuRepository.findAll(spec, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Spring Rolls", result.getContent().get(0).getItemName());
    }

    @Test
    void buildSpecification_FiltersBySpiceLevel() {
        SearchFilterDTO filter = new SearchFilterDTO(null, null, null, 15.00, null, SpiceLevel.HOT, null, List.of("spicy"));
        var spec = MenuSpecification.buildSpecification(filter);

        Page<Menu> result = menuRepository.findAll(spec, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Kung Pao Chicken", result.getContent().get(0).getItemName());
    }
}
