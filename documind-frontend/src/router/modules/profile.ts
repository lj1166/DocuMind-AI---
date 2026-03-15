const Layout = () => import("@/layout/index.vue");

export default [
  {
    path: "/profile",
    component: Layout,
    redirect: "/profile/info",
    meta: {
      title: "个人信息",
      icon: "ep:user",
      rank: 7
    },
    children: [
      {
        path: "/profile/info",
        name: "ProfileInfo",
        component: () => import("@/views/profile/info.vue"),
        meta: {
          title: "个人信息"
        }
      }
    ]
  },
  {
    path: "/profile-settings",
    component: Layout,
    redirect: "/profile/settings",
    meta: {
      title: "账号设置",
      icon: "ep:setting",
      rank: 8
    },
    children: [
      {
        path: "/profile/settings",
        name: "ProfileSettings",
        component: () => import("@/views/profile/settings.vue"),
        meta: {
          title: "账号设置"
        }
      }
    ]
  }
] satisfies Array<RouteConfigsTable>;
