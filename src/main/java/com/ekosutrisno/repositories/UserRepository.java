package com.ekosutrisno.repositories;

import com.ekosutrisno.models.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Eko Sutrisno
 * Selasa, 28/12/2021 11.42
 */
public class UserRepository {
    private final EntityManager entityManager;

    public UserRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<User> findAll() {
        return findAll(null, null);
    }

    public List<User> findAll(int start, int max) {
        return findAll((Integer) start, (Integer) max);
    }

    public Optional<User> getUserByUsername(String username) {
        TypedQuery<User> query = entityManager.createNamedQuery("getUserByUsername", User.class);
        query.setParameter("username", username);
        return query.getResultList().stream().findFirst();
    }

    public Optional<User> getUserByEmail(String email) {
        TypedQuery<User> query = entityManager.createNamedQuery("getUserByEmail", User.class);
        query.setParameter("email", email);
        return query.getResultList().stream().findFirst();
    }

    public List<User> searchForUserByUsernameOrEmail(String searchString) {
        return searchForUserByUsernameOrEmail(searchString, null, null);
    }

    public List<User> searchForUserByUsernameOrEmail(String searchString, int start, int max) {
        return searchForUserByUsernameOrEmail(searchString, (Integer) start, (Integer) max);
    }

    public User getUserById(String id) {
        return entityManager.find(User.class, UUID.fromString(id));
    }

    public User createUser(User user) {
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();
        entityManager.persist(user);
        transaction.commit();

        return user;
    }

    public void deleteUser(User user) {
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();
        entityManager.remove(user);
        transaction.commit();
    }

    public void close() {
        entityManager.close();
    }

    public User updateUser(User userEntity) {
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();
        entityManager.merge(userEntity);
        transaction.commit();

        return userEntity;
    }

    public int size() {
        return entityManager.createNamedQuery("getUserCount", Integer.class).getSingleResult();
    }


    /* This findAll For Local Private Implementation Class */
    private List<User> findAll(Integer start, Integer max) {
        TypedQuery<User> query = entityManager.createNamedQuery("searchForUser", User.class);

        if (start != null)
            query.setFirstResult(start);

        if (max != null)
            query.setMaxResults(max);

        query.setParameter("search", "%");
        return query.getResultList();
    }

    /* This searchForUserByUsernameOrEmail For Local Private Implementation Class */
    private List<User> searchForUserByUsernameOrEmail(String searchString, Integer start, Integer max) {
        TypedQuery<User> query = entityManager.createNamedQuery("searchForUser", User.class);
        query.setParameter("search", "%" + searchString + "%");

        if (start != null)
            query.setFirstResult(start);

        if (max != null)
            query.setMaxResults(max);

        return query.getResultList();
    }
}
