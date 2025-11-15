package com.hcl.mi.exceptions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.hcl.mi.requestdtos.ErrorResponseDto;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
 
	
	@ExceptionHandler(DuplicateCharacteristicException.class)
	public ResponseEntity<ErrorResponseDto> handleDuplicateCharacteristicException(DuplicateCharacteristicException exception,
			WebRequest webRequest) {
		ErrorResponseDto errorResponseDTO = new ErrorResponseDto(webRequest.getDescription(false), HttpStatus.CONFLICT,
				exception.getMessage(), LocalDateTime.now()); 
		return new ResponseEntity<>(errorResponseDTO, HttpStatus.CONFLICT); 
	}  
	
	@ExceptionHandler(GenericAlreadyExistsException.class) 
	public ResponseEntity<ErrorResponseDto> handlePlantAlreadyExistsException(GenericAlreadyExistsException exception,
			WebRequest webRequest) {
		ErrorResponseDto errorResponseDTO = new ErrorResponseDto(webRequest.getDescription(false), HttpStatus.CONFLICT,
				exception.getMessage(), LocalDateTime.now()); 
		return new ResponseEntity<>(errorResponseDTO, HttpStatus.CONFLICT); 
	} 
	
	@ExceptionHandler(GenericNotFoundException.class) 
	public ResponseEntity<ErrorResponseDto> handleGenericException(GenericNotFoundException exception,
			WebRequest webRequest) {
		ErrorResponseDto errorResponseDTO = new ErrorResponseDto(webRequest.getDescription(false), HttpStatus.NOT_FOUND,
				exception.getMessage(), LocalDateTime.now());  
		return new ResponseEntity<>(errorResponseDTO, HttpStatus.NOT_FOUND); 
	} 
	 

	
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<?> failureJSONConverter(RuntimeException ex){
		List<String> errors = new ArrayList<>();
		
		errors.add(ex.getMessage());

	    Map<String, List<String>> result = new HashMap<>();
	    result.put("errors", errors);

	    return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleExceptions(Exception ex){
		List<String> errors = new ArrayList<>();
		
		errors.add(ex.getMessage());

	    Map<String, List<String>> result = new HashMap<>();
	    result.put("errors", errors);

	    return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
	}
	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
	                                                              HttpHeaders headers,
	                                                              HttpStatusCode status,
	                                                              WebRequest request) {
	    Map<String, String> validationErrors = new HashMap<>();

	    for (ObjectError error : ex.getBindingResult().getAllErrors()) {
	        if (error instanceof FieldError fieldError) {
	            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
	        } else {
	            validationErrors.put("startDate", error.getDefaultMessage());
	        }
	    }

	    return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);
	}

}
