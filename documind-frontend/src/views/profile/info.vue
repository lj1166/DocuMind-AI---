<template>
  <div class="profile-info">
    <el-card>
      <template #header>
        <span class="card-title">个人信息</span>
      </template>

      <div class="profile-content">
        <div class="avatar-section">
          <el-avatar :size="120" :src="userInfo.avatar">
            {{ userInfo.username?.charAt(0) || "U" }}
          </el-avatar>
        </div>

        <div class="info-section">
          <el-form :model="formData" label-width="100px" :disabled="!isEditing">
            <el-form-item label="用户名">
              <el-input :value="userInfo.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="formData.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="formData.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="头像URL">
              <el-input v-model="formData.avatar" placeholder="请输入头像URL" />
            </el-form-item>
            <el-form-item label="角色">
              <el-tag>{{ userInfo.role || "user" }}</el-tag>
            </el-form-item>
            <el-form-item label="状态">
              <el-tag :type="userInfo.status === 1 ? 'success' : 'danger'">
                {{ userInfo.status === 1 ? "正常" : "禁用" }}
              </el-tag>
            </el-form-item>
          </el-form>

          <div class="actions" style="margin-top: 20px">
            <el-button v-if="!isEditing" type="primary" @click="handleEdit"
              >编辑信息</el-button
            >
            <template v-else>
              <el-button type="primary" :loading="saving" @click="handleSave"
                >保存</el-button
              >
              <el-button @click="handleCancel">取消</el-button>
            </template>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import {
  getUserInfo,
  updateProfile,
  type UpdateProfileRequest,
  type UserInfo
} from "@/api/user";

const userInfo = ref<UserInfo>(
  JSON.parse(localStorage.getItem("user-info") || "{}")
);
const isEditing = ref(false);
const saving = ref(false);

const formData = reactive<UpdateProfileRequest>({
  nickname: "",
  email: "",
  avatar: ""
});

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const response = await getUserInfo();
    userInfo.value = response.data;
    localStorage.setItem("user-info", JSON.stringify(response.data));

    // 初始化表单数据
    formData.nickname = userInfo.value.nickname || "";
    formData.email = userInfo.value.email || "";
    formData.avatar = userInfo.value.avatar || "";
  } catch (error) {
    console.error("加载用户信息失败:", error);
  }
};

// 编辑
const handleEdit = () => {
  isEditing.value = true;
};

// 保存
const handleSave = async () => {
  saving.value = true;
  try {
    const response = await updateProfile(formData);
    // HTTP拦截器已经提取了data字段，response直接是用户信息
    userInfo.value = response;
    localStorage.setItem("user-info", JSON.stringify(response));
    
    // 同时更新 store 中的用户信息
    const { useUserStoreHook } = await import("@/store/modules/user");
    useUserStoreHook().SET_NICKNAME(response.nickname || response.username);
    useUserStoreHook().SET_AVATAR(response.avatar || "");
    
    ElMessage.success("更新成功");
    isEditing.value = false;
  } catch (error: any) {
    console.error("更新失败:", error);
    ElMessage.error(error?.message || "更新失败");
  } finally {
    saving.value = false;
  }
};

// 取消
const handleCancel = () => {
  isEditing.value = false;
  // 恢复表单数据
  formData.nickname = userInfo.value.nickname || "";
  formData.email = userInfo.value.email || "";
  formData.avatar = userInfo.value.avatar || "";
};

onMounted(() => {
  loadUserInfo();
});
</script>

<style scoped lang="scss">
.profile-info {
  max-width: 1000px;
  padding: 20px;
  margin: 0 auto;

  .card-title {
    font-size: 18px;
    font-weight: 600;
  }

  .profile-content {
    display: flex;
    gap: 40px;

    .avatar-section {
      display: flex;
      flex-direction: column;
      align-items: center;
    }

    .info-section {
      flex: 1;
    }
  }
}
</style>
