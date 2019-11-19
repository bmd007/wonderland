package wonderland.security.authentication.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wonderland.security.authentication.domain.Permission;
import wonderland.security.authentication.domain.Role;
import wonderland.security.authentication.dto.PermissionDto;
import wonderland.security.authentication.exception.RoleAlreadyExistsException;
import wonderland.security.authentication.exception.RoleNotFoundException;
import wonderland.security.authentication.mapper.PermissionMapper;
import wonderland.security.authentication.repository.PermissionRepository;
import wonderland.security.authentication.repository.RoleRepository;

import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Service("roleService")
@Transactional
public class RoleServices {

    private RoleRepository roleRepository;

    public RoleServices(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role createARole(String roleName, Set<PermissionDto> permissions) {
        if (roleRepository.findById(roleName).isPresent()) {
            throw new RoleAlreadyExistsException(roleName);
        }
        var permissionsSet = permissions.stream().map(PermissionMapper::map).collect(toSet());
        return Role.newBuilder().withRoleName(roleName)
                .withPermissions(permissionsSet)
                .build();
    }

    public Role addAPermissionToRole(String roleName, PermissionDto permission) {
        return roleRepository.findById(roleName)
                .map(role -> {
                    var newPermissions = new HashSet<Permission>();
                    newPermissions.addAll(role.getPermissions());
                    newPermissions.add(PermissionMapper.map(permission));
                    return newPermissions;
                })
                .map(permissions -> Role.newBuilder()
                        .withRoleName(roleName)
                        .withPermissions(permissions)
                        .build())
                .orElseThrow(() -> new RoleNotFoundException(roleName));
    }

    public Role removeAPermissionFromRole(String roleName, PermissionDto permission) {
        return roleRepository.findById(roleName)
                .map(role -> {
                    var newPermissions = new HashSet<Permission>();
                    newPermissions.addAll(role.getPermissions());
                    newPermissions.remove(PermissionMapper.map(permission));
                    return newPermissions;
                })
                .map(permissions -> Role.newBuilder()
                        .withRoleName(roleName)
                        .withPermissions(permissions)
                        .build())
                .orElseThrow(() -> new RoleNotFoundException(roleName));
    }
}
