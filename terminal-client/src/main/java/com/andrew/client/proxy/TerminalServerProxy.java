package com.andrew.client.proxy;

import com.andrew.client.dto.Payload;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author andrew
 */
@FeignClient(name = "terminal-server-service")
public interface TerminalServerProxy {

  @PostMapping("/server")
  ResponseEntity logRequest(@RequestBody Payload payload);
}
