package com.szy.controller;


import cn.hutool.core.map.MapUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.szy.common.dto.MenuDto;
import com.szy.common.lang.Result;
import com.szy.entity.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;
import java.util.List;

/**
 * <p>
 *  权限控制器
 * </p>
 *
 * @author l
 * @since 2022-01-12
 */
@RestController
@RequestMapping("/menu")
@DS("jcxx")
public class AuthorityController extends BaseController{
    /**
     * 系统管理员可用功能列表
     * @param principal 身份验证
     * @return 成功信息，功能json
     */
    @GetMapping("/nav")
    public Result<Object> nav(Principal principal){
        User user = userService.getByUsername(principal.getName());
        //获取权限信息
        String userAuthorityInfo = userService.getUserAuthorityInfo(user.getId());
        String[] authorityInfoArray = StringUtils.tokenizeToStringArray(userAuthorityInfo,",");

        //获取导航栏信息
        List<MenuDto> navs = authorityService.getCurrentUserNav();
        return Result.ok(MapUtil.builder()
                .put("authority",authorityInfoArray)
                .put("nav",navs)
                .map());
    }
}
