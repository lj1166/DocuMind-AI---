import { http } from "@/utils/http";

/** 用户信息 */
export type UserInfo = {
  id: number;
  username: string;
  email?: string;
  nickname?: string;
  avatar?: string;
  role: string;
  status: number;
};

/** 登录响应 */
export type LoginResult = {
  code: number;
  message: string;
  data: {
    accessToken: string;
    tokenType: string;
    expiresIn: number;
    user: UserInfo;
  };
};

/** 注册请求 */
export type RegisterRequest = {
  username: string;
  password: string;
  email?: string;
  nickname?: string;
};

/** 注册响应 */
export type RegisterResult = {
  code: number;
  message: string;
  data: UserInfo;
};

/** 用户信息响应 */
export type UserResult = {
  code: number;
  message: string;
  data: UserInfo;
};

/** 登录 */
export const login = (data: { username: string; password: string }) => {
  return http.request<LoginResult>("post", "/api/auth/login", { data });
};

/** 注册 */
export const register = (data: RegisterRequest) => {
  return http.request<RegisterResult>("post", "/api/auth/register", { data });
};

/** 获取用户信息 */
export const getUserInfo = () => {
  return http.request<UserResult>("get", "/api/auth/profile");
};

/** 登出 */
export const logout = () => {
  return http.request("post", "/api/auth/logout");
};

/** 更新个人信息请求 */
export type UpdateProfileRequest = {
  nickname?: string;
  email?: string;
  avatar?: string;
};

/** 修改密码请求 */
export type UpdatePasswordRequest = {
  oldPassword: string;
  newPassword: string;
};

/** 更新个人信息 */
export const updateProfile = (data: UpdateProfileRequest) => {
  return http.request<UserInfo>("put", "/api/auth/profile", { data });
};

/** 修改密码 */
export const updatePassword = (data: UpdatePasswordRequest) => {
  return http.request<void>("put", "/api/auth/password", { data });
};
