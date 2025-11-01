package twitter.mapper.v1.viewMapper.impl;

import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.entity.post.Post;

import twitter.mapper.v1.viewMapper.PostViewMapper;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;


@Component
public class PostViewMapperImpl implements PostViewMapper {

    @Injection
    public PostViewMapperImpl() {
    }

    @Override
    public String postToBeautifulString(Post post, String authorLogin) {
        // 1. Подготовка переменных для итоговой строки
        String tagsString;
        String formattedDate;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 2. Обработка даты создания поста
        if (post.getCreatedAt() != null) {
            formattedDate = post.getCreatedAt().format(formatter);
        } else {
            formattedDate = "неизвестно";
            System.err.println("Ошибка: дата создания поста is null для поста с ID: " + post.getPostId());
        }

        // 3. Обработка имени автора
        if (authorLogin == null || authorLogin.isBlank()) {
            authorLogin = "неизвестен";
            System.err.println("Ошибка: автор не передан для поста с ID: " + post.getPostId());
        }

        // 4. Обработка списка тегов
        if (post.getTags() != null && post.getTags().length > 0) {
            tagsString = Arrays.stream(post.getTags())
                    .map(String::trim)
                    .map(name -> "#" + name)
                    .collect(Collectors.joining(", "));
        } else {
            tagsString = "Тегов нет";
        }

        return String.format("Публикация (ID- %d): {\n    Автор: %s;\n    Создано: %s;\n    Тема: %s;\n    Текст: %s;\n    Теги: %s;\n}",
                post.getPostId(), authorLogin, formattedDate, post.getTopic(), post.getText(), tagsString);
    }
}
