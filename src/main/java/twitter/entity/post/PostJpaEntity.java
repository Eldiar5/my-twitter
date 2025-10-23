package twitter.entity.post;

import jakarta.persistence.*;
import twitter.entity.tags.Tags;
import twitter.entity.user.User;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "posts")
public class PostJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    private User author;

    @Column(name = "topic")
    private String topic;

    @Column(name = "text")
    private String text;

    @ManyToMany(fetch = FetchType.EAGER,  cascade = {CascadeType.MERGE})
    @JoinTable(name = "post_and_tags", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tags> tags;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }


    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
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

    public List<Tags> getTags() {
        return tags;
    }

    public void setTags(List<Tags> tags) {
        this.tags = tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
