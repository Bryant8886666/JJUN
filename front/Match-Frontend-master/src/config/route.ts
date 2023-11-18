import Index from "../pages/index.vue";
import Team from "../pages/TeamPage.vue";
import User from "../pages/UserPage.vue";
import SearchPage from "../pages/SearchPage.vue";
import UserEditPage from "../pages/UserEditPage.vue";
import UserResultPage from "../pages/UserResultPage.vue";
import UserLoginPage from "../pages/UserLoginPage.vue";
import TeamAddPage from "../pages/TeamAddPage.vue";
import TeamUpdatePage from "../pages/TeamUpdatePage.vue";
import UserUpdatePage from "../pages/UserUpdatePage.vue";
import UserTeamJoinPage from "../pages/UserTeamJoinPage.vue";
import UserTeamCreatePage from "../pages/UserTeamCreatePage.vue";

const routes = [
    { path: '/', component: Index },
    { path: '/team', title:'找队伍',component: Team },
    { path: '/user', component: User },
    { path: '/search', component: SearchPage },
    { path: '/edit', component: UserEditPage },
    { path: '/user/list', component: UserResultPage },
    { path: '/user/login', component: UserLoginPage },
    { path: '/team/addTeam', component: TeamAddPage },
    { path: '/team/updateTeam', component: TeamUpdatePage },
    { path: '/user/update', component: UserUpdatePage },
    { path: '/team/my/join', component: UserTeamJoinPage },
    { path: '/team/my/create', component: UserTeamCreatePage },
]

export default routes;//导出