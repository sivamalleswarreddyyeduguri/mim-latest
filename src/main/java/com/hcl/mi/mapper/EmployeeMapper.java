package com.hcl.mi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hcl.mi.entities.Employee;
import com.hcl.mi.responsedtos.EmployeeDTO;


@Mapper(componentModel = "spring")
public interface EmployeeMapper {
	
//	EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);
	
	@Mapping(source = "empName", target = "name")
	@Mapping(source = "departmentName", target = "deptName")
	@Mapping(target = "id", ignore = true) // not required but better to put
	Employee convertEmployeeDTOToEntity(EmployeeDTO empDTO);
	
	
}
 