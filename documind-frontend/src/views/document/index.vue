<template>
  <div class="document-page">
    <div class="page-header">
      <h2>文档管理</h2>
      <p class="desc">管理所有智能体的知识库文档</p>
    </div>

    <!-- 智能体选择 -->
    <el-card class="filter-card">
      <el-select
        v-model="selectedAgentId"
        placeholder="选择智能体"
        style="width: 300px"
        clearable
        @change="loadDocuments"
      >
        <el-option
          v-for="agent in agents"
          :key="agent.id"
          :label="agent.name"
          :value="agent.id"
        >
          <span>{{ agent.name }}</span>
          <span style="margin-left: 10px; font-size: 12px; color: #909399">
            {{ agent.documentCount || 0 }} 个文档
          </span>
        </el-option>
      </el-select>
    </el-card>

    <!-- 文档列表 -->
    <el-card v-if="selectedAgentId">
      <DocumentManager :agentId="selectedAgentId" />
    </el-card>

    <el-empty
      v-else
      description="请选择一个智能体查看其文档"
      :image-size="200"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { getAgentList } from "@/api/agent";
import DocumentManager from "@/components/DocumentManager.vue";

const agents = ref<any[]>([]);
const selectedAgentId = ref<number>();

const loadAgents = async () => {
  try {
    agents.value = await getAgentList();
    if (agents.value.length > 0) {
      selectedAgentId.value = agents.value[0].id;
    }
  } catch (error) {
    console.error("加载智能体列表失败:", error);
  }
};

const loadDocuments = () => {
  // DocumentManager组件会自动加载
};

onMounted(() => {
  loadAgents();
});
</script>

<style scoped lang="scss">
.document-page {
  max-width: 1400px;
  padding: 20px;
  margin: 0 auto;

  .page-header {
    margin-bottom: 20px;

    h2 {
      margin: 0 0 8px;
      font-size: 24px;
      font-weight: 600;
    }

    .desc {
      margin: 0;
      font-size: 14px;
      color: #606266;
    }
  }

  .filter-card {
    margin-bottom: 20px;
  }
}
</style>
