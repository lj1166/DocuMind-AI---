import { http } from "@/utils/http";

/**
 * 统计概览响应
 */
export interface StatisticsOverview {
  totalAgents: number;
  totalDocuments: number;
  totalChats: number;
  totalSessions: number;
  totalTemplates: number;
  totalTokens: number;
  todayChats: number;
  todayTokens: number;
  weekChats: number;
  weekTokens: number;
}

/**
 * 智能体统计响应
 */
export interface AgentStatistics {
  agentId: number;
  agentName: string;
  chatCount: number;
  sessionCount: number;
  documentCount: number;
  templateCount: number;
  totalTokens: number;
  lastChatAt: string;
}

/**
 * 获取统计概览
 */
export function getStatisticsOverview() {
  return http.request<StatisticsOverview>("get", "/api/statistics/overview");
}

/**
 * 获取智能体排行
 */
export function getAgentRankings(rankBy = "chatCount", limit = 10) {
  return http.request<AgentStatistics[]>(
    "get",
    "/api/statistics/agents/rankings",
    {
      params: { rankBy, limit }
    }
  );
}

/**
 * 获取智能体统计
 */
export function getAgentStatistics(agentId: number) {
  return http.request<AgentStatistics>(
    "get",
    `/api/statistics/agents/${agentId}`
  );
}
