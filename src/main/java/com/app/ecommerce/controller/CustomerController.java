package com.app.ecommerce.controller;

import com.app.ecommerce.entity.Customer;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.models.request.PostCategoryRequestBody;
import com.app.ecommerce.models.request.PutCategoryRequestBody;
import com.app.ecommerce.repository.CustomerRepo;
import com.app.ecommerce.service.framework.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class CustomerController {

	@Autowired
	private CustomerRepo customerRepo;
	
    @PostMapping("/customers")
    public ResponseEntity<?> addNewCustomer() {
    	
    	customerRepo.save(Customer.builder().name("Youssef").build());
    	return ResponseEntity.ok("DONE");
    }
}
