package twofactorauth.entity;

import lombok.*;
import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

import twofactorauth.enums.UserGender;
import twofactorauth.enums.UserRole;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    private String uid;

    @NonNull
    @Column(name = "first_name")
    private String firstName;

    @NonNull
    @Column(name = "last_name")
    private String lastName;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserRole role;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserGender gender;

    @NonNull
    @Column(nullable = false)
    private String email;

    @NonNull
    @Column(nullable = false)
    private String password;

    @NonNull
    private String phone;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "tinyint default 0")
    private boolean isDeleted;

    @Column(name = "reset_password_date")
    private Long resetPasswordDate;

    @NonNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "invitation_id", nullable = false, referencedColumnName = "uid")
    private Invitation invitation;
}