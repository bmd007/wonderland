package wonderland.user.mapper;

import wonderland.user.domain.Permission;
import wonderland.user.dto.PermissionDto;

public class PermissionMapper {
    public static Permission map(PermissionDto permissionDto) {
        return new Permission(permissionDto.getApplication(), permissionDto.getName());
    }

    public static PermissionDto map(Permission permission) {
        return new PermissionDto(permission.getApplication(), permission.getName());
    }
}
