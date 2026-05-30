package com.szy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.szy.common.lang.ResponseCode;
import com.szy.common.lang.Result;
import com.szy.entity.DeviceFaultHistory;
import com.szy.service.DeviceFaultHistoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 设备故障历史控制器。
 * 提供故障记录的查询、处理等接口，
 * 支持按故障类型、处理状态筛选。
 * 历史故障页面不单独作为菜单，通过设备监控页面的按钮导航访问。
 *
 * @author l
 * @since 2026-05-16
 */
@RestController
@RequestMapping("/deviceFaultHistory")
public class DeviceFaultHistoryController extends BaseController {

    @Autowired
    private DeviceFaultHistoryService deviceFaultHistoryService;

    /**
     * 分页查询故障历史列表。
     * 支持按故障类型和处理状态筛选，
     * 结果按故障时间倒序排列，最新故障排在最前。
     *
     * @param currentPage 当前页码
     * @param pageSize    每页条数
     * @param faultType   故障类型筛选：offline/no_data
     * @param status      处理状态筛选：active/resolved
     * @return 分页故障列表
     */
    @GetMapping("/list")
    public Result list(@RequestParam("currentPage") Integer currentPage,
                       @RequestParam("pageSize") Integer pageSize,
                       @RequestParam(value = "faultType", required = false) String faultType,
                       @RequestParam(value = "status", required = false) String status) {
        Page<DeviceFaultHistory> page = new Page<>(currentPage, pageSize);
        QueryWrapper<DeviceFaultHistory> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(faultType)) {
            wrapper.eq("fault_type", faultType);
        }
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("fault_time");
        Page<DeviceFaultHistory> result = deviceFaultHistoryService.page(page, wrapper);
        return Result.ok(result);
    }

    /**
     * 人工处理故障。
     * 管理人员填写处理人和处理方式后提交，
     * 系统自动记录处理时间并将状态更新为 resolved（已恢复）。
     *
     * @param deviceFaultHistory 包含 id、handler(处理人)、handleMethod(处理方式) 的 JSON
     * @return 更新后的故障记录
     */
    @PostMapping("/handle")
    public Result handle(@RequestBody DeviceFaultHistory deviceFaultHistory) {
        DeviceFaultHistory byId = deviceFaultHistoryService.getById(deviceFaultHistory.getId());
        if (byId == null) {
            return Result.fail(ResponseCode.NOT_EXIST, "故障记录不存在");
        }
        byId.setHandler(deviceFaultHistory.getHandler());
        byId.setHandleMethod(deviceFaultHistory.getHandleMethod());
        byId.setHandleTime(new Date());
        byId.setStatus("resolved");
        deviceFaultHistoryService.updateById(byId);
        return Result.ok(byId);
    }

    /**
     * 根据ID查询单条故障记录详情。
     *
     * @param id 故障记录ID
     * @return 故障详情
     */
    @GetMapping("/info/{id}")
    public Result info(@PathVariable Long id) {
        DeviceFaultHistory fault = deviceFaultHistoryService.getById(id);
        if (fault == null) {
            return Result.fail(ResponseCode.NOT_EXIST, "故障记录不存在");
        }
        return Result.ok(fault);
    }

    /**
     * 按设备编码查询该设备的所有故障历史。
     * 用于在设备监控页面点击某台设备时，
     * 弹窗展示该设备的历史故障记录。
     *
     * @param currentPage 当前页码
     * @param pageSize    每页条数
     * @param deviceCode  设备编码(RTUCD)
     * @return 该设备的分页故障列表
     */
    @GetMapping("/list-by-device")
    public Result listByDevice(@RequestParam("currentPage") Integer currentPage,
                               @RequestParam("pageSize") Integer pageSize,
                               @RequestParam("deviceCode") String deviceCode) {
        Page<DeviceFaultHistory> page = new Page<>(currentPage, pageSize);
        QueryWrapper<DeviceFaultHistory> wrapper = new QueryWrapper<>();
        wrapper.eq("device_code", deviceCode);
        wrapper.orderByDesc("fault_time");
        return Result.ok(deviceFaultHistoryService.page(page, wrapper));
    }
}
