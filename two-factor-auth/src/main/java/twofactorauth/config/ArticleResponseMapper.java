package twofactorauth.config;

import org.modelmapper.Converter;
import org.modelmapper.PropertyMap;
import twofactorauth.dto.article.ArticleResponse;
import twofactorauth.entity.Article;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;

public class ArticleResponseMapper extends PropertyMap<Article, ArticleResponse> {

    @Override
    protected void configure() {
        using((Converter<Long, String>) mappingContext -> new SimpleDateFormat("dd-MM-yyyy")
                .format(Timestamp.from(Instant.ofEpochMilli(mappingContext.getSource()))))
                .map(source.getCreatedDate(), destination.getCreatedDate());
    }
}
