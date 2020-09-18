package twofactorauth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import twofactorauth.entity.Article;
import twofactorauth.exception.MultipartFileImageException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ImageService {

    public static final String IMAGE_IS_REQUIRED = "Image is required !";

    private List<String> validImageExtensions = Arrays.asList("jpg", "jpeg", "png");

    @Autowired
    private ArticleService articleService;

    public void validateImage(MultipartFile file) {
        String imageExtension = getExtension(file);

        validateExtension(imageExtension);
    }

    private String getExtension(MultipartFile file) {
        if (file == null) {
            throw new MultipartFileImageException(IMAGE_IS_REQUIRED);
        }
        String fileName = Optional.ofNullable(file.getOriginalFilename()).orElseThrow(() -> new MultipartFileImageException(IMAGE_IS_REQUIRED));
        String[] fileSplit = fileName.split("\\.");
        return fileSplit[fileSplit.length - 1];
    }

    private void validateExtension(String extension) {
        for (String validExtension : validImageExtensions) {
            if (validExtension.equalsIgnoreCase(extension)) {
                return;
            }
        }
        throw new MultipartFileImageException("Unsupported Image Format !");
    }

    public byte[] getArticleImage(String articleId) {
        Article article = articleService.getArticleById(articleId);
        return Optional.ofNullable(article.getImage()).orElseThrow(() -> new MultipartFileImageException(IMAGE_IS_REQUIRED));
    }
}
