package com.two.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.two.common.BaseResponse;
import com.two.common.ErrorCode;
import com.two.common.ResultUtil;
import com.two.exception.BusinessException;
import com.two.model.domain.Team;
import com.two.model.domain.User;
import com.two.model.domain.UserTeam;
import com.two.model.dto.TeamQuery;
import com.two.model.request.*;
import com.two.model.vo.TeamUserVO;
import com.two.service.TeamService;
import com.two.service.UserService;
import com.two.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @date: 2023/011/1
* @Description:    队伍controller
*/

@RestController
    @RequestMapping("/team")
//    @CrossOrigin(origins = {"http://localhost:5173/"})
    @Slf4j
    public class TeamController {


    @Resource
    private UserTeamService userTeamService;
        @Resource
        private UserService userService;

        @Resource
        private RedisTemplate redisTemplate;

        @Resource
        private TeamService teamService;

    /**
     * 获取我加入的队伍
     *
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        // 取出不重复的队伍 id
        // teamId userId
        // 1, 2
        // 1, 3
        // 2, 3
        // result
        // 1 => 2, 3
        // 2 => 3
        Map<Long, List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        return ResultUtil.success(teamList);
    }


    /**
     * 获取我创建的队伍
     *
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/createTeam")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        return ResultUtil.success(teamList);
    }


    @PostMapping("/quitTeam")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtil.success(result);
    }


    @PostMapping("/joinTeam")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtil.success(result);
    }

    @PostMapping("/addTeam")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        if (teamAddRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User logininUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        long teamId = teamService.addTeam(team,logininUser);
        return ResultUtil.success(teamId);
    }

    /**
     * 解散删除队伍
     * @param
     * @return
     */
        @PostMapping("/deleteTeam")
        public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
            if (deleteRequest == null || deleteRequest.getId() <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            long id = deleteRequest.getId();
            User loginUser = userService.getLoginUser(request);
            boolean result = teamService.deleteTeam(id, loginUser);
            if (!result) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
            }
            return ResultUtil.success(true);
        }

        @PostMapping("/updateTeam")
        public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
            if (teamUpdateRequest == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            User loginUser = userService.getLoginUser(request);
            boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
            if (!result) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
            }
            return ResultUtil.success(true);
        }

        @GetMapping("/getTeam")
        public BaseResponse<Team> getTeamById(long id){
            if (id <= 0){
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            Team team = teamService.getById(id);
            if (team == null){
                throw new BusinessException(ErrorCode.NULL_ERROR);
            }
            return ResultUtil.success(team);
        }


        @GetMapping("/listTeam")
        public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery,HttpServletRequest httpServletRequest) {
            if (teamQuery == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            boolean isAdmin = userService.isAdmin(httpServletRequest);
            // 1、查询队伍列表
            List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
            final List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
            // 2、判断当前用户是否已加入队伍
            QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
            try {
                User loginUser = userService.getLoginUser(httpServletRequest);
                userTeamQueryWrapper.eq("userId", loginUser.getId());
                userTeamQueryWrapper.in("teamId", teamIdList);
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                // 已加入的队伍 id 集合
                Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
                teamList.forEach(team -> {
                    boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                    team.setHasJoin(hasJoin);
                });
            } catch (Exception e) {}
            // 3、查询已加入队伍的人数
            QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
            userTeamJoinQueryWrapper.in("teamId", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
            // 队伍 id => 加入这个队伍的用户列表
            Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
            teamList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
            return ResultUtil.success(teamList);
        }
    
              @GetMapping("/list/TeamBypage")
        public BaseResponse<Page<Team>> listPageTeams(TeamQuery teamQuery) {
            if (teamQuery == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            Team team = new Team();
            BeanUtils.copyProperties(teamQuery, team);
            Page<Team> page = new Page<>(teamQuery.getPageNum(),teamQuery.getPageSize());
            QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
            Page<Team> resultPage = teamService.page(page,queryWrapper);
            return ResultUtil.success(resultPage);
        }
            }
