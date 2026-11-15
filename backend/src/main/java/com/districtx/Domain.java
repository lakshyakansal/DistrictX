package com.districtx;

import jakarta.persistence.*;
import java.time.Instant;
import java.math.BigDecimal;

@Entity @Table(name="app_users")
class AppUser {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
  @Column(nullable=false, unique=true) String email;
  @Column(nullable=false) String name;
  @Column(nullable=false) String password;
  @Column(nullable=false) String role="CUSTOMER";
  protected AppUser() {}
  AppUser(String name,String email,String password,String role){this.name=name;this.email=email;this.password=password;this.role=role;}
}
@Entity @Table(name="experiences")
class Experience {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
  @Column(nullable=false) String title;
  @Column(nullable=false) String category;
  @Column(length=3000) String description;
  String city;
  String venue;
  String imageUrl;
  String dateLabel;
  BigDecimal price;
  Integer available;
  Double rating;
  Boolean featured=false;
  Boolean active=true;
  protected Experience() {}
  Experience(String title,String category,String description,String city,String venue,String imageUrl,String dateLabel,BigDecimal price,Integer available,Double rating,Boolean featured){
    this.title=title;this.category=category;this.description=description;this.city=city;this.venue=venue;this.imageUrl=imageUrl;this.dateLabel=dateLabel;this.price=price;this.available=available;this.rating=rating;this.featured=featured;this.active=true;
  }
}
@Entity @Table(name="bookings")
class Booking {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
  @ManyToOne(optional=false) AppUser user;
  @ManyToOne(optional=false) Experience experience;
  Integer quantity;
  BigDecimal total;
  String status="CONFIRMED";
  String bookingCode;
  Instant createdAt=Instant.now();
  protected Booking() {}
  Booking(AppUser user,Experience experience,Integer quantity,BigDecimal total,String bookingCode){this.user=user;this.experience=experience;this.quantity=quantity;this.total=total;this.bookingCode=bookingCode;}
}
