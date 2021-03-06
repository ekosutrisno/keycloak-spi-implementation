package com.ekosutrisno;

import com.ekosutrisno.models.User;
import com.ekosutrisno.repositories.UserRepository;
import com.ekosutrisno.services.UserRepresentationService;
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

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Eko Sutrisno
 * Selasa, 28/12/2021 11.39
 */
public class ErajayaKeycloakStorageProvider implements UserStorageProvider,
        UserLookupProvider,
        UserRegistrationProvider,
        UserQueryProvider,
        CredentialInputUpdater,
        CredentialInputValidator {


    private final UserRepository userRepository;
    KeycloakSession keycloakSession;
    ComponentModel componentModel;

    public ErajayaKeycloakStorageProvider(UserRepository userRepository, KeycloakSession keycloakSession, ComponentModel componentModel) {
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
        User user = new User();
        user.setUsername(userModel.getUsername());
        user.setPassword(input.getChallengeResponse());
        userRepository.updateUser(user);
        return true;
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

    public UserRepresentationService getUserRepresentation(UserModel user) {
        UserRepresentationService userRepresentation;
        if (user instanceof CachedUserModel) {
            userRepresentation = (UserRepresentationService) ((CachedUserModel) user).getDelegateForUpdate();
        } else {
            userRepresentation = (UserRepresentationService) user;
        }
        return userRepresentation;
    }

    public UserRepresentationService getUserRepresentation(User user, RealmModel realm) {
        return new UserRepresentationService(keycloakSession, realm, componentModel, user, userRepository);
    }

    @Override
    public int getUsersCount(RealmModel realm) {
        return userRepository.size();
    }

    @Override
    public List<UserModel> getUsers(RealmModel realm) {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserRepresentationService(keycloakSession, realm, componentModel, user, userRepository))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserModel> getUsers(RealmModel realm, int firstResult, int maxResults) {
        return userRepository.findAll(firstResult, maxResults)
                .stream()
                .map(user -> new UserRepresentationService(keycloakSession, realm, componentModel, user, userRepository))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserModel> searchForUser(String search, RealmModel realm) {
        return userRepository.searchForUserByUsernameOrEmail(search)
                .stream()
                .map(user -> new UserRepresentationService(keycloakSession, realm, componentModel, user, userRepository))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserModel> searchForUser(String search, RealmModel realm, int firstResult, int maxResults) {
        return userRepository.searchForUserByUsernameOrEmail(search, firstResult, maxResults)
                .stream()
                .map(user -> new UserRepresentationService(keycloakSession, realm, componentModel, user, userRepository))
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
                .map(user -> new UserRepresentationService(keycloakSession, realm, componentModel, user, userRepository))
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
        return new UserRepresentationService(keycloakSession, realm, componentModel, userRepository.getUserById(id), userRepository);
    }

    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        return optionalUser.map(user -> getUserRepresentation(user, realm)).orElse(null);
    }

    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {
        Optional<User> optionalUser = userRepository.getUserByEmail(email);
        return optionalUser.map(user -> getUserRepresentation(user, realm)).orElse(null);
    }

    @Override
    public UserModel addUser(RealmModel realm, String username) {
        User user = new User();
        user.setUsername(username);
        user = userRepository.createUser(user);

        return new UserRepresentationService(keycloakSession, realm, componentModel, user, userRepository);
    }

    @Override
    public boolean removeUser(RealmModel realm, UserModel user) {
        User userEntity = userRepository.getUserById(StorageId.externalId(user.getId()));
        if (userEntity == null) {
            return false;
        }
        userRepository.deleteUser(userEntity);
        return true;
    }

    public String getPassword(UserModel user) {
        String password = null;
        if (user instanceof UserRepresentationService) {
            password = ((UserRepresentationService) user).getPassword();
        }
        return password;
    }

}
