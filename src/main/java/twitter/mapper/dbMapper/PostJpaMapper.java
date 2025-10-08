package twitter.mapper.dbMapper;

import twitter.configuration.Component;
import twitter.dao.UserDAO;
import twitter.entity.post.Post;
import twitter.entity.post.PostJpaEntity;
import twitter.entity.tags.Tags;
import twitter.entity.user.User;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostJpaMapper {

    /**
     * Преобразует доменный объект Post в JPA-сущность PostJpaEntity.
     * @param domainPost доменный объект
     * @param userDAO DAO для поиска автора по ID
     * @return JPA-сущность, готовая к сохранению в БД
     */
    public PostJpaEntity toEntity(Post domainPost, UserDAO userDAO) {
        if (domainPost == null) {
            return null;
        }

        PostJpaEntity entity = new PostJpaEntity();

        // Преобразуем простые поля
        entity.setPostId(domainPost.getPostId() != null ? domainPost.getPostId().longValue() : null);
        entity.setTopic(domainPost.getTopic());
        entity.setText(domainPost.getText());
        entity.setCreatedAt(domainPost.getCreatedAt());

        // Находим автора по ID через UserDAO
        if (domainPost.getAuthorId() != 0) {
            try {
                User author = userDAO.getUserById(domainPost.getAuthorId());
                entity.setAuthor(author);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Преобразуем массив строк с тегами в список сущностей Tags
        if (domainPost.getTags() != null) {
            List<Tags> tagEntities = Arrays.stream(domainPost.getTags())
                    .map(String::trim)
                    .filter(tagName -> !tagName.isEmpty())
                    .map(tagName -> {
                        Tags tag = new Tags();
                        tag.setTagName(tagName);
                        return tag;
                    })
                    .map(Tags -> new Tags()) // Создаем новую сущность Tags для каждого имени тега
                    .collect(Collectors.toList());
            entity.setTags(tagEntities);
        }

        return entity;
    }

    /**
     * Преобразует JPA-сущность PostJpaEntity в доменный объект Post.
     * @param entity JPA-сущность из базы данных
     * @return "чистый" доменный объект
     */
    public Post toDomain(PostJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        Post domainPost = new Post();
        domainPost.setPostId(entity.getPostId() != null ? entity.getPostId().intValue() : null);

        if (entity.getAuthor() != null && entity.getAuthor().getId() != null) {
            // Безопасное преобразование Long в Integer
            domainPost.setAuthorId(entity.getAuthor().getId().intValue());
        }

        domainPost.setTopic(entity.getTopic());
        domainPost.setText(entity.getText());
        domainPost.setCreatedAt(entity.getCreatedAt());
        if (entity.getTags() != null) {
            domainPost.setTags(entity.getTags().stream().map(Tags::getTagName).toArray(String[]::new));
        } else {
            domainPost.setTags(new String[0]);
        }
        return domainPost;
    }
}
