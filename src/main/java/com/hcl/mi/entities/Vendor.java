package com.hcl.mi.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "email"}))
public class Vendor {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ven_seq")
	@SequenceGenerator(name = "ven_seq", sequenceName = "ven_sequence", initialValue = 5001, allocationSize = 1)
	private int vendorId;
	
	@Size(min=3, max = 50, message = "vendor name should be min 5 char and max 50")
	private String name;
	
	@Email(message = "please provide valid email")
	private String email;
	
	private boolean status = true;
	
	@JsonIgnore
	@OneToMany(mappedBy="vendor", cascade = CascadeType.ALL)
	private List<InspectionLot> inspectionLot;
}
