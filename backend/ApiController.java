package com.districtx;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api")
class ApiController {
 private final UserRepository users; private final ExperienceRepository experiences; private final BookingRepository bookings; private final PasswordEncoder encoder; private final JwtService jwt;
 ApiController(UserRepository users,ExperienceRepository experiences,BookingRepository bookings,PasswordEncoder encoder,JwtService jwt){this.users=users;this.experiences=experiences;this.bookings=bookings;this.encoder=encoder;this.jwt=jwt;}

 record RegisterRequest(@NotBlank String name,@Email @NotBlank String email,@NotBlank @Size(min=8) String password){}
 record LoginRequest(@Email @NotBlank String email,@NotBlank String password){}
 record ExperienceRequest(@NotBlank String title,@NotBlank String category,String description,String city,String venue,String imageUrl,String dateLabel,@NotNull @DecimalMin("0.0") BigDecimal price,@NotNull @Min(0) Integer available,Double rating,Boolean featured){}
 record BookingRequest(@NotNull Long experienceId,@NotNull @Min(1) @Max(10) Integer quantity){}
 record AuthResponse(String token,Map<String,Object> user){}
 record BookingResponse(Long id,String bookingCode,String status,String title,Integer quantity,BigDecimal total,String createdAt){}

 @PostMapping("/auth/register") ResponseEntity<?> register(@Valid @RequestBody RegisterRequest r){
  if(users.findByEmailIgnoreCase(r.email()).isPresent())return ResponseEntity.badRequest().body(Map.of("message","Email already registered"));
  AppUser u=users.save(new AppUser(r.name(),r.email().toLowerCase(),encoder.encode(r.password()),"CUSTOMER"));return ResponseEntity.ok(auth(u));
 }
 @PostMapping("/auth/login") ResponseEntity<?> login(@Valid @RequestBody LoginRequest r){
  AppUser u=users.findByEmailIgnoreCase(r.email()).orElse(null);
  if(u==null||!encoder.matches(r.password(),u.password))return ResponseEntity.status(401).body(Map.of("message","Invalid email or password"));
  return ResponseEntity.ok(auth(u));
 }
 private AuthResponse auth(AppUser u){return new AuthResponse(jwt.issue(u),Map.of("id",u.id,"name",u.name,"email",u.email,"role",u.role));}

 @GetMapping("/experiences") List<Experience> list(@RequestParam(required=false) String category,@RequestParam(required=false) String q){
  List<Experience> all=(category==null||category.isBlank())?experiences.findByActiveTrueOrderByFeaturedDescIdDesc():experiences.findByCategoryIgnoreCaseAndActiveTrueOrderByFeaturedDescIdDesc(category);
  if(q==null||q.isBlank())return all;
  String term=q.toLowerCase();return all.stream().filter(e->(e.title+" "+Objects.toString(e.city,"")+" "+Objects.toString(e.category,"")).toLowerCase().contains(term)).toList();
 }
 @GetMapping("/experiences/{id}") ResponseEntity<?> detail(@PathVariable Long id){return experiences.findById(id).filter(e->Boolean.TRUE.equals(e.active)).<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(()->ResponseEntity.notFound().build());}

 @PostMapping("/bookings") @Transactional ResponseEntity<?> book(@Valid @RequestBody BookingRequest r,Authentication auth){
  AppUser u=users.findByEmailIgnoreCase(auth.getName()).orElseThrow();
  Experience e=experiences.findById(r.experienceId()).orElse(null);
  if(e==null||!Boolean.TRUE.equals(e.active))return ResponseEntity.notFound().build();
  if(e.available==null||e.available<r.quantity())return ResponseEntity.badRequest().body(Map.of("message","Not enough availability"));
  e.available-=r.quantity();experiences.save(e);
  BigDecimal total=e.price.multiply(BigDecimal.valueOf(r.quantity()));
  String code="DX"+Long.toString(System.currentTimeMillis(),36).toUpperCase()+ThreadLocalRandom.current().nextInt(100,999);
  Booking b=bookings.save(new Booking(u,e,r.quantity(),total,code));
  return ResponseEntity.ok(toResponse(b));
 }
 @GetMapping("/bookings/me") List<BookingResponse> myBookings(Authentication auth){AppUser u=users.findByEmailIgnoreCase(auth.getName()).orElseThrow();return bookings.findByUserIdOrderByCreatedAtDesc(u.id).stream().map(this::toResponse).toList();}
 private BookingResponse toResponse(Booking b){return new BookingResponse(b.id,b.bookingCode,b.status,b.experience.title,b.quantity,b.total,b.createdAt.toString());}

 @GetMapping("/admin/overview") Map<String,Object> overview(){
  return Map.of("users",users.count(),"experiences",experiences.count(),"bookings",bookings.count(),"revenue",bookings.findAll().stream().filter(b->"CONFIRMED".equals(b.status)).map(b->b.total).reduce(BigDecimal.ZERO,BigDecimal::add));
 }
 @PostMapping("/admin/experiences") ResponseEntity<Experience> create(@Valid @RequestBody ExperienceRequest r){return ResponseEntity.ok(experiences.save(from(r)));}
 @PutMapping("/admin/experiences/{id}") ResponseEntity<?> update(@PathVariable Long id,@Valid @RequestBody ExperienceRequest r){
  Experience e=experiences.findById(id).orElse(null);if(e==null)return ResponseEntity.notFound().build();
  Experience n=from(r);n.id=e.id;n.active=e.active;return ResponseEntity.ok(experiences.save(n));
 }
 @DeleteMapping("/admin/experiences/{id}") ResponseEntity<?> delete(@PathVariable Long id){Experience e=experiences.findById(id).orElse(null);if(e==null)return ResponseEntity.notFound().build();e.active=false;experiences.save(e);return ResponseEntity.noContent().build();}
 private Experience from(ExperienceRequest r){return new Experience(r.title(),r.category(),r.description(),r.city(),r.venue(),r.imageUrl(),r.dateLabel(),r.price(),r.available(),r.rating(),Boolean.TRUE.equals(r.featured()));}
}

@org.springframework.stereotype.Component
class SeedData implements org.springframework.boot.CommandLineRunner {
 private final ExperienceRepository repo;private final UserRepository users;private final PasswordEncoder encoder;
 SeedData(ExperienceRepository repo,UserRepository users,PasswordEncoder encoder){this.repo=repo;this.users=users;this.encoder=encoder;}
 public void run(String... args){
  if(users.findByEmailIgnoreCase("admin@districtx.local").isEmpty())users.save(new AppUser("DistrictX Admin","admin@districtx.local",encoder.encode("Admin@12345"),"ADMIN"));
  if(repo.count()>0)return;
  String[] imgs={
   "https://images.unsplash.com/photo-1506157786151-b8491531f063?w=1000",
   "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=1000",
   "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=1000",
   "https://images.unsplash.com/photo-1517649763962-0c623066013b?w=1000",
   "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=1000",
   "https://images.unsplash.com/photo-1514933651103-005eec06c04b?w=1000"
  };
  repo.saveAll(List.of(
   new Experience("Sunburn Arena: Neon Nights","Events","An immersive night of electronic music, lights and live performances.","Delhi","NSIC Exhibition Ground",imgs[0],"Sat, 28 Nov · 7:00 PM",new BigDecimal("1499"),250,4.8,true),
   new Experience("The Last Frame","Movies","A gripping cinematic experience. Choose your preferred show and enjoy the big screen.","Delhi","PVR Select Citywalk",imgs[1],"Today · Multiple shows",new BigDecimal("250"),120,4.5,true),
   new Experience("Olive Bar & Kitchen","Dining","Mediterranean-inspired dining in a beautiful garden setting. Reserve your table.","Delhi","Mehrauli, New Delhi",imgs[2],"Today · 12 PM – 11 PM",new BigDecimal("1200"),30,4.7,true),
   new Experience("Weekend Badminton","Sports","Book an indoor badminton court for your group. Equipment available on request.","Noida","Sector 18 Sports Arena",imgs[3],"Daily · 6 AM – 10 PM",new BigDecimal("399"),18,4.4,false),
   new Experience("Comedy Unfiltered","Events","A live stand-up comedy night featuring emerging voices and sharp new material.","Delhi","The Laugh Store",imgs[4],"Sun, 29 Nov · 8:00 PM",new BigDecimal("599"),90,4.6,true),
   new Experience("The Social Table","Dining","A lively neighbourhood restaurant for long conversations, food and drinks.","Gurugram","Cyber Hub",imgs[5],"Today · 1 PM – 11 PM",new BigDecimal("850"),22,4.3,false)
  ));
 }
}
