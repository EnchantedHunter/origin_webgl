import {createRouter, createWebHistory, NavigationGuardNext, RouteLocationNormalized, RouteRecordRaw} from "vue-router";
import Login from "@/views/Login.vue";
import Signup from "@/views/Signup.vue";
import Game from "@/views/Game.vue";
import NotFound from "@/views/NotFound.vue";
import Client from "@/net/Client";

const routes: Array<RouteRecordRaw> = [
    {
        path: "/",
        name: "Game",
        component: Game
    },
    {
        path: "/login",
        name: "Login",
        component: Login
    },
    {
        path: "/signup",
        name: "Signup",
        component: Signup
    },
    {
        path: "/about",
        name: "About",
        // route level code-splitting
        // this generates a separate chunk (about.[hash].js) for this route
        // which is lazy-loaded when the route is visited.
        component: () =>
            import(/* webpackChunkName: "about" */ "../views/About.vue")
    },
    {
        path: "/:catchAll(.*)",
        component: NotFound,
    }
];

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes
});
router.beforeEach((to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
    console.log(from)

    // всегда даем переход на "о нас"
    if (to.name == 'About') {
        next();
    }
    // всегда даем зарегистрироваться
    else if (to.name == 'Signup') {
        next();
    }
    // если не авторизованы надо перейти на логин форму
    else if (to.name !== 'Login' && Client.instance.ssid == null) {
        console.log("auth required, redirect to login")
        next({name: "Login"})
    } else if (to.name == 'Login') {
        console.log("routed to login");
        next();
    } else {
        next();
    }
})

export default router;