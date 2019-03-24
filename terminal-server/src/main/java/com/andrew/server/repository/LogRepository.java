package com.andrew.server.repository;

import com.andrew.server.model.Log;
import org.springframework.data.repository.CrudRepository;

/**
 * @author andrew
 */
public interface LogRepository extends CrudRepository<Log, Long> {

}