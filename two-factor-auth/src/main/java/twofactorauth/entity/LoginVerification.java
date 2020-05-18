package twofactorauth.entity;

import lombok.*;
import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "login_verification")
public class LoginVerification {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    private String uid;

    @Column(name = "login_date", nullable = false)
    private Long loginDate;

    @NonNull
    @Column(name = "verification_code", nullable = false)
    private Integer verificationCode;

    @NonNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "uid")
    private User user;

    @PrePersist
    public void setLoginDate() {
        this.loginDate= System.currentTimeMillis();
    }
}
