package desarrollador.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import desarrollador.api.models.Profile;
import desarrollador.api.models.Role;
import desarrollador.api.models.User;

public class AuthDtos {
    public static class RegisterRequest {
        public String email;
        public String password;
        public String nombre;
        public String apellido;
        public String telefono;
        public String direccion;
        public String region;
        public String ciudad;
        public String codigoPostal;
        public String role;
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }

    @JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    public static class LoginResponse {
        public String token;
        public UserDto user;
    }

    @JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    public static class UserDto {
        public Long id;
        public String email;
        public String role;
        public Profile profile;
        public String region;
        public String ciudad;

        public static UserDto from(User u) {
            UserDto d = new UserDto();
            d.id = u.getId();
            d.email = u.getEmail();
            d.role = u.getRole() != null ? u.getRole().name() : Role.USER.name();
            d.profile = u.getProfile();
            if (u.getProfile() != null) {
                d.region = u.getProfile().getRegion();
                d.ciudad = u.getProfile().getCiudad();
            }
            return d;
        }
    }
}
