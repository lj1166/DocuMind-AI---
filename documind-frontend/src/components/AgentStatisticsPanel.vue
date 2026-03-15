<template>
  <div class="agent-statistics-panel">
    <el-row :gutter="20">
      <!-- 基础统计 -->
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <el-icon :size="32" color="#409eff"><ChatDotRound /></el-icon>
            <div class="stat-data">
              <div class="stat-value">{{ statistics.chatCount || 0 }}</div>
              <div class="stat-label">对话次数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <el-icon :size="32" color="#67c23a"><MessageBox /></el-icon>
            <div class="stat-data">
              <div class="stat-value">{{ statistics.sessionCount || 0 }}</div>
              <div class="stat-label">会话数量</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <el-icon :size="32" color="#e6a23c"><Document /></el-icon>
            <div class="stat-data">
              <div class="stat-value">{{ statistics.documentCount || 0 }}</div>
              <div class="stat-label">文档数量</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <el-icon :size="32" color="#f56c6c"><Coin /></el-icon>
            <div class="stat-data">
              <div class="stat-value">
                {{ formatTokens(statistics.totalTokens || 0) }}
              </div>
              <div class="stat-label">Token消耗</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Token消耗详情 -->
    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <span>💰 Token 消耗详情</span>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="累计消耗">
          <el-tag type="danger" size="large"
            >{{ formatTokens(statistics.totalTokens || 0) }} Tokens</el-tag
          >
        </el-descriptions-item>
        <el-descriptions-item label="对话次数">
          <el-tag type="primary">{{ statistics.chatCount || 0 }} 次</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="平均每次">
          <el-tag type="warning">{{ avgTokensPerChat }} Tokens/次</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="最后使用">
          <span>{{ formatTime(statistics.lastChatAt) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="模板数量">
          <el-tag>{{ statistics.templateCount || 0 }} 个</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="会话数量">
          <el-tag>{{ statistics.sessionCount || 0 }} 个</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 使用分析 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>📊 使用效率分析</span>
          </template>
          <div class="analysis-list">
            <div class="analysis-item">
              <span class="analysis-label">文档利用率：</span>
              <el-progress
                :percentage="documentUtilization"
                :color="getProgressColor(documentUtilization)"
              />
            </div>
            <div class="analysis-item">
              <span class="analysis-label">模板覆盖率：</span>
              <el-progress
                :percentage="templateCoverage"
                :color="getProgressColor(templateCoverage)"
              />
            </div>
            <div class="analysis-item">
              <span class="analysis-label">会话活跃度：</span>
              <el-progress
                :percentage="sessionActivity"
                :color="getProgressColor(sessionActivity)"
              />
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>🎯 性能指标</span>
          </template>
          <div class="performance-list">
            <el-statistic
              title="平均对话长度"
              :value="avgSessionMessages"
              suffix="条消息/会话"
            />
            <el-divider />
            <el-statistic
              title="Token效率"
              :value="tokenEfficiency"
              suffix="Tokens/文档"
            />
            <el-divider />
            <el-statistic
              title="文档平均大小"
              :value="avgDocumentSize"
              suffix="KB"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import {
  ChatDotRound,
  MessageBox,
  Document,
  Coin
} from "@element-plus/icons-vue";
import { getAgentStatistics, type AgentStatistics } from "@/api/statistics";

const props = defineProps<{
  agentId: number;
}>();

const statistics = ref<AgentStatistics>({
  agentId: 0,
  agentName: "",
  chatCount: 0,
  sessionCount: 0,
  documentCount: 0,
  templateCount: 0,
  totalTokens: 0,
  lastChatAt: ""
});

// 格式化Token数量
const formatTokens = (tokens: number) => {
  if (tokens >= 1000000) {
    return (tokens / 1000000).toFixed(2) + "M";
  } else if (tokens >= 1000) {
    return (tokens / 1000).toFixed(1) + "K";
  }
  return tokens.toString();
};

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return "未使用";
  const date = new Date(time);
  const now = new Date();
  const diff = now.getTime() - date.getTime();

  if (diff < 60000) return "刚刚";
  if (diff < 3600000) return Math.floor(diff / 60000) + "分钟前";
  if (diff < 86400000) return Math.floor(diff / 3600000) + "小时前";
  if (diff < 604800000) return Math.floor(diff / 86400000) + "天前";

  return date.toLocaleDateString();
};

// 计算属性
const avgTokensPerChat = computed(() => {
  if (!statistics.value.chatCount) return 0;
  return Math.round(
    (statistics.value.totalTokens || 0) / statistics.value.chatCount
  );
});

const documentUtilization = computed(() => {
  if (!statistics.value.documentCount) return 0;
  return Math.min(
    100,
    Math.round(
      (statistics.value.chatCount / statistics.value.documentCount) * 10
    )
  );
});

const templateCoverage = computed(() => {
  if (!statistics.value.templateCount) return 0;
  return Math.min(100, statistics.value.templateCount * 20);
});

const sessionActivity = computed(() => {
  if (!statistics.value.sessionCount) return 0;
  return Math.min(
    100,
    Math.round((statistics.value.chatCount / statistics.value.sessionCount) * 5)
  );
});

const avgSessionMessages = computed(() => {
  if (!statistics.value.sessionCount) return 0;
  return Math.round(
    (statistics.value.chatCount * 2) / statistics.value.sessionCount
  );
});

const tokenEfficiency = computed(() => {
  if (!statistics.value.documentCount) return 0;
  return Math.round(
    (statistics.value.totalTokens || 0) / statistics.value.documentCount
  );
});

const avgDocumentSize = computed(() => {
  // 估算：假设平均每个文档100KB
  return statistics.value.documentCount * 100;
});

const getProgressColor = (percentage: number) => {
  if (percentage >= 75) return "#67c23a";
  if (percentage >= 50) return "#e6a23c";
  if (percentage >= 25) return "#f56c6c";
  return "#909399";
};

// 加载统计数据
const loadStatistics = async () => {
  try {
    statistics.value = await getAgentStatistics(props.agentId);
  } catch (error) {
    console.error("加载统计数据失败:", error);
  }
};

onMounted(() => {
  loadStatistics();
});
</script>

<style scoped lang="scss">
.agent-statistics-panel {
  .stat-card {
    .stat-item {
      display: flex;
      gap: 20px;
      align-items: center;

      .stat-data {
        flex: 1;

        .stat-value {
          margin-bottom: 5px;
          font-size: 32px;
          font-weight: 600;
          color: #303133;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }

  .analysis-list {
    .analysis-item {
      margin-bottom: 20px;

      &:last-child {
        margin-bottom: 0;
      }

      .analysis-label {
        display: block;
        margin-bottom: 10px;
        font-size: 14px;
        color: #606266;
      }
    }
  }

  .performance-list {
    :deep(.el-statistic) {
      text-align: center;
    }
  }
}
</style>
