package com.hcl.mi.repositories;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hcl.mi.entities.InspectionLot;

@Repository
public interface InspectionLotRepository extends JpaRepository<InspectionLot, Serializable> {
	
	List<InspectionLot> findAllBycreationDateBetween(LocalDate from, LocalDate to);
}
