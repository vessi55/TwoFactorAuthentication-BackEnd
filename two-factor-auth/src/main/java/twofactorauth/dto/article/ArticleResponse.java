package twofactorauth.dto.article;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {

    private String uid;

    private String title;

    private String content;

    private String userFirstName;

    private String userLastName;

    private String createdDate;

    private Boolean isShown = false;
}
