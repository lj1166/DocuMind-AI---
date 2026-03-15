<template>
  <div class="document-manager">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="uploadDialogVisible = true">
        <el-icon><Upload /></el-icon>
        上传文档
      </el-button>
      <el-input
        v-model="keyword"
        placeholder="搜索文件名"
        style="width: 300px"
        clearable
        @change="loadDocuments"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button :icon="Refresh" @click="loadDocuments">刷新</el-button>
    </div>

    <!-- 批量操作栏 -->
    <div v-if="selectedDocuments.length > 0" class="batch-toolbar">
      <span class="selected-count"
        >已选择 {{ selectedDocuments.length }} 个文档</span
      >
      <el-button type="danger" size="small" @click="handleBatchDelete">
        <el-icon><Delete /></el-icon>
        批量删除
      </el-button>
      <el-button type="warning" size="small" @click="handleBatchReVectorize">
        <el-icon><Refresh /></el-icon>
        批量重新向量化
      </el-button>
    </div>

    <!-- 文档列表 -->
    <el-table
      v-loading="loading"
      :data="documents"
      style="width: 100%; margin-top: 20px"
      :empty-text="loading ? '加载中...' : '暂无文档'"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="fileName" label="文件名" min-width="200">
        <template #default="{ row }">
          <div class="file-name">
            <el-icon :color="getFileTypeColor(row.fileType)" :size="18">
              <Document />
            </el-icon>
            <span>{{ row.fileName }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="fileType" label="类型" width="80" align="center">
        <template #default="{ row }">
          <el-tag size="small">{{ row.fileType.toUpperCase() }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="fileSize" label="大小" width="100" align="center">
        <template #default="{ row }">
          {{ formatFileSize(row.fileSize) }}
        </template>
      </el-table-column>

      <el-table-column
        prop="vectorStatus"
        label="状态"
        width="120"
        align="center"
      >
        <template #default="{ row }">
          <el-tag
            :type="getVectorStatusInfo(row.vectorStatus).type"
            size="small"
          >
            {{ getVectorStatusInfo(row.vectorStatus).text }}
          </el-tag>
          <div v-if="row.vectorStatus === 1" style="margin-top: 5px">
            <el-progress
              :percentage="50"
              :show-text="false"
              :stroke-width="4"
              status="warning"
            />
          </div>
        </template>
      </el-table-column>

      <el-table-column
        prop="chunkCount"
        label="分块数"
        width="100"
        align="center"
      >
        <template #default="{ row }">
          <span v-if="row.vectorStatus === 2">{{ row.chunkCount }}</span>
          <span v-else style="color: #909399">-</span>
        </template>
      </el-table-column>

      <el-table-column
        prop="createdAt"
        label="上传时间"
        width="160"
        align="center"
      >
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="300" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            size="small"
            link
            @click="handlePreview(row)"
          >
            预览
          </el-button>
          <el-button
            type="info"
            size="small"
            link
            @click="handleViewDetail(row)"
          >
            详情
          </el-button>
          <el-button
            type="success"
            size="small"
            link
            @click="handleDownload(row)"
          >
            下载
          </el-button>
          <el-button
            v-if="row.vectorStatus === 2"
            type="warning"
            size="small"
            link
            @click="handleReVectorize(row)"
          >
            重新向量化
          </el-button>
          <el-button
            v-if="row.vectorStatus === 3"
            type="warning"
            size="small"
            link
            @click="handleReVectorize(row)"
          >
            重试
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
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadDocuments"
        @current-change="loadDocuments"
      />
    </div>

    <!-- 上传对话框 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传文档"
      width="600px"
      @close="handleDialogClose"
    >
      <el-upload
        ref="uploadRef"
        class="upload-area"
        drag
        multiple
        :auto-upload="false"
        :on-change="handleFileChange"
        :file-list="fileList"
        accept=".pdf,.doc,.docx,.txt,.md,.xls,.xlsx"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽文件到此处或 <em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">
            支持 PDF、Word、Excel、TXT、Markdown 格式，单个文件最大 50MB
          </div>
        </template>
      </el-upload>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="uploadDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="uploading"
            :disabled="fileList.length === 0"
            @click="handleUpload"
          >
            开始上传
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 文档详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="文档详情" width="700px">
      <el-descriptions v-if="currentDocument" :column="2" border>
        <el-descriptions-item label="文件名">
          {{ currentDocument.fileName }}
        </el-descriptions-item>
        <el-descriptions-item label="文件类型">
          <el-tag size="small">{{
            currentDocument.fileType.toUpperCase()
          }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="文件大小">
          {{ formatFileSize(currentDocument.fileSize) }}
        </el-descriptions-item>
        <el-descriptions-item label="向量化状态">
          <el-tag
            :type="getVectorStatusInfo(currentDocument.vectorStatus).type"
          >
            {{ getVectorStatusInfo(currentDocument.vectorStatus).text }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="分块数量">
          {{ currentDocument.chunkCount || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="上传时间">
          {{ formatDateTime(currentDocument.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item
          v-if="currentDocument.vectorError"
          label="错误信息"
          :span="2"
        >
          <el-text type="danger">{{ currentDocument.vectorError }}</el-text>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 文档预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      :title="'预览: ' + (previewData?.fileName || '')"
      width="800px"
      destroy-on-close
    >
      <div v-loading="previewLoading" class="preview-content">
        <!-- 文本/Markdown预览 -->
        <div v-if="previewData?.previewType === 'text'" class="text-preview">
          <el-input
            v-if="previewData.fileType === 'txt'"
            type="textarea"
            :model-value="previewData.content"
            :rows="20"
            readonly
          />
          <div
            v-else-if="previewData.fileType === 'md'"
            class="markdown-preview"
            v-html="renderMarkdown(previewData.content || '')"
          />
        </div>

        <!-- PDF提示 -->
        <el-result
          v-else-if="previewData?.previewType === 'pdf'"
          icon="warning"
          title="PDF文件预览"
          :sub-title="previewData.message"
        >
          <template #extra>
            <el-button type="primary" @click="handleDownloadFromPreview">
              下载查看
            </el-button>
          </template>
        </el-result>

        <!-- 不支持预览 -->
        <el-result
          v-else-if="previewData?.previewType === 'unsupported'"
          icon="warning"
          title="不支持在线预览"
          :sub-title="previewData.message"
        >
          <template #extra>
            <el-button type="primary" @click="handleDownloadFromPreview">
              下载查看
            </el-button>
          </template>
        </el-result>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import type { UploadFile, UploadInstance } from "element-plus";
import {
  Upload,
  Search,
  Refresh,
  Document,
  UploadFilled,
  Delete
} from "@element-plus/icons-vue";
import {
  getDocuments,
  uploadDocument,
  deleteDocument,
  getDocumentStatus,
  downloadDocument,
  previewDocument,
  reVectorizeDocument,
  formatFileSize,
  getFileTypeColor,
  getVectorStatusInfo,
  type Document as DocumentType,
  type DocumentPreview
} from "@/api/document";
import { marked } from "marked";

const props = defineProps<{
  agentId: number;
}>();

const loading = ref(false);
const uploading = ref(false);
const documents = ref<DocumentType[]>([]);
const currentPage = ref(1);
const pageSize = ref(20);
const total = ref(0);
const keyword = ref("");

const uploadDialogVisible = ref(false);
const detailDialogVisible = ref(false);
const previewDialogVisible = ref(false);
const fileList = ref<UploadFile[]>([]);
const uploadRef = ref<UploadInstance>();
const currentDocument = ref<DocumentType | null>(null);
const previewData = ref<DocumentPreview | null>(null);
const previewLoading = ref(false);
const selectedDocuments = ref<DocumentType[]>([]);

let pollingTimer: number | null = null;

// 加载文档列表
const loadDocuments = async () => {
  loading.value = true;
  try {
    const response = await getDocuments(
      props.agentId,
      currentPage.value,
      pageSize.value,
      keyword.value
    );
    documents.value = response.records;
    total.value = response.total;

    // 检查是否有处理中的文档，如果有则启动轮询
    const hasProcessing = documents.value.some(doc => doc.vectorStatus === 1);
    if (hasProcessing) {
      startPolling();
    } else {
      stopPolling();
    }
  } catch (error: any) {
    console.error("加载文档列表失败:", error);
    const errorMsg =
      error?.data?.message || error?.message || "加载文档列表失败";
    ElMessage.error(errorMsg);
  } finally {
    loading.value = false;
  }
};

// 启动轮询
const startPolling = () => {
  if (pollingTimer) return;

  pollingTimer = window.setInterval(async () => {
    // 只轮询状态为处理中的文档
    const processingDocs = documents.value.filter(
      doc => doc.vectorStatus === 1
    );

    if (processingDocs.length === 0) {
      stopPolling();
      return;
    }

    for (const doc of processingDocs) {
      try {
        const updated = await getDocumentStatus(doc.id);
        // 更新文档状态
        const index = documents.value.findIndex(d => d.id === doc.id);
        if (index !== -1) {
          documents.value[index] = updated;
        }
      } catch (error) {
        console.error("轮询文档状态失败:", error);
      }
    }
  }, 2000); // 每2秒轮询一次
};

// 停止轮询
const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer);
    pollingTimer = null;
  }
};

// 文件选择变化
const handleFileChange = (file: UploadFile, files: UploadFile[]) => {
  // 验证文件大小
  if (file.size && file.size > 52428800) {
    ElMessage.error(`文件 ${file.name} 超过 50MB 限制`);
    // 从files中移除
    const index = files.findIndex(f => f.uid === file.uid);
    if (index !== -1) {
      files.splice(index, 1);
    }
    return;
  }

  // 验证文件类型
  const validTypes = [".pdf", ".doc", ".docx", ".txt", ".md", ".xls", ".xlsx"];
  const fileExt = "." + file.name.split(".").pop()?.toLowerCase();
  if (!validTypes.includes(fileExt)) {
    ElMessage.error(
      `文件 ${file.name} 格式不支持，仅支持：PDF、Word、Excel、TXT、Markdown`
    );
    // 从files中移除
    const index = files.findIndex(f => f.uid === file.uid);
    if (index !== -1) {
      files.splice(index, 1);
    }
    return;
  }

  // 更新fileList
  fileList.value = files;
};

// 上传文件
const handleUpload = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning("请先选择要上传的文件");
    return;
  }

  uploading.value = true;
  let successCount = 0;
  let failCount = 0;
  const failedFiles: string[] = [];

  for (const fileItem of fileList.value) {
    try {
      if (fileItem.raw) {
        await uploadDocument(props.agentId, fileItem.raw);
        successCount++;
      }
    } catch (error: any) {
      console.error(`上传文件 ${fileItem.name} 失败:`, error);
      failCount++;
      const errorMsg = error?.data?.message || error?.message || "未知错误";
      failedFiles.push(`${fileItem.name}: ${errorMsg}`);
    }
  }

  uploading.value = false;

  if (successCount > 0) {
    ElMessage.success(`成功上传 ${successCount} 个文件`);
    loadDocuments();
  }

  if (failCount > 0) {
    ElMessage.error({
      message: `${failCount} 个文件上传失败`,
      duration: 5000
    });
    // 在控制台显示详细错误
    console.error("上传失败的文件:", failedFiles);
  }

  if (failCount === 0) {
    uploadDialogVisible.value = false;
  }
};

// 对话框关闭
const handleDialogClose = () => {
  fileList.value = [];
  uploadRef.value?.clearFiles();
};

// 查看详情
const handleViewDetail = (row: DocumentType) => {
  currentDocument.value = row;
  detailDialogVisible.value = true;
};

// 预览文档
const handlePreview = async (row: DocumentType) => {
  previewLoading.value = true;
  previewDialogVisible.value = true;
  previewData.value = null;
  currentDocument.value = row;

  try {
    previewData.value = await previewDocument(row.id);
  } catch (error: any) {
    console.error("预览失败:", error);
    ElMessage.error(error?.data?.message || "预览失败");
    previewDialogVisible.value = false;
  } finally {
    previewLoading.value = false;
  }
};

// 从预览对话框下载
const handleDownloadFromPreview = () => {
  if (currentDocument.value) {
    handleDownload(currentDocument.value);
  }
  previewDialogVisible.value = false;
};

// 渲染Markdown
const renderMarkdown = (content: string) => {
  try {
    return marked(content);
  } catch (error) {
    console.error("Markdown渲染失败:", error);
    return content;
  }
};

// 下载文档
const handleDownload = (row: DocumentType) => {
  try {
    downloadDocument(row.id, row.fileName);
    ElMessage.success("开始下载");
  } catch (error: any) {
    console.error("下载失败:", error);
    ElMessage.error("下载失败");
  }
};

// 重新向量化（包括失败重试）
const handleReVectorize = async (row: DocumentType) => {
  try {
    await ElMessageBox.confirm(
      `确定要重新向量化文档 "${row.fileName}" 吗？这将删除旧的向量数据并重新处理。`,
      "重新向量化确认",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }
    );

    await reVectorizeDocument(row.id);
    ElMessage.success("已开始重新向量化，请稍候刷新查看状态");

    // 立即刷新列表
    setTimeout(() => {
      loadDocuments();
    }, 1000);
  } catch (error: any) {
    if (error !== "cancel") {
      console.error("重新向量化失败:", error);
      ElMessage.error(error?.data?.message || "重新向量化失败");
    }
  }
};

// 选择变化
const handleSelectionChange = (selection: DocumentType[]) => {
  selectedDocuments.value = selection;
};

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedDocuments.value.length} 个文档吗？这将同时删除相关的向量数据。`,
      "批量删除确认",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }
    );

    let successCount = 0;
    let failCount = 0;

    for (const doc of selectedDocuments.value) {
      try {
        await deleteDocument(doc.id);
        successCount++;
      } catch (error) {
        console.error(`删除文档 ${doc.fileName} 失败:`, error);
        failCount++;
      }
    }

    if (successCount > 0) {
      ElMessage.success(`成功删除 ${successCount} 个文档`);
    }
    if (failCount > 0) {
      ElMessage.error(`${failCount} 个文档删除失败`);
    }

    selectedDocuments.value = [];
    loadDocuments();
  } catch (error: any) {
    if (error !== "cancel") {
      console.error("批量删除失败:", error);
    }
  }
};

// 批量重新向量化
const handleBatchReVectorize = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要对选中的 ${selectedDocuments.value.length} 个文档重新向量化吗？`,
      "批量重新向量化确认",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }
    );

    let successCount = 0;
    let failCount = 0;

    for (const doc of selectedDocuments.value) {
      try {
        await reVectorizeDocument(doc.id);
        successCount++;
      } catch (error) {
        console.error(`重新向量化 ${doc.fileName} 失败:`, error);
        failCount++;
      }
    }

    if (successCount > 0) {
      ElMessage.success(`成功提交 ${successCount} 个文档的重新向量化任务`);
    }
    if (failCount > 0) {
      ElMessage.error(`${failCount} 个文档操作失败`);
    }

    selectedDocuments.value = [];
    setTimeout(() => {
      loadDocuments();
    }, 1000);
  } catch (error: any) {
    if (error !== "cancel") {
      console.error("批量重新向量化失败:", error);
    }
  }
};

// 删除文档
const handleDelete = async (row: DocumentType) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文档"${row.fileName}"吗？删除后将无法恢复。`,
      "删除确认",
      {
        confirmButtonText: "确定删除",
        cancelButtonText: "取消",
        type: "warning"
      }
    );

    await deleteDocument(row.id);
    ElMessage.success("删除成功");
    loadDocuments();
  } catch (error: any) {
    if (error !== "cancel") {
      console.error("删除文档失败:", error);
      const errorMsg = error?.data?.message || error?.message || "删除文档失败";
      ElMessage.error(errorMsg);
    }
  }
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

onMounted(() => {
  loadDocuments();
});

onBeforeUnmount(() => {
  stopPolling();
});
</script>

<style scoped lang="scss">
.document-manager {
  .toolbar {
    display: flex;
    gap: 10px;
    align-items: center;
  }

  .batch-toolbar {
    display: flex;
    gap: 12px;
    align-items: center;
    padding: 12px 16px;
    margin-top: 15px;
    background: #ecf5ff;
    border: 1px solid #d9ecff;
    border-radius: 4px;

    .selected-count {
      margin-right: auto;
      font-weight: 500;
      color: #409eff;
    }
  }

  .file-name {
    display: flex;
    gap: 8px;
    align-items: center;
  }

  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }

  .upload-area {
    :deep(.el-upload) {
      width: 100%;
    }

    :deep(.el-upload-dragger) {
      width: 100%;
      padding: 40px 0;
    }

    :deep(.el-icon--upload) {
      margin-bottom: 16px;
      font-size: 67px;
      color: #c0c4cc;
    }

    :deep(.el-upload__text) {
      font-size: 14px;
      color: #606266;

      em {
        font-style: normal;
        color: #409eff;
      }
    }

    :deep(.el-upload__tip) {
      margin-top: 12px;
      font-size: 12px;
      color: #909399;
      text-align: center;
    }
  }

  .preview-content {
    min-height: 400px;

    .text-preview {
      :deep(.el-textarea__inner) {
        font-family: "Courier New", monospace;
        line-height: 1.6;
      }

      .markdown-preview {
        min-height: 400px;
        max-height: 600px;
        padding: 20px;
        overflow-y: auto;
        line-height: 1.8;
        background: #f5f7fa;
        border-radius: 4px;

        :deep(h1) {
          padding-bottom: 10px;
          margin: 20px 0 10px;
          font-size: 24px;
          border-bottom: 2px solid #ddd;
        }

        :deep(h2) {
          padding-bottom: 8px;
          margin: 18px 0 8px;
          font-size: 20px;
          border-bottom: 1px solid #eee;
        }

        :deep(h3) {
          margin: 16px 0 8px;
          font-size: 18px;
        }

        :deep(p) {
          margin: 10px 0;
        }

        :deep(code) {
          padding: 2px 6px;
          font-family: "Courier New", monospace;
          background: #f0f0f0;
          border-radius: 3px;
        }

        :deep(pre) {
          padding: 15px;
          overflow-x: auto;
          color: #abb2bf;
          background: #282c34;
          border-radius: 4px;

          code {
            padding: 0;
            color: inherit;
            background: none;
          }
        }

        :deep(ul),
        :deep(ol) {
          padding-left: 30px;
          margin: 10px 0;
        }

        :deep(blockquote) {
          padding: 10px 15px;
          padding-left: 15px;
          margin: 15px 0;
          color: #606266;
          background: #ecf5ff;
          border-left: 4px solid #409eff;
        }

        :deep(table) {
          width: 100%;
          margin: 15px 0;
          border-collapse: collapse;

          th,
          td {
            padding: 8px 12px;
            text-align: left;
            border: 1px solid #ddd;
          }

          th {
            font-weight: 600;
            background: #f5f7fa;
          }
        }
      }
    }
  }
}
</style>
