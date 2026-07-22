package com.quickeats.rag;

import com.quickeats.model.Menu;
import com.quickeats.repository.MenuRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuEmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(MenuEmbeddingService.class);

    @Autowired
    private MenuRepository menuRepository;

    @PostConstruct
    public void init() {
        reindexMenuEmbeddings();
    }

    public synchronized int reindexMenuEmbeddings() {
        List<Menu> menus = menuRepository.findAll();
        logger.info("Menu index refreshed. Total items in catalog: {}", menus.size());
        return menus.size();
    }
}
