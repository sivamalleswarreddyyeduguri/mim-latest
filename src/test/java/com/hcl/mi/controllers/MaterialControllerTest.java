package com.hcl.mi.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcl.mi.RespMsg;
import com.hcl.mi.controllers.MaterialController;
import com.hcl.mi.services.MaterialService;

@WebMvcTest(MaterialController.class)
class MaterialControllerTest {

	@BeforeEach
	void setUp() throws Exception {
		
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Autowired
	private MockMvc mockMvc;
  
	@MockBean
	private MaterialService materialService;
	
	public static String asJsonString(final Object obj) {
	    try {
	    	String writeValueAsString = new ObjectMapper().writeValueAsString(obj);
	        return writeValueAsString;
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}

	@Test
	void test_GetMaterial() throws Exception {

		String matId = "M-01";
		when(materialService.getMaterial(matId)).thenReturn(null);
		
// Approach 1 taking json object as string format
		
//		String expected = "{\"message\" : \"resource not found\"}";
//		ObjectMapper mapper = new ObjectMapper();
////		String readingFromJson = mapper.readTree(expected).path("message").asText();
////		System.out.println(readingFromJson);
//		
//		MvcResult result = mockMvc.perform(MockMvcRequestBuilders
//				.get("/material/{id}", matId)
//				.contentType(MediaType.APPLICATION_JSON))
//		        .andExpect(status().isNotFound())
//		        .andExpect(MockMvcResultMatchers.jsonPath("message", is(mapper.readTree(expected).path("message").asText())))
//		        .andReturn();
//		
//		String actual = result.getResponse().getContentAsString();
//		System.out.println(actual + "==============");
		
// Approach 2 	taking java object	
		
		RespMsg msg = new RespMsg("message", "resource not found", 404);
		ObjectMapper mapper = new ObjectMapper();
		String writeValueAsString = mapper.writeValueAsString(msg);
		System.out.println(writeValueAsString);
		
	
		
		
		JSONObject jsonObject = new JSONObject();
//		jsonObject.put("country", "INDIA");
//		jsonObject.put("name", "Harinath");
//		jsonObject.put("Qualifications", List.of("10th", "12th", "15th"));
		
//		jsonObject.put("message", "resource not found");
		
//		jsonObject.put(writeValueAsString, true);
		System.out.println(jsonObject.toString());
		System.out.println(jsonObject.getString("key"));
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders
		.get("/material/{id}", matId)
		.contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
//        .andExpect(MockMvcResultMatchers.jsonPath("$", is(jsonObject.toString())))
        .andReturn();
		
		String actual = result.getResponse().getContentAsString();
		System.out.println(actual);
		
		assertEquals(jsonObject.toString(), actual);
		
	}

//	@Test
//	void test_GetMaterialCharcteristics()throws Exception {
//		String matId = "M-01";
//		when(materialService.getMaterial(matId)).thenReturn(null);
//		
//		mockMvc.perform(get("/material/view/char?materialId=", matId))
//		       .andDo(print())
//		       .andExpect(status().isOk());
//	}

}


