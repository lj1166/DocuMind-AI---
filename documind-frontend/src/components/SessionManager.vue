<template>
  <div class="session-manager">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleCreate">
        <el-icon><ChatDotRound /></el-icon>
        新建会话
      </el-button>
      <el-input
        v-model="keyword"
        placeholder="搜索会话标题"
        style="width: 300px"
        clearable
        @change="loadSessions"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button :icon="Refresh" @click="loadSessions">刷新</el-button>
    </div>

    <!-- 会话列表 -->
    <div v-loading="loading" class="session-list">
      <el-empty
        v-if="!loading && sessions.length === 0"
        description="暂无会话，点击上方新建会话开始对话"
      />

      <div v-else class="session-cards">
        <el-card
          v-for="session in sessions"
          :key="session.id"
          class="session-card"
          shadow="hover"
        >
          <div class="session-header">
            <div class="session-title">
              <el-icon><ChatLineRound /></el-icon>
              <span>{{ session.title }}</span>
            </div>
            <div class="session-actions">
              <el-button
                type="primary"
                size="small"
                link
                @click="handleOpenChat(session)"
              >
                <el-icon><Position /></el-icon>
                打开对话
              </el-button>
              <el-dropdown trigger="click">
                <el-button size="small" link>
                  <el-icon><More /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleRename(session)">
                      <el-icon><Edit /></el-icon>
                      重命名
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="handleDelete(session)">
                      <el-icon><Delete /></el-icon>
                      <span style="color: #f56c6c">删除</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <div class="session-info">
            <div class="info-item">
              <el-icon color="#909399"><ChatLineRound /></el-icon>
              <span>{{ session.messageCount || 0 }} 条消息</span>
            </div>
            <div class="info-item">
              <el-icon color="#909399"><Clock /></el-icon>
              <span>{{
                session.lastMessageAt
                  ? formatTime(session.lastMessageAt)
                  : formatTime(session.createdAt)
              }}</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="loadSessions"
        @current-change="loadSessions"
      />
    </div>

    <!-- 重命名对话框 -->
    <el-dialog v-model="renameDialogVisible" title="重命名会话" width="500px">
      <el-input
        v-model="newTitle"
        placeholder="请输入新的会话标题"
        maxlength="200"
        show-word-limit
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="renameDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSaveRename">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  ChatDotRound,
  ChatLineRound,
  Search,
  Refresh,
  Position,
  More,
  Edit,
  Delete,
  Clock
} from "@element-plus/icons-vue";
import {
  getSessions,
  createSession,
  updateSessionTitle,
  deleteSession,
  type ChatSession
} from "@/api/session";

const router = useRouter();

const props = defineProps<{
  agentId: number;
}>();

const loading = ref(false);
const saving = ref(false);
const sessions = ref<ChatSession[]>([]);
const currentPage = ref(1);
const pageSize = ref(20);
const total = ref(0);
const keyword = ref("");

const renameDialogVisible = ref(false);
const currentSession = ref<ChatSession | null>(null);
const newTitle = ref("");

// 加载会话列表
const loadSessions = async () => {
  loading.value = true;
  try {
    const response = await getSessions(
      props.agentId,
      currentPage.value,
      pageSize.value,
      keyword.value
    );
    sessions.value = response.records;
    total.value = response.total;
  } catch (error: any) {
    console.error("加载会话列表失败:", error);
    const errorMsg =
      error?.data?.message || error?.message || "加载会话列表失败";
    ElMessage.error(errorMsg);
  } finally {
    loading.value = false;
  }
};

// 创建新会话
const handleCreate = async () => {
  try {
    const session = await createSession(props.agentId, "新会话");
    ElMessage.success("会话创建成功");
    // 跳转到对话页面
    router.push(`/agents/${props.agentId}/chat/${session.sessionId}`);
  } catch (error: any) {
    console.error("创建会话失败:", error);
    const errorMsg = error?.data?.message || error?.message || "创建会话失败";
    ElMessage.error(errorMsg);
  }
};

// 打开对话（使用sessionId而不是id）
const handleOpenChat = (session: ChatSession) => {
  router.push(`/agents/${props.agentId}/chat/${session.sessionId}`);
};

// 重命名
const handleRename = (session: ChatSession) => {
  currentSession.value = session;
  newTitle.value = session.title;
  renameDialogVisible.value = true;
};

// 保存重命名
const handleSaveRename = async () => {
  if (!currentSession.value) return;

  if (!newTitle.value || newTitle.value.trim() === "") {
    ElMessage.warning("请输入会话标题");
    return;
  }

  saving.value = true;
  try {
    await updateSessionTitle(currentSession.value.id, newTitle.value);
    ElMessage.success("重命名成功");
    renameDialogVisible.value = false;
    loadSessions();
  } catch (error: any) {
    console.error("重命名失败:", error);
    const errorMsg = error?.data?.message || error?.message || "重命名失败";
    ElMessage.error(errorMsg);
  } finally {
    saving.value = false;
  }
};

// 删除会话
const handleDelete = async (session: ChatSession) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除会话"${session.title}"吗？删除后所有对话历史将无法恢复。`,
      "删除确认",
      {
        confirmButtonText: "确定删除",
        cancelButtonText: "取消",
        type: "warning"
      }
    );

    await deleteSession(session.id);
    ElMessage.success("删除成功");
    loadSessions();
  } catch (error: any) {
    if (error !== "cancel") {
      console.error("删除会话失败:", error);
      const errorMsg = error?.data?.message || error?.message || "删除会话失败";
      ElMessage.error(errorMsg);
    }
  }
};

// 格式化时间
const formatTime = (time: string) => {
  const date = new Date(time);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60));
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60));
      return minutes === 0 ? "刚刚" : `${minutes}分钟前`;
    }
    return `${hours}小时前`;
  } else if (days < 7) {
    return `${days}天前`;
  } else {
    return date.toLocaleDateString("zh-CN", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit"
    });
  }
};

onMounted(() => {
  loadSessions();
});
</script>

<style scoped lang="scss">
.session-manager {
  .toolbar {
    display: flex;
    gap: 10px;
    align-items: center;
    margin-bottom: 20px;
  }

  .session-list {
    min-height: 300px;
  }

  .session-cards {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 16px;
  }

  .session-card {
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 4px 12px rgb(0 0 0 / 10%);
      transform: translateY(-2px);
    }

    .session-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;

      .session-title {
        display: flex;
        flex: 1;
        gap: 8px;
        align-items: center;
        font-size: 16px;
        font-weight: 600;
        color: #303133;

        span {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .session-actions {
        display: flex;
        gap: 8px;
        align-items: center;
      }
    }

    .session-info {
      display: flex;
      gap: 20px;
      font-size: 13px;
      color: #606266;

      .info-item {
        display: flex;
        gap: 6px;
        align-items: center;
      }
    }
  }

  .pagination {
    display: flex;
    justify-content: center;
    margin-top: 20px;
  }
}

@media (width <= 768px) {
  .session-cards {
    grid-template-columns: 1fr !important;
  }
}
</style>
