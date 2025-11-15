package com.hcl.mi.repositories;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hcl.mi.entities.InspectionLot;
import com.lowagie.text.Element;

@Repository
public interface InspectionLotRepository extends JpaRepository<InspectionLot, Serializable>, JpaSpecificationExecutor<InspectionLot> {
	
	List<InspectionLot> findAllBycreationDateBetween(LocalDate from, LocalDate to);
	
	@Query(
		    value = "SELECT * FROM mim.inspection_lot WHERE UPPER(result) = 'PENDING'",
		    nativeQuery = true
		)
		List<InspectionLot> findAllPendingNative();
		
	@Query(
		    value = "SELECT * FROM mim.inspection_lot WHERE UPPER(result) = 'REJECTED'",
		    nativeQuery = true
		)
	    List<InspectionLot> findAllRejectedNative();
} 
