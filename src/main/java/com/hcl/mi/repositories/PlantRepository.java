package com.hcl.mi.repositories;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hcl.mi.entities.Plant;

@Repository
public interface PlantRepository extends JpaRepository<Plant, Serializable> {

	Optional<Plant> findByPlantName(String plantName);

}
 