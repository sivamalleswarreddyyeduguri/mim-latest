package com.hcl.mi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "insp_actu")
@Data
@Builder
public class InspectionActuals {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer actualId;
	
	@ManyToOne
	@JsonIgnore
    private InspectionLot inspectionLot;
	
	@ManyToOne
	@JsonIgnore
	private MaterialInspectionCharacteristics materialInspectionCharacteristics;
	
	private Double maximumMeasurement;
	 
	private Double minimumMeasurement;
	
	

	public InspectionActuals() {
		super();
		// TODO Auto-generated constructor stub
	}

	public InspectionActuals(Integer actualId, InspectionLot inspectionLot,
			MaterialInspectionCharacteristics materialInspectionCharacteristics, Double maximumMeasurement,
			Double minimumMeasurement) {
		super();
		this.actualId = actualId;
		this.inspectionLot = inspectionLot;
		this.materialInspectionCharacteristics = materialInspectionCharacteristics;
		this.maximumMeasurement = maximumMeasurement;
		this.minimumMeasurement = minimumMeasurement;
		
	}
	
	

}
