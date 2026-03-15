/**
 * 聊天 API 接口
 *
 * @author DocuMind Team
 */

import { http } from "@/utils/http";
import { getToken } from "@/utils/auth";

// 对话历史类型
export interface ChatHistory {
  id: number;
  agentId: number;
  userId: number;
  sessionId: string;
  role: string; // user/assistant
  content: string;
  sources?: string; // JSON
  tokens?: number;
  createdAt: string;
}

// 发送消息请求
export interface ChatRequest {
  agentId: string;
  sessionId: string;
  message: string;
}

/**
 * 发送消息（同步）
 */
export const sendMessage = (
  agentId: number,
  sessionId: string,
  message: string
) => {
  return http.request<string>("post", "/api/chat/send", {
    data: {
      agentId: agentId.toString(),
      sessionId,
      message
    }
  });
};

/**
 * 发送消息（流式SSE）
 */
export const sendMessageStream = (
  agentId: number,
  sessionId: string,
  message: string,
  onMessage: (chunk: string) => void,
  onComplete: () => void,
  onError: (error: any) => void
) => {
  // 从Cookie获取token
  const tokenData = getToken();
  const token = tokenData?.accessToken || "";

  // 使用fetch API with SSE
  const apiBaseUrl =
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
  fetch(`${apiBaseUrl}/api/chat/stream`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`
    },
    body: JSON.stringify({
      agentId: agentId.toString(),
      sessionId,
      message
    })
  })
    .then(response => {
      // 检查响应状态
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      const reader = response.body?.getReader();
      if (!reader) {
        throw new Error("无法读取响应流");
      }

      const decoder = new TextDecoder();

      const readStream = () => {
        reader
          .read()
          .then(({ done, value }) => {
            if (done) {
              onComplete();
              return;
            }

            const chunk = decoder.decode(value, { stream: true });

            // 解析SSE格式：data: xxx
            const lines = chunk.split("\n");
            for (const line of lines) {
              if (line.startsWith("data:")) {
                const content = line.substring(5).trim(); // 去掉 "data:" 前缀
                if (content && content !== "[DONE]") {
                  onMessage(content);
                }
              }
            }

            readStream();
          })
          .catch(error => {
            onError(error);
          });
      };

      readStream();
    })
    .catch(error => {
      onError(error);
    });
};

/**
 * 获取对话历史
 */
export const getChatHistory = (sessionId: string) => {
  return http.request<ChatHistory[]>("get", `/api/chat/history/${sessionId}`);
};

/**
 * 删除单条消息
 */
export const deleteMessage = (messageId: number) => {
  return http.request<void>("delete", `/api/chat/history/${messageId}`);
};

/**
 * 重新生成回答
 */
export const regenerateMessage = (
  agentId: number,
  sessionId: string,
  onMessage: (chunk: string) => void,
  onComplete: () => void,
  onError: (error: any) => void
) => {
  const tokenData = getToken();
  const token = tokenData?.accessToken || "";

  const apiBaseUrl =
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
  fetch(`${apiBaseUrl}/api/chat/regenerate`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`
    },
    body: JSON.stringify({
      agentId: agentId.toString(),
      sessionId,
      message: "" // 不需要消息，后端会自动找到最后一条
    })
  })
    .then(response => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      const reader = response.body?.getReader();
      if (!reader) {
        throw new Error("无法读取响应流");
      }

      const decoder = new TextDecoder();

      const readStream = () => {
        reader
          .read()
          .then(({ done, value }) => {
            if (done) {
              onComplete();
              return;
            }

            const chunk = decoder.decode(value, { stream: true });
            const lines = chunk.split("\n");
            for (const line of lines) {
              if (line.startsWith("data:")) {
                const content = line.substring(5).trim();
                if (content && content !== "[DONE]") {
                  onMessage(content);
                }
              }
            }

            readStream();
          })
          .catch(error => {
            onError(error);
          });
      };

      readStream();
    })
    .catch(error => {
      onError(error);
    });
};
