<template>
  <div class="template-manager">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        创建模板
      </el-button>
      <el-select
        v-model="sortBy"
        placeholder="排序方式"
        style="width: 150px"
        @change="loadTemplates"
      >
        <el-option label="按优先级" value="priority" />
        <el-option label="按使用次数" value="usage" />
        <el-option label="按创建时间" value="created" />
      </el-select>
      <el-button :icon="Refresh" @click="loadTemplates">刷新</el-button>
    </div>

    <!-- 模板列表 -->
    <el-table
      v-loading="loading"
      :data="templates"
      style="width: 100%; margin-top: 20px"
      :empty-text="loading ? '加载中...' : '暂无模板'"
    >
      <el-table-column prop="templateName" label="模板名称" min-width="150" />

      <el-table-column
        prop="templateType"
        label="类型"
        width="100"
        align="center"
      >
        <template #default="{ row }">
          <el-tag
            :type="getTemplateTypeTag(row.templateType).color"
            size="small"
          >
            {{ getTemplateTypeTag(row.templateType).text }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column
        prop="priority"
        label="优先级"
        width="100"
        align="center"
      >
        <template #default="{ row }">
          <el-tag v-if="row.priority >= 5" type="warning" size="small">
            {{ row.priority }}
          </el-tag>
          <span v-else>{{ row.priority }}</span>
        </template>
      </el-table-column>

      <el-table-column
        prop="usageCount"
        label="使用次数"
        width="100"
        align="center"
      />

      <el-table-column prop="isActive" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.isActive"
            :active-value="1"
            :inactive-value="0"
            @change="handleToggleStatus(row)"
          />
        </template>
      </el-table-column>

      <el-table-column
        prop="createdAt"
        label="创建时间"
        width="160"
        align="center"
      >
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="240" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button type="info" size="small" link @click="handleCopy(row)">
            复制
          </el-button>
          <el-button type="warning" size="small" link @click="handleView(row)">
            查看
          </el-button>
          <el-button type="danger" size="small" link @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadTemplates"
        @current-change="loadTemplates"
      />
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="模板名称" prop="templateName">
          <el-input
            v-model="formData.templateName"
            placeholder="请输入模板名称"
          />
        </el-form-item>

        <el-form-item label="模板类型" prop="templateType">
          <el-select v-model="formData.templateType" placeholder="请选择类型">
            <el-option label="默认" value="default" />
            <el-option label="场景" value="scenario" />
            <el-option label="任务" value="task" />
          </el-select>
        </el-form-item>

        <el-form-item label="优先级" prop="priority">
          <el-slider
            v-model="formData.priority"
            :min="0"
            :max="10"
            show-input
            :marks="{ 0: '最低', 5: '中等', 10: '最高' }"
          />
        </el-form-item>

        <el-form-item label="提示词内容" prop="promptContent">
          <el-input
            v-model="formData.promptContent"
            type="textarea"
            :rows="10"
            placeholder="请输入提示词内容，支持 Markdown 格式"
          />
          <div class="char-count">
            {{ formData.promptContent?.length || 0 }} 字符
          </div>
        </el-form-item>

        <el-form-item label="使用说明" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="说明该模板的使用场景和触发条件"
          />
        </el-form-item>

        <el-form-item label="触发条件" prop="triggerCondition">
          <el-input
            v-model="formData.triggerCondition"
            type="textarea"
            :rows="4"
            placeholder='JSON 格式，例如：{"keywords": ["关键词1", "关键词2"]}'
          />
          <div class="hint">
            <el-text size="small" type="info">
              配置触发条件（JSON格式），如关键词匹配、文档类型等
            </el-text>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看对话框 -->
    <el-dialog v-model="viewDialogVisible" title="模板详情" width="700px">
      <el-descriptions v-if="currentTemplate" :column="1" border>
        <el-descriptions-item label="模板名称">
          {{ currentTemplate.templateName }}
        </el-descriptions-item>
        <el-descriptions-item label="模板类型">
          <el-tag
            :type="getTemplateTypeTag(currentTemplate.templateType).color"
            size="small"
          >
            {{ getTemplateTypeTag(currentTemplate.templateType).text }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          {{ currentTemplate.priority }}
        </el-descriptions-item>
        <el-descriptions-item label="使用次数">
          {{ currentTemplate.usageCount }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag
            :type="currentTemplate.isActive === 1 ? 'success' : 'info'"
            size="small"
          >
            {{ currentTemplate.isActive === 1 ? "启用" : "禁用" }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提示词内容">
          <div class="prompt-content">{{ currentTemplate.promptContent }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="使用说明">
          {{ currentTemplate.description || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="触发条件">
          <pre
            v-if="currentTemplate.triggerCondition"
            class="trigger-condition"
            >{{ formatJSON(currentTemplate.triggerCondition) }}</pre
          >
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import {
  ElMessage,
  ElMessageBox,
  type FormInstance,
  type FormRules
} from "element-plus";
import { Plus, Refresh } from "@element-plus/icons-vue";
import {
  getTemplates,
  createTemplate,
  updateTemplate,
  deleteTemplate,
  copyTemplate,
  getTemplateTypeTag,
  type PromptTemplate
} from "@/api/template";

const props = defineProps<{
  agentId: number;
}>();

const loading = ref(false);
const saving = ref(false);
const templates = ref<PromptTemplate[]>([]);
const currentPage = ref(1);
const pageSize = ref(20);
const total = ref(0);
const sortBy = ref("priority");

const dialogVisible = ref(false);
const viewDialogVisible = ref(false);
const dialogTitle = ref("创建模板");
const isEdit = ref(false);
const currentTemplate = ref<PromptTemplate | null>(null);
const formRef = ref<FormInstance>();

const formData = reactive({
  templateName: "",
  templateType: "scenario",
  promptContent: "",
  description: "",
  triggerCondition: "",
  priority: 0
});

const formRules: FormRules = {
  templateName: [
    { required: true, message: "请输入模板名称", trigger: "blur" }
  ],
  templateType: [
    { required: true, message: "请选择模板类型", trigger: "change" }
  ],
  promptContent: [
    { required: true, message: "请输入提示词内容", trigger: "blur" }
  ],
  priority: [{ required: true, message: "请设置优先级", trigger: "change" }]
};

// 加载模板列表
const loadTemplates = async () => {
  loading.value = true;
  try {
    const response = await getTemplates(
      props.agentId,
      currentPage.value,
      pageSize.value,
      sortBy.value
    );
    templates.value = response.records;
    total.value = response.total;
  } catch (error: any) {
    console.error("加载模板列表失败:", error);
    const errorMsg =
      error?.data?.message || error?.message || "加载模板列表失败";
    ElMessage.error(errorMsg);
  } finally {
    loading.value = false;
  }
};

// 创建模板
const handleCreate = () => {
  isEdit.value = false;
  dialogTitle.value = "创建模板";
  resetForm();
  dialogVisible.value = true;
};

// 编辑模板
const handleEdit = (row: PromptTemplate) => {
  isEdit.value = true;
  dialogTitle.value = "编辑模板";
  currentTemplate.value = row;

  Object.assign(formData, {
    templateName: row.templateName,
    templateType: row.templateType,
    promptContent: row.promptContent,
    description: row.description || "",
    triggerCondition: row.triggerCondition || "",
    priority: row.priority
  });

  dialogVisible.value = true;
};

// 查看模板
const handleView = (row: PromptTemplate) => {
  currentTemplate.value = row;
  viewDialogVisible.value = true;
};

// 复制模板
const handleCopy = async (row: PromptTemplate) => {
  try {
    await copyTemplate(row.id);
    ElMessage.success("复制成功");
    loadTemplates();
  } catch (error: any) {
    console.error("复制模板失败:", error);
    const errorMsg = error?.data?.message || error?.message || "复制模板失败";
    ElMessage.error(errorMsg);
  }
};

// 切换状态
const handleToggleStatus = async (row: PromptTemplate) => {
  try {
    await updateTemplate(row.id, { isActive: row.isActive });
    ElMessage.success("状态更新成功");
  } catch (error: any) {
    console.error("更新状态失败:", error);
    const errorMsg = error?.data?.message || error?.message || "更新状态失败";
    ElMessage.error(errorMsg);
    // 恢复原状态
    row.isActive = row.isActive === 1 ? 0 : 1;
  }
};

// 删除模板
const handleDelete = async (row: PromptTemplate) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除模板"${row.templateName}"吗？`,
      "删除确认",
      {
        confirmButtonText: "确定删除",
        cancelButtonText: "取消",
        type: "warning"
      }
    );

    await deleteTemplate(row.id);
    ElMessage.success("删除成功");
    loadTemplates();
  } catch (error: any) {
    if (error !== "cancel") {
      console.error("删除模板失败:", error);
      const errorMsg = error?.data?.message || error?.message || "删除模板失败";
      ElMessage.error(errorMsg);
    }
  }
};

// 保存
const handleSave = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async valid => {
    if (!valid) {
      ElMessage.warning("请填写完整的模板信息");
      return;
    }

    saving.value = true;
    try {
      if (isEdit.value && currentTemplate.value) {
        await updateTemplate(currentTemplate.value.id, formData);
        ElMessage.success("更新成功");
      } else {
        await createTemplate(props.agentId, formData);
        ElMessage.success("创建成功");
      }
      dialogVisible.value = false;
      loadTemplates();
    } catch (error: any) {
      console.error("保存失败:", error);
      const errorMsg = error?.data?.message || error?.message || "保存失败";
      ElMessage.error(errorMsg);
    } finally {
      saving.value = false;
    }
  });
};

// 对话框关闭
const handleDialogClose = () => {
  resetForm();
};

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    templateName: "",
    templateType: "scenario",
    promptContent: "",
    description: "",
    triggerCondition: "",
    priority: 0
  });
  formRef.value?.resetFields();
};

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  const date = new Date(dateStr);
  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  });
};

// 格式化JSON
const formatJSON = (jsonStr: string) => {
  try {
    return JSON.stringify(JSON.parse(jsonStr), null, 2);
  } catch {
    return jsonStr;
  }
};

onMounted(() => {
  loadTemplates();
});
</script>

<style scoped lang="scss">
.template-manager {
  .toolbar {
    display: flex;
    gap: 10px;
    align-items: center;
  }

  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }

  .char-count {
    margin-top: 5px;
    font-size: 12px;
    color: #909399;
    text-align: right;
  }

  .hint {
    margin-top: 5px;
  }

  .prompt-content {
    max-height: 300px;
    padding: 10px;
    overflow-y: auto;
    word-break: break-word;
    white-space: pre-wrap;
    background: #f5f7fa;
    border-radius: 4px;
  }

  .trigger-condition {
    padding: 10px;
    overflow-x: auto;
    font-size: 12px;
    background: #f5f7fa;
    border-radius: 4px;
  }
}
</style>
