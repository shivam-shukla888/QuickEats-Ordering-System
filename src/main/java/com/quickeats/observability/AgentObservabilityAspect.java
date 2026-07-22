package com.quickeats.observability;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class AgentObservabilityAspect {

    private static final Logger logger = LoggerFactory.getLogger(AgentObservabilityAspect.class);

    @Autowired
    private AgentCallLogRepository agentCallLogRepository;

    @Around("execution(* com.quickeats.agent.OrderingAgentService.*(..)) || " +
            "execution(* com.quickeats.rag.RecommendationService.*(..)) || " +
            "execution(* com.quickeats.support.SupportAgentService.*(..)) || " +
            "execution(* com.quickeats.orchestrator.AgentOrchestrator.*(..))")
    public Object logAgentCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String targetClassName = joinPoint.getTarget().getClass().getSimpleName();

        String agentType = "ORDERING";
        if (targetClassName.contains("Recommendation")) {
            agentType = "RAG";
        } else if (targetClassName.contains("Support")) {
            agentType = "SUPPORT";
        } else if (targetClassName.contains("Orchestrator")) {
            agentType = "ORCHESTRATOR";
        }

        Object[] args = joinPoint.getArgs();
        String prompt = (args != null && args.length > 0 && args[0] != null) ? args[0].toString() : "N/A";
        String toolsInvoked = methodName;

        boolean success = false;
        String errorMessage = null;
        Object result = null;

        try {
            result = joinPoint.proceed();
            success = true;
            return result;
        } catch (Throwable t) {
            errorMessage = t.getMessage();
            throw t;
        } finally {
            long latencyMs = System.currentTimeMillis() - startTime;
            try {
                // Estimate token usage (rough approximation: ~1 token per 4 chars of prompt + output)
                int estimatedTokens = (prompt.length() / 4) + (result != null ? result.toString().length() / 4 : 20);

                AgentCallLog log = new AgentCallLog(
                        agentType,
                        1L, // Anonymous or default user
                        prompt.length() > 500 ? prompt.substring(0, 500) + "..." : prompt,
                        toolsInvoked,
                        latencyMs,
                        estimatedTokens,
                        success,
                        errorMessage
                );
                agentCallLogRepository.save(log);
                logger.info("⚡ [AgentObservability] Logged AI Agent call: type={}, latency={}ms, success={}", agentType, latencyMs, success);
            } catch (Exception e) {
                logger.warn("Failed to log agent call metric gracefully: {}", e.getMessage());
            }
        }
    }
}
