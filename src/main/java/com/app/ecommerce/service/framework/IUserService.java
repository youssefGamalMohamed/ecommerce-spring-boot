package com.app.ecommerce.service.framework;

import com.app.ecommerce.entity.User;
import com.app.ecommerce.exception.models.IdNotFoundException;
import com.app.ecommerce.models.request.UserRequestBody;
import com.app.ecommerce.models.response.success.AddUserResponseBody;
import com.app.ecommerce.models.response.success.DeleteUserResponseBody;

public interface IUserService {
    AddUserResponseBody addNewUser(UserRequestBody userRequestBody);

    User getUserById(Long id) throws IdNotFoundException;

    DeleteUserResponseBody deleteUserById(Long id) throws IdNotFoundException;
}
