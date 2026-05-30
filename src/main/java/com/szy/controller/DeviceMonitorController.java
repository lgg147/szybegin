package com.szy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.szy.common.lang.Result;
import com.szy.entity.RtuBaseInfo;
import com.szy.service.DeviceMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 设备监控控制器。
 * 提供设备状态查询和数据统计接口，
 * 前端设备监控页面由此获取数据。
 * 权限标识：xtgl_sbjc（系统管理-设备监控）
 *
 * @author l
 * @since 2026-05-16
 */
@RestController
@RequestMapping("/deviceMonitor")
public class DeviceMonitorController extends BaseController {

    @Autowired
    private DeviceMonitorService deviceMonitorService;

    /**
     * 分页查询设备列表。
     * 按在线状态筛选，按最近通讯时间倒序排列。
     *
     * @param currentPage 当前页码
     * @param pageSize    每页条数
     * @param online      在线状态筛选：1-在线 0-离线 不传则查全部
     * @return 分页设备列表，包含每台设备的编号、名称、在线状态、通讯时间等信息
     */
    @PreAuthorize("hasAuthority('xtgl_sbjc')")
    @GetMapping("/list")
    public Result list(@RequestParam("currentPage") Integer currentPage,
                       @RequestParam("pageSize") Integer pageSize,
                       @RequestParam(value = "online", required = false) String online) {
        Page<RtuBaseInfo> page = deviceMonitorService.listDevices(currentPage, pageSize, null, online);
        return Result.ok(page);
    }

    /**
     * 获取设备运行状态统计数据。
     * 返回设备总数、在线数、离线数，
     * 用于前端顶部统计卡片展示。
     *
     * @return 包含 total / onlineCount / offlineCount 的统计数据
     */
    @PreAuthorize("hasAuthority('xtgl_sbjc')")
    @GetMapping("/statistics")
    public Result statistics() {
        Map<String, Object> stats = deviceMonitorService.getStatistics();
        return Result.ok(stats);
    }

    /**
     * 合并接口：一次请求返回统计数据和设备列表。
     * 前端进页面时调用此接口，代替原来的两次独立请求（/list + /statistics），
     * 减少 HTTP 往返次数，提升页面加载速度。
     */
    @PreAuthorize("hasAuthority('xtgl_sbjc')")
    @GetMapping("/overview")
    public Result overview(@RequestParam("currentPage") Integer currentPage,
                           @RequestParam("pageSize") Integer pageSize,
                           @RequestParam(value = "online", required = false) String online) {
        Map<String, Object> data = deviceMonitorService.getOverview(currentPage, pageSize, online);
        return Result.ok(data);
    }
}
