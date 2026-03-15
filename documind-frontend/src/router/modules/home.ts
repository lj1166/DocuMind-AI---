const { VITE_HIDE_HOME } = import.meta.env;
const Layout = () => import("@/layout/index.vue");

export default {
  path: "/",
  name: "Home",
  component: Layout,
  redirect: "/agents/list", // 首页重定向到智能体列表
  meta: {
    icon: "ep/home-filled",
    title: "首页",
    rank: 0,
    showLink: false // 隐藏首页菜单项
  },
  children: [
    {
      path: "/welcome",
      name: "Welcome",
      component: () => import("@/views/welcome/index.vue"),
      meta: {
        title: "首页",
        showLink: false // 隐藏欢迎页
      }
    }
  ]
} satisfies RouteConfigsTable;
