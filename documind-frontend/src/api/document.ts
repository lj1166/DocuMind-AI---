/**
 * 文档管理 API 接口
 *
 * @author DocuMind Team
 */

import { http } from "@/utils/http";
import { getToken } from "@/utils/auth";

// 文档类型定义
export interface Document {
  id: number;
  agentId: number;
  fileName: string;
  fileType: string;
  fileSize: number;
  chunkCount: number;
  vectorStatus: number; // 0-待处理 1-处理中 2-已完成 3-失败
  vectorError?: string;
  createdAt: string;
  updatedAt: string;
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
 * 上传文档
 */
export const uploadDocument = (agentId: number, file: File) => {
  const formData = new FormData();
  formData.append("file", file);

  return http.request<Document>("post", `/api/agents/${agentId}/documents`, {
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data"
    }
  });
};

/**
 * 获取文档列表（分页）
 */
export const getDocuments = (
  agentId: number,
  page: number = 1,
  pageSize: number = 20,
  keyword?: string
) => {
  return http.request<PageResponse<Document>>(
    "get",
    `/api/agents/${agentId}/documents`,
    {
      params: { page, pageSize, keyword }
    }
  );
};

/**
 * 获取文档详情
 */
export const getDocumentDetail = (id: number) => {
  return http.request<Document>("get", `/api/documents/${id}`);
};

/**
 * 删除文档
 */
export const deleteDocument = (id: number) => {
  return http.request<void>("delete", `/api/documents/${id}`);
};

/**
 * 查询文档状态
 */
export const getDocumentStatus = (id: number) => {
  return http.request<Document>("get", `/api/documents/${id}/status`);
};

/**
 * 下载文档
 */
export const downloadDocument = (id: number, fileName: string) => {
  // 从Cookie获取token
  const tokenData = getToken();
  const token = tokenData?.accessToken || "";
  const url = `/api/documents/${id}/download`;

  // 创建隐藏的a标签进行下载
  const apiBaseUrl =
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
  const link = document.createElement("a");
  link.href = `${apiBaseUrl}/api${url}?token=${token}`;
  link.download = fileName;
  link.click();
};

/**
 * 格式化文件大小
 */
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + " " + sizes[i];
};

/**
 * 获取文件类型图标颜色
 */
export const getFileTypeColor = (fileType: string): string => {
  const colors: Record<string, string> = {
    pdf: "#f56c6c",
    doc: "#409eff",
    docx: "#409eff",
    txt: "#909399",
    md: "#67c23a"
  };
  return colors[fileType.toLowerCase()] || "#909399";
};

/**
 * 获取向量化状态信息
 */
export const getVectorStatusInfo = (status: number) => {
  const statusMap: Record<
    number,
    { text: string; type: string; color: string }
  > = {
    0: { text: "待处理", type: "info", color: "#909399" },
    1: { text: "处理中", type: "warning", color: "#e6a23c" },
    2: { text: "已完成", type: "success", color: "#67c23a" },
    3: { text: "失败", type: "danger", color: "#f56c6c" }
  };
  return statusMap[status] || statusMap[0];
};

/**
 * 预览文档
 */
export interface DocumentPreview {
  fileName: string;
  fileType: string;
  fileSize: number;
  previewType: string; // text/pdf/unsupported
  content?: string; // 文本内容（txt/md）
  downloadUrl?: string; // 下载链接（pdf/其他）
  message?: string;
}

export const previewDocument = (id: number) => {
  return http.request<DocumentPreview>("get", `/api/documents/${id}/preview`);
};

/**
 * 重新向量化文档
 */
export const reVectorizeDocument = (id: number) => {
  return http.request<Document>("post", `/api/documents/${id}/re-vectorize`);
};
