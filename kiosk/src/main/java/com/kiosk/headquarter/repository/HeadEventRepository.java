package com.kiosk.headquarter.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.Event;

public interface HeadEventRepository extends JpaRepository<Event, Integer> {
}
