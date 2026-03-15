<template>
  <div class="agent-form-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">{{ isEdit ? "编辑智能体" : "创建智能体" }}</span>
          <el-button text @click="handleBack">
            <el-icon><Back /></el-icon>
            返回
          </el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="120px"
        size="large"
      >
        <!-- 基本信息 -->
        <el-divider content-position="left">基本信息</el-divider>

        <el-form-item label="智能体名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入智能体名称，如：我的私人医生"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="角色名称" prop="roleName">
          <el-input
            v-model="formData.roleName"
            placeholder="请输入角色名称，如：私人医生"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="智能体描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入智能体描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="头像URL" prop="avatar">
          <el-input
            v-model="formData.avatar"
            placeholder="请输入头像URL（可选）"
          />
        </el-form-item>

        <!-- 系统提示词 -->
        <el-divider content-position="left">系统提示词</el-divider>

        <el-form-item label="提示词模式" prop="promptMode">
          <el-radio-group v-model="formData.promptMode">
            <el-radio value="single">单一提示词</el-radio>
            <el-radio value="multi">多模板模式</el-radio>
          </el-radio-group>
          <div class="form-tip">
            单一提示词：使用一个固定的系统提示词；多模板模式：根据场景自动切换提示词模板
          </div>
        </el-form-item>

        <el-form-item label="系统提示词" prop="systemPrompt">
          <el-input
            v-model="formData.systemPrompt"
            type="textarea"
            :rows="10"
            placeholder="请输入系统提示词，定义智能体的角色、职责和行为规范"
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>

        <!-- AI模型配置 -->
        <el-divider content-position="left">AI模型配置</el-divider>

        <el-form-item label="模型名称" prop="modelName">
          <el-select v-model="formData.modelName" placeholder="请选择模型">
            <el-option label="qwen-plus（推荐）" value="qwen-plus" />
            <el-option label="qwen-max" value="qwen-max" />
            <el-option label="qwen-turbo" value="qwen-turbo" />
          </el-select>
        </el-form-item>

        <el-form-item label="温度参数" prop="temperature">
          <el-slider
            v-model="formData.temperature"
            :min="0"
            :max="1"
            :step="0.01"
            show-input
            :input-size="'small'"
          />
          <div class="form-tip">
            控制输出的随机性，0表示确定性输出，1表示创造性输出
          </div>
        </el-form-item>

        <el-form-item label="最大Token数" prop="maxTokens">
          <el-input-number
            v-model="formData.maxTokens"
            :min="100"
            :max="8000"
            :step="100"
          />
          <div class="form-tip">限制单次回复的最大长度</div>
        </el-form-item>

        <el-form-item label="Top-P参数" prop="topP">
          <el-slider
            v-model="formData.topP"
            :min="0"
            :max="1"
            :step="0.01"
            show-input
            :input-size="'small'"
          />
          <div class="form-tip">核采样参数，控制输出多样性</div>
        </el-form-item>

        <!-- RAG配置 -->
        <el-divider content-position="left">RAG检索配置</el-divider>

        <el-form-item label="启用RAG" prop="ragEnabled">
          <el-switch
            v-model="ragEnabledSwitch"
            active-text="开启"
            inactive-text="关闭"
          />
          <div class="form-tip">
            开启后，智能体将从上传的文档中检索相关内容来回答问题
          </div>
        </el-form-item>

        <el-form-item label="检索Top-K" prop="ragTopK">
          <el-input-number
            v-model="formData.ragTopK"
            :min="1"
            :max="10"
            :disabled="!ragEnabledSwitch"
          />
          <div class="form-tip">检索相关文档的数量</div>
        </el-form-item>

        <el-form-item label="相似度阈值" prop="ragSimilarityThreshold">
          <el-slider
            v-model="formData.ragSimilarityThreshold"
            :min="0"
            :max="1"
            :step="0.01"
            show-input
            :input-size="'small'"
            :disabled="!ragEnabledSwitch"
          />
          <div class="form-tip">
            只检索相似度高于此阈值的文档，值越高筛选越严格
          </div>
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ isEdit ? "保存修改" : "创建智能体" }}
          </el-button>
          <el-button @click="handleBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { Back } from "@element-plus/icons-vue";
import {
  createAgent,
  updateAgent,
  getAgentDetail,
  type AgentCreateRequest
} from "@/api/agent";

const router = useRouter();
const route = useRoute();

const formRef = ref();
const submitting = ref(false);
const isEdit = computed(() => !!route.params.id);

// 表单数据
const formData = reactive<AgentCreateRequest>({
  name: "",
  description: "",
  avatar: "",
  roleName: "",
  systemPrompt: "",
  promptMode: "single",
  modelName: "qwen-plus",
  temperature: 0.7,
  maxTokens: 2000,
  topP: 0.9,
  ragEnabled: 1,
  ragTopK: 10, // 优化为10
  ragSimilarityThreshold: 0.5
});

// RAG开关（用于UI显示）
const ragEnabledSwitch = computed({
  get: () => formData.ragEnabled === 1,
  set: (val: boolean) => {
    formData.ragEnabled = val ? 1 : 0;
  }
});

// 表单验证规则
const rules = {
  name: [{ required: true, message: "请输入智能体名称", trigger: "blur" }],
  roleName: [{ required: true, message: "请输入角色名称", trigger: "blur" }],
  systemPrompt: [
    { required: true, message: "请输入系统提示词", trigger: "blur" }
  ],
  promptMode: [
    { required: true, message: "请选择提示词模式", trigger: "change" }
  ],
  modelName: [{ required: true, message: "请选择模型", trigger: "change" }]
};

// 加载智能体数据（编辑模式）
const loadAgentData = async () => {
  if (!isEdit.value) return;

  try {
    const id = Number(route.params.id);
    const agent = await getAgentDetail(id);

    // 填充表单数据
    Object.assign(formData, {
      name: agent.name,
      description: agent.description,
      avatar: agent.avatar,
      roleName: agent.roleName,
      systemPrompt: agent.systemPrompt,
      promptMode: agent.promptMode,
      modelName: agent.modelName,
      temperature: agent.temperature,
      maxTokens: agent.maxTokens,
      topP: agent.topP,
      ragEnabled: agent.ragEnabled,
      ragTopK: agent.ragTopK,
      ragSimilarityThreshold: agent.ragSimilarityThreshold
    });
  } catch (error) {
    console.error("加载智能体数据失败:", error);
    ElMessage.error("加载数据失败");
    handleBack();
  }
};

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    submitting.value = true;

    if (isEdit.value) {
      // 编辑模式
      const id = Number(route.params.id);
      await updateAgent(id, formData);
      ElMessage.success("更新成功");
    } else {
      // 创建模式
      await createAgent(formData);
      ElMessage.success("创建成功");
    }

    handleBack();
  } catch (error) {
    if (error !== false) {
      console.error("提交失败:", error);
      ElMessage.error(isEdit.value ? "更新失败" : "创建失败");
    }
  } finally {
    submitting.value = false;
  }
};

// 返回列表
const handleBack = () => {
  router.push("/agents/list");
};

onMounted(() => {
  loadAgentData();
});
</script>

<style scoped lang="scss">
.agent-form-container {
  max-width: 1000px;
  padding: 20px;
  margin: 0 auto;

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .title {
      font-size: 18px;
      font-weight: 600;
    }
  }

  .form-tip {
    margin-top: 5px;
    font-size: 12px;
    line-height: 1.5;
    color: #909399;
  }

  :deep(.el-divider__text) {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
  }
}
</style>
