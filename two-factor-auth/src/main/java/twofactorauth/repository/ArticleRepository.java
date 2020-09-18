package twofactorauth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import twofactorauth.entity.Article;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, String> {

    List<Article> findAllByOrderByCreatedDateDesc();

    List<Article> findAllByUserEmailOrderByCreatedDateDesc(String email);
}
