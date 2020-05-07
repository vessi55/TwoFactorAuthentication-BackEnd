package twofactorauth.entity;

import lombok.*;
import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

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

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @NonNull
    private UserRole role;

    @NonNull
    @Column(nullable = false, unique = true)
    private String email;

    @NonNull
    @Column(nullable = false)
    private String password;

    @NonNull
    private String phone;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "invitation_id", nullable = false, referencedColumnName = "uid")
    @NonNull
    private Invitation invitation;
}