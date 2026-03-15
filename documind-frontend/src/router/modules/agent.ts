const Layout = () => import("@/layout/index.vue");

export default [
  {
    path: "/agents",
    component: Layout,
    redirect: "/agents/list",
    meta: {
      title: "我的智能体",
      icon: "ri:robot-2-line",
      rank: 1
    },
    children: [
      {
        path: "/agents/list",
        name: "AgentList",
        component: () => import("@/views/agent/list.vue"),
        meta: {
          title: "我的智能体"
        }
      },
      {
        path: "/agents/create",
        name: "AgentCreate",
        component: () => import("@/views/agent/form.vue"),
        meta: {
          title: "创建智能体",
          showLink: false
        }
      },
      {
        path: "/agents/:id/edit",
        name: "AgentEdit",
        component: () => import("@/views/agent/form.vue"),
        meta: {
          title: "编辑智能体",
          showLink: false
        }
      },
      {
        path: "/agents/:id",
        name: "AgentDetail",
        component: () => import("@/views/agent/detail.vue"),
        meta: {
          title: "智能体详情",
          showLink: false
        }
      },
      {
        path: "/agents/:id/chat/:sessionId?",
        name: "AgentChat",
        component: () => import("@/views/agent/chat.vue"),
        meta: {
          title: "对话",
          showLink: false
        }
      }
    ]
  },
  {
    path: "/agents-templates",
    component: Layout,
    redirect: "/agents/templates",
    meta: {
      title: "提示词模板",
      icon: "ep:collection",
      rank: 2
    },
    children: [
      {
        path: "/agents/templates",
        name: "TemplateManage",
        component: () => import("@/views/template/index.vue"),
        meta: {
          title: "提示词模板"
        }
      }
    ]
  },
  {
    path: "/agents-documents",
    component: Layout,
    redirect: "/agents/documents",
    meta: {
      title: "文档管理",
      icon: "ep:document",
      rank: 3
    },
    children: [
      {
        path: "/agents/documents",
        name: "DocumentManage",
        component: () => import("@/views/document/index.vue"),
        meta: {
          title: "文档管理"
        }
      }
    ]
  },
  {
    path: "/agents-statistics",
    component: Layout,
    redirect: "/agents/statistics",
    meta: {
      title: "使用统计",
      icon: "ep:data-analysis",
      rank: 4
    },
    children: [
      {
        path: "/agents/statistics",
        name: "Statistics",
        component: () => import("@/views/statistics/index.vue"),
        meta: {
          title: "使用统计"
        }
      }
    ]
  }
] satisfies Array<RouteConfigsTable>;
