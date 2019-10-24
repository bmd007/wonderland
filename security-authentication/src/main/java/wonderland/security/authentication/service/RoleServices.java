package wonderland.security.authentication.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import wonderland.security.authentication.domain.Permission;
import wonderland.security.authentication.domain.Role;
import wonderland.security.authentication.dto.PermissionDto;
import wonderland.security.authentication.exception.RoleAlreadyExistsException;
import wonderland.security.authentication.exception.RoleNotFoundException;
import wonderland.security.authentication.mapper.PermissionMapper;
import wonderland.security.authentication.repository.PermissionRepository;
import wonderland.security.authentication.repository.RoleRepository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Service("userService")
@Transactional
public class RoleServices {

    private RoleRepository roleRepository;

    public RoleServices(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Mono<Role> createARole(String roleName, Set<PermissionDto> permissions) {
        if (roleRepository.findById(roleName).isPresent()) {
            return Mono.error(new RoleAlreadyExistsException(roleName));
        }
        var permissionsSet = permissions.stream().map(PermissionMapper::map).collect(toSet());
        return Mono.just(Role.newBuilder())
                .map(builder -> builder.withRoleName(roleName)
                        .withPermissions(permissionsSet)
                        .build());
    }

    public Mono<Role> addAPermissionToRole(String roleName, PermissionDto permission) {
        return Mono.just(roleRepository.findById(roleName))
                .filter(Optional::isPresent).map(Optional::get).switchIfEmpty(Mono.error(new RoleNotFoundException(roleName)))
                .map(role -> {
                    var newPermissions = new HashSet<Permission>();
                    newPermissions.addAll(role.getPermissions());
                    newPermissions.add(PermissionMapper.map(permission));
                    return newPermissions;
                })
                .map(permissions -> Role.newBuilder()
                        .withRoleName(roleName)
                        .withPermissions(permissions)
                        .build());
    }

    public Mono<Role> removeAPermissionFromRole(String roleName, PermissionDto permission) {
        return Mono.just(roleRepository.findById(roleName))
                .filter(Optional::isPresent).map(Optional::get).switchIfEmpty(Mono.error(new RoleNotFoundException(roleName)))
                .map(role -> {
                    var newPermissions = new HashSet<Permission>();
                    newPermissions.addAll(role.getPermissions());
                    newPermissions.remove(PermissionMapper.map(permission));
                    return newPermissions;
                })
                .map(permissions -> Role.newBuilder()
                        .withRoleName(roleName)
                        .withPermissions(permissions)
                        .build());
    }
}
