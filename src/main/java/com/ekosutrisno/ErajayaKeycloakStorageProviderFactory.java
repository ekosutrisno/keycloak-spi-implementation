package com.ekosutrisno;

import com.ekosutrisno.models.User;
import com.ekosutrisno.repositories.UserRepository;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.*;

/**
 * @author Eko Sutrisno
 * Selasa, 28/12/2021 11.40
 */
public class ErajayaKeycloakStorageProviderFactory implements UserStorageProviderFactory<ErajayaKeycloakStorageProvider> {
    public static final int PORT_LIMIT = 65535;
    public static final String PROVIDER_NAME_ID = "erajaya-spi-postgres-custom-storage";
    Map<String, String> properties;
    Map<String, EntityManagerFactory> entityManagerFactories = new HashMap<>();

    protected static final List<ProviderConfigProperty> configMetadata;

    public static final String DB_CONNECTION_NAME_KEY = "db:connectionName";
    public static final String DB_HOST_KEY = "db:host";
    public static final String DB_DATABASE_KEY = "db:database";
    public static final String DB_USERNAME_KEY = "db:username";
    public static final String DB_PASSWORD_KEY = "db:password";
    public static final String DB_PORT_KEY = "db:port";

    static {
        configMetadata = ProviderConfigurationBuilder.create()
                // Connection Name
                .property().name(DB_CONNECTION_NAME_KEY)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Connection Name")
                .defaultValue("erajaya-spi-postgres-custom-storage")
                .helpText("Name of the connection, can be chosen individually. Enables connection sharing between providers if the same name is provided. Overrides currently saved connection properties.")
                .add()

                // Connection Host
                .property().name(DB_HOST_KEY)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Database Host")
                .defaultValue("localhost")
                .helpText("Host of the connection")
                .add()

                // Connection Database
                .property().name(DB_DATABASE_KEY)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Database Name")
                .defaultValue("users")
                .helpText("Database of the user table storage")
                .add()

                // DB Username
                .property().name(DB_USERNAME_KEY)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Database Username")
                .defaultValue("postgres")
                .add()

                // DB Password
                .property().name(DB_PASSWORD_KEY)
                .type(ProviderConfigProperty.PASSWORD)
                .label("Database Password")
                .defaultValue("root")
                .add()

                // DB Port
                .property().name(DB_PORT_KEY)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Database Port")
                .defaultValue("5432")
                .add()
                .build();
    }

    @Override
    public String getId() {
        return PROVIDER_NAME_ID;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configMetadata;
    }

    @Override
    public ErajayaKeycloakStorageProvider create(KeycloakSession session, ComponentModel model) {
        properties = new HashMap<>();
        String dbConnectionName = model.getConfig().getFirst("db:connectionName");
        EntityManagerFactory entityManagerFactory = entityManagerFactories.get(dbConnectionName);

        if (entityManagerFactory == null) {
            MultivaluedHashMap<String, String> config = model.getConfig();
            properties.put("hibernate.connection.driver_class", "org.postgresql.Driver");
            properties.put("hibernate.connection.url",
                    String.format("jdbc:postgresql://%s:%s/%s",
                            config.getFirst(DB_HOST_KEY),
                            config.getFirst(DB_PORT_KEY),
                            config.getFirst(DB_DATABASE_KEY)));
            properties.put("hibernate.connection.username", config.getFirst(DB_USERNAME_KEY));
            properties.put("hibernate.connection.password", config.getFirst(DB_PASSWORD_KEY));
            properties.put("hibernate.show-sql", "true");
            properties.put("hibernate.archive.autodetection", "class, hbm");
            properties.put("hibernate.hbm2ddl.auto", "update");
            properties.put("hibernate.connection.autocommit", "true");

            entityManagerFactory = new HibernatePersistenceProvider().createContainerEntityManagerFactory(getPersistenceUnitInfo(), properties);
            entityManagerFactories.put(dbConnectionName, entityManagerFactory);
        }
        UserRepository userRepository = new UserRepository(entityManagerFactory.createEntityManager());
        return new ErajayaKeycloakStorageProvider(userRepository, session, model);
    }

    @Override
    public void onUpdate(KeycloakSession session, RealmModel realm, ComponentModel oldModel, ComponentModel newModel) {
        String oldCnName = oldModel.getConfig().getFirst(DB_CONNECTION_NAME_KEY);
        entityManagerFactories.remove(oldCnName);
        onCreate(session, realm, newModel);
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
        MultivaluedHashMap<String, String> configMap = config.getConfig();
        if (configMap.getFirst(DB_CONNECTION_NAME_KEY).isEmpty()) {
            throw new ComponentValidationException("Connection name empty.");
        }
        if (configMap.getFirst(DB_HOST_KEY).isEmpty()) {
            throw new ComponentValidationException("Database host empty.");
        }
        if (!isNumeric(configMap.getFirst(DB_PORT_KEY)) || Long.parseLong(configMap.getFirst(DB_PORT_KEY)) > PORT_LIMIT) {
            throw new ComponentValidationException("Invalid port. (Empty or NaN)");
        }
        if (configMap.getFirst(DB_DATABASE_KEY).isEmpty()) {
            throw new ComponentValidationException("Database name empty.");
        }
        if (configMap.getFirst(DB_USERNAME_KEY).isEmpty()) {
            throw new ComponentValidationException("Database username empty.");
        }
        if (configMap.getFirst(DB_PASSWORD_KEY).isEmpty()) {
            throw new ComponentValidationException("Database password empty.");
        }
    }

    private PersistenceUnitInfo getPersistenceUnitInfo() {
        return new PersistenceUnitInfo() {
            @Override
            public String getPersistenceUnitName() {
                return "erajaya_user_storage_postgresql";
            }

            @Override
            public String getPersistenceProviderClassName() {
                return "org.hibernate.jpa.HibernatePersistenceProvider";
            }

            @Override
            public PersistenceUnitTransactionType getTransactionType() {
                return PersistenceUnitTransactionType.RESOURCE_LOCAL;
            }

            @Override
            public DataSource getJtaDataSource() {
                return null;
            }

            @Override
            public DataSource getNonJtaDataSource() {
                return null;
            }

            @Override
            public List<String> getMappingFileNames() {
                return Collections.emptyList();
            }

            @Override
            public List<URL> getJarFileUrls() {
                try {
                    return Collections.list(this.getClass()
                            .getClassLoader()
                            .getResources(""));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public URL getPersistenceUnitRootUrl() {
                return null;
            }

            @Override
            public List<String> getManagedClassNames() {
                List<String> managedClasses = new LinkedList<>();
                managedClasses.add(User.class.getName());
                return managedClasses;
            }

            @Override
            public boolean excludeUnlistedClasses() {
                return false;
            }

            @Override
            public SharedCacheMode getSharedCacheMode() {
                return SharedCacheMode.UNSPECIFIED;
            }

            @Override
            public ValidationMode getValidationMode() {
                return ValidationMode.AUTO;
            }

            @Override
            public Properties getProperties() {
                return new Properties();
            }

            @Override
            public String getPersistenceXMLSchemaVersion() {
                return "2.2";
            }

            @Override
            public ClassLoader getClassLoader() {
                return null;
            }

            @Override
            public void addTransformer(ClassTransformer transformer) {
            }

            @Override
            public ClassLoader getNewTempClassLoader() {
                return null;
            }
        };
    }

    static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
