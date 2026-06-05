package com.binarylabyrinth.adminservice.client;

import com.binarylabyrinth.adminservice.dto.external.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/users")
    List<UserDto> listAllUsers();
}
