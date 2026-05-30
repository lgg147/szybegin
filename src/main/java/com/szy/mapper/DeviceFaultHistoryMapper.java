package com.szy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.szy.entity.DeviceFaultHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备故障历史 Mapper，操作 gcdd 库的 device_fault_history 表。
 * 继承 MyBatis-Plus 的 BaseMapper，提供标准 CRUD 和分页查询能力。
 * 注：未标注 @DS 注解，使用默认数据源（application.yml 中 primary 指定的 gcdd）。
 *
 * @author l
 * @since 2026-05-16
 */
@Mapper
public interface DeviceFaultHistoryMapper extends BaseMapper<DeviceFaultHistory> {
}
