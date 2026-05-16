package com.szy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.szy.entity.*;
import com.szy.mapper.FlowMapper;
import com.szy.mapper.RiverCollectorMapper;
import com.szy.mapper.WaterLevelCollectorMapper;
import com.szy.mapper.WaterLevelMapper;
import com.szy.service.FlowService;
import com.szy.service.RiverCollectorService;
import com.szy.service.WaterLevelCollectorService;
import com.szy.service.WaterLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.szy.common.lang.Const.*;

@Service
@DS("collector")
@Slf4j
public class RiverCollectorServiceImpl extends ServiceImpl<RiverCollectorMapper, RiverCollector> implements RiverCollectorService {
    @Autowired
    private RiverCollectorMapper riverCollectorMapper;
    @Autowired
    private FlowMapper flowMapper;
    @Autowired
    private WaterLevelMapper waterLevelMapper;
    @Autowired
    private FlowService flowService;
    @Autowired
    private WaterLevelService waterLevelService;
    @Override
    public void syncFlowData() {
        // 查询最新的流量监测记录
        List<RiverCollector> collectorList = riverCollectorMapper.selectLastRecord();
        if (collectorList.isEmpty()) {
            log.info("未获取到所有站点的数据");
        }
        for (RiverCollector collector : collectorList) {
            if (collector != null && collector.getQ1() != null) {
                Flow flow = new Flow();
                // 站码，监测时间和 瞬时流量 累计流量
                flow.setMpCd(collector.getStcd());
                if (collector.getQ1() <= 0) {
                    flow.setMpQ((double) 0);
                }
                else {
                    flow.setMpQ(collector.getQ1());
                }
                // 插入累计流量
                if (collector.getSl1() != null) {
                    flow.setAccW(collector.getSl1());
                }
                else {
                    flow.setAccW((double) 0);
                }
                flow.setTm(collector.getTm());
                // 插入流量数据
                try {
                    flowMapper.insert(flow);
                    flowService.getDeterInformation(flow);
                    log.info("已获取流量站"+ collector.getStcd() +"最新的数据");
                } catch (Exception e) {
                    // 处理插入失败的情况，例如违反唯一索引约束
                    log.error("未获取流量站"+ collector.getStcd() +"最新的数据", e);
                }
            }
            else {
                log.info("暂无流量数据");
            }
            if (collector != null && collector.getZ1() != null) {
                WaterLevel waterLevel = new WaterLevel();
                // 站点，编码，水位，监测时间
                waterLevel.setPosition(RESERVIOR);
                waterLevel.setCode(collector.getStcd());
                waterLevel.setValue(collector.getZ1());
                waterLevel.setMonitorTime(collector.getTm());
                // 插入水位数据
                try {
                    waterLevelMapper.insert(waterLevel);
                    waterLevelService.getDeterInformation(waterLevel);
                    log.info("已获取两河口水库水位站最新的数据");
                } catch (Exception e) {
                    // 处理插入失败的情况，例如违反唯一索引约束
                    log.error("未获取两河口水库水位站最新的数据", e);
                }
            }
        }
    }

}
