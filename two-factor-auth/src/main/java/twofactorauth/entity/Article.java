package twofactorauth.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import twofactorauth.enums.UserGender;
import twofactorauth.enums.UserRole;

import javax.persistence.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "articles")
public class Article {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    private String uid;

    @Lob
    @NonNull
    private byte[] image;

    @NonNull
    @Column(name = "title")
    private String title;

    @NonNull
    @Column(name = "content")
    private String content;

    @Column(name = "created_date", nullable = false)
    private Long createdDate;

    @NonNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "uid")
    private User user;

    @PrePersist
    public void setCreatedDate() {
        this.createdDate = System.currentTimeMillis();
    }
}
