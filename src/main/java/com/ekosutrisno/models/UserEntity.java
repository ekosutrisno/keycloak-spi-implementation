package com.ekosutrisno.models;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Eko Sutrisno
 * Selasa, 04/01/2022 09.43
 */
@NamedQueries({
        @NamedQuery(name = "getUserEntityByUsername", query = "select u from UserEntity u where u.name = :username"),
        @NamedQuery(name = "getUserEntityByEmail", query = "select u from UserEntity u where u.email = :email"),
        @NamedQuery(name = "getUserEntityCount", query = "select count(u) from UserEntity u"),
        @NamedQuery(name = "getAllUserEntities", query = "select u from UserEntity u"),
        @NamedQuery(name = "searchForUserEntity", query = "select u from UserEntity u where " +
                "( lower(u.name) like :search or u.email like :search ) order by u.name"),
})
@Entity
@Table(name = UserEntity.TABLE_NAME)
public class UserEntity {
    static final String TABLE_NAME = "users";
    private static final String DEFAULT_PASSWORD = "$2a$12$/Tk40V/uKBHL9G0MuYpF/ueKG73vp10CPPlSBc8pMR8zWVd382QxW"; // (123456)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "use_ldap", nullable = false)
    private Byte useLdap = 0;

    @Column(name = "is_active", nullable = false)
    private Byte isActive = 1;

    @Column(name = "email_verified_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date emailVerifiedAt;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "two_factor_secret")
    private String twoFactorSecret;

    @Column(name = "two_factor_recovery_codes")
    private String twoFactorRecoveryCodes;

    @Column(name = "remember_token", length = 100)
    private String rememberToken;

    @Column(name = "current_team_id")
    private Long currentTeamId;

    @Column(name = "profile_photo_path")
    private String profilePhotoPath;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    public UserEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Byte getUseLdap() {
        return useLdap;
    }

    public void setUseLdap(Byte useLdap) {
        this.useLdap = useLdap;
    }

    public Byte getIsActive() {
        return isActive;
    }

    public void setIsActive(Byte isActive) {
        this.isActive = isActive;
    }

    public Date getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(Date emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTwoFactorSecret() {
        return twoFactorSecret;
    }

    public void setTwoFactorSecret(String twoFactorSecret) {
        this.twoFactorSecret = twoFactorSecret;
    }

    public String getTwoFactorRecoveryCodes() {
        return twoFactorRecoveryCodes;
    }

    public void setTwoFactorRecoveryCodes(String twoFactorRecoveryCodes) {
        this.twoFactorRecoveryCodes = twoFactorRecoveryCodes;
    }

    public String getRememberToken() {
        return rememberToken;
    }

    public void setRememberToken(String rememberToken) {
        this.rememberToken = rememberToken;
    }

    public Long getCurrentTeamId() {
        return currentTeamId;
    }

    public void setCurrentTeamId(Long currentTeamId) {
        this.currentTeamId = currentTeamId;
    }

    public String getProfilePhotoPath() {
        return profilePhotoPath;
    }

    public void setProfilePhotoPath(String profilePhotoPath) {
        this.profilePhotoPath = profilePhotoPath;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    @PrePersist
    public void prePersist() {
        createdAt = new Date();
        updatedAt = createdAt;
        password = DEFAULT_PASSWORD;

    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = new Date();
    }

}
