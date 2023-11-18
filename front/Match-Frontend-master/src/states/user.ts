import {UserType} from "../models/user";

let currentUser: UserType;

const setCurrentUserState=(user :UserType)=>{
    return currentUser;
}

const getCurrentUserState= () : UserType =>{
    return currentUser;
}
export {
    setCurrentUserState,
    getCurrentUserState,
}