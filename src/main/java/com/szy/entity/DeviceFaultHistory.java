package com.szy.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 设备故障历史记录实体，映射 gcdd 库的 device_fault_history 表。
 * 记录设备运行过程中出现的各类故障（离线、数据未采集、数据异常等），
 * 支持管理人员回溯排查和处理记录归档。
 * 故障由定时任务自动检测写入，也可手动录入。
 *
 * @author l
 * @since 2026-05-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("device_fault_history")
public class DeviceFaultHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 设备编码(RTUCD)，对应 t_rtu_baseinfo.RTUCD，用于关联设备基础信息 */
    @TableField("device_code")
    private String deviceCode;

    /** 设备名称，冗余存储方便展示，避免每次查询关联表 */
    @TableField("device_name")
    private String deviceName;

    /** 设备类型，如水位站、流量站、水质站等 */
    @TableField("device_type")
    private String deviceType;

    /**
     * 故障类型枚举：
     * offline  - 通讯离线（ONLINE='0' 或最后通讯时间超过24小时）
     * no_data  - 硬件采集数据异常（设备在线但采集表无新数据）
     */
    @TableField("fault_type")
    private String faultType;

    /** 故障描述，详细说明故障现象和可能原因 */
    @TableField("fault_description")
    private String faultDescription;

    /** 故障发生时间，系统检测到故障的时间点 */
    @TableField("fault_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date faultTime;

    /** 恢复时间，设备恢复正常后由系统或人工填写 */
    @TableField("recovery_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date recoveryTime;

    /**
     * 处理状态：
     * active   - 未恢复/待处理（故障尚未解决）
     * resolved - 已恢复/已处理（设备已正常或已人工确认）
     */
    @TableField("status")
    private String status;

    /** 处理人，填写故障处理人员的姓名 */
    @TableField("handler")
    private String handler;

    /** 处理方式，描述具体的处理措施（如重启设备、检查网络、更换硬件等） */
    @TableField("handle_method")
    private String handleMethod;

    /** 处理时间，人工处理操作的时间戳 */
    @TableField("handle_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;

    /** 记录创建时间，由 MyBatis-Plus 自动填充 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 记录更新时间，由 MyBatis-Plus 自动填充 */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
