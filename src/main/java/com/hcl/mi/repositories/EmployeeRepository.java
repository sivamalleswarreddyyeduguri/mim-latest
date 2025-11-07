package com.hcl.mi.repositories;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hcl.mi.entities.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Serializable>{

}
