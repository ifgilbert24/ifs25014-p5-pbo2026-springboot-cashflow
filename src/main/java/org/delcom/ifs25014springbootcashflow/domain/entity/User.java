package org.delcom.ifs25014springbootcashflow.domain.entity;

public class User {

    private final int id;
    private final String username;
    private String passwordHash;
    private String fullName;
    private final Role role;
    private String photoPath;

    public User(
            int id,
            String username,
            String passwordHash,
            String fullName,
            Role role,
            String photoPath
    ) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.photoPath = photoPath;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public void changeFullName(String newFullName) {
        this.fullName = newFullName;
    }

    public void changePasswordHash(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void changePhotoPath(String newPhotoPath) {
        this.photoPath = newPhotoPath;
    }
}

