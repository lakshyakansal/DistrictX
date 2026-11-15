package com.districtx;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
  return http.csrf(c->c.disable()).cors(c->c.configurationSource(corsSource()))
   .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a->a.requestMatchers("/api/auth/**","/api/experiences/**","/actuator/health").permitAll()
    .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .requestMatchers("/api/bookings/**").authenticated().anyRequest().permitAll())
   .addFilterBefore(jwtFilter,UsernamePasswordAuthenticationFilter.class).build();
 }
 @Bean CorsConfigurationSource corsSource(){
  CorsConfiguration c=new CorsConfiguration(); c.setAllowedOriginPatterns(List.of("*"));c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("*"));c.setAllowCredentials(false);
  UrlBasedCorsConfigurationSource s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;
 }
}
@Component
class JwtService {
 private final byte[] key; private final long expiration;
 JwtService(@Value("${app.jwt.secret}") String secret,@Value("${app.jwt.expiration-ms}") long expiration){key=secret.getBytes(StandardCharsets.UTF_8);this.expiration=expiration;}
 String issue(AppUser u){return Jwts.builder().subject(u.email).claim("role",u.role).issuedAt(new java.util.Date()).expiration(new java.util.Date(System.currentTimeMillis()+expiration)).signWith(Keys.hmacShaKeyFor(key)).compact();}
 String email(String token){return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(key)).build().parseSignedClaims(token).getPayload().getSubject();}
 String role(String token){return (String)Jwts.parser().verifyWith(Keys.hmacShaKeyFor(key)).build().parseSignedClaims(token).getPayload().get("role");}
}
@Component
class JwtFilter extends org.springframework.web.filter.OncePerRequestFilter {
 private final JwtService jwt;
 JwtFilter(JwtService jwt){this.jwt=jwt;}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  String h=req.getHeader("Authorization");
  if(h!=null&&h.startsWith("Bearer ")&&org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()==null){
   try{String t=h.substring(7);String email=jwt.email(t);String role=jwt.role(t);
    var auth=new UsernamePasswordAuthenticationToken(email,null,List.of(new SimpleGrantedAuthority("ROLE_"+role)));
    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
   }catch(Exception ignored){}
  } chain.doFilter(req,res);
 }
}
