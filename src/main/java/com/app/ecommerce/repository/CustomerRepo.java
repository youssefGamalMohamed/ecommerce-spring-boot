package com.app.ecommerce.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.ecommerce.entity.Customer;

@Repository
public interface CustomerRepo extends JpaRepository<Customer,Long> {
	

}
