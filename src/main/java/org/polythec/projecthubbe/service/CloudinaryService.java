package org.polythec.projecthubbe.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.polythec.projecthubbe.repository.CloudinaryUploadResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryService() {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "duxnqtjbb",
                "api_key", "783549275919169",
                "api_secret", "a6oIPBSfRBqY6NYIn-CeXjd-vKs"
        ));
    }

    public CloudinaryUploadResult uploadFile(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return new CloudinaryUploadResult() {{
            setPublic_id((String) uploadResult.get("public_id"));
            setSecure_url((String) uploadResult.get("secure_url"));
        }};
    }
}