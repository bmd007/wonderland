package wonderland.security.authentication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wonderland.security.authentication.domain.UserAccount;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findUserByEmail(String email);

    Optional<UserAccount> findUserByPhoneNumber(String phoneNumber);

    Optional<UserAccount> findUserByPhoneNumberOrEmail(String phoneNumber, String email);
}
