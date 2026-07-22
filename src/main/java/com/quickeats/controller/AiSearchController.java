package com.quickeats.controller;

import com.quickeats.dto.MenuResponseDTO;
import com.quickeats.dto.SearchFilterDTO;
import com.quickeats.model.Menu;
import com.quickeats.repository.MenuRepository;
import com.quickeats.repository.MenuSpecification;
import com.quickeats.service.AiSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class AiSearchController {

    @Autowired
    private AiSearchService aiSearchService;

    @Autowired
    private MenuRepository menuRepository;

    @GetMapping
    public ResponseEntity<Page<MenuResponseDTO>> searchMenuItems(
            @RequestParam(required = false, defaultValue = "") String query,
            @PageableDefault(size = 8, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        SearchFilterDTO filter = aiSearchService.parseSearchQuery(query);
        Specification<Menu> spec = MenuSpecification.buildSpecification(filter);
        Page<MenuResponseDTO> results = menuRepository.findAll(spec, pageable)
                .map(MenuResponseDTO::fromEntity);

        return ResponseEntity.ok(results);
    }
}
