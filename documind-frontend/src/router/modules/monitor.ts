const Layout = () => import("@/layout/index.vue");

export default [
  {
    path: "/monitor",
    component: Layout,
    redirect: "/monitor/dashboard",
    meta: {
      title: "监控大屏",
      icon: "ep:monitor",
      rank: 5
    },
    children: [
      {
        path: "/monitor/dashboard",
        name: "MonitorDashboard",
        component: () => import("@/views/monitor/dashboard.vue"),
        meta: {
          title: "监控大屏"
        }
      }
    ]
  },
  {
    path: "/monitor-api",
    component: Layout,
    redirect: "/monitor/api",
    meta: {
      title: "API监控",
      icon: "ep:connection",
      rank: 6
    },
    children: [
      {
        path: "/monitor/api",
        name: "MonitorAPI",
        component: () => import("@/views/monitor/api.vue"),
        meta: {
          title: "API监控"
        }
      }
    ]
  }
] satisfies Array<RouteConfigsTable>;
