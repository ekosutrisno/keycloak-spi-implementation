package com.ekosutrisno.services;

import com.ekosutrisno.models.User;
import com.ekosutrisno.repositories.UserRepository;
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
public class UserRepresentationService extends AbstractUserAdapterFederatedStorage {
    private User userEntity;
    private final UserRepository userRepository;

    public UserRepresentationService(KeycloakSession session,
                                     RealmModel realm,
                                     ComponentModel storageProviderModel,
                                     User userEntity,
                                     UserRepository userRepository) {
        super(session, realm, storageProviderModel);
        this.userEntity = userEntity;
        this.userRepository = userRepository;
    }


    @Override
    public String getUsername() {
        return userEntity.getUsername();
    }

    @Override
    public void setUsername(String username) {
        userEntity.setUsername(username);
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
        if (name.equals("phone")) {
            userEntity.setPhone(value);
        } else {
            super.setSingleAttribute(name, value);
        }
    }

    @Override
    public void removeAttribute(String name) {
        if (name.equals("phone")) {
            userEntity.setPhone(null);
        } else {
            super.removeAttribute(name);
        }
        userEntity = userRepository.updateUser(userEntity);
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        if (name.equals("phone")) {
            userEntity.setPhone(values.get(0));
        } else {
            super.setAttribute(name, values);
        }
        userEntity = userRepository.updateUser(userEntity);
    }

    @Override
    public String getFirstAttribute(String name) {
        if (name.equals("phone")) {
            return userEntity.getPhone();
        } else {
            return super.getFirstAttribute(name);
        }
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        Map<String, List<String>> attrs = super.getAttributes();
        MultivaluedHashMap<String, String> all = new MultivaluedHashMap<>();
        all.putAll(attrs);
        all.add("phone", userEntity.getPhone());
        return all;
    }

    @Override
    public List<String> getAttribute(String name) {
        if (name.equals("phone")) {
            List<String> phone = new LinkedList<>();
            phone.add(userEntity.getPhone());
            return phone;
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
