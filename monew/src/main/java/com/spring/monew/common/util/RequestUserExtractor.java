package com.spring.monew.common.util;

import com.spring.monew.auth.config.HeaderUserAuthentication;
import java.security.Principal;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RequestUserExtractor {

  public UUID extractUserId(Principal principal) {
    if (principal instanceof HeaderUserAuthentication auth) {
      String userIdStr = (String) auth.getPrincipal();
      if (userIdStr != null && !userIdStr.isBlank()) {
        try {
          return UUID.fromString(userIdStr);
        } catch (IllegalArgumentException ignored) {
          return null;
        }
      }
    }
    return null;
  }

}
