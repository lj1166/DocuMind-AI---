/**
 * 提示词模板 API 接口
 *
 * @author DocuMind Team
 */

import { http } from "@/utils/http";

// 提示词模板类型定义
export interface PromptTemplate {
  id: number;
  agentId: number;
  templateName: string;
  templateType: string; // default-默认, scenario-场景, task-任务
  promptContent: string;
  description?: string;
  triggerCondition?: string; // JSON格式
  priority: number;
  isActive: number; // 0-禁用 1-启用
  usageCount: number;
  lastUsedAt?: string;
  createdAt: string;
  updatedAt: string;
}

// 创建模板请求
export interface TemplateCreateRequest {
  agentId: number;
  templateName: string;
  templateType: string;
  promptContent: string;
  description?: string;
  triggerCondition?: string;
  priority?: number;
}

// 更新模板请求
export interface TemplateUpdateRequest {
  templateName?: string;
  templateType?: string;
  promptContent?: string;
  description?: string;
  triggerCondition?: string;
  priority?: number;
  isActive?: number;
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
 * 创建提示词模板
 */
export const createTemplate = (
  agentId: number,
  data: Omit<TemplateCreateRequest, "agentId">
) => {
  return http.request<PromptTemplate>(
    "post",
    `/api/agents/${agentId}/templates`,
    {
      data: { ...data, agentId }
    }
  );
};

/**
 * 获取模板列表（分页）
 */
export const getTemplates = (
  agentId: number,
  page: number = 1,
  pageSize: number = 20,
  sortBy: string = "priority"
) => {
  return http.request<PageResponse<PromptTemplate>>(
    "get",
    `/api/agents/${agentId}/templates`,
    {
      params: { page, pageSize, sortBy }
    }
  );
};

/**
 * 获取模板详情
 */
export const getTemplateDetail = (id: number) => {
  return http.request<PromptTemplate>("get", `/api/templates/${id}`);
};

/**
 * 更新模板
 */
export const updateTemplate = (id: number, data: TemplateUpdateRequest) => {
  return http.request<PromptTemplate>("put", `/api/templates/${id}`, { data });
};

/**
 * 删除模板
 */
export const deleteTemplate = (id: number) => {
  return http.request<void>("delete", `/api/templates/${id}`);
};

/**
 * 复制模板
 */
export const copyTemplate = (id: number) => {
  return http.request<PromptTemplate>("post", `/api/templates/${id}/copy`);
};

/**
 * 获取模板类型标签
 */
export const getTemplateTypeTag = (type: string) => {
  const typeMap: Record<string, { text: string; color: string }> = {
    default: { text: "默认", color: "" },
    scenario: { text: "场景", color: "warning" },
    task: { text: "任务", color: "success" }
  };
  return typeMap[type] || typeMap["scenario"];
};
