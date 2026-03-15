<template>
  <div class="agent-detail-container">
    <el-card v-loading="loading" class="header-card">
      <!-- 头部信息 -->
      <div class="agent-header">
        <div class="header-left">
          <el-avatar
            :size="80"
            :src="agentData.avatar"
            :style="{ backgroundColor: '#409EFF' }"
          >
            {{ agentData.name?.charAt(0) }}
          </el-avatar>
          <div class="agent-info">
            <h2 class="agent-name">{{ agentData.name }}</h2>
            <div class="agent-tags">
              <el-tag type="primary" size="small">{{
                agentData.roleName
              }}</el-tag>
              <el-tag
                :type="agentData.status === 1 ? 'success' : 'info'"
                size="small"
              >
                {{ agentData.status === 1 ? "启用中" : "已禁用" }}
              </el-tag>
              <el-tag
                :type="agentData.promptMode === 'multi' ? 'warning' : ''"
                size="small"
              >
                {{ agentData.promptMode === "multi" ? "多模板" : "单一提示词" }}
              </el-tag>
            </div>
            <p class="agent-description">
              {{ agentData.description || "暂无描述" }}
            </p>
          </div>
        </div>
        <div class="header-right">
          <el-button type="primary" size="large" @click="handleChat">
            <el-icon><ChatDotRound /></el-icon>
            进入对话
          </el-button>
          <el-button size="large" @click="handleEdit">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button type="danger" size="large" plain @click="handleDelete">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card" @click="activeTab = 'documents'">
          <div class="stat-icon" style="background: #e6f7ff">
            <el-icon :size="24" color="#1890ff"><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ agentData.documentCount || 0 }}</div>
            <div class="stat-label">文档数量</div>
          </div>
        </div>

        <div class="stat-card" @click="activeTab = 'sessions'">
          <div class="stat-icon" style="background: #f0f9ff">
            <el-icon :size="24" color="#0ea5e9"><ChatDotRound /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ agentData.chatCount || 0 }}</div>
            <div class="stat-label">对话次数</div>
          </div>
        </div>

        <div class="stat-card" @click="activeTab = 'templates'">
          <div class="stat-icon" style="background: #fef3e2">
            <el-icon :size="24" color="#f59e0b"><Collection /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ agentData.templateCount || 0 }}</div>
            <div class="stat-label">模板数量</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon" style="background: #f0fdf4">
            <el-icon :size="24" color="#10b981"><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">
              {{
                agentData.lastChatAt
                  ? formatTime(agentData.lastChatAt)
                  : "未使用"
              }}
            </div>
            <div class="stat-label">最后使用</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- Tab 页 -->
    <el-card class="tab-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="文档管理" name="documents">
          <DocumentManager v-if="agentData.id" :agentId="agentData.id" />
        </el-tab-pane>

        <el-tab-pane label="会话列表" name="sessions">
          <SessionManager v-if="agentData.id" :agentId="agentData.id" />
        </el-tab-pane>

        <el-tab-pane label="提示词模板" name="templates">
          <TemplateManager v-if="agentData.id" :agentId="agentData.id" />
        </el-tab-pane>

        <el-tab-pane label="统计信息" name="statistics">
          <AgentStatisticsPanel v-if="agentData.id" :agentId="agentData.id" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  ChatDotRound,
  Edit,
  Delete,
  Document,
  Collection,
  Clock
} from "@element-plus/icons-vue";
import { getAgentDetail, deleteAgent, type Agent } from "@/api/agent";
import DocumentManager from "@/components/DocumentManager.vue";
import TemplateManager from "@/components/TemplateManager.vue";
import SessionManager from "@/components/SessionManager.vue";
import AgentStatisticsPanel from "@/components/AgentStatisticsPanel.vue";

const router = useRouter();
const route = useRoute();

const loading = ref(false);
const activeTab = ref((route.query.tab as string) || "documents"); // 支持URL参数
const agentData = ref<Agent>({} as Agent);

// 加载智能体详情
const loadAgentDetail = async () => {
  loading.value = true;
  try {
    const id = Number(route.params.id);
    agentData.value = await getAgentDetail(id);
  } catch (error) {
    console.error("加载智能体详情失败:", error);
    ElMessage.error("加载详情失败");
    router.push("/agents/list");
  } finally {
    loading.value = false;
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
    return date.toLocaleDateString("zh-CN");
  }
};

// 进入对话
const handleChat = () => {
  router.push(`/agents/${route.params.id}/chat`);
};

// 编辑智能体
const handleEdit = () => {
  router.push(`/agents/${route.params.id}/edit`);
};

// 删除智能体
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除智能体"${agentData.value.name}"吗？删除后将无法恢复，所有关联的文档、会话和对话历史也会被删除。`,
      "删除确认",
      {
        confirmButtonText: "确定删除",
        cancelButtonText: "取消",
        type: "warning"
      }
    );

    const id = Number(route.params.id);
    await deleteAgent(id);
    ElMessage.success("删除成功");
    router.push("/agents/list");
  } catch (error) {
    if (error !== "cancel") {
      console.error("删除失败:", error);
      ElMessage.error("删除失败");
    }
  }
};

onMounted(() => {
  loadAgentDetail();
});
</script>

<style scoped lang="scss">
.agent-detail-container {
  max-width: 1400px;
  padding: 20px;
  margin: 0 auto;

  .header-card {
    margin-bottom: 20px;

    .agent-header {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      padding-bottom: 30px;
      border-bottom: 1px solid #f0f0f0;

      .header-left {
        display: flex;
        flex: 1;
        gap: 20px;

        .agent-info {
          flex: 1;

          .agent-name {
            margin: 0 0 10px;
            font-size: 24px;
            font-weight: 600;
            color: #303133;
          }

          .agent-tags {
            display: flex;
            gap: 10px;
            margin-bottom: 15px;
          }

          .agent-description {
            margin: 0;
            font-size: 14px;
            line-height: 1.6;
            color: #606266;
          }
        }
      }

      .header-right {
        display: flex;
        gap: 10px;
      }
    }

    .stats-cards {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 20px;
      margin-top: 30px;

      .stat-card {
        display: flex;
        gap: 15px;
        align-items: center;
        padding: 20px;
        cursor: pointer;
        background: #fafafa;
        border-radius: 8px;
        transition: all 0.3s;

        &:hover {
          background: #f5f5f5;
          transform: translateY(-2px);
        }

        .stat-icon {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 50px;
          height: 50px;
          border-radius: 10px;
        }

        .stat-content {
          flex: 1;

          .stat-value {
            margin-bottom: 5px;
            font-size: 24px;
            font-weight: 600;
            color: #303133;
          }

          .stat-label {
            font-size: 13px;
            color: #909399;
          }
        }
      }
    }
  }

  .tab-card {
    :deep(.el-tabs__header) {
      margin-bottom: 20px;
    }

    :deep(.el-tabs__item) {
      font-size: 15px;
      font-weight: 500;
    }
  }
}

@media (width <= 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr) !important;
  }
}

@media (width <= 768px) {
  .agent-header {
    flex-direction: column;
    gap: 20px;

    .header-right {
      width: 100%;

      button {
        flex: 1;
      }
    }
  }

  .stats-cards {
    grid-template-columns: 1fr !important;
  }
}
</style>
