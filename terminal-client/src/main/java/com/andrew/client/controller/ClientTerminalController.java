package com.andrew.client.controller;


import com.andrew.client.service.ClientTerminalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author andrew
 */
@RestController
@RequestMapping(value = "/client")
public class ClientTerminalController {

  private final ClientTerminalService terminalService;

  public ClientTerminalController(ClientTerminalService terminalService) {
    this.terminalService = terminalService;
  }

  @GetMapping
  public ResponseEntity getTerminal() {

    return ResponseEntity.ok(terminalService.getTerminal());
  }
}
