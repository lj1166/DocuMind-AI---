<template>
  <div class="chat-container">
    <!-- 侧边栏 - 智能体信息 -->
    <div v-loading="agentLoading" class="chat-sidebar">
      <div v-if="!agentLoading && agentData.id" class="agent-info">
        <el-avatar :size="60" :src="agentData.avatar">
          {{ agentData.name?.charAt(0) }}
        </el-avatar>
        <h3>{{ agentData.name }}</h3>
        <el-tag type="primary" size="small">{{ agentData.roleName }}</el-tag>
        <p class="description">{{ agentData.description }}</p>

        <div class="stats">
          <div class="stat-item">
            <el-icon><Document /></el-icon>
            <span>{{ agentData.documentCount }} 文档</span>
          </div>
          <div class="stat-item">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ agentData.chatCount }} 对话</span>
          </div>
        </div>

        <el-button text @click="handleBack">
          <el-icon><Back /></el-icon>
          返回详情
        </el-button>
      </div>
    </div>

    <!-- 主聊天区域 -->
    <div class="chat-main">
      <!-- 聊天头部 -->
      <div class="chat-header">
        <div class="session-info">
          <h3>{{ sessionTitle }}</h3>
          <el-tag v-if="sessionData.sessionId" size="small">
            {{ messages.length }} 条消息
          </el-tag>
        </div>
        <div class="header-actions">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索对话内容"
            clearable
            style="width: 250px; margin-right: 10px"
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button text @click="handleClearHistory">
            <el-icon><Delete /></el-icon>
            清空对话
          </el-button>
        </div>
      </div>

      <!-- 消息列表 -->
      <div ref="messageListRef" class="message-list">
        <el-empty
          v-if="messages.length === 0"
          description="开始对话吧！"
          :image-size="120"
        />

        <div
          v-for="(msg, index) in messages"
          :key="msg.id || `temp-${index}-${msg.createdAt}`"
          class="message-item"
        >
          <div :class="['message', msg.role]">
            <div class="message-avatar">
              <el-avatar
                v-if="msg.role === 'user'"
                :size="36"
                :src="'/logo.jpg'"
              >
                {{ userInfo?.username?.charAt(0) || "U" }}
              </el-avatar>
              <el-avatar v-else :size="36" :src="agentData.avatar">
                {{ agentData.name?.charAt(0) }}
              </el-avatar>
            </div>
            <div class="message-content">
              <div class="message-header">
                <span class="sender">{{
                  msg.role === "user" ? "我" : agentData.name
                }}</span>
                <span class="time">{{ formatTime(msg.createdAt) }}</span>
              </div>
              <div
                class="message-text markdown-content"
                v-html="formatMessage(msg.content)"
              />

              <!-- 来源文档标签 -->
              <div
                v-if="msg.role === 'assistant' && msg.sources"
                class="message-sources"
              >
                <el-icon><Document /></el-icon>
                <span class="sources-label">参考文档：</span>
                <el-tag
                  v-for="(source, idx) in parseSources(msg.sources)"
                  :key="idx"
                  size="small"
                  type="info"
                  style="margin-right: 5px"
                >
                  {{ source }}
                </el-tag>
              </div>

              <div class="message-actions">
                <el-button
                  text
                  size="small"
                  @click="handleCopyMessage(msg.content)"
                >
                  <el-icon><DocumentCopy /></el-icon>
                  复制
                </el-button>
                <el-button
                  text
                  size="small"
                  @click="handleDeleteMessage(msg, index)"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
                <el-button
                  v-if="
                    msg.role === 'assistant' && index === messages.length - 1
                  "
                  text
                  size="small"
                  @click="handleRegenerateMessage()"
                >
                  <el-icon><Refresh /></el-icon>
                  重新生成
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 正在生成的消息（流式） - 纯文本显示 -->
        <div v-if="isGenerating" class="message-item">
          <div class="message assistant">
            <div class="message-avatar">
              <el-avatar :size="36" :src="agentData.avatar">
                {{ agentData.name?.charAt(0) }}
              </el-avatar>
            </div>
            <div class="message-content">
              <div class="message-header">
                <span class="sender">{{ agentData.name }}</span>
                <span class="time">正在输入...</span>
              </div>
              <div class="message-text streaming-preview">
                <template v-if="streamingMessage">
                  <!-- 流式中：纯文本显示，保留格式 -->
                  <pre
                    class="streaming-content">{{ streamingMessage }}<span class="cursor">|</span></pre>
                </template>
                <template v-else>
                  <el-icon class="is-loading"><Loading /></el-icon>
                  <span>正在思考中...</span>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input">
        <el-input
          v-model="userInput"
          type="textarea"
          :rows="4"
          placeholder="输入消息... (Ctrl+Enter 发送)"
          :disabled="isGenerating"
          @keydown.ctrl.enter="handleSend"
        />
        <div class="input-actions">
          <div class="left-actions">
            <el-upload
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleFileSelect"
              accept=".txt,.md,.pdf,.doc,.docx,.xls,.xlsx"
            >
              <el-tooltip
                content="支持 PDF、Word、Excel、TXT、Markdown (最大50MB)"
                placement="top"
              >
                <el-button text size="small">
                  <el-icon><Paperclip /></el-icon>
                  上传文件
                </el-button>
              </el-tooltip>
            </el-upload>
            <div v-if="selectedFile" class="selected-file">
              <el-tag closable @close="selectedFile = null">
                {{ selectedFile.name }}
              </el-tag>
            </div>
          </div>
          <div class="right-actions">
            <el-button :disabled="isGenerating" @click="userInput = ''">
              清空
            </el-button>
            <el-button
              type="primary"
              :loading="isGenerating"
              :disabled="!userInput.trim()"
              @click="handleSend"
            >
              <el-icon><Position /></el-icon>
              发送
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Document,
  ChatDotRound,
  Back,
  Delete,
  Position,
  Paperclip,
  Loading,
  DocumentCopy,
  Refresh,
  Search
} from "@element-plus/icons-vue";
import { getAgentDetail, type Agent } from "@/api/agent";
import {
  createSession,
  getSessionBySessionId,
  type ChatSession
} from "@/api/session";
import {
  sendMessageStream,
  getChatHistory,
  deleteMessage,
  regenerateMessage,
  type ChatHistory
} from "@/api/chat";
import { uploadDocument } from "@/api/document";
import type { UploadFile } from "element-plus";
import { marked } from "marked";

// 配置marked（需要在使用前配置）
marked.use({
  breaks: true, // 单个换行转<br>
  gfm: true, // GitHub风格Markdown
  headerIds: false, // 禁用header id
  mangle: false // 禁用邮箱混淆
});

const router = useRouter();
const route = useRoute();

const agentData = ref<Agent>({} as Agent);
const sessionData = ref<ChatSession>({} as ChatSession);
const sessionTitle = ref("新会话");
const messages = ref<ChatHistory[]>([]);
const allMessages = ref<ChatHistory[]>([]); // 所有消息（用于搜索）
const userInput = ref("");
const isGenerating = ref(false);
const messageListRef = ref<HTMLElement>();
const userInfo = ref(JSON.parse(localStorage.getItem("user-info") || "{}"));
const streamingMessage = ref(""); // 流式接收的消息
const agentLoading = ref(false); // 智能体加载状态
const selectedFile = ref<UploadFile | null>(null); // 选择的文件
const searchKeyword = ref(""); // 搜索关键词

// 加载智能体信息
const loadAgentData = async () => {
  agentLoading.value = true;
  try {
    const agentId = Number(route.params.id);
    agentData.value = await getAgentDetail(agentId);
  } catch (error) {
    console.error("加载智能体信息失败:", error);
    ElMessage.error("加载失败");
  } finally {
    agentLoading.value = false;
  }
};

// 加载或创建会话
const loadSession = async () => {
  try {
    const agentId = Number(route.params.id);
    const sessionId = route.params.sessionId as string;

    if (sessionId) {
      // 加载已有会话
      const session = await getSessionBySessionId(sessionId);
      sessionData.value = session;
      sessionTitle.value = session.title;

      // 加载对话历史
      await loadHistory(session.sessionId);
    } else {
      // 创建新会话
      const session = await createSession(agentId);
      sessionData.value = session;
      sessionTitle.value = session.title;

      // 更新URL（避免刷新时再次创建）
      router.replace(`/agents/${agentId}/chat/${session.sessionId}`);
    }
  } catch (error: any) {
    console.error("加载会话失败:", error);
    const errorMsg = error?.data?.message || "加载会话失败";
    ElMessage.error(errorMsg);
  }
};

// 加载对话历史
const loadHistory = async (sessionId: string) => {
  try {
    const history = await getChatHistory(sessionId);
    allMessages.value = history; // 保存所有消息
    messages.value = history; // 显示的消息
    nextTick(() => {
      scrollToBottom();
    });
  } catch (error: any) {
    console.error("加载历史失败:", error);
  }
};

// 搜索对话内容
const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    // 如果搜索关键词为空，显示所有消息
    messages.value = allMessages.value;
    return;
  }

  const keyword = searchKeyword.value.toLowerCase();
  messages.value = allMessages.value.filter(msg =>
    msg.content.toLowerCase().includes(keyword)
  );

  if (messages.value.length === 0) {
    ElMessage.info("没有找到匹配的消息");
  } else {
    ElMessage.success(`找到 ${messages.value.length} 条匹配消息`);
  }
};

// 文件选择
const handleFileSelect = async (file: UploadFile) => {
  // 验证文件大小
  if (file.size && file.size > 52428800) {
    ElMessage.error("文件不能超过50MB");
    return;
  }

  // 验证文件类型
  const validTypes = [".txt", ".md", ".pdf", ".doc", ".docx", ".xls", ".xlsx"];
  const fileExt = "." + file.name.split(".").pop()?.toLowerCase();
  if (!validTypes.includes(fileExt)) {
    ElMessage.error(
      "不支持的文件类型，仅支持：PDF、Word、Excel、TXT、Markdown"
    );
    return;
  }

  selectedFile.value = file;
  ElMessage.success(`已选择文件: ${file.name}，发送消息时会一起上传`);
};

// 发送消息（流式）
const handleSend = async () => {
  if (!userInput.value.trim() && !selectedFile.value) {
    ElMessage.warning("请输入消息或选择文件");
    return;
  }

  if (!sessionData.value.sessionId) {
    ElMessage.error("会话未初始化");
    return;
  }

  // 如果有文件，先上传
  if (selectedFile.value && selectedFile.value.raw) {
    try {
      ElMessage.info("正在上传文件...");
      await uploadDocument(agentData.value.id, selectedFile.value.raw);
      ElMessage.success("文件上传成功，正在向量化...");

      // 添加提示到消息
      const uploadMsg = userInput.value
        ? `${userInput.value}\n\n[已上传文件: ${selectedFile.value.name}]`
        : `[已上传文件: ${selectedFile.value.name}]`;
      userInput.value = uploadMsg;
      selectedFile.value = null;
    } catch (error: any) {
      ElMessage.error(
        "文件上传失败: " + (error?.data?.message || error?.message)
      );
      return;
    }
  }

  const userMessage = userInput.value;
  userInput.value = "";
  isGenerating.value = true;
  streamingMessage.value = ""; // 清空流式消息

  // 立即显示用户消息（临时），提供即时反馈
  const userMsg: ChatHistory = {
    id: 0,
    agentId: agentData.value.id,
    userId: 0,
    sessionId: sessionData.value.sessionId,
    role: "user",
    content: userMessage,
    createdAt: new Date().toISOString()
  };
  messages.value.push(userMsg);

  // 滚动到底部
  nextTick(() => {
    scrollToBottom();
  });

  // 使用流式API
  sendMessageStream(
    agentData.value.id,
    sessionData.value.sessionId,
    userMessage,
    // onMessage - 接收每个chunk
    (chunk: string) => {
      streamingMessage.value += chunk;
      nextTick(() => {
        scrollToBottom();
      });
    },
    // onComplete - 流结束
    async () => {
      // 流式完成后，只获取最新的消息来更新，而不是重新加载全部历史
      try {
        const history = await getChatHistory(sessionData.value.sessionId);

        // 找到最后2条消息（用户消息+AI回复）
        const latestMessages = history.slice(-2);

        // 替换临时消息：删除最后一条临时用户消息，添加数据库的真实消息
        if (
          messages.value.length > 0 &&
          messages.value[messages.value.length - 1].id === 0
        ) {
          messages.value.pop(); // 删除临时用户消息
        }

        // 添加数据库的真实消息（带真实ID，会触发重新渲染）
        messages.value.push(...latestMessages);
        allMessages.value = [...messages.value]; // 同步
      } catch (error) {
        console.error("获取最新消息失败:", error);
      }

      // 清空状态
      streamingMessage.value = "";
      isGenerating.value = false;

      nextTick(() => {
        scrollToBottom();
      });
    },
    // onError - 错误处理
    (error: any) => {
      console.error("发送消息失败:", error);
      const errorMsg = error?.message || "发送失败，请重试";
      ElMessage.error(errorMsg);

      // 显示错误消息
      const errorMsgObj: ChatHistory = {
        id: 0,
        agentId: agentData.value.id,
        userId: 0,
        sessionId: sessionData.value.sessionId,
        role: "assistant",
        content: `❌ 错误：${errorMsg}`,
        createdAt: new Date().toISOString()
      };
      messages.value.push(errorMsgObj);

      isGenerating.value = false;
      streamingMessage.value = "";
    }
  );
};

// 复制消息
const handleCopyMessage = async (content: string) => {
  try {
    await navigator.clipboard.writeText(content);
    ElMessage.success("已复制到剪贴板");
  } catch (error) {
    console.error("复制失败:", error);
    ElMessage.error("复制失败");
  }
};

// 刷新消息格式（强制重新渲染）
const handleRefreshMessage = (index: number) => {
  // 创建新数组触发重新渲染
  const newMessages = [...messages.value];
  const msg = newMessages[index];

  // 创建新对象，改变引用
  newMessages[index] = {
    ...msg,
    content: msg.content,
    // 添加时间戳确保key变化
    _refreshed: Date.now()
  };

  messages.value = newMessages;

  // 等待渲染完成
  nextTick(() => {
    ElMessage.success("格式已刷新");
  });
};

// 删除消息
const handleDeleteMessage = async (msg: ChatHistory, index: number) => {
  try {
    await ElMessageBox.confirm("确定要删除这条消息吗？", "删除确认", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    });

    await deleteMessage(msg.id);
    messages.value.splice(index, 1);
    ElMessage.success("消息已删除");
  } catch (error: any) {
    if (error !== "cancel") {
      console.error("删除消息失败:", error);
      ElMessage.error(error?.data?.message || "删除失败");
    }
  }
};

// 重新生成回答
const handleRegenerateMessage = async () => {
  if (!agentData.value?.id || !currentSessionId.value) {
    ElMessage.error("会话信息异常");
    return;
  }

  try {
    await ElMessageBox.confirm(
      "确定要重新生成最后一条回答吗？当前回答将被删除。",
      "重新生成确认",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }
    );

    // 删除最后一条AI回复
    const lastMessage = messages.value[messages.value.length - 1];
    if (lastMessage.role === "assistant") {
      messages.value.pop();
    }

    // 重置流式消息状态
    streamingMessage.value = "";
    isGenerating.value = true;

    regenerateMessage(
      agentData.value.id,
      currentSessionId.value,
      chunk => {
        streamingMessage.value += chunk;
        scrollToBottom();
      },
      async () => {
        // 流结束后，只获取最新的AI回复
        try {
          const history = await getChatHistory(currentSessionId.value!);
          const latestMessage = history[history.length - 1]; // 最新的AI回复

          if (latestMessage && latestMessage.role === "assistant") {
            messages.value.push(latestMessage); // 添加新的AI回复
            allMessages.value = [...messages.value]; // 同步
          }
        } catch (error) {
          console.error("获取最新消息失败:", error);
        }

        streamingMessage.value = "";
        isGenerating.value = false;
        scrollToBottom();

        ElMessage.success("回答已重新生成");
      },
      error => {
        console.error("重新生成失败:", error);
        ElMessage.error("重新生成失败: " + error.message);
        isGenerating.value = false;
      }
    );
  } catch (error: any) {
    if (error !== "cancel") {
      console.error("重新生成失败:", error);
    }
  }
};

// 清空对话历史
const handleClearHistory = () => {
  ElMessage.info("清空功能开发中...");
};

// 返回详情页
const handleBack = () => {
  router.push(`/agents/${route.params.id}`);
};

// 滚动到底部
const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight;
  }
};

// 格式化消息（Markdown渲染）
const formatMessage = (content: string) => {
  if (!content) return "";

  try {
    // 使用marked.parse而不是marked()
    const rendered = marked.parse(content, {
      async: false,
      breaks: true,
      gfm: true
    }) as string;

    // 调试：查看是否包含HTML标签
    const hasHTML = rendered.includes("<h") || rendered.includes("<table");
    console.log(
      "Markdown渲染:",
      hasHTML ? "✅有HTML标签" : "❌无HTML标签",
      rendered.substring(0, 200)
    );

    return rendered;
  } catch (error) {
    console.error("Markdown解析失败:", error);
    return content.replace(/\n/g, "<br>");
  }
};

// 解析来源文档（JSON字符串）
const parseSources = (sources: string): string[] => {
  try {
    if (!sources) return [];
    // 如果是逗号分隔的字符串
    if (typeof sources === "string" && !sources.startsWith("[")) {
      return sources
        .split(",")
        .map(s => s.trim())
        .filter(s => s);
    }
    // 如果是JSON数组
    const parsed = JSON.parse(sources);
    return Array.isArray(parsed) ? parsed : [];
  } catch (error) {
    console.error("解析来源文档失败:", error);
    return [];
  }
};

// 格式化时间
const formatTime = (time: string) => {
  const date = new Date(time);
  return date.toLocaleTimeString("zh-CN", {
    hour: "2-digit",
    minute: "2-digit"
  });
};

onMounted(() => {
  loadAgentData();
  loadSession();
});
</script>

<!-- 全局Markdown样式，不使用scoped -->
<style lang="scss">
@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }

  50% {
    opacity: 0;
  }
}

.markdown-content {
  h1,
  h2,
  h3,
  h4,
  h5,
  h6 {
    display: block !important;
    margin: 16px 0 8px !important;
    font-weight: 600 !important;
    line-height: 1.4 !important;
  }

  h1 {
    font-size: 20px !important;
  }

  h2 {
    font-size: 18px !important;
  }

  h3 {
    font-size: 16px !important;
  }

  h4 {
    font-size: 14px !important;
  }

  p {
    display: block !important;
    margin: 8px 0 !important;
    line-height: 1.6 !important;
  }

  table {
    display: table !important;
    width: 100% !important;
    margin: 12px 0 !important;
    border-collapse: collapse !important;

    thead {
      display: table-header-group !important;
    }

    tbody {
      display: table-row-group !important;
    }

    tr {
      display: table-row !important;
    }

    th,
    td {
      display: table-cell !important;
      padding: 8px 12px !important;
      border: 1px solid #dcdfe6 !important;
    }

    th {
      font-weight: 600 !important;
      background: #f5f7fa !important;
    }
  }

  ul,
  ol {
    display: block !important;
    padding-left: 24px !important;
    margin: 8px 0 !important;
  }

  li {
    display: list-item !important;
    margin: 4px 0 !important;
  }

  hr {
    display: block !important;
    margin: 16px 0 !important;
    border: none !important;
    border-top: 1px solid #e4e7ed !important;
  }

  strong {
    font-weight: 600 !important;
  }

  blockquote {
    display: block !important;
    padding: 8px 12px !important;
    margin: 12px 0 !important;
    background: #f0f9ff !important;
    border-left: 3px solid #409eff !important;
  }
}

.chat-container {
  display: flex;
  height: calc(100vh - 100px);
  background: #f5f7fa;

  .chat-sidebar {
    width: 280px;
    padding: 20px;
    overflow-y: auto;
    background: white;
    border-right: 1px solid #e4e7ed;

    .agent-info {
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;

      h3 {
        margin: 15px 0 10px;
        font-size: 18px;
      }

      .description {
        margin: 15px 0;
        font-size: 13px;
        line-height: 1.6;
        color: #606266;
      }

      .stats {
        width: 100%;
        margin: 20px 0;

        .stat-item {
          display: flex;
          gap: 8px;
          align-items: center;
          padding: 8px 0;
          font-size: 14px;
          color: #606266;
        }
      }
    }
  }

  .chat-main {
    display: flex;
    flex: 1;
    flex-direction: column;
    background: white;

    .chat-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 15px 20px;
      border-bottom: 1px solid #e4e7ed;

      .session-info {
        display: flex;
        gap: 10px;
        align-items: center;

        h3 {
          margin: 0;
          font-size: 16px;
        }
      }

      .header-actions {
        display: flex;
        align-items: center;
      }
    }

    .message-list {
      flex: 1;
      padding: 20px;
      overflow-y: auto;
      scroll-behavior: smooth;

      .message-item {
        margin-bottom: 20px;

        .message {
          display: flex;
          gap: 12px;

          &.user {
            flex-direction: row-reverse;

            .message-content {
              align-items: flex-end;

              .message-text {
                color: white;
                background: #409eff;
              }
            }
          }

          .message-avatar {
            flex-shrink: 0;
          }

          .message-content {
            display: flex;
            flex: 1;
            flex-direction: column;
            gap: 6px;
            max-width: 70%;

            .message-header {
              display: flex;
              gap: 10px;
              align-items: center;
              font-size: 12px;
              color: #909399;

              .sender {
                font-weight: 600;
              }
            }

            .message-text {
              padding: 12px 16px;
              line-height: 1.6;
              word-wrap: break-word;
              background: #f4f4f5;
              border-radius: 8px;

              &.typing {
                display: flex;
                gap: 8px;
                align-items: center;
                color: #909399;
              }

              &.streaming-preview {
                // 流式预览样式
                .streaming-content {
                  margin: 0;
                  font-family: inherit;
                  line-height: 1.6;
                  word-wrap: break-word;
                  white-space: pre-wrap;

                  .cursor {
                    display: inline;
                    margin-left: 2px;
                    font-weight: bold;
                    color: #409eff;
                    animation: blink 1s infinite;
                  }
                }
              }

              // Markdown样式（使用强选择器确保优先级）
              & h1,
              & h2,
              & h3,
              & h4 {
                display: block !important;
                margin: 16px 0 8px !important;
                font-weight: 600 !important;
                line-height: 1.4 !important;
              }

              & h1 {
                font-size: 20px !important;
              }

              & h2 {
                font-size: 18px !important;
              }

              & h3 {
                font-size: 16px !important;
              }

              & h4 {
                font-size: 14px !important;
              }

              & table {
                display: table !important;
                width: 100%;
                margin: 12px 0;
                font-size: 14px;
                border-collapse: collapse;

                th,
                td {
                  padding: 8px 12px;
                  text-align: left;
                  border: 1px solid #dcdfe6;
                }

                th {
                  font-weight: 600;
                  color: #303133;
                  background: #f5f7fa;
                }

                tr:hover {
                  background: #fafafa;
                }
              }

              & strong {
                font-weight: 600 !important;
                color: #303133 !important;
              }

              & em {
                font-style: italic !important;
                color: #606266 !important;
              }

              & blockquote {
                padding: 8px 12px;
                margin: 12px 0;
                color: #606266;
                background: #f0f9ff;
                border-left: 3px solid #409eff;

                p {
                  margin: 0;
                }
              }

              & hr {
                display: block !important;
                margin: 16px 0 !important;
                border: none !important;
                border-top: 1px solid #e4e7ed !important;
              }

              & ul,
              & ol {
                display: block !important;
                padding-left: 24px !important;
                margin: 8px 0 !important;
              }

              & li {
                display: list-item !important;
                margin: 4px 0 !important;
                line-height: 1.6 !important;
              }

              & code {
                padding: 2px 6px;
                font-family: "Courier New", monospace;
                font-size: 13px;
                color: #e74c3c;
                background: #f5f5f5;
                border-radius: 3px;
              }

              & pre {
                padding: 12px;
                margin: 12px 0;
                overflow-x: auto;
                background: #f5f5f5;
                border-radius: 6px;

                code {
                  padding: 0;
                  color: #303133;
                  background: none;
                }
              }

              & p {
                display: block !important;
                margin: 8px 0 !important;

                &:first-child {
                  margin-top: 0 !important;
                }

                &:last-child {
                  margin-bottom: 0 !important;
                }
              }

              & a {
                color: #409eff;
                text-decoration: none;

                &:hover {
                  text-decoration: underline;
                }
              }
            }

            .message-sources {
              display: flex;
              gap: 8px;
              align-items: center;
              padding: 8px 12px;
              margin-top: 10px;
              font-size: 12px;
              color: #606266;
              background: #f5f7fa;
              border-radius: 4px;

              .sources-label {
                font-weight: 500;
              }
            }

            .message-actions {
              margin-top: 6px;
              opacity: 0;
              transition: opacity 0.2s;
            }
          }

          &:hover .message-actions {
            opacity: 1;
          }
        }

        &.user {
          .message-content {
            align-items: flex-end;

            .message-text {
              color: white;
              background: #409eff;
            }
          }
        }
      }
    }
  }

  .chat-input {
    padding: 20px;
    background: #fafafa;
    border-top: 1px solid #e4e7ed;

    .input-actions {
      display: flex;
      justify-content: space-between;
      margin-top: 10px;

      .left-actions,
      .right-actions {
        display: flex;
        gap: 10px;
        align-items: center;
      }

      .selected-file {
        display: flex;
        align-items: center;
      }
    }
  }
} // 全局Markdown样式（最高优先级）
</style>
