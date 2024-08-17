package com.example.demo.controllers;


import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;

@RestController
@RequestMapping("/api/item")
public class ItemController {

	@Autowired
	private ItemRepository itemRepository;

	private static final Logger logger = LogManager.getLogger(ItemController.class);

	@GetMapping
	public ResponseEntity<List<Item>> getItems() {
		logger.info("Fetching all items");
		List<Item> items = itemRepository.findAll();
		if (items.isEmpty()) {
			logger.info("No items found");
		}
		return ResponseEntity.ok(items);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Item> getItemById(@PathVariable Long id) {
		logger.info("Fetching item by ID: {}", id);
		return ResponseEntity.of(itemRepository.findById(id));
	}

	@GetMapping("/name/{name}")
	public ResponseEntity<List<Item>> getItemsByName(@PathVariable String name) {
		logger.info("Fetching items by name: {}", name);
		List<Item> items = itemRepository.findByName(name);
		if (items == null || items.isEmpty()) {
			logger.warn("No items found with name: {}", name);
			return ResponseEntity.notFound().build();
		}
		logger.info("Found {} items with name: {}", items.size(), name);
		return ResponseEntity.ok(items);
	}

}