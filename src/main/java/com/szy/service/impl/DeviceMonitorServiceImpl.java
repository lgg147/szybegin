package com.szy.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.szy.entity.RtuBaseInfo;
import com.szy.mapper.RtuBaseInfoMapper;
import com.szy.service.DeviceMonitorService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备监控业务实现类。
 * 通过 @DS("collector") 切换到 collector 数据源，
 * 直接查询 t_rtu_baseinfo 表获取设备状态信息。
 *
 * @author l
 * @since 2026-05-16
 */
@Service
@DS("collector")
public class DeviceMonitorServiceImpl implements DeviceMonitorService {

    @Autowired
    private RtuBaseInfoMapper rtuBaseInfoMapper;

    @Override
    public Page<RtuBaseInfo> listDevices(Integer currentPage, Integer pageSize, String deviceType, String online) {
        Page<RtuBaseInfo> page = new Page<>(currentPage, pageSize);
        QueryWrapper<RtuBaseInfo> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(online)) {
            wrapper.eq("ONLINE", online);
        }
        wrapper.orderByDesc("LATEST_TM");
        return rtuBaseInfoMapper.selectPage(page, wrapper);
    }

    @Override
    public Map<String, Object> getStatistics() {
        // 一次查询全量设备，在 Java 中统计（设备数量通常不多，1次查询比3次COUNT更高效）
        List<RtuBaseInfo> allList = rtuBaseInfoMapper.selectList(null);
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", allList.size());
        stats.put("onlineCount", allList.stream().filter(d -> "1".equals(d.getOnline())).count());
        stats.put("offlineCount", allList.stream().filter(d -> "0".equals(d.getOnline())).count());
        return stats;
    }

    @Override
    public Map<String, Object> getOverview(Integer currentPage, Integer pageSize, String online) {
        Map<String, Object> result = new HashMap<>();
        // 一次查询同时返回列表和统计，避免前端发两次请求
        Page<RtuBaseInfo> page = listDevices(currentPage, pageSize, null, online);
        result.put("deviceList", page);
        result.put("statistics", getStatistics());
        return result;
    }
}
