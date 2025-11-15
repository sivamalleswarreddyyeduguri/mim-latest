package com.hcl.mi.repositories;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hcl.mi.entities.InspectionActuals;

@Repository
public interface InspectionActualsRepository extends JpaRepository<InspectionActuals, Serializable> {
	
	@Query(value = "select * from insp_actu where inspection_lot_lot_id = :lot AND material_inspection_characteristics_ch_id = :charId", nativeQuery = true)
	InspectionActuals findByInspectionLotAndmaterialInspectionCharacteristics(int lot, int charId);

	@Query(
		    value = "SELECT * FROM mim.insp_actu WHERE inspection_lot_lot_id = :lotId",
		    nativeQuery = true
		)
	List<InspectionActuals> findAllByLotId(@Param("lotId") Integer lotId); 

}
