package com.example.demo;

import com.example.demo.controllers.CartController;
import com.example.demo.controllers.ItemController;
import com.example.demo.controllers.OrderController;
import com.example.demo.controllers.UserController;
import com.example.demo.model.persistence.*;
import com.example.demo.model.persistence.repositories.*;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.ModifyCartRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SareetaApplicationTests {
	private CartController cartController;
	private ItemController itemController;
	private OrderController orderController;
	private UserController userController;

	private UserRepository userRepository = mock(UserRepository.class);
	private ItemRepository itemRepository = mock(ItemRepository.class);
	private CartRepository cartRepository = mock(CartRepository.class);
	private OrderRepository orderRepository = mock(OrderRepository.class);
	private BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);

	@Before
	public void setup() {
		cartController = new CartController();
		itemController = new ItemController();
		orderController = new OrderController();
		userController = new UserController();

		TestHelper.injectObjects(cartController, "userRepository", userRepository);
		TestHelper.injectObjects(cartController, "itemRepository", itemRepository);
		TestHelper.injectObjects(cartController, "cartRepository", cartRepository);
		TestHelper.injectObjects(itemController, "itemRepository", itemRepository);
		TestHelper.injectObjects(orderController, "userRepository", userRepository);
		TestHelper.injectObjects(orderController, "orderRepository", orderRepository);
		TestHelper.injectObjects(userController, "userRepository", userRepository);
		TestHelper.injectObjects(userController, "cartRepository", cartRepository);
		TestHelper.injectObjects(userController, "bCryptPasswordEncoder", encoder);
	}

	@Test
	public void testAddToCart() {
		when(userRepository.findByUsername("test")).thenReturn(getUser());
		when(itemRepository.findById(1L)).thenReturn(Optional.of(getItem1()));
		ModifyCartRequest request = new ModifyCartRequest();
		request.setUsername("test");
		request.setItemId(1L);
		request.setQuantity(1);

		ResponseEntity<Cart> response = cartController.addTocart(request);
		Assert.assertNotNull(response);
		Assert.assertEquals(200, response.getStatusCodeValue());
		Cart cart = response.getBody();
		Assert.assertTrue(cart.getItems().contains(getItem1()));

		request.setUsername("test 2");
		ResponseEntity<Cart> response2 = cartController.addTocart(request);
		Assert.assertEquals(404, response2.getStatusCodeValue());

		request.setUsername("test");
		request.setItemId(2L);
		ResponseEntity<Cart> response3 = cartController.addTocart(request);
		Assert.assertEquals(404, response3.getStatusCodeValue());
	}

	@Test
	public void testRemoveFromCart() {
		when(userRepository.findByUsername("test")).thenReturn(getUser());
		when(itemRepository.findById(2L)).thenReturn(Optional.of(getItem2()));
		ModifyCartRequest request = new ModifyCartRequest();
		request.setUsername("test");
		request.setItemId(2L);
		request.setQuantity(1);

		ResponseEntity<Cart> response = cartController.removeFromcart(request);
		Assert.assertNotNull(response);
		Assert.assertEquals(200, response.getStatusCodeValue());
		Cart cart = response.getBody();
		Assert.assertFalse(cart.getItems().contains(getItem2()));

		request.setUsername("test 2");
		ResponseEntity<Cart> response2 = cartController.removeFromcart(request);
		Assert.assertEquals(404, response2.getStatusCodeValue());

		request.setUsername("test");
		request.setItemId(3L);
		ResponseEntity<Cart> response3 = cartController.removeFromcart(request);
		Assert.assertEquals(404, response3.getStatusCodeValue());
	}

	@Test
	public void testGetAllItems() {
		when(itemRepository.findAll()).thenReturn(getItems());
		ResponseEntity<List<Item>> response = itemController.getItems();
		Assert.assertNotNull(response);
		Assert.assertEquals(200, response.getStatusCodeValue());
		Assert.assertArrayEquals(getItems().toArray(), response.getBody().toArray());
	}

	@Test
	public void testGetItemById() {
		when(itemRepository.findById(1L)).thenReturn(Optional.of(getItem1()));
		ResponseEntity<Item> response = itemController.getItemById(1L);
		Assert.assertNotNull(response);
		Assert.assertEquals(200, response.getStatusCodeValue());
		Assert.assertEquals(response.getBody().getId(), getItem1().getId());
	}

	@Test
	public void testSubmitOrder(){
		when(userRepository.findByUsername("test")).thenReturn(getUser());

		final ResponseEntity<UserOrder> response = orderController.submit("test");
		Assert.assertNotNull(response);
		Assert.assertEquals(200,response.getStatusCodeValue());
		UserOrder order = response.getBody();
		Assert.assertEquals(order.getUser().getId(),getUser().getId());
		Assert.assertTrue(order.getItems().contains(getItem1()));


		final ResponseEntity<UserOrder> response2 = orderController.submit("test 2");
		Assert.assertEquals(404,response2.getStatusCodeValue());

	}

	@Test
	public void testGetOrdersForUser() {
		User user = getUser();
		when(userRepository.findByUsername("test")).thenReturn(user);
		when(orderRepository.findByUser(user)).thenReturn(getUserOrders());

		ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("test");
		Assert.assertNotNull(response);
		Assert.assertEquals(200, response.getStatusCodeValue());
		Assert.assertFalse(response.getBody().isEmpty());

		ResponseEntity<List<UserOrder>> response2 = orderController.getOrdersForUser("test 2");
		Assert.assertEquals(404, response2.getStatusCodeValue());
	}

	@Test
	public void testCreateUser() {
		CreateUserRequest request = new CreateUserRequest();
		request.setUsername("test");
		request.setPassword("1234567");
		request.setConfirmPassword("1234567");

		ResponseEntity<User> response = userController.createUser(request);
		Assert.assertNotNull(response);
		Assert.assertEquals(200, response.getStatusCodeValue());

		User user = response.getBody();
		Assert.assertNotNull(user);
		Assert.assertEquals(0, user.getId());
		Assert.assertEquals("test", user.getUsername());
	}

	@Test
	public void testFindUserByUserName(){
		CreateUserRequest request = new CreateUserRequest();
		request.setUsername("test");
		request.setPassword("1234567");
		request.setConfirmPassword("1234567");

		final ResponseEntity<User> response1 = userController.createUser(request);
		Assert.assertEquals(200,response1.getStatusCodeValue());
		when(userRepository.findByUsername("test")).thenReturn(response1.getBody());
		final ResponseEntity<User> response2 = userController.findByUserName("test");
		Assert.assertNotNull(response2);
		Assert.assertEquals(200,response2.getStatusCodeValue());
		Assert.assertEquals("test",response2.getBody().getUsername());

	}
	@Test
	public void testFindByUserNameUserNotFound(){

		final ResponseEntity<User> response1 = userController.findByUserName("test");
		Assert.assertNotNull(response1);
		Assert.assertEquals(404,response1.getStatusCodeValue());
		Assert.assertNull(response1.getBody());

	}
	@Test
	public void testFindUserById(){
		CreateUserRequest request = new CreateUserRequest();
		request.setUsername("test");
		request.setPassword("1234567");
		request.setConfirmPassword("1234567");

		final ResponseEntity<User> response1 = userController.createUser(request);
		Assert.assertEquals(200,response1.getStatusCodeValue());

		when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(response1.getBody()));
		final ResponseEntity<User> response2 = userController.findById(1L);
		Assert.assertNotNull(response2);
		Assert.assertEquals(200,response2.getStatusCodeValue());
		Assert.assertEquals("test",response2.getBody().getUsername());

	}
	@Test
	public void testFindByIdUserNotFound(){

		final ResponseEntity<User> response1 = userController.findById(1L);
		Assert.assertNotNull(response1);
		Assert.assertEquals(404,response1.getStatusCodeValue());
		Assert.assertNull(response1.getBody());

	}

	@Test
	public void testCreateUserUnmatchedPassword(){
		CreateUserRequest request = new CreateUserRequest();
		request.setUsername("test");
		request.setPassword("1234567");
		request.setConfirmPassword("1234568");

		final ResponseEntity<User> response = userController.createUser(request);

		Assert.assertNotNull(response);
		Assert.assertEquals(400,response.getStatusCodeValue());

	}

	private User getUser(){
		User user = new User();
		user.setId(1L);
		user.setUsername("test");
		Cart cart= new Cart();
		cart.setUser(user);
		cart.addItem(getItem1());
		user.setCart(cart);
		return user;
	}


	private Item getItem1() {
		return new Item(1L, "item 1", "test item", BigDecimal.valueOf(10));
	}

	private Item getItem2() {
		return new Item(2L, "item 2", "test item 2", BigDecimal.valueOf(10));
	}

	private List<Item> getItems() {
		Item item1 = new Item(1L, "item 1", "test item", BigDecimal.valueOf(10));
		Item item2 = new Item(2L, "item 2", "test item 2", BigDecimal.valueOf(10));
		return Arrays.asList(item1, item2);
	}

	private List<UserOrder> getUserOrders() {
		UserOrder userOrder = new UserOrder();
		User user = getUser();
		userOrder.setUser(user);
		userOrder.setItems(user.getCart().getItems());
		userOrder.setTotal(user.getCart().getTotal());
		userOrder.setId(1L);
		return Arrays.asList(userOrder);
	}
}