package twofactorauth.entity;

import lombok.*;
import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

import twofactorauth.enums.UserRole;
import twofactorauth.enums.UserStatus;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "invitations")
public class Invitation {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    private String uid;

    @NonNull
    @Column(nullable = false)
    private String email;

    @Column(name = "created_date", nullable = false)
    private Long createdDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @NonNull
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @NonNull
    private UserStatus status;

    @NonNull
    @Column(name = "verification_code", nullable = false)
    private String verificationCode;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "tinyint default 0")
    private boolean isDeleted;

    @PrePersist
    public void setCreatedDate() {
        this.createdDate = System.currentTimeMillis();
    }

}