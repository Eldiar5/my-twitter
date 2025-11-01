package twitter.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter.configuration.Profile;
import twitter.dao.UserDAO;
import twitter.entity.post.PostJpaEntity;
import twitter.entity.tags.Tags;
import twitter.mapper.v1.dbMapper.PostJpaMapper;
import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.dao.PostDAO;
import twitter.entity.post.Post;
import twitter.exceptions.DataAccessException;
import twitter.exceptions.PostNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Profile(active = {"default", "prod"})
public class HibernatePostDAO implements PostDAO {

    private static final Logger logger = LoggerFactory.getLogger(HibernatePostDAO.class);

    private final EntityManagerFactory entityManagerFactory;
    private final PostJpaMapper postJpaMapper;
    private final UserDAO userDAO;

    @Injection
    public HibernatePostDAO(EntityManagerFactory entityManagerFactory, PostJpaMapper postJpaMapper, UserDAO userDAO) {
        this.entityManagerFactory = entityManagerFactory;
        this.postJpaMapper = postJpaMapper;
        this.userDAO = userDAO;
    }

    private Tags findOrCreateTagByName(EntityManager entityManager, String name) {
        try {
            TypedQuery<Tags> query = entityManager
                    .createQuery("SELECT t FROM Tags t WHERE t.tagName = :name", Tags.class);
            query.setParameter("name", name);

            return query.getSingleResult();

        } catch (NoResultException ex) {
            Tags tags = new Tags();
            tags.setTagName(name);
            entityManager.persist(tags);
            return tags;
        }
    }

    @Override
    public Post saveNewPost(Post domainPost) {
        logger.info("Сохранение нового поста от пользователя с ID: {}", domainPost.getAuthor().getId());
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {
                entityManager.getTransaction().begin();
                PostJpaEntity entityToSave = postJpaMapper.toEntity(domainPost, userDAO);

                if (!Objects.isNull(domainPost.getTags()) && domainPost.getTags().length > 0) {

                    List<Tags> managedTags = Arrays.stream(domainPost.getTags())
                            .map(String::trim)
                            .filter(tagName -> !tagName.isEmpty())
                            .map(tagName -> findOrCreateTagByName(entityManager, tagName)) // <-- Используем наш helper
                            .toList();

                    entityToSave.setTags(managedTags);
                }

                entityManager.persist(entityToSave);
                entityManager.getTransaction().commit();

                Post savedDomainPost = postJpaMapper.toDomain(entityToSave);
                logger.info("Пост успешно сохранен с ID: {}", savedDomainPost.getPostId());
                return savedDomainPost;
            } catch (Exception ex) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                logger.error("Ошибка при сохранении поста от пользователя с ID: {}", domainPost.getAuthor().getId(), ex);
                throw new DataAccessException("Ошибка при сохранении поста", ex);
            }
        }
    }

    @Override
    public List<Post> getAllPosts() {
        logger.info("Получение всех постов");
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            List<PostJpaEntity> entities = entityManager.createQuery("select p from PostJpaEntity p ORDER BY p.createdAt DESC", PostJpaEntity.class).getResultList();
            List<Post> posts = entities.stream()
                    .map(postJpaMapper::toDomain)
                    .collect(Collectors.toList());

            logger.info("Найдено {} постов", posts.size());
            return posts;
        } catch (Exception ex) {
            logger.error("Ошибка при получении всех постов", ex);
            throw new DataAccessException("Ошибка при получении всех постов", ex);
        }
    }

    @Override
    public List<Post> getAllPostsByUser(Long userId) {
        logger.info("Получение всех постов для пользователя с ID: {}", userId);
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            TypedQuery<PostJpaEntity> query = entityManager
                    // Обращаемся к полю author, а через точку - к его полю id
                    .createQuery("select p from PostJpaEntity p where p.author.id = :authorId ORDER BY p.createdAt DESC", PostJpaEntity.class);

            query.setParameter("authorId", userId);
            List<PostJpaEntity> entities = query.getResultList();
            List<Post> posts = entities.stream()
                    .map(postJpaMapper::toDomain)
                    .collect(Collectors.toList());

            logger.info("Найдено {} постов для пользователя с ID: {}", posts.size(), userId);
            return posts;
        } catch (Exception ex) {
            logger.error("Ошибка при получении постов для пользователя с ID: {}", userId, ex);
            throw new DataAccessException("Ошибка при получении постов для пользователя: " + userId, ex);
        }
    }

    @Override
    public List<Post> getAllPostsByTag(String tag) {
        logger.info("Получение всех постов по тегу: '{}'", tag);
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {

            // criteriaAPI query
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<PostJpaEntity> query = builder.createQuery(PostJpaEntity.class);
            Root<PostJpaEntity> root = query.from(PostJpaEntity.class);
            Join<PostJpaEntity, Tags> tagJoin = root.join("tags");
            Predicate byTagPredicate = builder.equal(tagJoin.get("tagName"), tag);
            query.where(byTagPredicate);
            query.orderBy(builder.desc(root.get("createdAt")));

            TypedQuery<PostJpaEntity> jQuery = entityManager.createQuery(query);

            List<PostJpaEntity> entities = jQuery.getResultList();
            List<Post> posts = entities.stream()
                    .map(postJpaMapper::toDomain)
                    .collect(Collectors.toList());

            logger.info("Найдено {} постов по тегу: '{}'", posts.size(), tag);
            return posts;
        } catch (Exception ex) {
            logger.error("Ошибка при получении постов по тегу: '{}'", tag, ex);
            throw new DataAccessException("Ошибка при получении постов по тегу: " + tag, ex);
        }
    }

    @Override
    public List<Post> getAllPostsByUserIdIn(Long[] userIds) {
        logger.info("Получение всех постов для пользователей с ID: {}", Arrays.toString(userIds));
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            TypedQuery<PostJpaEntity> query = entityManager.createQuery(
                    // Используем оператор IN, как и в SQL
                    "select p FROM PostJpaEntity p WHERE p.author.id IN (:userIds) ORDER BY p.createdAt DESC", PostJpaEntity.class
            );
            query.setParameter("userIds", List.of(userIds)); // Передаем список ID
            List<PostJpaEntity> entities = query.getResultList();
            List<Post> posts = entities.stream()
                    .map(postJpaMapper::toDomain)
                    .collect(Collectors.toList());

            logger.info("Найдено {} постов для {} пользователей", posts.size(), userIds.length);
            return posts;
        } catch (Exception ex) {
            logger.error("Ошибка при получении постов для пользователей с ID: {}", Arrays.toString(userIds), ex);
            throw new DataAccessException("Ошибка при получении постов для списка пользователей", ex);
        }
    }

    @Override
    public void deletePost(Post domainPost) {
        Long postId = domainPost.getPostId() != null ? domainPost.getPostId().longValue() : null;
        logger.info("Удаление поста с ID: {}", postId);
        if (postId == null) {
            logger.warn("Попытка удалить пост с null ID. Операция прервана.");
            return;
        }

        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {
                entityManager.getTransaction().begin();

                if (domainPost.getPostId() == null) return;
                PostJpaEntity managedPost = entityManager.find(PostJpaEntity.class, postId);

                // Теперь удаляем управляемую сущность
                if (managedPost != null) {
                    entityManager.remove(managedPost);
                    logger.info("Пост с ID: {} успешно удален", postId);
                } else {
                    logger.warn("Попытка удалить несуществующий пост с ID: {}. Запись в БД не найдена.", postId);
                }

                entityManager.getTransaction().commit();
            } catch (Exception ex) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                logger.error("Ошибка при удалении поста с ID: {}", postId, ex);
                throw new DataAccessException("Ошибка при удалении поста с ID: " + postId, ex);
            }
        }
    }

    @Override
    public Post getPostById(Long postId) {
        logger.info("Поиск поста по ID: {}", postId);
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            PostJpaEntity entity = entityManager.find(PostJpaEntity.class, postId);
            if (entity == null) {
                logger.warn("Пост с ID {} не найден.", postId);
                throw new PostNotFoundException("Публикация с ID " + postId + " не найдена.");
            }
            logger.info("Найден пост с ID: {}", postId);
            return postJpaMapper.toDomain(entity);
        } catch (Exception ex) {
            logger.error("Ошибка при поиске поста с ID: {}", postId, ex);
            throw new DataAccessException("Ошибка при поиске поста с ID: " + postId, ex);
        }
    }
}
