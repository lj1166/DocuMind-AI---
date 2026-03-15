<template>
  <div class="profile-settings">
    <el-card>
      <template #header>
        <span class="card-title">账号设置</span>
      </template>

      <el-form
        ref="formRef"
        :model="passwordForm"
        label-width="100px"
        :rules="rules"
      >
        <el-divider content-position="left">密码修改</el-divider>

        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码（6-20位）"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="submitting"
            @click="handleUpdatePassword"
          >
            修改密码
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { ElMessage } from "element-plus";
import { updatePassword, type UpdatePasswordRequest } from "@/api/user";
import type { FormInstance, FormRules } from "element-plus";

const formRef = ref<FormInstance>();
const submitting = ref(false);

const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: ""
});

// 自定义验证规则
const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value === "") {
    callback(new Error("请再次输入新密码"));
  } else if (value !== passwordForm.newPassword) {
    callback(new Error("两次输入的密码不一致"));
  } else {
    callback();
  }
};

const rules: FormRules = {
  oldPassword: [{ required: true, message: "请输入当前密码", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, max: 20, message: "密码长度必须在6-20位之间", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: "blur" }
  ]
};

// 修改密码
const handleUpdatePassword = async () => {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();

    submitting.value = true;
    const request: UpdatePasswordRequest = {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    };

    await updatePassword(request);
    ElMessage.success("密码修改成功，请重新登录");

    // 清空表单
    handleReset();

    // 3秒后跳转到登录页
    setTimeout(() => {
      // 清除登录信息
      localStorage.removeItem("user-info");
      window.location.href = "/#/login";
    }, 3000);
  } catch (error: any) {
    if (error !== false) {
      console.error("修改密码失败:", error);
      ElMessage.error(error?.data?.message || "修改密码失败");
    }
  } finally {
    submitting.value = false;
  }
};

// 重置表单
const handleReset = () => {
  passwordForm.oldPassword = "";
  passwordForm.newPassword = "";
  passwordForm.confirmPassword = "";
  formRef.value?.clearValidate();
};
</script>

<style scoped lang="scss">
.profile-settings {
  max-width: 800px;
  padding: 20px;
  margin: 0 auto;

  .card-title {
    font-size: 18px;
    font-weight: 600;
  }
}
</style>
