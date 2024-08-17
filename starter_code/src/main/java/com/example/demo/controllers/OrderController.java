package com.example.demo.controllers;


import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;

@RestController
@RequestMapping("/api/order")
public class OrderController {


	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrderRepository orderRepository;

	private static final Logger logger = LogManager.getLogger(ItemController.class);

	@PostMapping("/submit/{username}")
	public ResponseEntity<UserOrder> submit(@PathVariable String username) {
		logger.info("Attempting to submit order for username: {}", username);
		User user = userRepository.findByUsername(username);
		if(user == null) {
			logger.warn("Failed to submit order: User not found for username: {}", username);
			return ResponseEntity.notFound().build();
		}
		UserOrder order = UserOrder.createFromCart(user.getCart());
		orderRepository.save(order);
		logger.info("Order successfully submitted for username: {}", username);
		return ResponseEntity.ok(order);
	}

	@GetMapping("/history/{username}")
	public ResponseEntity<List<UserOrder>> getOrdersForUser(@PathVariable String username) {
		logger.info("Fetching order history for username: {}", username);
		User user = userRepository.findByUsername(username);
		if(user == null) {
			logger.warn("Order history fetch failed: User not found for username: {}", username);
			return ResponseEntity.notFound().build();
		}
		List<UserOrder> orders = orderRepository.findByUser(user);
		if (orders.isEmpty()) {
			logger.info("No orders found for username: {}", username);
		} else {
			logger.info("Retrieved {} orders for username: {}", orders.size(), username);
		}
		return ResponseEntity.ok(orders);
	}
}