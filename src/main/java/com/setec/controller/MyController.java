package com.setec.controller;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;


import com.setec.repos.ProductRepo;
import com.setec.entities.PostProductDAO;
import com.setec.entities.Product;
import com.setec.entities.PutProductDAO;

@RestController
@RequestMapping("/api/product")
public class MyController {

    @Autowired
    private ProductRepo productRepo;
    //http://localhost:8080/api/product
    @GetMapping
    public ResponseEntity<?> getAllProducts() {

        List<Product> products = productRepo.findAll();

        if (products.isEmpty()) {
            return ResponseEntity
                    .status(204)
                    .body(Map.of("message", "Product list is empty"));
        }

        return ResponseEntity.ok(products);
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProduct(@ModelAttribute PostProductDAO postProduct) throws Exception {
    	String uploadDir = new File("myApp/static").getAbsolutePath();
    	File dir =new File(uploadDir);
    	if (!dir.exists()) {
    	    dir.mkdirs();
    	}
    	var file = postProduct.getFile();
    	String uniqueName = UUID.randomUUID()+"_"+file.getOriginalFilename();
    	String filePart = Paths.get(uploadDir,uniqueName).toString();
    	file.transferTo(new File(filePart));
    	
    	var product = new Product();
    	product.setName(postProduct.getName());
    	product.setPrice(postProduct.getPrice());
    	product.setQty(postProduct.getQty());
    	product.setImageUrl("/static/"+uniqueName);
    	productRepo.save(product);
    	
        return ResponseEntity.status(200).body(product);
        
    }
 // ✅ UPDATE product (with optional image upload)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> updateProduct(@PathVariable("id") Long id, @ModelAttribute PostProductDAO putProduct) throws Exception {
	    Optional<Product> existingProductOpt = productRepo.findById(id);
	    if (existingProductOpt.isEmpty()) {
	        return ResponseEntity.status(404).body(Map.of("message", "Product not found"));
	    }
	
	    Product existingProduct = existingProductOpt.get();
	
	    // Update basic fields
	    existingProduct.setName(putProduct.getName());
	    existingProduct.setPrice(putProduct.getPrice());
	    existingProduct.setQty(putProduct.getQty());
	
	    // Handle image upload if provided
	    if (putProduct.getFile() != null && !putProduct.getFile().isEmpty()) {
	        String uploadDir = new File("myApp/static").getAbsolutePath();
	        File dir = new File(uploadDir);
	        if (!dir.exists()) {
	            dir.mkdirs();
	        }
	
	        // ✅ Delete old image file if exists
	        if (existingProduct.getImageUrl() != null) {
	            String oldImagePath = Paths.get(uploadDir, 
	                new File(existingProduct.getImageUrl()).getName()).toString();
	            File oldFile = new File(oldImagePath);
	            if (oldFile.exists()) {
	                oldFile.delete();
	            }
	        }
	
	        // ✅ Save new image
	        var file = putProduct.getFile();
	        String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();
	        String filePart = Paths.get(uploadDir, uniqueName).toString();
	        file.transferTo(new File(filePart));
	
	        existingProduct.setImageUrl("/static/" + uniqueName);
	    }
	
	    productRepo.save(existingProduct);
	    return ResponseEntity.ok(existingProduct);
	}
    @GetMapping({"/{id}","/id/{id}"})
    public ResponseEntity<?> getProductById(@PathVariable("id") Long id) {
        Optional<Product> product = productRepo.findById(id);
        if (product.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Product not found"));
        }
        return ResponseEntity.ok(product.get());
    }
    @DeleteMapping({"/{id}","/id/{id}"})
    @Transactional
    public ResponseEntity<?> deleteProduct(@PathVariable("id") Long id) {
        Optional<Product> productOpt = productRepo.findById(id);
        if (productOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Product not found"));
        }

        Product product = productOpt.get();

        // Delete image file if exists
        if (product.getImageUrl() != null) {
            String uploadDir = new File("myApp/static").getAbsolutePath();
            String imagePath = Paths.get(uploadDir, new File(product.getImageUrl()).getName()).toString();
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                imageFile.delete();
            }
        }

        productRepo.delete(product); // ✅ safe inside transaction
        return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
    }


}




