package wonderland.security.authentication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wonderland.security.authentication.domain.Permission;

import java.util.Optional;


@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findPermissionByApplication(String application);

    Optional<Permission> findPermissionByApplicationAndName(String application, String name);
}
