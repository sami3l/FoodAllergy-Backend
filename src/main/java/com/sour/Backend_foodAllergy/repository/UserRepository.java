package com.sour.Backend_foodAllergy.repository;
import com.sour.Backend_foodAllergy.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findById(String  Id);


    Optional<User> findByUsername(String username);
}
