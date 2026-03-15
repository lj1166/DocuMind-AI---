<template>
  <div class="agent-list-container">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <div class="header-left">
        <h2>我的智能体</h2>
        <span class="agent-count">共 {{ total }} 个</span>
      </div>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        创建智能体
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filter-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索智能体名称或角色"
        clearable
        style="width: 300px"
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-select
        v-model="filterStatus"
        placeholder="状态筛选"
        clearable
        style="width: 150px; margin-left: 10px"
        @change="handleFilter"
      >
        <el-option label="全部" :value="null" />
        <el-option label="启用中" :value="1" />
        <el-option label="已禁用" :value="0" />
      </el-select>

      <el-select
        v-model="sortBy"
        placeholder="排序方式"
        style="width: 150px; margin-left: 10px"
        @change="handleSort"
      >
        <el-option label="创建时间" value="createdAt" />
        <el-option label="更新时间" value="updatedAt" />
        <el-option label="名称" value="name" />
        <el-option label="使用频率" value="chatCount" />
      </el-select>

      <el-radio-group
        v-model="sortOrder"
        style="margin-left: 10px"
        @change="handleSort"
      >
        <el-radio-button label="desc">降序</el-radio-button>
        <el-radio-button label="asc">升序</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 智能体卡片列表 -->
    <div
      v-loading="loading"
      class="agent-grid"
      :element-loading-text="'加载中...'"
    >
      <template v-if="!loading && agentList.length === 0">
        <!-- 空状态 -->
        <el-empty
          description="暂无智能体，快去创建一个吧！"
          style="grid-column: 1 / -1; margin: 40px 0"
        >
          <el-button type="primary" @click="handleCreate">创建智能体</el-button>
        </el-empty>
      </template>

      <el-card
        v-for="agent in agentList"
        v-else
        :key="agent.id"
        class="agent-card"
        shadow="hover"
        @click="handleViewDetail(agent.id)"
      >
        <div class="card-header">
          <el-avatar
            :size="60"
            :src="agent.avatar"
            :style="{ backgroundColor: getRandomColor() }"
          >
            {{ agent.name.charAt(0) }}
          </el-avatar>
          <div class="agent-info">
            <h3 class="agent-name">{{ agent.name }}</h3>
            <el-tag size="small" type="primary">{{ agent.roleName }}</el-tag>
          </div>
        </div>

        <div class="card-body">
          <p class="agent-description">
            {{ agent.description || "暂无描述" }}
          </p>

          <div class="agent-stats">
            <div class="stat-item">
              <el-icon><Document /></el-icon>
              <span>{{ agent.documentCount }} 文档</span>
            </div>
            <div class="stat-item">
              <el-icon><ChatDotRound /></el-icon>
              <span>{{ agent.chatCount }} 对话</span>
            </div>
            <div class="stat-item">
              <el-icon><Collection /></el-icon>
              <span>{{ agent.templateCount }} 模板</span>
            </div>
          </div>

          <div class="agent-meta">
            <el-tag
              :type="agent.status === 1 ? 'success' : 'info'"
              size="small"
            >
              {{ agent.status === 1 ? "启用中" : "已禁用" }}
            </el-tag>
            <el-tag size="small" type="warning">
              {{ agent.promptMode === "multi" ? "多模板" : "单一提示词" }}
            </el-tag>
          </div>
        </div>

        <div class="card-footer" @click.stop>
          <el-button type="primary" text @click="handleChat(agent.id)">
            <el-icon><ChatDotRound /></el-icon>
            进入对话
          </el-button>
          <el-button text @click="handleEdit(agent.id)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button
            text
            :type="agent.status === 1 ? 'warning' : 'success'"
            @click="handleToggleStatus(agent)"
          >
            <el-icon><Switch /></el-icon>
            {{ agent.status === 1 ? "禁用" : "启用" }}
          </el-button>
          <el-button type="danger" text @click="handleDelete(agent)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Plus,
  Search,
  Document,
  ChatDotRound,
  Collection,
  Edit,
  Delete,
  Switch
} from "@element-plus/icons-vue";
import {
  getAgentPage,
  deleteAgent,
  toggleAgentStatus,
  type Agent,
  type AgentPageParams
} from "@/api/agent";

const router = useRouter();

// 数据状态
const loading = ref(false);
const agentList = ref<Agent[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(20);

// 搜索和筛选
const searchKeyword = ref("");
const filterStatus = ref<number | null>(null);
const sortBy = ref("createdAt");
const sortOrder = ref("desc");

// 获取智能体列表
const fetchAgentList = async () => {
  loading.value = true;
  try {
    const params: AgentPageParams = {
      page: currentPage.value,
      pageSize: pageSize.value,
      sortBy: sortBy.value,
      sortOrder: sortOrder.value
    };

    if (searchKeyword.value) {
      params.keyword = searchKeyword.value;
    }
    if (filterStatus.value !== null) {
      params.status = filterStatus.value;
    }

    const res = await getAgentPage(params);
    agentList.value = res.items;
    total.value = res.total;
  } catch (error: any) {
    console.error("获取智能体列表失败:", error);
    const errorMsg =
      error?.data?.message || error?.message || "获取智能体列表失败";
    ElMessage.error(errorMsg);
    agentList.value = []; // 出错时清空列表
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchAgentList();
};

// 筛选
const handleFilter = () => {
  currentPage.value = 1;
  fetchAgentList();
};

// 排序
const handleSort = () => {
  currentPage.value = 1;
  fetchAgentList();
};

// 分页
const handlePageChange = () => {
  fetchAgentList();
};

const handleSizeChange = () => {
  currentPage.value = 1;
  fetchAgentList();
};

// 创建智能体
const handleCreate = () => {
  router.push("/agents/create");
};

// 查看详情
const handleViewDetail = (id: number) => {
  router.push(`/agents/${id}`);
};

// 编辑智能体
const handleEdit = (id: number) => {
  router.push(`/agents/${id}/edit`);
};

// 进入对话
const handleChat = (id: number) => {
  router.push(`/agents/${id}/chat`);
};

// 切换状态
const handleToggleStatus = async (agent: Agent) => {
  try {
    await toggleAgentStatus(agent.id);
    ElMessage.success(agent.status === 1 ? "已禁用智能体" : "已启用智能体");
    fetchAgentList();
  } catch (error) {
    console.error("切换状态失败:", error);
    ElMessage.error("操作失败");
  }
};

// 删除智能体
const handleDelete = async (agent: Agent) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除智能体"${agent.name}"吗？删除后将无法恢复，所有关联的文档、会话和对话历史也会被删除。`,
      "删除确认",
      {
        confirmButtonText: "确定删除",
        cancelButtonText: "取消",
        type: "warning"
      }
    );

    await deleteAgent(agent.id);
    ElMessage.success("删除成功");
    fetchAgentList();
  } catch (error) {
    if (error !== "cancel") {
      console.error("删除失败:", error);
      ElMessage.error("删除失败");
    }
  }
};

// 随机颜色（用于头像背景）
const getRandomColor = () => {
  const colors = [
    "#409EFF",
    "#67C23A",
    "#E6A23C",
    "#F56C6C",
    "#909399",
    "#00D7E9",
    "#5470C6",
    "#91CC75"
  ];
  return colors[Math.floor(Math.random() * colors.length)];
};

onMounted(() => {
  fetchAgentList();
});
</script>

<style scoped lang="scss">
.agent-list-container {
  max-width: 1400px;
  padding: 20px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;

  .header-left {
    display: flex;
    gap: 10px;
    align-items: center;

    h2 {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
    }

    .agent-count {
      font-size: 14px;
      color: #909399;
    }
  }
}

.filter-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.agent-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.agent-card {
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-5px);
  }

  .card-header {
    display: flex;
    gap: 15px;
    align-items: center;
    margin-bottom: 15px;

    .agent-info {
      flex: 1;

      .agent-name {
        margin: 0 0 8px;
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }
    }
  }

  .card-body {
    .agent-description {
      display: -webkit-box;
      min-height: 44px;
      margin-bottom: 15px;
      overflow: hidden;
      text-overflow: ellipsis;
      -webkit-line-clamp: 2;
      font-size: 14px;
      line-height: 1.6;
      color: #606266;
      -webkit-box-orient: vertical;
    }

    .agent-stats {
      display: flex;
      gap: 20px;
      margin-bottom: 15px;

      .stat-item {
        display: flex;
        gap: 5px;
        align-items: center;
        font-size: 14px;
        color: #909399;

        .el-icon {
          font-size: 16px;
        }
      }
    }

    .agent-meta {
      display: flex;
      gap: 10px;
      margin-bottom: 15px;
    }
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    padding-top: 15px;
    border-top: 1px solid #f0f0f0;
  }
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
