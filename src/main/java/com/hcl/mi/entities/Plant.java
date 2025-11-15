package com.hcl.mi.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;


@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "plantName"}))
public class Plant extends BaseEntity{

	@Id
	private String plantId;
	
	private String plantName;
	
	private boolean status = true;
	
	private String state;
	
	private String city;
	
	@JsonIgnore
	@OneToMany(mappedBy="plant", cascade = CascadeType.ALL)
	private List<InspectionLot> inspectionLot;
	
}
