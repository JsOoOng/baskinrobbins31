package com.kiosk.branch.status.reopsitory;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.IcecreamFlavor;



public interface IcecreamFlavorMapper
extends JpaRepository<IcecreamFlavor, Integer>{


    List<IcecreamFlavor> findByIsActiveTrue();


}