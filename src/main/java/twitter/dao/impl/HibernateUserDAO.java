package twitter.dao.impl;

import jakarta.persistence.*;
import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.configuration.Profile;
import twitter.dao.UserDAO;
import twitter.entity.user.User;
import twitter.entity.user.UserType;
import twitter.exceptions.DataAccessException;
import twitter.exceptions.UserNotFoundException;

import java.util.List;

@Component
@Profile(active = {"default", "prod"})
public class HibernateUserDAO implements UserDAO {

    private final EntityManagerFactory entityManagerFactory;

    @Injection
    public HibernateUserDAO(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public User saveNewUser(User user) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {
                entityManager.getTransaction().begin();
                entityManager.persist(user);
                entityManager.getTransaction().commit();
                return user;
            } catch (Exception ex) {
                entityManager.getTransaction().rollback();
                throw new DataAccessException("Ошибка при сохранении пользователя: " + user.getLogin(), ex);
            }
        }
    }

    @Override
    public User getUserByLogin(String login) throws UserNotFoundException {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            // Создаём типизированный запрос к сущности User
            TypedQuery<User> query = entityManager
                    .createQuery("select u from User u where u.login = :login", User.class);

            // Устанавливаем именованный параметр
            query.setParameter("login", login);

            return query.getSingleResult();

        } catch (NoResultException e) {
            throw new UserNotFoundException("Пользователь с логином '" + login + "' не найден.");
        } catch (Exception ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    @Override
    public User getUserById(int id) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            User user = entityManager.find(User.class, id);

            if (user == null) {
                throw new UserNotFoundException("Пользователь с ID " + id + " не найден.");
            }

            return user;

        } catch (Exception ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    @Override
    public List<User> getAllUsersByUserType(UserType userType) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {

            TypedQuery<User> query = entityManager
                    .createQuery("select u from User u where u.userType = :type", User.class);

            query.setParameter("type", userType);
            return query.getResultList();

        } catch (Exception ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    @Override
    public List<User> getAllUsers() {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            return entityManager.createQuery("from User", User.class).getResultList();
        } catch (Exception ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    @Override
    public boolean isUserExists(String login) {
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
            return count > 0;

        } catch (Exception ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }
}
