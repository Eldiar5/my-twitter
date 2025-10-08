package twitter.entity.post;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Post {

    private Integer authorId;
    private Integer postId;
    private String topic;
    private String text;
    private String[] tags;
    private LocalDateTime createdAt;

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
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

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean hasTag(String tag) {

        if (tags == null) {
            return false;
        }

        for(String t : this.tags) {
            if(tag.equals(t)) {
                return true;
            }
        }
        return false;
    }

    public String toFile() {
        DateTimeFormatter fCreatedAt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String createdAtString = this.createdAt.format(fCreatedAt);
        return String.format("[POST]{%d}{%d}{%s}{%s}{%s}{%s}",
                this.authorId, this.postId, this.topic, this.text, String.join(",", this.tags), createdAtString);
    }
}
