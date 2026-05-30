package com.szy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.szy.entity.RtuBaseInfo;

import java.util.Map;

/**
 * 设备监控业务接口，定义设备状态查询和统计功能。
 * 读取 collector 库中 t_rtu_baseinfo 表的设备状态数据，
 * 为设备监控页面提供数据支撑。
 *
 * @author l
 * @since 2026-05-16
 */
public interface DeviceMonitorService {

    /**
     * 分页查询设备列表，支持按在线状态筛选。
     * 结果按最近通讯时间倒序排列，最新通讯的设备排在最前。
     *
     * @param currentPage 当前页码
     * @param pageSize    每页条数
     * @param deviceType  设备类型（预留参数，当前未启用）
     * @param online      在线状态筛选：1-在线 0-离线 null-全部
     * @return 分页后的设备列表
     */
    Page<RtuBaseInfo> listDevices(Integer currentPage, Integer pageSize, String deviceType, String online);

    /**
     * 获取设备运行状态统计数据。
     * 返回设备总数、在线数、离线数等概览指标，
     * 用于设备监控页面顶部的统计卡片展示。
     *
     * @return 包含 total(总数)、onlineCount(在线数)、offlineCount(离线数) 的 Map
     */
    Map<String, Object> getStatistics();

    /**
     * 合并接口：一次请求返回统计数据和设备列表。
     * 将 getStatistics 和 listDevices 合并为一次数据库查询，
     * 避免前端进页面时发两次请求，减少页面加载时间。
     *
     * @param currentPage 当前页码
     * @param pageSize    每页条数
     * @param online      在线状态筛选
     * @return 包含 statistics(统计数据) 和 deviceList(分页设备列表) 的 Map
     */
    Map<String, Object> getOverview(Integer currentPage, Integer pageSize, String online);
}
