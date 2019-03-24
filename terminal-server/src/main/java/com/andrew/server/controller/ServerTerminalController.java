package com.andrew.server.controller;

import com.andrew.server.dto.Payload;
import com.andrew.server.service.ServerTerminalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author andrew
 */
@RestController
@RequestMapping(value = "/server")
public class ServerTerminalController {

  private final ServerTerminalService terminalService;

  public ServerTerminalController(ServerTerminalService terminalService) {
    this.terminalService = terminalService;
  }

  @PostMapping
  public ResponseEntity logRequest(@RequestBody Payload payload) {
    terminalService.logRequest(payload);

    return ResponseEntity.ok().build();
  }
}
