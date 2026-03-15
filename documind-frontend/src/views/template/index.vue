<template>
  <div class="template-page">
    <div class="page-header">
      <h2>提示词模板管理</h2>
      <p class="desc">管理所有智能体的提示词模板</p>
    </div>

    <!-- 智能体选择 -->
    <el-card class="filter-card">
      <el-select
        v-model="selectedAgentId"
        placeholder="选择智能体"
        style="width: 300px"
        clearable
        @change="loadTemplates"
      >
        <el-option
          v-for="agent in agents"
          :key="agent.id"
          :label="agent.name"
          :value="agent.id"
        >
          <span>{{ agent.name }}</span>
          <span style="margin-left: 10px; font-size: 12px; color: #909399">
            {{ agent.templateCount || 0 }} 个模板
          </span>
        </el-option>
      </el-select>
    </el-card>

    <!-- 模板列表 -->
    <el-card v-if="selectedAgentId">
      <TemplateManager :agentId="selectedAgentId" />
    </el-card>

    <el-empty
      v-else
      description="请选择一个智能体查看其提示词模板"
      :image-size="200"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { getAgentList } from "@/api/agent";
import TemplateManager from "@/components/TemplateManager.vue";

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

const loadTemplates = () => {
  // TemplateManager组件会自动加载
};

onMounted(() => {
  loadAgents();
});
</script>

<style scoped lang="scss">
.template-page {
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
