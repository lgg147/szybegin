package com.szy.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * RTU设备扩展信息实体，映射 collector 库的 t_rtu_baseinfo_extend 表。
 * 用于存储设备的分组信息和站点类型等扩展属性，与 RtuBaseInfo 一对一关联。
 *
 * @author l
 * @since 2026-05-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_rtu_baseinfo_extend")
public class RtuBaseInfoExtend implements Serializable {
    private static final long serialVersionUID = 1L;

    /** RTU编码，主键，与 t_rtu_baseinfo.RTUCD 一一对应 */
    @TableId(value = "RTUCD")
    private String rtuCd;

    /**
     * 分组ID，逗号分隔。
     * 关联 t_rtu_groupinfo 表，例如 ",2,3," 表示该设备属于"流量"和"视频"分组。
     * 1-水位, 2-流量, 3-视频
     */
    @TableField("GROUP_ID")
    private String groupId;

    /** 站点类型编码，标识设备类型（如水位站、流量站、水质站等） */
    @TableField("STTP")
    private String sttp;

    /** 所属用户ID，标识设备归属的管理员 */
    @TableField("USER_ID")
    private Integer userId;
}
