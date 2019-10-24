package wonderland.security.authentication.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import wonderland.security.authentication.domain.Permission;
import wonderland.security.authentication.dto.PermissionDto;
import wonderland.security.authentication.exception.PermissionAlreadyExistsException;
import wonderland.security.authentication.exception.PermissionNotFoundException;
import wonderland.security.authentication.mapper.PermissionMapper;
import wonderland.security.authentication.repository.PermissionRepository;
import wonderland.security.authentication.repository.RoleRepository;

import java.util.Optional;

@Service("userService")
@Transactional
public class PermissionServices {

    private PermissionRepository permissionRepository;

    public PermissionServices(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
    }

    public Mono<PermissionDto> createAPermission(String name, String application) {
        if (permissionRepository.findPermissionByApplicationAndName(application, name).isPresent()) {
            return Mono.error(new PermissionAlreadyExistsException(name, application));
        }
        return Mono.just(new Permission(application, name))
                .map(permissionRepository::save)
                .map(PermissionMapper::map);
    }

    public void removePermission(String name, String application) {
        Mono.just(permissionRepository.findPermissionByApplicationAndName(application, name))
                .filter(Optional::isPresent).map(Optional::get).switchIfEmpty(Mono.error(new PermissionNotFoundException(application, name)))
                .subscribe(permission -> permissionRepository.deleteById(permission.getId()));
    }
}
