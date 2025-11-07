package com.hcl.mi.repositories;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hcl.mi.entities.Vendor;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Serializable> {
	
	@Query(value = "select * from vendor where status = :st", nativeQuery = true)
	List<Vendor> findAllActiveVendors(boolean st);

	boolean existsByName(String name);

	boolean existsByEmail(String email);

}
  