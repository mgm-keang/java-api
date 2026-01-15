package com.setec.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.setec.entities.Product; 

public interface ProductRepo extends JpaRepository<Product, Integer>{

	Optional<Product> findById(Long id);

	void deleteById(Long id);
	
}



