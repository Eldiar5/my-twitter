package twitter.dto.v1;

public class PostDto {

    private String author;
    private String createdAt;
    private String topic;
    private String text;
    private String tags;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String toFile() {
        return String.format("Публикация: {\n    Автор: %s;\n    Создано: %s;\n    Тема: %s;\n    Текст: %s;\n    Теги: %s;\n}",
                this.author, this.createdAt, this.topic, this.text, this.tags);
    }
}
