package twitter.dao.impl;

import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.configuration.Profile;
import twitter.dao.UserDAO;
import twitter.entity.user.User;
import twitter.entity.user.UserType;
import twitter.exceptions.DataAccessException;

import java.util.List;

@Component
@Profile(active = {"default", "prod"})
public class HibernateUserDAO implements UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(HibernateUserDAO.class);

    private final EntityManagerFactory entityManagerFactory;

    @Injection
    public HibernateUserDAO(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public User saveNewUser(User user) {
        logger.info("Сохранение нового пользователя с логином: {}", user.getLogin());
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {
                entityManager.getTransaction().begin();
                entityManager.persist(user);
                entityManager.getTransaction().commit();
                logger.info("Пользователь {} успешно сохранен с ID: {}", user.getLogin(), user.getId());
                return user;
            } catch (Exception ex) {
                entityManager.getTransaction().rollback();
                logger.error("Ошибка при сохранении пользователя: {}", user.getLogin(), ex);
                throw new DataAccessException("Ошибка при сохранении пользователя: " + user.getLogin(), ex);
            }
        }
    }

    @Override
    public User getUserByLogin(String login) {
        logger.info("Поиск пользователя по логину: {}", login);
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            // Создаём типизированный запрос к сущности User
            TypedQuery<User> query = entityManager
                    .createQuery("select u from User u where u.login = :login", User.class);

            // Устанавливаем именованный параметр
            query.setParameter("login", login);

            // Выполняем запрос и получаем результат
            User user = query.getSingleResult();
            logger.info("Найден пользователь с логином: {}", login);
            return user;
        } catch (NoResultException ex) {
            logger.warn("Пользователь с логином '{}' не найден.", login);
            throw new DataAccessException("Пользователь с логином '" + login + "' не найден.");
        } catch (Exception ex) {
            logger.error("Ошибка при поиске пользователя по логину: {}", login, ex);
            throw new DataAccessException("Ошибка при поиске пользователя: " + login, ex);
        }
    }

    @Override
    public User getUserById(int id) {
        logger.info("Поиск пользователя по ID: {}", id);
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            User user = entityManager.find(User.class, id);

            if (user == null) {
                logger.warn("Пользователь с ID {} не найден.", id);
                throw new DataAccessException("Пользователь с ID " + id + " не найден.");
            }

            logger.info("Найден пользователь с ID: {}", id);
            return user;

        } catch (Exception ex) {
            logger.error("Ошибка при поиске пользователя по ID: {}", id, ex);
            throw new DataAccessException("Ошибка при поиске пользователя по ID: " + id, ex);
        }
    }

    @Override
    public List<User> getAllUsersByUserType(UserType userType) {
        logger.info("Получение всех пользователей с типом: {}", userType);
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {

            TypedQuery<User> query = entityManager
                    .createQuery("select u from User u where u.userType = :type", User.class);

            query.setParameter("type", userType);

            List<User> users = query.getResultList();
            logger.info("Найдено {} пользователей с типом {}", users.size(), userType);
            return users;
        } catch (Exception ex) {
            logger.error("Ошибка при получении пользователей по типу: {}", userType, ex);
            throw new DataAccessException("Ошибка при получении пользователей по типу: " + userType + ex);
        }
    }

    @Override
    public List<User> getAllUsers() {
        logger.info("Получение всех пользователей");
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            List<User> users = entityManager.createQuery("from User", User.class).getResultList();
            logger.info("Найдено всего {} пользователей", users.size());
            return users;
        } catch (Exception ex) {
            logger.error("Ошибка при получении всех пользователей", ex);
            throw new DataAccessException("Ошибка при получении всех пользователей" + ex);
        }
    }

    @Override
    public boolean isUserExists(String login) {
        logger.debug("Проверка существования пользователя с логином: {}", login);
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            // 1. Создаём JPQL-запрос, который считает количество пользователей с заданным логином.
            //    Мы используем COUNT(u), так как нам не нужны сами данные, а только их количество.
            //    Это очень быстро и эффективно.
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.login = :loginValue", Long.class
            );

            // 2. Устанавливаем параметр в запросе
            query.setParameter("loginValue", login);

            // 3. Выполняем запрос и получаем результат (количество).
            //    getSingleResult() здесь безопасен, т.к. COUNT всегда вернёт ровно одно число.
            Long count = query.getSingleResult();

            // 4. Если количество больше 0, значит, пользователь существует.
            boolean exists = count > 0;
            logger.debug("Результат проверки для логина {}: {}", login, exists);
            return exists;

        } catch (Exception ex) {
            logger.error("Ошибка при проверке существования пользователя: {}", login, ex);
            throw new DataAccessException("Ошибка при проверке существования пользователя: " + login + ex);
        }
    }
}
