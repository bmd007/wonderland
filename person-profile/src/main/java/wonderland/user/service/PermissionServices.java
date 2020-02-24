package wonderland.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wonderland.user.exception.PermissionAlreadyExistsException;
import wonderland.user.exception.PermissionNotFoundException;
import wonderland.user.mapper.PermissionMapper;
import wonderland.user.domain.Permission;
import wonderland.user.dto.PermissionDto;
import wonderland.user.repository.PermissionRepository;
import wonderland.user.repository.RoleRepository;

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
