package wonderland.security.authentication.mapper;

import wonderland.security.authentication.domain.Permission;
import wonderland.security.authentication.dto.PermissionDto;

public class PermissionMapper {
    public static Permission map(PermissionDto permissionDto) {
        return new Permission(permissionDto.getApplication(), permissionDto.getName());
    }

    public static PermissionDto map(Permission permission) {
        return new PermissionDto(permission.getApplication(), permission.getName());
    }
}
