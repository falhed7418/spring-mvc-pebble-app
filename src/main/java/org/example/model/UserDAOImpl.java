package org.example.model;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class UserDAOImpl implements UserDAO {
    private EntityManager entityManager;

    public UserDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public User getUserByUsernamePassword(String username, String password) {
        String sql = "select u from User u where u.username = :username and u.password = :password";
//        join fetch u.roles rs
        Query query = entityManager.createQuery(sql, User.class);
        query.setParameter("username", username);
        query.setParameter("password", password);
//        javax.persistence.NoResultException
        return (User) query.getSingleResult();
    }
}
