package org.example.model;


public interface UserDAO {
    User getUserByUsernamePassword(String username,String password);
}
