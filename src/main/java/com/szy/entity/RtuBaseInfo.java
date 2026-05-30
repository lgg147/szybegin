package com.szy.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * RTU设备基础信息实体，映射 collector 库的 t_rtu_baseinfo 表。
 * 该表由数据采集系统维护，记录了所有RTU设备的注册信息、在线状态和最近通讯时间，
 * 是设备监控模块的数据核心来源。
 *
 * @author l
 * @since 2026-05-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_rtu_baseinfo")
public class RtuBaseInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /** RTU编码，主键，设备的唯一标识 */
    @TableId(value = "RTUCD")
    private String rtuCd;

    /** 测站编码，对应水利标准中的测站编号（如 st_river_r.STCD） */
    @TableField("STCD")
    private String stcd;

    /** 测站名称，设备的中文名称，用于前端展示 */
    @TableField("STNM")
    private String stnm;

    /** 设备安装日期 */
    @TableField("BDATE")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date bdate;

    /** 安装位置经度 */
    @TableField("LGTD")
    private Double lgtd;

    /** 安装位置纬度 */
    @TableField("LTTD")
    private Double lttd;

    /** 站址，设备的详细安装位置描述 */
    @TableField("STLC")
    private String stlc;

    /**
     * 通讯预警值，单位小时。
     * 超过此时间未收到设备上报数据，则认为通讯异常。
     * 例如值为24表示设备超过24小时未通讯则触发离线告警。
     */
    @TableField("WARN_VAL")
    private Double warnVal;

    /** 最近通讯时间，设备最后一次与服务器通信的时间戳 */
    @TableField("LATEST_TM")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date latestTm;

    /**
     * 设备是否在线。
     * 1-在线，0-离线。
     * 该字段由数据采集系统根据心跳上报自动更新。
     */
    @TableField("ONLINE")
    private String online;

    /** 备注信息 */
    @TableField("REMARK")
    private String remark;
}
