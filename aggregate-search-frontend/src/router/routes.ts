import { RouteRecordRaw } from "vue-router";
import HomePage from "@/pages/HomePage.vue";

export const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "home",
    component: HomePage,
  },
  {
    path: "/:category",
    component: HomePage,
  },
];
