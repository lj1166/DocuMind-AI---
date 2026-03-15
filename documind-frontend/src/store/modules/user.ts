import { defineStore } from "pinia";
import {
  type userType,
  store,
  router,
  resetRouter,
  routerArrays,
  storageLocal
} from "../utils";
import {
  type LoginResult,
  type UserInfo,
  login as loginApi,
  getUserInfo,
  logout as logoutApi
} from "@/api/user";
import { useMultiTagsStoreHook } from "./multiTags";
import { type DataInfo, setToken, removeToken, userKey } from "@/utils/auth";

export const useUserStore = defineStore("pure-user", {
  state: (): userType => ({
    // 头像
    avatar: storageLocal().getItem<DataInfo<number>>(userKey)?.avatar ?? "",
    // 用户名
    username: storageLocal().getItem<DataInfo<number>>(userKey)?.username ?? "",
    // 昵称
    nickname: storageLocal().getItem<DataInfo<number>>(userKey)?.nickname ?? "",
    // 页面级别权限
    roles: storageLocal().getItem<DataInfo<number>>(userKey)?.roles ?? [],
    // 按钮级别权限
    permissions:
      storageLocal().getItem<DataInfo<number>>(userKey)?.permissions ?? [],
    // 是否勾选了登录页的免登录
    isRemembered: false,
    // 登录页的免登录存储几天，默认7天
    loginDay: 7
  }),
  actions: {
    /** 存储头像 */
    SET_AVATAR(avatar: string) {
      this.avatar = avatar;
    },
    /** 存储用户名 */
    SET_USERNAME(username: string) {
      this.username = username;
    },
    /** 存储昵称 */
    SET_NICKNAME(nickname: string) {
      this.nickname = nickname;
    },
    /** 存储角色 */
    SET_ROLES(roles: Array<string>) {
      this.roles = roles;
    },
    /** 存储按钮级别权限 */
    SET_PERMS(permissions: Array<string>) {
      this.permissions = permissions;
    },
    /** 存储是否勾选了登录页的免登录 */
    SET_ISREMEMBERED(bool: boolean) {
      this.isRemembered = bool;
    },
    /** 设置登录页的免登录存储几天 */
    SET_LOGINDAY(value: number) {
      this.loginDay = Number(value);
    },
    /** 登入 */
    async loginByUsername(data: { username: string; password: string }) {
      return new Promise<{ success: boolean; data?: any }>(
        (resolve, reject) => {
          loginApi(data)
            .then(response => {
              // HTTP拦截器已经处理了统一响应格式，response直接是data内容
              if (response && response.accessToken) {
                // 保存Token
                const tokenData = {
                  accessToken: response.accessToken,
                  expires: new Date().getTime() + response.expiresIn,
                  username: response.user.username,
                  nickname: response.user.nickname || response.user.username,
                  avatar: response.user.avatar || "",
                  roles: [response.user.role],
                  permissions: []
                };
                setToken(tokenData);

                // 更新store状态
                this.SET_USERNAME(response.user.username);
                this.SET_NICKNAME(
                  response.user.nickname || response.user.username
                );
                this.SET_AVATAR(response.user.avatar || "");
                this.SET_ROLES([response.user.role]);

                resolve({ success: true, data: response });
              } else {
                reject(new Error("登录失败：响应数据格式错误"));
              }
            })
            .catch(error => {
              reject(error);
            });
        }
      );
    },
    /** 前端登出 */
    async logOut() {
      try {
        await logoutApi();
      } catch (error) {
        console.error("登出失败:", error);
      } finally {
        this.username = "";
        this.roles = [];
        this.permissions = [];
        removeToken();
        useMultiTagsStoreHook().handleTags("equal", [...routerArrays]);
        resetRouter();
        router.push("/login");
      }
    },
    /** 获取用户信息 */
    async getUserInfo() {
      try {
        const response = await getUserInfo();
        if (response?.code === 200) {
          this.SET_USERNAME(response.data.username);
          this.SET_NICKNAME(response.data.nickname || response.data.username);
          this.SET_AVATAR(response.data.avatar || "");
          this.SET_ROLES([response.data.role]);
          return response.data;
        }
      } catch (error) {
        console.error("获取用户信息失败:", error);
        throw error;
      }
    }
  }
});

export function useUserStoreHook() {
  return useUserStore(store);
}
