package com.quickeats.controller;

import com.quickeats.model.Menu;
import com.quickeats.model.Order;
import com.quickeats.model.Restaurant;
import com.quickeats.model.User;
import com.quickeats.repository.MenuRepository;
import com.quickeats.repository.OrderRepository;
import com.quickeats.repository.RestaurantRepository;
import com.quickeats.service.GroqClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AiRecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroqClientService groqClientService;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private RestaurantRepository restaurantRepository;

    @MockBean
    private MenuRepository menuRepository;

    private Restaurant sampleRestaurant;
    private Menu sampleMenu1;
    private Menu sampleMenu2;
    private Menu sampleMenu3;
    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        sampleRestaurant = new Restaurant("Punjab Dhaba", "Delhi", "North Indian");
        sampleRestaurant.setId(1L);

        sampleMenu1 = new Menu(sampleRestaurant, "Butter Chicken", 14.99, "Rich tomato butter gravy");
        sampleMenu2 = new Menu(sampleRestaurant, "Dal Makhani", 9.99, "Slow cooked black lentils");
        sampleMenu3 = new Menu(sampleRestaurant, "Garlic Naan", 2.99, "Tandoori flatbread");

        User sampleUser = new User("Priya Singh", "priya@example.com", "password", "CUSTOMER");
        sampleUser.setId(2L);

        sampleOrder = new Order(sampleUser, sampleRestaurant, 24.98, "Butter Chicken, Garlic Naan");
        sampleOrder.setId(200L);

        when(restaurantRepository.findAll()).thenReturn(List.of(sampleRestaurant));
        when(menuRepository.findAll()).thenReturn(List.of(sampleMenu1, sampleMenu2, sampleMenu3));
    }

    @Test
    void testRecommendations_WithHistory_GroqSuccess() throws Exception {
        when(orderRepository.findByUserId(2L)).thenReturn(List.of(sampleOrder));

        String mockGroqJsonResponse = """
                [
                  {
                    "restaurantName": "Punjab Dhaba",
                    "itemName": "Dal Makhani",
                    "reason": "Complements your past order of Butter Chicken with rich vegetarian lentils."
                  },
                  {
                    "restaurantName": "Punjab Dhaba",
                    "itemName": "Garlic Naan",
                    "reason": "Your favorite side bread to pair with North Indian gravies."
                  },
                  {
                    "restaurantName": "Punjab Dhaba",
                    "itemName": "Butter Chicken",
                    "reason": "Matches your exact flavor profile based on previous order history."
                  }
                ]
                """;

        when(groqClientService.chatCompletion(anyString(), anyString())).thenReturn(mockGroqJsonResponse);

        mockMvc.perform(get("/api/recommendations/user/2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].restaurantName", is("Punjab Dhaba")))
                .andExpect(jsonPath("$[0].itemName", is("Dal Makhani")));
    }

    @Test
    void testRecommendations_NoHistory_Fallback() throws Exception {
        when(orderRepository.findByUserId(99L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/recommendations/user/99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].restaurantName", is("Punjab Dhaba")))
                .andExpect(jsonPath("$[0].itemName", is("Butter Chicken")));
    }

    @Test
    void testRecommendations_GroqError_Fallback() throws Exception {
        when(orderRepository.findByUserId(2L)).thenReturn(List.of(sampleOrder));
        when(groqClientService.chatCompletion(anyString(), anyString())).thenThrow(new RuntimeException("Groq Service Unavailable"));

        mockMvc.perform(get("/api/recommendations/user/2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].restaurantName", is("Punjab Dhaba")));
    }
}
