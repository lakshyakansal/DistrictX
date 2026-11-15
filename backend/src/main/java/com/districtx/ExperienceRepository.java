package com.districtx;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ExperienceRepository extends JpaRepository<Experience,Long> {
 List<Experience> findByActiveTrueOrderByFeaturedDescIdDesc();
 List<Experience> findByCategoryIgnoreCaseAndActiveTrueOrderByFeaturedDescIdDesc(String category);
}
