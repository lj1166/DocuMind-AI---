import { http } from "@/utils/http";

/**
 * API端点统计
 */
export interface ApiEndpointStats {
  endpoint: string;
  httpMethod: string;
  totalCalls: number;
  successCalls: number;
  failedCalls: number;
  avgResponseTime: number;
  maxResponseTime: number;
  minResponseTime: number;
}

/**
 * API总体统计
 */
export interface ApiOverallStats {
  totalCalls: number;
  successCalls: number;
  failedCalls: number;
  avgResponseTime: number;
  successRate: number;
}

/**
 * 获取API端点统计（今日）
 */
export function getApiEndpointStats() {
  return http.request<ApiEndpointStats[]>("get", "/api/monitor/endpoints");
}

/**
 * 获取API总体统计（今日）
 */
export function getApiOverallStats() {
  return http.request<ApiOverallStats>("get", "/api/monitor/overview");
}

/**
 * 获取今日调用量
 */
export function getTodayCallCount() {
  return http.request<number>("get", "/api/monitor/today/count");
}

/**
 * 获取今日成功率
 */
export function getTodaySuccessRate() {
  return http.request<number>("get", "/api/monitor/today/success-rate");
}
