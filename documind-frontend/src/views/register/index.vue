<script setup lang="ts">
import Motion from "../login/utils/motion";
import { useRouter } from "vue-router";
import { message } from "@/utils/message";
import { ref, reactive } from "vue";
import { debounce } from "@pureadmin/utils";
import { useEventListener } from "@vueuse/core";
import type { FormInstance } from "element-plus";
import { register as registerApi, type RegisterRequest } from "@/api/user";
import { bg, illustration } from "../login/utils/static";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import { useDataThemeChange } from "@/layout/hooks/useDataThemeChange";

import dayIcon from "@/assets/svg/day.svg?component";
import darkIcon from "@/assets/svg/dark.svg?component";
import Lock from "~icons/ri/lock-fill";
import User from "~icons/ri/user-3-fill";
import Mail from "~icons/ri/mail-fill";

defineOptions({
  name: "Register"
});

const router = useRouter();
const loading = ref(false);
const disabled = ref(false);
const ruleFormRef = ref<FormInstance>();

const { dataTheme, dataThemeChange } = useDataThemeChange();

const ruleForm = reactive<RegisterRequest>({
  username: "",
  password: "",
  email: "",
  nickname: ""
});

// 表单验证规则
const registerRules = {
  username: [
    {
      required: true,
      message: "请输入用户名",
      trigger: "blur"
    },
    {
      min: 3,
      max: 50,
      message: "用户名长度必须在3-50个字符之间",
      trigger: "blur"
    }
  ],
  password: [
    {
      required: true,
      message: "请输入密码",
      trigger: "blur"
    },
    {
      min: 6,
      max: 50,
      message: "密码长度必须在6-50个字符之间",
      trigger: "blur"
    }
  ],
  email: [
    {
      type: "email",
      message: "请输入正确的邮箱地址",
      trigger: "blur"
    }
  ]
};

const onRegister = async (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  await formEl.validate(valid => {
    if (valid) {
      loading.value = true;
      registerApi(ruleForm)
        .then(res => {
          if (res.code === 200) {
            message("注册成功，请登录", { type: "success" });
            // 注册成功后跳转到登录页
            setTimeout(() => {
              router.push("/login");
            }, 1000);
          } else {
            message(res.message || "注册失败", { type: "error" });
          }
        })
        .catch(error => {
          message(error.message || "注册失败", { type: "error" });
        })
        .finally(() => (loading.value = false));
    }
  });
};

const immediateDebounce: any = debounce(
  formRef => onRegister(formRef),
  1000,
  true
);

useEventListener(document, "keydown", ({ code }) => {
  if (
    ["Enter", "NumpadEnter"].includes(code) &&
    !disabled.value &&
    !loading.value
  )
    immediateDebounce(ruleFormRef.value);
});

const goToLogin = () => {
  router.push("/login");
};
</script>

<template>
  <div class="select-none">
    <img :src="bg" class="wave" />
    <div class="flex-c absolute right-5 top-3">
      <!-- 主题 -->
      <el-switch
        v-model="dataTheme"
        inline-prompt
        :active-icon="dayIcon"
        :inactive-icon="darkIcon"
        @change="dataThemeChange"
      />
    </div>
    <div class="login-container">
      <div class="img">
        <component :is="illustration" />
      </div>
      <div class="login-box">
        <div class="login-form">
          <img src="/logo.jpg" class="logo" alt="DocuMind AI" />
          <Motion>
            <h2 class="outline-hidden">用户注册</h2>
          </Motion>

          <el-form
            ref="ruleFormRef"
            :model="ruleForm"
            :rules="registerRules"
            size="large"
          >
            <Motion :delay="100">
              <el-form-item prop="username">
                <el-input
                  v-model="ruleForm.username"
                  clearable
                  placeholder="用户名（3-50个字符）"
                  :prefix-icon="useRenderIcon(User)"
                />
              </el-form-item>
            </Motion>

            <Motion :delay="150">
              <el-form-item prop="password">
                <el-input
                  v-model="ruleForm.password"
                  clearable
                  show-password
                  placeholder="密码（至少6个字符）"
                  :prefix-icon="useRenderIcon(Lock)"
                />
              </el-form-item>
            </Motion>

            <Motion :delay="200">
              <el-form-item prop="email">
                <el-input
                  v-model="ruleForm.email"
                  clearable
                  placeholder="邮箱（可选）"
                  :prefix-icon="useRenderIcon(Mail)"
                />
              </el-form-item>
            </Motion>

            <Motion :delay="250">
              <el-form-item prop="nickname">
                <el-input
                  v-model="ruleForm.nickname"
                  clearable
                  placeholder="昵称（可选）"
                  :prefix-icon="useRenderIcon(User)"
                />
              </el-form-item>
            </Motion>

            <Motion :delay="300">
              <el-button
                class="w-full mt-4!"
                size="default"
                type="primary"
                :loading="loading"
                :disabled="disabled"
                @click="onRegister(ruleFormRef)"
              >
                注册
              </el-button>
            </Motion>

            <Motion :delay="350">
              <el-button class="w-full mt-2!" size="default" @click="goToLogin">
                已有账号？去登录
              </el-button>
            </Motion>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url("@/style/login.css");

.logo {
  display: block;
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  border-radius: 8px;
}
</style>

<style lang="scss" scoped>
:deep(.el-input-group__append, .el-input-group__prepend) {
  padding: 0;
}
</style>
