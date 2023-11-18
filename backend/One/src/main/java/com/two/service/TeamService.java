package com.two.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.two.model.domain.Team;
import com.two.model.domain.User;
import com.two.model.dto.TeamQuery;
import com.two.model.request.TeamJoinRequest;
import com.two.model.request.TeamQuitRequest;
import com.two.model.request.TeamUpdateRequest;
import com.two.model.vo.TeamUserVO;

import java.util.List;

/**
* @author Admin
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-11-02 14:15:59
*/
public interface TeamService extends IService<Team> {



    /**
     * 删除（解散）队伍
     *
     * @param id
     * @param loginUser
     * @return
     */
    boolean deleteTeam(long id, User loginUser);



    /**
     * 退出队伍
     *
     * @param teamQuitRequest
     * @param loginUser
     * @return
     * **/
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);



    /**
     * 加入队伍
     *
     * @param teamJoinRequest
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);


    /**
     * 更新队伍
     * @param team
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest team, User loginUser);


    /**
     * 搜索队伍
     *
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     *   添加队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

}
