/**
 * 智能体 API 接口
 *
 * @author DocuMind Team
 */

import { http } from "@/utils/http";

// 智能体类型定义
export interface Agent {
  id: number;
  userId: number;
  name: string;
  description?: string;
  avatar?: string;
  roleName: string;
  systemPrompt: string;
  promptMode: string;
  modelName: string;
  temperature: number;
  maxTokens: number;
  topP: number;
  ragEnabled: number;
  ragTopK: number;
  ragSimilarityThreshold: number;
  documentCount: number;
  chatCount: number;
  sessionCount: number;
  templateCount: number;
  totalTokens: number;
  lastChatAt?: string;
  status: number;
  createdAt: string;
  updatedAt: string;
}

// 创建智能体请求
export interface AgentCreateRequest {
  name: string;
  description?: string;
  avatar?: string;
  roleName: string;
  systemPrompt: string;
  promptMode?: string;
  modelName?: string;
  temperature?: number;
  maxTokens?: number;
  topP?: number;
  ragEnabled?: number;
  ragTopK?: number;
  ragSimilarityThreshold?: number;
}

// 更新智能体请求
export interface AgentUpdateRequest {
  name?: string;
  description?: string;
  avatar?: string;
  roleName?: string;
  systemPrompt?: string;
  promptMode?: string;
  modelName?: string;
  temperature?: number;
  maxTokens?: number;
  topP?: number;
  ragEnabled?: number;
  ragTopK?: number;
  ragSimilarityThreshold?: number;
  status?: number;
}

// 分页响应
export interface PageResponse<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

// 分页查询参数
export interface AgentPageParams {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: string;
  status?: number;
  keyword?: string;
}

/**
 * 创建智能体
 */
export const createAgent = (data: AgentCreateRequest) => {
  return http.request<Agent>("post", "/api/agents", { data });
};

/**
 * 获取智能体列表（不分页）
 */
export const getAgentList = () => {
  return http.request<Agent[]>("get", "/api/agents/list");
};

/**
 * 分页查询智能体
 */
export const getAgentPage = (params: AgentPageParams) => {
  return http.request<PageResponse<Agent>>("get", "/api/agents", { params });
};

/**
 * 获取智能体详情
 */
export const getAgentDetail = (id: number) => {
  return http.request<Agent>("get", `/api/agents/${id}`);
};

/**
 * 更新智能体
 */
export const updateAgent = (id: number, data: AgentUpdateRequest) => {
  return http.request<Agent>("put", `/api/agents/${id}`, { data });
};

/**
 * 删除智能体
 */
export const deleteAgent = (id: number) => {
  return http.request<void>("delete", `/api/agents/${id}`);
};

/**
 * 切换智能体状态
 */
export const toggleAgentStatus = (id: number) => {
  return http.request<Agent>("patch", `/api/agents/${id}/status`);
};

/**
 * 统计智能体数量
 */
export const countAgents = () => {
  return http.request<number>("get", "/api/agents/count");
};
