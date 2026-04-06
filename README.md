# Ecommerce-Spring-Boot
Backend E-commerce Application that enables Customers to buy products online and helps them to select from different Categories on the website

	
   ### Technologies ###
	 - Framework: Spring Boot 4.0.5
	 - Security: Spring Security 7.x + JJWT 0.12.6 (JWT stateless auth)
	 - ORM: Spring Data JPA + Hibernate 7.x
	 - DB: MySQL 8 (MySQL Connector/J 9.6.0)
	 - Cache: Redis (Lettuce, Spring Boot managed)
	 - Mapping: MapStruct 1.6.3
	 - Boilerplate: Lombok 1.18.44
	 - API Docs: SpringDoc OpenAPI 3.0.2 (Swagger UI)
	 - Monitoring: Spring Boot Actuator
	
   ### Swagger UI ###
	 
![1](https://github.com/youssefGamalMohamed/Ecommerce-Spring-Boot-App/assets/47324621/c991e6e9-a587-44b8-8766-7ff90f40ef25)
![2](https://github.com/youssefGamalMohamed/Ecommerce-Spring-Boot-App/assets/47324621/bc6969f1-b82d-4a59-9cef-e7b8efcf8834)
![3](https://github.com/youssefGamalMohamed/Ecommerce-Spring-Boot-App/assets/47324621/7b458b01-9e5c-49e0-9a12-04bdf61a0f47)
![4](https://github.com/youssefGamalMohamed/Ecommerce-Spring-Boot-App/assets/47324621/54ab3e23-8c76-4f1c-8b33-5e6a013dd79c)
![5](https://github.com/youssefGamalMohamed/Ecommerce-Spring-Boot-App/assets/47324621/c904f0f6-ead8-4ec2-8c1e-4563ee61085e)



  ### Installation ###
	 - Database
	  . Create DB Schema on MySQL named "ecommerce"
	  . Default connection: localhost:3306, user: root, password: 1234 (see application.yml)

	 - Redis
	  . Start Redis on localhost:6379 (default, no auth required for local dev)


### Postman Collection ###
  - Download Postman Collection from: [here](https://github.com/youssefGamalMohamed/Ecommerce-Spring-Boot-App/blob/main/Ecommerce.postman_collection.json)

---

## Implementation Notes

### saveAndFlush() for Optimistic Locking (007-spring-boot-upgrade)
All write operations on entities with `@Version` (optimistic locking) use `saveAndFlush()` instead of `save()` to ensure the version is immediately incremented and returned in the response. This prevents stale version values in API responses.

**Root cause**: `save()` defers the flush to transaction commit, so the returned entity still has the old version. `saveAndFlush()` forces an immediate DB write.

**Files changed**:
- `src/main/java/com/app/ecommerce/category/CategoryServiceImpl.java` - line 123
- `src/main/java/com/app/ecommerce/product/ProductServiceImpl.java` - line 113
- `src/main/java/com/app/ecommerce/order/OrderServiceImpl.java` - lines 72, 76, 125
- `src/main/java/com/app/ecommerce/cart/CartServiceImpl.java` - lines 79, 113, 137

### Jackson Setup (007-spring-boot-upgrade)
Spring Boot 4.0.5 uses Jackson 3 (`tools.jackson.*`), but Jackson 3's datatype modules (`tools.jackson.datatype:jackson-datatype-jsr310`) are not yet stable (only RC versions available).

**Current setup**: Uses Jackson 2 datatype module transitively from `jjwt-jackson`. This works because:
- Spring Boot's `ObjectMapper` auto-configuration uses Jackson 3 core
- Jackson 2 and Jackson 3 have different package names (`com.fasterxml.jackson` vs `tools.jackson`) so they coexist
- JJWT's serialization uses its own Jackson 2 integration

**Note**: If date serialization issues arise, consider upgrading to `tools.jackson.datatype:jackson-datatype-jsr310:3.x` once stable versions are released.
