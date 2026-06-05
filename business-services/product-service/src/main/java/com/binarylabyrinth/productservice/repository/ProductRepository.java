package com.binarylabyrinth.productservice.repository;

import com.binarylabyrinth.productservice.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * ProductRepository - MongoDB Data Access Object
 * 
 * Extends MongoRepository to provide CRUD operations for Product entity.
 * MongoRepository automatically provides:
 * - save(Product): Create or update
 * - findById(String): Retrieve by primary key
 * - findAll(): Retrieve all documents
 * - delete(Product): Delete document
 * - count(): Count total documents
 * - exists(String): Check existence
 * 
 * Generic types:
 * - Product: Entity class
 * - String: ID type (MongoDB ObjectId represented as String)
 * 
 * Database: MongoDB
 * Collection: "products"
 * 
 * @author Binary Labyrinth
 * @version 1.0
 */
public interface ProductRepository extends MongoRepository<Product, String> {
}
