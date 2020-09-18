package twofactorauth.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import twofactorauth.dto.article.ArticleRequest;
import twofactorauth.dto.article.ArticleResponse;
import twofactorauth.dto.invitation.InvitationResponse;
import twofactorauth.entity.Article;
import twofactorauth.entity.Invitation;
import twofactorauth.entity.User;
import twofactorauth.exception.ElementNotFoundException;
import twofactorauth.exception.MultipartFileImageException;
import twofactorauth.repository.ArticleRepository;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ArticleService {



    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ModelMapper modelMapper;

    public ArticleResponse addArticle(@Valid ArticleRequest articleRequest) throws IOException {

        imageService.validateImage(articleRequest.getImage());

        User user = userService.findUserByEmail(articleRequest.getUserEmail());
        Article article = new Article(articleRequest.getImage().getBytes(), articleRequest.getTitle(), articleRequest.getContent(), user);

        return modelMapper.map(articleRepository.save(article), ArticleResponse.class);
    }

    public Article getArticleById(String articleId) {
        Optional<Article> article = articleRepository.findById(articleId);
        return article.orElseThrow(() -> new ElementNotFoundException("Article does not exist !"));
    }

    public List<ArticleResponse> getAllArticles() {
        List<Article> articles = articleRepository.findAllByOrderByCreatedDateDesc();

        return getArticleResponses(articles);
    }

    public List<ArticleResponse> getMyArticles(String email) {
        List<Article> articles = articleRepository.findAllByUserEmailOrderByCreatedDateDesc(email);

        return getArticleResponses(articles);
    }

    private List<ArticleResponse> getArticleResponses(List<Article> articles) {
        return articles.stream()
                .map(article -> modelMapper.map(article, ArticleResponse.class))
                .collect(Collectors.toList());
    }
}
