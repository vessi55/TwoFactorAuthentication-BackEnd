package twofactorauth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twofactorauth.dto.article.ArticleRequest;
import twofactorauth.dto.article.ArticleResponse;
import twofactorauth.entity.Article;
import twofactorauth.service.ArticleService;
import twofactorauth.service.ImageService;

import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ImageService imageService;

    @PostMapping
    public ResponseEntity<ArticleResponse> addArticle(@ModelAttribute ArticleRequest articleRequest) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(articleService.addArticle(articleRequest));
    }

    @GetMapping
    public ResponseEntity<List<ArticleResponse>> getAllArticles() {
        return ResponseEntity.status(HttpStatus.OK).body(articleService.getAllArticles());
    }

    @GetMapping("/{email}")
    public ResponseEntity<List<ArticleResponse>> getMyArticles(@PathVariable("email") String email) {
        return ResponseEntity.status(HttpStatus.OK).body(articleService.getMyArticles(email));
    }

    @GetMapping(value = "/image/{articleId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> downloadArticleImage(@PathVariable(value = "articleId") String articleId) {
        return ResponseEntity.ok(imageService.getArticleImage(articleId));
    }

    @GetMapping("/id/{articleId}")
    public ResponseEntity<Article> getArticleById(@PathVariable("articleId") String articleId) {
        return ResponseEntity.ok(articleService.getArticleById(articleId));
    }
}
