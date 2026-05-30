package com.szy.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.szy.entity.DeviceFaultHistory;
import com.szy.mapper.DeviceFaultHistoryMapper;
import com.szy.service.DeviceFaultHistoryService;
import org.springframework.stereotype.Service;

/**
 * 设备故障历史 Service 实现类。
 * 通过 @DS("gcdd") 切换到 gcdd 业务数据库，
 * 使用 MyBatis-Plus 提供的标准 ServiceImpl 基类简化开发。
 *
 * @author l
 * @since 2026-05-16
 */
@Service
@DS("gcdd")
public class DeviceFaultHistoryServiceImpl extends ServiceImpl<DeviceFaultHistoryMapper, DeviceFaultHistory> implements DeviceFaultHistoryService {
}
