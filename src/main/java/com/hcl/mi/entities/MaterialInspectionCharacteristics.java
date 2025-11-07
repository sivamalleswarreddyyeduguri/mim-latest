package com.hcl.mi.entities;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "mat_isp_ch")
@Getter 
@Setter
public class MaterialInspectionCharacteristics {
	
	@Id	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chare_seq")
	@SequenceGenerator(name = "chare_seq", sequenceName = "chare_sequence", initialValue = 101, allocationSize = 1)
	@Column(name = "ch_id")
	private Integer characteristicId;
	 
	@Column(name = "ch_desc")
	private String characteristicDescription;
	
	@Column(name = "tol_ul")
	private Double upperToleranceLimit;
	
	@Column(name = "tol_ll")
	private Double lowerToleranceLimit;
	
	@Column(name = "uom")
	private String unitOfMeasure;
	
	@ManyToOne
	@JsonIgnore
	private Material material;
	
	
	@JsonIgnore
	@OneToMany(mappedBy="materialInspectionCharacteristics", cascade = CascadeType.ALL)
	private List<InspectionActuals> inspectionActuals = new ArrayList<>();

}
