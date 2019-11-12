package wonderland.security.authentication.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wonderland.security.authentication.domain.Permission;
import wonderland.security.authentication.dto.PermissionDto;
import wonderland.security.authentication.exception.PermissionAlreadyExistsException;
import wonderland.security.authentication.exception.PermissionNotFoundException;
import wonderland.security.authentication.mapper.PermissionMapper;
import wonderland.security.authentication.repository.PermissionRepository;
import wonderland.security.authentication.repository.RoleRepository;

@Service("permissionService")
@Transactional
public class PermissionServices {

    private PermissionRepository permissionRepository;

    public PermissionServices(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
    }

    public PermissionDto createAPermission(String name, String application) {
        if (permissionRepository.findPermissionByApplicationAndName(application, name).isPresent()) {
            throw new PermissionAlreadyExistsException(name, application);
        }
        var newPermission = new Permission(application, name);
        var savedPermission = permissionRepository.save(newPermission);
        return PermissionMapper.map(savedPermission);
    }

    public void removePermission(String name, String application) {
        permissionRepository.findPermissionByApplicationAndName(application, name)
                .ifPresentOrElse(permission -> permissionRepository.deleteById(permission.getPermissionId()),
                        () -> {
                            throw new PermissionNotFoundException(application, name);
                        });
    }
}
