package com.quickeats.service;

import com.quickeats.dto.RestaurantRequestDTO;
import com.quickeats.dto.RestaurantResponseDTO;
import com.quickeats.exception.ResourceNotFoundException;
import com.quickeats.model.Restaurant;
import com.quickeats.repository.MenuRepository;
import com.quickeats.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant sampleRestaurant;

    @BeforeEach
    void setUp() {
        sampleRestaurant = new Restaurant("Pizza Palace", "123 Main St", "Italian");
        sampleRestaurant.setId(1L);
    }

    @Test
    void createRestaurant_Success() {
        RestaurantRequestDTO requestDTO = new RestaurantRequestDTO("Pizza Palace", "123 Main St", "Italian");
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(inv -> {
            Restaurant r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        RestaurantResponseDTO created = restaurantService.createRestaurant(requestDTO);

        assertNotNull(created);
        assertEquals(1L, created.getId());
        assertEquals("Pizza Palace", created.getName());
        assertEquals("Italian", created.getCuisineType());
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    @Test
    void getRestaurantById_Success() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(sampleRestaurant));

        RestaurantResponseDTO dto = restaurantService.getRestaurantById(1L);

        assertNotNull(dto);
        assertEquals("Pizza Palace", dto.getName());
    }

    @Test
    void getRestaurantById_NotFound_ThrowsException() {
        when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> restaurantService.getRestaurantById(99L));
    }
}
