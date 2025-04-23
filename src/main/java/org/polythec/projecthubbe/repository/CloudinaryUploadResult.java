package org.polythec.projecthubbe.repository;

public class CloudinaryUploadResult {
    private String public_id;
    private String secure_url;

    // Getters and setters
    public String getPublicId() { return public_id; }

    public String getPublic_id() {
        return public_id;
    }

    public void setPublic_id(String public_id) {
        this.public_id = public_id;
    }

    public String getSecure_url() {
        return secure_url;
    }

    public void setSecure_url(String secure_url) {
        this.secure_url = secure_url;
    }

    public String getSecureUrl() { return secure_url; }
}