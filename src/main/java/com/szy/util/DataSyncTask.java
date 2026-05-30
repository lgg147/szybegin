package com.szy.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.szy.entity.DeviceFaultHistory;
import com.szy.entity.FlowCollector;
import com.szy.entity.RiverCollector;
import com.szy.entity.RtuBaseInfo;
import com.szy.entity.WaterLevelCollector;
import com.szy.mapper.FlowCollectorMapper;
import com.szy.mapper.RiverCollectorMapper;
import com.szy.mapper.RtuBaseInfoMapper;
import com.szy.mapper.WaterLevelCollectorMapper;
import com.szy.service.DeviceFaultHistoryService;
import com.szy.service.RiverCollectorService;
import com.szy.service.WaterLevelService;
import com.szy.service.WaterQualityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 数据同步与设备状态巡检定时任务。
 * 每5分钟执行一次，包含两类任务：
 * 1. 数据同步：将 collector 库的采集数据同步到业务库
 * 2. 设备巡检：检查设备在线状态和采集状态，自动生成/恢复故障记录
 *
 * 故障检测类型：
 *   offline - 通讯离线：ONLINE='0' 或最后通讯时间超过24小时
 *   no_data - 硬件采集数据异常：设备在线但采集表中无新数据
 *
 * no_data 检测覆盖的采集表：
 * - st_river_r（流河水文，含流量和水位）
 * - wr_mp_q_r（水资源流量监测）
 * - wr_mp_z_r（水资源水位监测）
 *
 * @author l
 * @since 2026-05-16
 */
@Service
@Slf4j
public class DataSyncTask {
    @Autowired
    private WaterLevelService waterLevelService;
    @Autowired
    private WaterQualityService waterQualityService;
    @Autowired
    private RiverCollectorService riverCollectorService;
    @Autowired
    private RtuBaseInfoMapper rtuBaseInfoMapper;
    @Autowired
    private DeviceFaultHistoryService deviceFaultHistoryService;
    @Autowired
    private RiverCollectorMapper riverCollectorMapper;
    @Autowired
    private FlowCollectorMapper flowCollectorMapper;
    @Autowired
    private WaterLevelCollectorMapper waterLevelCollectorMapper;

    /**
     * 每5分钟从水质采集表(e.g. bb)同步数据到水质监测表。
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void synWaterQualityData() {
        waterQualityService.syncQualityData();
    }

    /**
     * 每5分钟从流量采集表(st_river_r)同步数据到流量监测表。
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void synflowData() {
        riverCollectorService.syncFlowData();
    }

    /**
     * 每5分钟从水位采集表(wr_mp_z_r)同步数据到水位监测表。
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void synWaterLevelData() {
        waterLevelService.syncLevelData();
    }

    /**
     * 每5分钟执行一次设备状态巡检。
     * 检查所有设备的在线状态和采集状态：
     * - offline检测：根据 ONLINE 字段和最后通讯时间判断
     * - no_data检测：查询所有采集表，合并有最新数据的测站编码集合，
     *   如果设备在线但其编码不在该集合中，则判定为硬件采集数据异常
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void monitorDeviceStatus() {
        log.info("开始设备状态巡检...");
        List<RtuBaseInfo> deviceList = rtuBaseInfoMapper.selectList(null);
        Date now = new Date();

        // 查询所有采集表中近2小时内有数据的测站编码，用于 no_data 判断
        Set<String> stationCodesWithData = queryStationCodesWithRecentData(now);

        for (RtuBaseInfo device : deviceList) {
            try {
                checkDevice(device, now, stationCodesWithData);
            } catch (Exception e) {
                log.error("巡检设备 {} 异常: {}", device.getRtuCd(), e.getMessage());
            }
        }
        log.info("设备状态巡检完成，共检查 {} 台设备，{} 张采集表有近2小时数据",
                deviceList.size(), stationCodesWithData.size());
    }

    /**
     * 查询所有采集表中近2小时内有最新数据的测站编码集合。
     * 合并多张采集表的结果，只要任意一张表有该站的数据就不算异常。
     * <p>
     * 当前覆盖的采集表：
     * - st_river_r：流河水文表，字段 STCD，含流量(Q1)和水位(Z1)
     * - wr_mp_q_r：水资源流量监测表，字段 MP_CD
     * - wr_mp_z_r：水资源水位监测表，字段 MP_CD
     *
     * @param now 当前时间
     * @return 近2小时内有数据的测站编码集合
     */
    private Set<String> queryStationCodesWithRecentData(Date now) {
        long twoHoursAgo = now.getTime() - 2 * 3600 * 1000L;
        Set<String> codes = new HashSet<>();

        // 从 st_river_r 表查询（水文标准表，含流量和水位数据）
        List<RiverCollector> riverData = riverCollectorMapper.selectLastRecord();
        for (RiverCollector r : riverData) {
            if (r.getTm() != null && r.getTm().getTime() >= twoHoursAgo && r.getStcd() != null) {
                codes.add(r.getStcd());
            }
        }

        // 从 wr_mp_q_r 表查询（水资源流量监测）
        List<FlowCollector> flowData = flowCollectorMapper.selectLastRecord();
        for (FlowCollector f : flowData) {
            if (f.getTm() != null && f.getTm().getTime() >= twoHoursAgo && f.getMpCd() != null) {
                codes.add(f.getMpCd());
            }
        }

        // 从 wr_mp_z_r 表查询（水资源水位监测）
        List<WaterLevelCollector> levelData = waterLevelCollectorMapper.selectLastRecord();
        for (WaterLevelCollector l : levelData) {
            if (l.getTm() != null && l.getTm().getTime() >= twoHoursAgo && l.getMpCd() != null) {
                codes.add(l.getMpCd());
            }
        }

        return codes;
    }

    /**
     * 检查单台设备的状态并更新故障记录。
     * <p>
     * 判断逻辑：
     * 1. offline检测：如果 ONLINE=0 或最近通讯时间超过24小时 → 判定为离线
     * 2. no_data检测：如果设备在线，但所有采集表中都没有该设备编码的最新数据 → 硬件采集数据异常
     * 3. 有故障且尚无活跃记录 → 新增故障记录(status=active)
     * 4. 有活跃故障但设备已恢复 → 自动恢复(status=resolved, 写入恢复时间)
     * <p>
     * 通讯超时阈值默认24小时，可在 t_rtu_baseinfo.WARN_VAL 中按设备单独配置。
     *
     * @param device              设备基础信息
     * @param now                 当前时间
     * @param stationCodesWithData 所有采集表中近2小时内有数据的测站编码集合
     */
    private void checkDevice(RtuBaseInfo device, Date now, Set<String> stationCodesWithData) {
        String deviceCode = device.getRtuCd();
        String deviceName = device.getStnm();
        Date latestTm = device.getLatestTm();
        String online = device.getOnline();
        double warnVal = device.getWarnVal() != null ? device.getWarnVal() : 24;

        // 查询该设备所有未恢复的故障记录（用于去重和恢复判断）
        QueryWrapper<DeviceFaultHistory> activeFaultQuery = new QueryWrapper<>();
        activeFaultQuery.eq("device_code", deviceCode);
        activeFaultQuery.eq("status", "active");
        activeFaultQuery.orderByDesc("fault_time");
        List<DeviceFaultHistory> activeFaults = deviceFaultHistoryService.list(activeFaultQuery);

        // === offline 检测 ===
        boolean isOffline = "0".equals(online);
        boolean commTimeout = latestTm != null
                && (now.getTime() - latestTm.getTime()) > (long) (warnVal * 3600 * 1000);

        if (isOffline || commTimeout) {
            // 设备离线：如果尚无活跃的离线故障，则新增
            boolean hasActiveOfflineFault = activeFaults.stream()
                    .anyMatch(f -> "offline".equals(f.getFaultType()));
            if (!hasActiveOfflineFault) {
                DeviceFaultHistory fault = new DeviceFaultHistory();
                fault.setDeviceCode(deviceCode);
                fault.setDeviceName(deviceName);
                fault.setFaultType("offline");
                fault.setFaultTime(latestTm != null ? latestTm : now);
                fault.setStatus("active");
                String desc = isOffline ? "设备离线" : "通讯超时（超过" + warnVal + "小时未通讯）";
                fault.setFaultDescription(desc);
                deviceFaultHistoryService.save(fault);
                log.warn("新增离线故障: {} - {}", deviceName, desc);
            }
            // 设备已离线，不再检查 no_data
            return;
        }

        // 设备在线：检查是否有活跃的离线故障，有则自动恢复
        for (DeviceFaultHistory activeFault : activeFaults) {
            if ("offline".equals(activeFault.getFaultType())) {
                activeFault.setStatus("resolved");
                activeFault.setRecoveryTime(now);
                deviceFaultHistoryService.updateById(activeFault);
                log.info("设备离线故障已恢复: {}", deviceName);
            }
        }

        // === no_data 检测 ===
        // 设备在线但所有采集表中都没有该设备编码的最新数据 → 硬件采集数据异常
        // 设备的 STCD 同时查 st_river_r(STCD) 和 wr_mp_*(MP_CD)
        boolean hasData = device.getStcd() != null && stationCodesWithData.contains(device.getStcd());
        if (!hasData) {
            boolean hasActiveNoDataFault = activeFaults.stream()
                    .anyMatch(f -> "no_data".equals(f.getFaultType()));
            if (!hasActiveNoDataFault) {
                DeviceFaultHistory fault = new DeviceFaultHistory();
                fault.setDeviceCode(deviceCode);
                fault.setDeviceName(deviceName);
                fault.setFaultType("no_data");
                fault.setFaultTime(now);
                fault.setStatus("active");
                fault.setFaultDescription("硬件采集数据异常（设备在线但采集表无新数据）");
                deviceFaultHistoryService.save(fault);
                log.warn("新增硬件采集数据异常: {} - 设备在线但采集表无新数据", deviceName);
            }
        } else {
            // 设备有数据：恢复活跃的 no_data 故障
            for (DeviceFaultHistory activeFault : activeFaults) {
                if ("no_data".equals(activeFault.getFaultType())) {
                    activeFault.setStatus("resolved");
                    activeFault.setRecoveryTime(now);
                    deviceFaultHistoryService.updateById(activeFault);
                    log.info("硬件采集数据异常已恢复: {}", deviceName);
                }
            }
        }
    }
}
