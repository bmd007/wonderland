package wonderland.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wonderland.user.exception.RoleAlreadyExistsException;
import wonderland.user.exception.RoleNotFoundException;
import wonderland.user.mapper.PermissionMapper;
import wonderland.user.domain.Permission;
import wonderland.user.domain.Role;
import wonderland.user.dto.PermissionDto;
import wonderland.user.repository.PermissionRepository;
import wonderland.user.repository.RoleRepository;

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
