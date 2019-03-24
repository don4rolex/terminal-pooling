package com.andrew.server.service;

import com.andrew.server.dto.Payload;

/**
 * @author andrew
 */
public interface ServerTerminalService {

  void logRequest(Payload payload);
}
