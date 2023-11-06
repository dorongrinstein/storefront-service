package com.sjsu.storefront.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.sjsu.storefront.common.AuthZCheck;
import com.sjsu.storefront.data.model.Image;
import com.sjsu.storefront.data.model.Product;
import com.sjsu.storefront.data.respository.ImageRepository;
import com.sjsu.storefront.data.respository.ProductRepository;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/items")
public class ProductController {

	  @Autowired
	  ProductRepository productRepository;
	  
	  @Autowired
	  private ImageRepository imageRepository;
	  
	  @Autowired
	  private ObjectMapper objectMapper;
	  
	  @Autowired
	  private HttpSession httpSession;
	  
	  @Operation(summary = "Get all items in the system")
	  @GetMapping
	  public List<Product> getAllItems() {
	      return (List<Product>) productRepository.findAll();
	  }
	
	  @Operation(summary = "Get a Item by id")
	  @GetMapping("/{id}")
	  public ResponseEntity<Product> getItemById(@PathVariable Long id) {
	      Product item = productRepository.findById(id).orElse(null);
	      if (item == null) {
	          return ResponseEntity.notFound().build();
	      }
	      return ResponseEntity.ok(item);
	  }
	  
	  @Operation(summary = "Create a NEW Item in the system")
	  @AuthZCheck // Apply the AuthAspect to this method
	  @PostMapping
	  public ResponseEntity<String> createItem(@RequestBody Product item) {

    	  List<Image> images = item.getImages();
	      imageRepository.saveAll(images);
	      item.setImages(images);
	      productRepository.save(item);
	      return ResponseEntity.created(null).body("Item created successfully");
	      
	  }
	
	  @Operation(summary = "Add a new Image to the Product")
	  @AuthZCheck // Apply the AuthAspect to this method
	  @PostMapping("{id}/images")
	  public ResponseEntity<String> addImage(@PathVariable Long id, @RequestBody Image image) {

		  Product existingItem = productRepository.findById(id).orElse(null);
	      if (existingItem == null) {
	          return ResponseEntity.notFound().build();
	      }
	      existingItem.addImage(image);
	      productRepository.save(existingItem);
	      return ResponseEntity.ok("Images Successfully added");
	      
	  }
	  
	  @Operation(summary = "Delete an Image from the Product, given image ID")
	  @AuthZCheck // Apply the AuthAspect to this method
	  @DeleteMapping("{id}/images/{imgId}")
	  public ResponseEntity<String> deleteImage(@PathVariable Long id,@PathVariable Long imgId, @RequestBody Image image) {

		  Product existingItem = productRepository.findById(id).orElse(null);
	      if (existingItem == null) {
	          return ResponseEntity.notFound().build();
	      }
	      existingItem.deleteImage(imgId);
	      productRepository.save(existingItem);
	      return ResponseEntity.ok("Images Successfully DELETED");
	      
	  }
	  
	  @Operation(summary = "Delete an item in the system given Item's id")
	  @AuthZCheck // Apply the AuthAspect to this method
	  @DeleteMapping("/{id}")
	  public ResponseEntity<String> deleteItem(@PathVariable Long id) {

	      productRepository.deleteById(id);
	      return ResponseEntity.noContent().build();
	  }
	  
	  @Operation(summary = "Update an item given Item's id, the whole Item object needs to be passed in the request")
	  @AuthZCheck // Apply the AuthAspect to this method
	  @PutMapping("/{id}")
	  public ResponseEntity<Product> updateItem(@PathVariable Long id, @RequestBody Product item) {
	      Product existingItem = productRepository.findById(id).orElse(null);
	      if (existingItem == null) {
	          return ResponseEntity.notFound().build();
	      }
	      existingItem.set(item);
	      productRepository.save(existingItem);
	      return ResponseEntity.ok(existingItem);
	  }
	  
	  
	  @Operation(summary = "Upadate an Item, given partial data in the Request")
	  @AuthZCheck // Apply the AuthAspect to this method
	  @PatchMapping("/{id}")
	  public ResponseEntity<Product> patchUser(@PathVariable Long id, @RequestBody JsonPatch patch) {
	      Product item = productRepository.findById(id).orElse(null);
	      if (item == null) {
	          return ResponseEntity.notFound().build();
	      }

	   // Apply the JSON patch to the user object
	  	try {
	  		
	      JsonNode itemNode = objectMapper.valueToTree(item);
	      JsonNode patchedNode = patch.apply(itemNode);
	      Product patchedItem;
	      patchedItem = objectMapper.treeToValue(patchedNode, Product.class);

	      // Save the updated user object to the database
	      productRepository.save(patchedItem);

	      // Return the updated user object
	      return ResponseEntity.ok(patchedItem);
	      
	  	} catch (JsonProcessingException | IllegalArgumentException e) {
	  		e.printStackTrace();
	  		return ResponseEntity.badRequest().build();
		} catch (JsonPatchException e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().build();
		} 
	  }
}