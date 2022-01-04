package com.ekosutrisno.services;

import com.ekosutrisno.models.UserEntity;
import com.ekosutrisno.repositories.UserEntityRepository;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Eko Sutrisno
 * Selasa, 28/12/2021 11.45
 */
public class UserEntityRepresentationService extends AbstractUserAdapterFederatedStorage {
    private UserEntity userEntity;
    private final UserEntityRepository userRepository;

    public UserEntityRepresentationService(KeycloakSession session,
                                           RealmModel realm,
                                           ComponentModel storageProviderModel,
                                           UserEntity userEntity,
                                           UserEntityRepository userRepository) {
        super(session, realm, storageProviderModel);
        this.userEntity = userEntity;
        this.userRepository = userRepository;
    }


    @Override
    public String getUsername() {
        return userEntity.getName();
    }

    @Override
    public void setUsername(String username) {
        userEntity.setName(username);
        userRepository.updateUser(userEntity);
    }

    @Override
    public String getEmail() {
        return userEntity.getEmail();
    }

    @Override
    public void setEmail(String email) {
        userEntity.setEmail(email);
        userRepository.updateUser(userEntity);
    }

    @Override
    public void setSingleAttribute(String name, String value) {
        if (name.equals("isActive")) {
            userEntity.setIsActive(Byte.valueOf(value));
        } else {
            super.setSingleAttribute(name, value.toString());
        }
    }

    @Override
    public void removeAttribute(String name) {
        if (name.equals("isActive")) {
            userEntity.setIsActive((byte) 0);
        } else {
            super.removeAttribute(name);
        }
        userEntity = userRepository.updateUser(userEntity);
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        if (name.equals("isActive")) {
            userEntity.setIsActive(Byte.valueOf(values.get(0)));
        } else {
            super.setAttribute(name, values);
        }
        userEntity = userRepository.updateUser(userEntity);
    }

    @Override
    public String getFirstAttribute(String name) {
        if (name.equals("isActive")) {
            return userEntity.getIsActive().toString();
        } else {
            return super.getFirstAttribute(name);
        }
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        Map<String, List<String>> attrs = super.getAttributes();
        MultivaluedHashMap<String, String> all = new MultivaluedHashMap<>();
        all.putAll(attrs);
        all.add("isActive", userEntity.getIsActive().toString());
        return all;
    }

    @Override
    public List<String> getAttribute(String name) {
        if (name.equals("isActive")) {
            List<String> data = new LinkedList<>();
            data.add(userEntity.getIsActive().toString());
            return data;
        } else {
            return super.getAttribute(name);
        }
    }

    @Override
    public String getId() {
        return StorageId.keycloakId(storageProviderModel, userEntity.getId().toString());
    }

    public String getPassword() {
        return userEntity.getPassword();
    }

    public void setPassword(String password) {
        userEntity.setPassword(password);
        userEntity = userRepository.updateUser(userEntity);
    }
}
