package com.szy.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.szy.entity.RtuBaseInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * RTU设备基础信息 Mapper，操作 collector 库的 t_rtu_baseinfo 表。
 * 继承 MyBatis-Plus 的 BaseMapper，提供标准 CRUD 方法。
 * 数据源在 @DS 注解中指定，由 dynamic-datasource 框架动态切换。
 *
 * @author l
 * @since 2026-05-16
 */
@Mapper
@DS("collector")
public interface RtuBaseInfoMapper extends BaseMapper<RtuBaseInfo> {
}
