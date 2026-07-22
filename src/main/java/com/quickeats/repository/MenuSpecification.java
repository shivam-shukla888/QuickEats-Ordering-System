package com.quickeats.repository;

import com.quickeats.dto.SearchFilterDTO;
import com.quickeats.model.Menu;
import com.quickeats.model.Restaurant;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MenuSpecification {

    public static Specification<Menu> buildSpecification(SearchFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return criteriaBuilder.conjunction();
            }

            Join<Menu, Restaurant> restaurantJoin = null;

            // 1. Restaurant Name filter
            if (filter.getRestaurantName() != null && !filter.getRestaurantName().trim().isEmpty()) {
                restaurantJoin = root.join("restaurant");
                String pattern = "%" + filter.getRestaurantName().trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(restaurantJoin.get("name")), pattern));
            }

            // 2. Cuisine Type filter (join with Restaurant)
            if (filter.getCuisineType() != null && !filter.getCuisineType().trim().isEmpty()) {
                if (restaurantJoin == null) {
                    restaurantJoin = root.join("restaurant");
                }
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(restaurantJoin.get("cuisineType")),
                        filter.getCuisineType().trim().toLowerCase()
                ));
            }

            // 3. Address Keywords filter
            if (filter.getAddressKeywords() != null && !filter.getAddressKeywords().trim().isEmpty()) {
                if (restaurantJoin == null) {
                    restaurantJoin = root.join("restaurant");
                }
                String pattern = "%" + filter.getAddressKeywords().trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(restaurantJoin.get("address")), pattern));
            }

            // 4. Price range filter
            if (filter.getMinPrice() != null && filter.getMinPrice() > 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null && filter.getMaxPrice() > 0) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }

            // 5. Is Veg filter
            if (filter.getIsVeg() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isVeg"), filter.getIsVeg()));
            }

            // 6. Spice Level filter
            if (filter.getSpiceLevel() != null) {
                predicates.add(criteriaBuilder.equal(root.get("spiceLevel"), filter.getSpiceLevel()));
            }

            // 7. Keywords filter (OR'd matches against itemName, description, tags with null safety)
            if (filter.getKeywords() != null && !filter.getKeywords().isEmpty()) {
                List<Predicate> keywordPredicates = new ArrayList<>();
                for (String kw : filter.getKeywords()) {
                    if (kw == null || kw.trim().isEmpty()) continue;
                    String pattern = "%" + kw.trim().toLowerCase() + "%";

                    Predicate matchItemName = criteriaBuilder.like(criteriaBuilder.lower(root.get("itemName")), pattern);
                    Predicate matchDesc = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.get("description"), "")), pattern);
                    Predicate matchTags = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.get("tags"), "")), pattern);

                    keywordPredicates.add(criteriaBuilder.or(matchItemName, matchDesc, matchTags));
                }

                if (!keywordPredicates.isEmpty()) {
                    predicates.add(criteriaBuilder.or(keywordPredicates.toArray(new Predicate[0])));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
