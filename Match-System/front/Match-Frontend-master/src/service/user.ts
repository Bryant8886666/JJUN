import myAxios from "../plugins/myAxios";
import {getCurrentUserState, setCurrentUserState} from "../states/user";


export const getCurrentUser = async () => {
    const currentUser = getCurrentUserState();
    if (currentUser) {
        return currentUser;
    }
// 不存在则从远程获取

    const res = await myAxios.get('/user/current');
    if (res.code === 200) {
        setCurrentUserState(res.data);
        return res.data;
    }
    return null;
}

// export  const getCurrentUser =async ()=>{
//     const res =await myAxios.get('user/current');
//     console.log('在user.ts方法里执行完getCurrentUser方法后res的值是'+res)
//     if (res.code == 200){
//         console.log('当前用户信息已获取')
//         setCurrentUserState(res.data);
//         console.log('执行setCurrentUserState(res.data);后res的data值'+res.data)
//         return res.data;
//     }
//     else {
//         console.log('当前user.ts里res的值是'+res)
//     }
//     return null;
// }