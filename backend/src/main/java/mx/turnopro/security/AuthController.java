package mx.turnopro.security;

import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;

@RestController @RequestMapping("/api/auth") class AuthController {
    private final PasswordEncoder encoder;private final SecretKey key;private final String user,hash;private final long expiry;
    AuthController(PasswordEncoder e,SecretKey k,@Value("${app.admin.username:}") String u,@Value("${app.admin.password:}") String p,@Value("${app.jwt.expiration-ms}") long x){encoder=e;key=k;user=u;hash=p.isBlank()?"":e.encode(p);expiry=x;}
    record Login(@NotBlank String username,@NotBlank String password){}
    @PostMapping("/login") Map<String,String> login(@Valid @RequestBody Login r){if(user.isBlank()||hash.isBlank()||!user.equals(r.username())||!encoder.matches(r.password(),hash))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Credenciales no válidas.");String token=Jwts.builder().subject(user).claim("role","ADMINISTRADOR").issuedAt(Date.from(Instant.now())).expiration(Date.from(Instant.now().plusMillis(expiry))).signWith(key).compact();return Map.of("token",token,"tokenType","Bearer");}
    @PostMapping("/logout") Map<String,String> logout(){return Map.of("message","Sesión cerrada en el cliente.");}
}
