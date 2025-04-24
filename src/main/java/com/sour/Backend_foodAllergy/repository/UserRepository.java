package com.sour.Backend_foodAllergy.repository;
import com.sour.Backend_foodAllergy.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface UserRepository extends MongoRepository<User, Long> {
}
