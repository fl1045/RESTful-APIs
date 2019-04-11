package com.appsdeveloperblog.app.ws.ui.controller;


import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appsdeveloperblog.app.ws.exceptions.UserServiceException;
import com.appsdeveloperblog.app.ws.service.AddressesService;
import com.appsdeveloperblog.app.ws.service.UserService;
import com.appsdeveloperblog.app.ws.shared.dto.AddressDTO;
import com.appsdeveloperblog.app.ws.shared.dto.UserDto;
import com.appsdeveloperblog.app.ws.ui.model.request.PasswordResetModel;
import com.appsdeveloperblog.app.ws.ui.model.request.PasswordResetRequestModel;
import com.appsdeveloperblog.app.ws.ui.model.request.UserDetailsRequestModel;
import com.appsdeveloperblog.app.ws.ui.model.response.AddressesRest;
import com.appsdeveloperblog.app.ws.ui.model.response.ErrorMessages;
import com.appsdeveloperblog.app.ws.ui.model.response.OperationStatusModel;
import com.appsdeveloperblog.app.ws.ui.model.response.RequestOperationName;
import com.appsdeveloperblog.app.ws.ui.model.response.RequestOperationStatus;
import com.appsdeveloperblog.app.ws.ui.model.response.UserRest;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;


@RestController
@RequestMapping("/users")    //http://localhost:8080/users/
public class UserController {
	
	@Autowired
	UserService userService;
	
	@Autowired
	AddressesService addressesService;
	
	@ApiImplicitParams({
		@ApiImplicitParam(name="authorization", value="${userController.authorizationHeader.description}", paramType="header")
	})
	@GetMapping(path="/{id}", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	public UserRest getUser(@PathVariable String id) {
		UserDto userDto = userService.getUserByUserId(id);
		
		return new ModelMapper().map(userDto,UserRest.class);
	}
		
	
	@PostMapping(
			consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) throws Exception {
		
		if(userDetails.getFirstName().isEmpty()) 
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
		
		// for return purpose
		UserRest returnValue = new UserRest();
		
		
		//UserDto userDto = new UserDto();
		//BeanUtils.copyProperties(userDetails, userDto);
		
		ModelMapper modelMapper = new ModelMapper();
		UserDto userDto = modelMapper.map(userDetails, UserDto.class);
		
		
		UserDto createdUser = userService.createUser(userDto);
		returnValue = modelMapper.map(createdUser,UserRest.class);
		return returnValue;
	}
	
	@ApiImplicitParams({
		@ApiImplicitParam(name="authorization", value="${userController.authorizationHeader.description}", paramType="header")
	})
	@PutMapping(path="/{id}",
			consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	public UserRest updateUser(@RequestBody UserDetailsRequestModel userDetails, @PathVariable String id) {
		if(userDetails.getFirstName().isEmpty()) 
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
		
		UserRest returnValue = new UserRest();
		UserDto userDto = new UserDto();
		//populate userDto using userDetails
		BeanUtils.copyProperties(userDetails, userDto);
		UserDto createdUser = userService.updateUser(id, userDto);
		BeanUtils.copyProperties(createdUser, returnValue);
		return returnValue;
	}
	
	@ApiImplicitParams({
		@ApiImplicitParam(name="authorization", value="${userController.authorizationHeader.description}", paramType="header")
	})
	@DeleteMapping(path="/{id}", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	public OperationStatusModel deleteUser(@PathVariable String id) {
		OperationStatusModel returnValue = new OperationStatusModel();
		returnValue.setOperationName(RequestOperationName.DELETE.name());
		userService.deleteUser(id);
		returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		return returnValue;
	}
	
	@ApiImplicitParams({
		@ApiImplicitParam(name="authorization", value="${userController.authorizationHeader.description}", paramType="header")
	})
	@GetMapping(produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	public List<UserRest> getUsers(@RequestParam(value="page", defaultValue="0") int page,
									@RequestParam(value="limit", defaultValue="10") int limit) {
		List<UserRest> returnValue = new ArrayList<>();
		List<UserDto> users = userService.getUsers(page, limit);
		for(UserDto userDto:users) {
			UserRest userModel = new UserRest();
			BeanUtils.copyProperties(userDto, userModel);
			returnValue.add(userModel);
		}
		
		return returnValue;
	}
	
	@ApiImplicitParams({
		@ApiImplicitParam(name="authorization", value="${userController.authorizationHeader.description}", paramType="header")
	})
	@GetMapping(path="/{id}/addresses", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	public List<AddressesRest> getUserAddresses(@PathVariable String id) {
		
		List<AddressesRest> returnValue = new ArrayList<>();
		
		//find user records based on userId, return AddressDto
		List<AddressDTO> addressesDTO = addressesService.getAddresses(id);
		
		if(addressesDTO !=null && !addressesDTO.isEmpty())
		{
		java.lang.reflect.Type listType = new TypeToken<List<AddressesRest>>() {}.getType();
		returnValue = new ModelMapper().map(addressesDTO,listType);
		}
		
		return returnValue;
	}
	
	@ApiImplicitParams({
		@ApiImplicitParam(name="authorization", value="${userController.authorizationHeader.description}", paramType="header")
	})
	@GetMapping(path="/{id}/addresses/{addressId}", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	public AddressesRest getUserAddress(@PathVariable String addressId) {
		AddressDTO addressDTO = addressesService.getAddress(addressId);
		ModelMapper modelMapper = new ModelMapper();
		return modelMapper.map(addressDTO,AddressesRest.class);
	}
	
	
	@GetMapping(path="/email-verification", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	public OperationStatusModel verifyEmailToken(@RequestParam(value="token") String token) {
		OperationStatusModel returnValue = new OperationStatusModel();
		returnValue.setOperationName(RequestOperationName.VERIFY_EMAIL.name());
		
		boolean isVerified = userService.verifyEmailToken(token);
		if(isVerified) {
			returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		}
		else
		{
			returnValue.setOperationResult(RequestOperationStatus.ERROR.name());			
		}
		
		return returnValue;
		
	}
	
	@PostMapping(path="/password-reset-request", 
			consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	public OperationStatusModel requestRest(@RequestBody PasswordResetRequestModel passwordResetRequestModel) {
		
		OperationStatusModel returnValue = new OperationStatusModel();
		boolean operationResult = userService.requestPasswordReset(passwordResetRequestModel.getEmail());
		returnValue.setOperationName(RequestOperationName.REQUEST_PASSWORD_RESET.name());
		returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
		
		if(operationResult) {
			returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		}
		
		return returnValue;
	}
	
	@PostMapping(path="/password-reset",
			consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	public OperationStatusModel resetPassword(@RequestBody PasswordResetModel passwordResetModel){
		OperationStatusModel returnValue = new OperationStatusModel();
		boolean operationResult = userService.resetPassword(
				passwordResetModel.getToken(),
				passwordResetModel.getPassword());
		
		
		returnValue.setOperationName(RequestOperationName.PASSWORD_RESET.name());
		returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
		
		if(operationResult) {
			returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		}
		
		return returnValue;
		
	}
	
	
}
 