<template>
  <div class="statistics-container">
    <!-- 数据概览卡片 -->
    <el-card class="overview-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">📊 数据概览</span>
          <el-button
            :icon="Refresh"
            circle
            :loading="loading"
            @click="loadStatistics"
          />
        </div>
      </template>

      <div class="overview-stats">
        <div class="stat-box">
          <div class="stat-icon" style="background: #e6f7ff">
            <el-icon :size="32" color="#1890ff"><Grid /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.totalAgents || 0 }}</div>
            <div class="stat-label">智能体总数</div>
          </div>
        </div>

        <div class="stat-box">
          <div class="stat-icon" style="background: #f0f9ff">
            <el-icon :size="32" color="#0ea5e9"><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.totalDocuments || 0 }}</div>
            <div class="stat-label">文档总数</div>
          </div>
        </div>

        <div class="stat-box">
          <div class="stat-icon" style="background: #fef3e2">
            <el-icon :size="32" color="#f59e0b"><ChatDotRound /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.totalChats || 0 }}</div>
            <div class="stat-label">对话总数</div>
          </div>
        </div>

        <div class="stat-box">
          <div class="stat-icon" style="background: #f0fdf4">
            <el-icon :size="32" color="#10b981"><Collection /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.totalTemplates || 0 }}</div>
            <div class="stat-label">模板总数</div>
          </div>
        </div>

        <div class="stat-box">
          <div class="stat-icon" style="background: #fef2f2">
            <el-icon :size="32" color="#ef4444"><Coin /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">
              {{ formatTokens(overview.totalTokens || 0) }}
            </div>
            <div class="stat-label">Token消耗</div>
          </div>
        </div>

        <div class="stat-box">
          <div class="stat-icon" style="background: #fdf4ff">
            <el-icon :size="32" color="#d946ef"><MessageBox /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.totalSessions || 0 }}</div>
            <div class="stat-label">会话总数</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- Token消耗统计 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="8">
        <el-card>
          <template #header>
            <span class="card-title">💰 Token消耗统计</span>
          </template>
          <div class="token-stats">
            <div class="token-item">
              <span class="token-label">今日消耗：</span>
              <span class="token-value">{{
                formatTokens(overview.todayTokens || 0)
              }}</span>
            </div>
            <div class="token-item">
              <span class="token-label">本周消耗：</span>
              <span class="token-value">{{
                formatTokens(overview.weekTokens || 0)
              }}</span>
            </div>
            <div class="token-item">
              <span class="token-label">累计消耗：</span>
              <span class="token-value primary">{{
                formatTokens(overview.totalTokens || 0)
              }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <span class="card-title">📈 使用趋势</span>
          </template>
          <div class="trend-stats">
            <div class="trend-item">
              <span class="trend-label">今日对话：</span>
              <span class="trend-value">{{ overview.todayChats || 0 }} 次</span>
            </div>
            <div class="trend-item">
              <span class="trend-label">本周对话：</span>
              <span class="trend-value">{{ overview.weekChats || 0 }} 次</span>
            </div>
            <div class="trend-item">
              <span class="trend-label">平均对话/天：</span>
              <span class="trend-value">{{ avgChatsPerDay }} 次</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <span class="card-title">🎯 活跃度</span>
          </template>
          <div class="activity-stats">
            <div class="activity-item">
              <span class="activity-label">活跃智能体：</span>
              <span class="activity-value">{{ activeAgents }}</span>
            </div>
            <div class="activity-item">
              <span class="activity-label">总智能体：</span>
              <span class="activity-value">{{
                overview.totalAgents || 0
              }}</span>
            </div>
            <div class="activity-item">
              <span class="activity-label">活跃率：</span>
              <span class="activity-value primary">{{ activityRate }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 排行榜 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span class="card-title">🏆 智能体使用排行</span>
              <el-radio-group
                v-model="rankBy"
                size="small"
                @change="loadRankings"
              >
                <el-radio-button label="chatCount">对话数</el-radio-button>
                <el-radio-button label="totalTokens">Token</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <el-empty
            v-if="rankings.length === 0"
            description="暂无数据"
            :image-size="100"
          />
          <div v-else class="rankings-list">
            <div
              v-for="(agent, index) in rankings"
              :key="agent.agentId"
              class="ranking-item"
            >
              <div class="rank" :class="['rank-' + (index + 1)]">
                {{ index + 1 }}
              </div>
              <div class="agent-info">
                <div class="name">{{ agent.agentName }}</div>
                <div class="stats">
                  <span v-if="rankBy === 'chatCount'"
                    >{{ agent.chatCount }} 次对话</span
                  >
                  <span v-else
                    >{{ formatTokens(agent.totalTokens) }} Tokens</span
                  >
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span class="card-title">📅 最近使用</span>
          </template>
          <el-empty
            v-if="recentAgents.length === 0"
            description="暂无数据"
            :image-size="100"
          />
          <div v-else class="recent-list">
            <div
              v-for="agent in recentAgents"
              :key="agent.agentId"
              class="recent-item"
            >
              <div class="agent-info">
                <div class="name">{{ agent.agentName }}</div>
                <div class="time">{{ formatTime(agent.lastChatAt) }}</div>
              </div>
              <div class="stats">
                <el-tag size="small">{{ agent.chatCount }} 次对话</el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import {
  Grid,
  Document,
  ChatDotRound,
  Collection,
  Coin,
  MessageBox,
  Refresh
} from "@element-plus/icons-vue";
import {
  getStatisticsOverview,
  getAgentRankings,
  type StatisticsOverview,
  type AgentStatistics
} from "@/api/statistics";

const loading = ref(false);
const overview = ref<StatisticsOverview>({
  totalAgents: 0,
  totalDocuments: 0,
  totalChats: 0,
  totalSessions: 0,
  totalTemplates: 0,
  totalTokens: 0,
  todayChats: 0,
  todayTokens: 0,
  weekChats: 0,
  weekTokens: 0
});
const rankings = ref<AgentStatistics[]>([]);
const recentAgents = ref<AgentStatistics[]>([]);
const rankBy = ref("chatCount");

// 计算属性
const avgChatsPerDay = computed(() => {
  if (!overview.value.totalChats || !overview.value.totalAgents) return 0;
  return Math.round(overview.value.weekChats / 7);
});

const activeAgents = computed(() => {
  return rankings.value.filter(a => a.chatCount > 0).length;
});

const activityRate = computed(() => {
  if (!overview.value.totalAgents) return 0;
  return Math.round((activeAgents.value / overview.value.totalAgents) * 100);
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

// 加载统计数据
const loadStatistics = async () => {
  loading.value = true;
  try {
    overview.value = await getStatisticsOverview();
    await loadRankings();
    await loadRecentAgents();
  } catch (error) {
    console.error("加载统计数据失败:", error);
  } finally {
    loading.value = false;
  }
};

// 加载排行榜
const loadRankings = async () => {
  try {
    rankings.value = await getAgentRankings(rankBy.value, 10);
  } catch (error) {
    console.error("加载排行榜失败:", error);
  }
};

// 加载最近使用
const loadRecentAgents = async () => {
  try {
    recentAgents.value = await getAgentRankings("lastChatAt", 10);
  } catch (error) {
    console.error("加载最近使用失败:", error);
  }
};

onMounted(() => {
  loadStatistics();
});
</script>

<style scoped lang="scss">
.statistics-container {
  max-width: 1400px;
  padding: 20px;
  margin: 0 auto;

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .card-title {
    font-size: 16px;
    font-weight: 600;
  }

  .overview-stats {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 15px;

    .stat-box {
      padding: 20px;
      text-align: center;
      background: #fafafa;
      border-radius: 8px;

      .stat-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 60px;
        height: 60px;
        margin: 0 auto 15px;
        border-radius: 12px;
      }

      .stat-content {
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

  .token-stats,
  .trend-stats,
  .activity-stats {
    .token-item,
    .trend-item,
    .activity-item {
      display: flex;
      justify-content: space-between;
      padding: 15px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .token-label,
      .trend-label,
      .activity-label {
        font-size: 14px;
        color: #606266;
      }

      .token-value,
      .trend-value,
      .activity-value {
        font-size: 16px;
        font-weight: 600;
        color: #303133;

        &.primary {
          font-size: 18px;
          color: #409eff;
        }
      }
    }
  }

  .rankings-list,
  .recent-list {
    .ranking-item,
    .recent-item {
      display: flex;
      gap: 15px;
      align-items: center;
      padding: 15px;
      border-bottom: 1px solid #f0f0f0;
      transition: background 0.2s;

      &:hover {
        background: #fafafa;
      }

      &:last-child {
        border-bottom: none;
      }

      .rank {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 40px;
        height: 40px;
        font-size: 18px;
        font-weight: 600;
        color: #606266;
        background: #f0f0f0;
        border-radius: 8px;

        &.rank-1 {
          color: #fff;
          background: linear-gradient(135deg, #ffd700, #ffed4e);
        }

        &.rank-2 {
          color: #fff;
          background: linear-gradient(135deg, #c0c0c0, #e8e8e8);
        }

        &.rank-3 {
          color: #fff;
          background: linear-gradient(135deg, #cd7f32, #e39958);
        }
      }

      .agent-info {
        flex: 1;

        .name {
          margin-bottom: 5px;
          font-size: 15px;
          font-weight: 500;
          color: #303133;
        }

        .stats,
        .time {
          font-size: 13px;
          color: #909399;
        }
      }
    }
  }
}

@media (width <= 1200px) {
  .overview-stats {
    grid-template-columns: repeat(3, 1fr) !important;
  }
}

@media (width <= 768px) {
  .overview-stats {
    grid-template-columns: repeat(2, 1fr) !important;
  }
}
</style>
