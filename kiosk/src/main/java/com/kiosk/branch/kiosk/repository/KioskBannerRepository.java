package com.kiosk.branch.kiosk.repository;

import com.kiosk.entity.KioskBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KioskBannerRepository extends JpaRepository<KioskBanner, Integer> {
    Optional<KioskBanner> findByKioskId(Integer kioskId);
}
