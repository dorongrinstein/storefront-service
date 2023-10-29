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
import com.sjsu.storefront.data.model.Image;
import com.sjsu.storefront.data.model.Item;
import com.sjsu.storefront.data.respository.ImageRepository;
import com.sjsu.storefront.data.respository.ItemRepository;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/items")
public class ItemController {

	  @Autowired
	  ItemRepository itemRepository;
	  
	  @Autowired
	  private ImageRepository imageRepository;
	  
	  @Autowired
	  private ObjectMapper objectMapper;
	  
	  @Operation(summary = "Get all items in the system")
	  @GetMapping
	  public List<Item> getAllItems() {
	      return (List<Item>) itemRepository.findAll();
	  }
	
	  @Operation(summary = "Get a Item by id")
	  @GetMapping("/{id}")
	  public ResponseEntity<Item> getItemById(@PathVariable Long id) {
	      Item item = itemRepository.findById(id).orElse(null);
	      if (item == null) {
	          return ResponseEntity.notFound().build();
	      }
	      return ResponseEntity.ok(item);
	  }
	  
	  @Operation(summary = "Create a NEW Item in the system")
	  @PostMapping
	  public ResponseEntity<Item> createItem(@RequestBody Item item) {
		  //log.info("Creating item : " + item);
		  
	      List<Image> images = item.getImages();
	      imageRepository.saveAll(images);
	      item.setImages(images);
	      itemRepository.save(item);
	      return ResponseEntity.created(null).body(item);
	  }
	  
	  @Operation(summary = "Delete an item in the system given Item's id")
	  @DeleteMapping("/{id}")
	  public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
	      itemRepository.deleteById(id);
	      return ResponseEntity.noContent().build();
	  }
	  
	  @Operation(summary = "Update an item given Item's id, the whole Item object needs to be passed in the request")
	  @PutMapping("/{id}")
	  public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
	      Item existingItem = itemRepository.findById(id).orElse(null);
	      if (existingItem == null) {
	          return ResponseEntity.notFound().build();
	      }
	      existingItem.set(item);
	      itemRepository.save(existingItem);
	      return ResponseEntity.ok(existingItem);
	  }
	  
	  @Operation(summary = "Upadate an Item, given partial data in the Request")
	  @PatchMapping("/{id}")
	  public ResponseEntity<Item> patchUser(@PathVariable Long id, @RequestBody JsonPatch patch) {
	      Item item = itemRepository.findById(id).orElse(null);
	      if (item == null) {
	          return ResponseEntity.notFound().build();
	      }

	   // Apply the JSON patch to the user object
	  	try {
	  		
	      JsonNode itemNode = objectMapper.valueToTree(item);
	      JsonNode patchedNode = patch.apply(itemNode);
	      Item patchedItem;
	      patchedItem = objectMapper.treeToValue(patchedNode, Item.class);

	      // Save the updated user object to the database
	      itemRepository.save(patchedItem);

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
