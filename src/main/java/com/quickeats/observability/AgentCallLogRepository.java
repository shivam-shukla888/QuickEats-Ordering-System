package com.quickeats.observability;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgentCallLogRepository extends JpaRepository<AgentCallLog, Long> {

    List<AgentCallLog> findTop50ByOrderByTimestampDesc();

    List<AgentCallLog> findByAgentType(String agentType);

    List<AgentCallLog> findByTimestampAfter(LocalDateTime since);

    @Query("SELECT COUNT(a) FROM AgentCallLog a WHERE a.success = true")
    long countSuccessfulCalls();

    @Query("SELECT AVG(a.latencyMs) FROM AgentCallLog a")
    Double getAverageLatency();
}
