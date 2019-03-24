package com.andrew.server.repository;

import com.andrew.server.model.Terminal;
import org.springframework.data.repository.CrudRepository;

/**
 * @author andrew
 */
public interface ServerTerminalRepository extends CrudRepository<Terminal, String> {

}