package com.ekosutrisno;

import com.ekosutrisno.models.UserEntity;
import com.ekosutrisno.repositories.UserEntityRepository;
import com.ekosutrisno.services.UserEntityRepresentationService;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.*;
import org.keycloak.models.cache.CachedUserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Eko Sutrisno
 * Selasa, 28/12/2021 11.39
 */
public class DepKeycloakStorageProvider implements UserStorageProvider,
        UserLookupProvider,
        UserRegistrationProvider,
        UserQueryProvider,
        CredentialInputUpdater,
        CredentialInputValidator {


    private final UserEntityRepository userRepository;
    KeycloakSession keycloakSession;
    ComponentModel componentModel;

    public DepKeycloakStorageProvider(UserEntityRepository userRepository, KeycloakSession keycloakSession, ComponentModel componentModel) {
        this.userRepository = userRepository;
        this.keycloakSession = keycloakSession;
        this.componentModel = componentModel;
    }

    @Override
    public void close() {
        userRepository.close();
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType) && getPassword(user) != null;
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        if (!(credentialInput instanceof UserCredentialModel)) return false;
        if (supportsCredentialType(credentialInput.getType())) {
            String password = getPassword(user);
            return password != null && password.equals(credentialInput.getChallengeResponse());
        } else {
            return false; // invalid cred type
        }
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean updateCredential(RealmModel realm, UserModel userModel, CredentialInput input) {
        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) return false;

        Optional<UserEntity> user = userRepository.getUserByUsername(userModel.getUsername());
        if (user.isPresent()) {
            user.get().setPassword(input.getChallengeResponse());
            userRepository.updateUser(user.get());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
        if (!supportsCredentialType(credentialType)) return;
        getUserRepresentation(user).setPassword(null);
    }

    @Override
    public Set<String> getDisableableCredentialTypes(RealmModel realm, UserModel user) {
        if (getUserRepresentation(user).getPassword() != null) {
            Set<String> set = new HashSet<>();
            set.add(PasswordCredentialModel.TYPE);
            return set;
        } else {
            return Collections.emptySet();
        }
    }

    public UserEntityRepresentationService getUserRepresentation(UserModel user) {
        UserEntityRepresentationService userRepresentation;
        if (user instanceof CachedUserModel) {
            userRepresentation = (UserEntityRepresentationService) ((CachedUserModel) user).getDelegateForUpdate();
        } else {
            userRepresentation = (UserEntityRepresentationService) user;
        }
        return userRepresentation;
    }

    public UserEntityRepresentationService getUserRepresentation(UserEntity user, RealmModel realm) {
        return new UserEntityRepresentationService(keycloakSession, realm, componentModel, user, userRepository);
    }

    @Override
    public int getUsersCount(RealmModel realm) {
        return userRepository.size();
    }

    @Override
    public List<UserModel> getUsers(RealmModel realm) {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserEntityRepresentationService(keycloakSession, realm, componentModel, user, userRepository))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserModel> getUsers(RealmModel realm, int firstResult, int maxResults) {
        return userRepository.findAll(firstResult, maxResults)
                .stream()
                .map(user -> new UserEntityRepresentationService(keycloakSession, realm, componentModel, user, userRepository))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserModel> searchForUser(String search, RealmModel realm) {
        return userRepository.searchForUserByUsernameOrEmail(search)
                .stream()
                .map(user -> new UserEntityRepresentationService(keycloakSession, realm, componentModel, user, userRepository))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserModel> searchForUser(String search, RealmModel realm, int firstResult, int maxResults) {
        return userRepository.searchForUserByUsernameOrEmail(search, firstResult, maxResults)
                .stream()
                .map(user -> new UserEntityRepresentationService(keycloakSession, realm, componentModel, user, userRepository))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm) {
        // TODO Will probably never implement; Only used by REST API
        return new ArrayList<>();
    }

    @Override
    public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm, int firstResult,
                                         int maxResults) {
        return userRepository.findAll(firstResult, maxResults)
                .stream()
                .map(user -> new UserEntityRepresentationService(keycloakSession, realm, componentModel, user, userRepository))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group, int firstResult, int maxResults) {
        // TODO Will probably never implement
        return new ArrayList<>();
    }

    @Override
    public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group) {
        // TODO Will probably never implement
        return new ArrayList<>();
    }

    @Override
    public List<UserModel> searchForUserByUserAttribute(String attrName, String attrValue, RealmModel realm) {
        // TODO Will probably never implement
        return new ArrayList<>();
    }

    @Override
    public UserModel getUserById(String keycloakId, RealmModel realm) {
        // keycloakId := keycloak internal id; needs to be mapped to external id
        String id = StorageId.externalId(keycloakId);
        return new UserEntityRepresentationService(keycloakSession, realm, componentModel, userRepository.getUserById(id), userRepository);
    }

    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {
        Optional<UserEntity> optionalUser = userRepository.getUserByUsername(username);
        return optionalUser.map(user -> getUserRepresentation(user, realm)).orElse(null);
    }

    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {
        Optional<UserEntity> optionalUser = userRepository.getUserByEmail(email);
        return optionalUser.map(user -> getUserRepresentation(user, realm)).orElse(null);
    }

    @Override
    public UserModel addUser(RealmModel realm, String username) {
        UserEntity user = new UserEntity();
        user.setName(username);
        user = userRepository.createUser(user);

        return new UserEntityRepresentationService(keycloakSession, realm, componentModel, user, userRepository);
    }

    @Override
    public boolean removeUser(RealmModel realm, UserModel user) {
        UserEntity userEntity = userRepository.getUserById(StorageId.externalId(user.getId()));
        if (userEntity == null) {
            return false;
        }
        userRepository.deleteUser(userEntity);
        return true;
    }

    /* Modification to get user Password */
    public String getPassword(UserModel user) {
        Optional<UserEntity> optionalUser = userRepository.getUserByUsername(user.getUsername());
        return optionalUser.map(UserEntity::getPassword).orElse(null);
    }

}
