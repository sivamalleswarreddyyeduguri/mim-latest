package com.hcl.mi.repositories;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hcl.mi.entities.Material;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Serializable> {

	List<Material> findAll();

	Optional<Material> findByMaterialDesc(String materialDesc);

	List<Material> findAllByStatus(boolean b);

}
