package desarrollador.api.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ImagenCloudService {
    private final Cloudinary cloudinary;

    public ImagenCloudService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String subirImagen(MultipartFile file) throws IOException {
        Map options = ObjectUtils.asMap(
                "folder", "productos_app",
                "resource_type", "image"
        );
        Map res = cloudinary.uploader().upload(file.getBytes(), options);
        Object url = res.get("secure_url");
        return url == null ? null : url.toString();
    }
}
