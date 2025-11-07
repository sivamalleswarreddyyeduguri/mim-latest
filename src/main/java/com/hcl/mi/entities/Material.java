package com.hcl.mi.entities;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
@Table
public class Material {
	
	@Id
	@Column(name = "mat_id")
	private String materialId;
	
	@Column(name = "mat_desc", unique = true)
	@Size(min = 5, max = 256, message = "material description should be greater than 5 char and less than 256 char")
	private String materialDesc;
	
	@NotBlank(message = "invalid material type")
	private String type;
	
	private boolean status = true;
	
	@JsonIgnore
	@OneToMany(mappedBy = "material", cascade = CascadeType.ALL)
	private List<MaterialInspectionCharacteristics> materialChar = new ArrayList<>();
	
	@JsonIgnore
	@OneToMany(mappedBy="material", cascade = CascadeType.ALL)
	private List<InspectionLot> inspectionLot;
}
