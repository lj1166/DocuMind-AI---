/**
 * 会话管理 API 接口
 *
 * @author DocuMind Team
 */

import { http } from "@/utils/http";

// 会话类型定义
export interface ChatSession {
  id: number;
  sessionId: string;
  agentId: number;
  userId: number;
  title: string;
  messageCount: number;
  lastMessageAt?: string;
  createdAt: string;
}

// 创建会话请求
export interface SessionCreateRequest {
  agentId: number;
  title?: string;
}

// 分页响应
export interface PageResponse<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
  pages: number;
}

/**
 * 创建会话
 */
export const createSession = (agentId: number, title?: string) => {
  return http.request<ChatSession>("post", `/api/agents/${agentId}/sessions`, {
    data: { agentId, title }
  });
};

/**
 * 获取会话列表（分页）
 */
export const getSessions = (
  agentId: number,
  page: number = 1,
  pageSize: number = 20,
  keyword?: string
) => {
  return http.request<PageResponse<ChatSession>>(
    "get",
    `/api/agents/${agentId}/sessions`,
    {
      params: { page, pageSize, keyword }
    }
  );
};

/**
 * 获取会话详情
 */
export const getSessionDetail = (id: number) => {
  return http.request<ChatSession>("get", `/api/sessions/${id}`);
};

/**
 * 通过sessionId获取会话详情
 */
export const getSessionBySessionId = (sessionId: string) => {
  return http.request<ChatSession>(
    "get",
    `/api/sessions/by-session-id/${sessionId}`
  );
};

/**
 * 更新会话标题
 */
export const updateSessionTitle = (id: number, title: string) => {
  return http.request<ChatSession>("put", `/api/sessions/${id}`, {
    params: { title }
  });
};

/**
 * 删除会话
 */
export const deleteSession = (id: number) => {
  return http.request<void>("delete", `/api/sessions/${id}`);
};
