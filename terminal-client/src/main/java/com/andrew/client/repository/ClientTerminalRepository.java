package com.andrew.client.repository;

import com.andrew.client.model.Terminal;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author andrew
 */
public interface ClientTerminalRepository extends CrudRepository<Terminal, String> {

  @Query("SELECT t FROM Terminal t WHERE t.inUse = false")
  List<Terminal> getAvailableTerminals();

  @Transactional
  @Modifying
  @Query("UPDATE Terminal t SET t.inUse =:inUse WHERE t.id = :terminalId")
  void setInUse(@Param("terminalId") String terminalId, @Param("inUse") boolean inUse);
}