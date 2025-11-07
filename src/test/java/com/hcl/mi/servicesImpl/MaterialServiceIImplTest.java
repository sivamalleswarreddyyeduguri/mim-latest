package com.hcl.mi.servicesImpl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.hcl.mi.entities.Material;
import com.hcl.mi.repositories.MaterialRepository;
import com.hcl.mi.servicesImpl.MaterialServiceIImpl;

@ExtendWith(SpringExtension.class)
class MaterialServiceIImplTest {
	
	@InjectMocks
	private MaterialServiceIImpl materialService;
	
	@Mock
	private MaterialRepository materialRepository;

	@BeforeEach
	void setUp() throws Exception {
		
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void test_GetMaterial() {
		
		Material material = new Material();
		material.setMaterialId("M-01");
		material.setMaterialDesc("GRAPHITE GASKET 101");
		material.setType("RAW MATERILA");
		material.setStatus(false);
		
		Optional<Material> expectedMaterial = Optional.of(material);
		
		when(materialRepository.findById("M-01")).thenReturn(expectedMaterial);
		
		Material actual = materialService.getMaterial("M-01");
		
//		assertNull(actual);
//		assertEquals(expectedMaterial.get(), actual);
//		assertAll(
//				() -> assertEquals(expectedMaterial.get(), actual)
//				);
		
	}

}
