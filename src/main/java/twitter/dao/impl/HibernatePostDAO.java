package twitter.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import twitter.configuration.Profile;
import twitter.dao.UserDAO;
import twitter.entity.post.PostJpaEntity;
import twitter.mapper.dbMapper.PostJpaMapper;
import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.dao.PostDAO;
import twitter.entity.post.Post;
import twitter.exceptions.DataAccessException;
import twitter.exceptions.PostNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile(active = {"default", "prod"})
public class HibernatePostDAO implements PostDAO {

    private final EntityManagerFactory entityManagerFactory;
    private final PostJpaMapper postJpaMapper;
    private final UserDAO userDAO;

    @Injection
    public HibernatePostDAO(EntityManagerFactory entityManagerFactory, PostJpaMapper postJpaMapper, UserDAO userDAO) {
        this.entityManagerFactory = entityManagerFactory;
        this.postJpaMapper = postJpaMapper;
        this.userDAO = userDAO;
    }

    @Override
    public Post saveNewPost(Post domainPost) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {
                entityManager.getTransaction().begin();
                PostJpaEntity entityToSave = postJpaMapper.toEntity(domainPost, userDAO);
                entityManager.persist(entityToSave);
                entityManager.getTransaction().commit();
                return postJpaMapper.toDomain(entityToSave);
            } catch (Exception ex) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                throw new DataAccessException("Ошибка при сохранении поста: " + ex, ex);
            }
        }
    }

    @Override
    public List<Post> getAllPosts() {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            List<PostJpaEntity> entities = entityManager.createQuery("select p from PostJpaEntity p", PostJpaEntity.class).getResultList();
            return entities.stream()
                    .map(postJpaMapper::toDomain)
                    .collect(Collectors.toList());
        } catch(Exception ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    @Override
    public List<Post> getAllPostsByUser(Long userId) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            TypedQuery<PostJpaEntity> query = entityManager
                    // Обращаемся к полю author, а через точку - к его полю id
                    .createQuery("select p from PostJpaEntity p where p.author.id = :authorId ORDER BY p.createdAt DESC", PostJpaEntity.class);

            query.setParameter("authorId", userId);
            List<PostJpaEntity> entities = query.getResultList();
            return entities.stream()
                    .map(postJpaMapper::toDomain)
                    .collect(Collectors.toList());
        }  catch(Exception ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    @Override
    public List<Post> getAllPostsByTag(String tag) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            TypedQuery<PostJpaEntity> query = entityManager
                    .createQuery("select p from PostJpaEntity p join p.tags t where t.tagName = :tag", PostJpaEntity.class);

            query.setParameter("tag", tag);
            List<PostJpaEntity> entities = query.getResultList();
            return entities.stream()
                    .map(postJpaMapper::toDomain)
                    .collect(Collectors.toList());
        } catch(Exception ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    @Override
    public List<Post> getAllPostsByUserIdIn(Long[] userIds) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            TypedQuery<PostJpaEntity> query = entityManager.createQuery(
                    // Используем оператор IN, как и в SQL
                    "select p FROM PostJpaEntity p WHERE p.author.id IN (:userIds)", PostJpaEntity.class
            );
            query.setParameter("userIds", List.of(userIds)); // Передаем список ID
            List<PostJpaEntity> entities = query.getResultList();
            return entities.stream()
                    .map(postJpaMapper::toDomain)
                    .collect(Collectors.toList());
        } catch(Exception ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    @Override
    public void deletePost(Post domainPost) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {
                entityManager.getTransaction().begin();

                if (domainPost.getPostId() == null) return;
                PostJpaEntity managedPost = entityManager.find(PostJpaEntity.class, domainPost.getPostId().longValue());

                // Теперь удаляем управляемую сущность
                if (managedPost != null) {
                    entityManager.remove(managedPost);
                }

                entityManager.getTransaction().commit();
            } catch (Exception ex) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                throw new DataAccessException("Ошибка при удалении поста с ID: " + domainPost.getPostId(), ex);
            }
        }
    }

    @Override
    public Post getPostById(Long postId) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            PostJpaEntity entity = entityManager.find(PostJpaEntity.class, postId);
            if (entity == null) {
                throw new PostNotFoundException("Публикация с ID " + postId + " не найдена.");
            }
            return postJpaMapper.toDomain(entity);
        } catch (Exception ex) {
            throw new DataAccessException("Ошибка при поиске поста с ID: " + postId, ex);
        }
    }
}
