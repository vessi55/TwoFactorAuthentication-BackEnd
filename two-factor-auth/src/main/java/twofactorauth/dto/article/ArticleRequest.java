package twofactorauth.dto.article;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotBlank
    private String userEmail;

    private MultipartFile image;
}
