package com.ekosutrisno.repositories;

import com.ekosutrisno.models.UserEntity;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * @author Eko Sutrisno
 * Selasa, 28/12/2021 11.42
 */
public class UserEntityRepository {
    private final EntityManager entityManager;

    public UserEntityRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<UserEntity> findAll() {
        return findAll(null, null);
    }

    public List<UserEntity> findAll(int start, int max) {
        return findAll((Integer) start, (Integer) max);
    }

    public Optional<UserEntity> getUserByUsername(String username) {
        TypedQuery<UserEntity> query = entityManager.createNamedQuery("getUserEntityByUsername", UserEntity.class);
        query.setParameter("username", username);
        return query.getResultList().stream().findFirst();
    }

    public Optional<UserEntity> getUserByEmail(String email) {
        TypedQuery<UserEntity> query = entityManager.createNamedQuery("getUserEntityByEmail", UserEntity.class);
        query.setParameter("email", email);
        return query.getResultList().stream().findFirst();
    }

    public List<UserEntity> searchForUserByUsernameOrEmail(String searchString) {
        return searchForUserByUsernameOrEmail(searchString, null, null);
    }

    public List<UserEntity> searchForUserByUsernameOrEmail(String searchString, int start, int max) {
        return searchForUserByUsernameOrEmail(searchString, (Integer) start, (Integer) max);
    }

    public UserEntity getUserById(String id) {
        return entityManager.find(UserEntity.class, Long.parseLong(id));
    }

    public UserEntity createUser(UserEntity user) {
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();
        entityManager.persist(user);
        transaction.commit();

        return user;
    }

    public void deleteUser(UserEntity user) {
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();
        entityManager.remove(user);
        transaction.commit();
    }

    public void close() {
        entityManager.close();
    }

    public UserEntity updateUser(UserEntity userEntity) {
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();
        entityManager.merge(userEntity);
        transaction.commit();

        return userEntity;
    }

    public int size() {
        return entityManager.createNamedQuery("getUserEntityCount", Integer.class).getSingleResult();
    }


    /* This findAll For Local Private Implementation Class */
    private List<UserEntity> findAll(Integer start, Integer max) {
        TypedQuery<UserEntity> query = entityManager.createNamedQuery("searchForUserEntity", UserEntity.class);

        if (start != null)
            query.setFirstResult(start);

        if (max != null)
            query.setMaxResults(max);

        query.setParameter("search", "%");
        return query.getResultList();
    }

    /* This searchForUserByUsernameOrEmail For Local Private Implementation Class */
    private List<UserEntity> searchForUserByUsernameOrEmail(String searchString, Integer start, Integer max) {
        TypedQuery<UserEntity> query = entityManager.createNamedQuery("searchForUserEntity", UserEntity.class);
        query.setParameter("search", "%" + searchString + "%");

        if (start != null)
            query.setFirstResult(start);

        if (max != null)
            query.setMaxResults(max);

        return query.getResultList();
    }
}
