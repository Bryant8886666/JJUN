package com.two.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.two.mapper.UserTeamMapper;
import com.two.model.domain.UserTeam;
import com.two.service.UserTeamService;
import org.springframework.stereotype.Service;

@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam> implements UserTeamService {



}
