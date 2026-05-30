<template>
  <!-- 设备监控页面 -->
  <div class="div1">
    <!-- 面包屑导航 -->
    <div class="div-bread">
      <el-breadcrumb separator-class="el-icon-arrow-right" style="padding-top: 5px; padding-left: 10px">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>系统管理</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/devicemonitor' }">设备监控</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="div-body">
      <div class="div-body-main">
        <!-- 顶部统计卡片：展示设备总数、在线数、离线数 -->
        <div class="stat-cards">
          <div class="stat-card">
            <div class="stat-num">{{ statistics.total }}</div>
            <div class="stat-label">设备总数</div>
          </div>
          <div class="stat-card stat-online">
            <div class="stat-num">{{ statistics.onlineCount }}</div>
            <div class="stat-label">到报</div>
          </div>
          <div class="stat-card stat-offline">
            <div class="stat-num">{{ statistics.offlineCount }}</div>
            <div class="stat-label">未到报</div>
          </div>
        </div>

        <!-- 筛选区域：在线状态下拉框 + 刷新按钮 -->
        <div class="div-body-main-header">
          <div style="align-self: center; margin-left: 10px">
            <el-select v-model="queryInfo.online" placeholder="到报状态" clearable @change="loadPage" style="width: 140px">
              <el-option label="全部" value=""></el-option>
              <el-option label="到报" value="1"></el-option>
              <el-option label="未到报" value="0"></el-option>
            </el-select>
          </div>
          <el-button type="warning" round style="margin-left: auto; align-self: center; margin-right: 10px"
            @click="goToFaultHistory">历史故障</el-button>
          <el-button type="primary" round style="align-self: center; margin-right: 10px"
            @click="refreshData">刷新</el-button>
        </div>

        <!-- 设备列表表格：点击行可查看该设备的故障历史 -->
        <div class="div-body-main-body">
          <el-table :data="deviceList" border stripe height="100%" style="width: 100%"
            :header-cell-style="{ background: '#cfe2f3', color: '#606266' }"
            @row-click="showFaultHistory">
            <el-table-column type="index" label="序号" width="80px" align="center" :index="tableIndex">
            </el-table-column>
            <el-table-column prop="stnm" label="设备名称" align="center" show-overflow-tooltip>
            </el-table-column>
            <el-table-column prop="rtuCd" label="设备编号" align="center" width="130px">
            </el-table-column>
            <el-table-column prop="stcd" label="测站编码" align="center" width="110px">
            </el-table-column>
            <!-- 在线状态：绿色标签=在线，红色标签=离线 -->
            <el-table-column label="到报状态" align="center" width="100px">
              <template slot-scope="scope">
                <el-tag :type="scope.row.online === '1' ? 'success' : 'danger'" size="small">
                  {{ scope.row.online === '1' ? '到报' : '未到报' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="latestTm" label="最后通讯时间" align="center" width="170px">
              <template slot-scope="scope">
                <span>{{ scope.row.latestTm | formatDate }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="stlc" label="安装位置" align="center" show-overflow-tooltip>
            </el-table-column>
            <!-- 通讯预警值：超过此小时数未通讯即判定为通讯异常 -->
            <!-- <el-table-column prop="warnVal" label="通讯预警(h)" align="center" width="110px">
            </el-table-column> -->
          </el-table>
        </div>

        <!-- 分页 -->
        <div id="div-body-main-footer">
          <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
            :current-page="queryInfo.currentPage" :page-sizes="[10, 20, 30, 40]" :page-size="queryInfo.pageSize"
            layout="total, sizes, prev, pager, next, jumper" :total="total">
          </el-pagination>
        </div>
      </div>
    </div>

    <!-- 故障历史对话框：点击设备行后弹窗展示该设备的故障记录 -->
    <el-dialog title="故障历史" :visible.sync="faultDialogVisible" width="70%" @close="faultDialogClosed" center
      :close-on-press-escape="false">
      <el-table :data="faultList" border stripe style="width: 100%"
        :header-cell-style="{ background: '#cfe2f3', color: '#606266' }">
        <el-table-column type="index" label="序号" width="60px" align="center">
        </el-table-column>
        <!-- 故障类型：offline=通讯离线(红色) -->
        <el-table-column prop="faultType" label="故障类型" align="center" width="120px">
          <template slot-scope="scope">
            <el-tag :type="scope.row.faultType === 'offline' ? 'danger' : 'warning'" size="small">
              {{ scope.row.faultType === 'offline' ? '通讯离线' : scope.row.faultType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="faultDescription" label="故障描述" align="center" show-overflow-tooltip>
        </el-table-column>
        <el-table-column prop="faultTime" label="故障时间" align="center" width="160px">
          <template slot-scope="scope">{{ scope.row.faultTime | formatDate }}</template>
        </el-table-column>
        <el-table-column prop="recoveryTime" label="恢复时间" align="center" width="160px">
          <template slot-scope="scope">{{ scope.row.recoveryTime | formatDate }}</template>
        </el-table-column>
        <!-- 处理状态：active=未恢复(红色) resolved=已恢复(绿色) -->
        <el-table-column prop="status" label="状态" align="center" width="80px">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'active' ? 'danger' : 'success'" size="small">
              {{ scope.row.status === 'active' ? '未恢复' : '已恢复' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handler" label="处理人" align="center" width="100px">
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 15px; text-align: center" @current-change="handleFaultPageChange"
        :current-page="faultQuery.currentPage" :page-size="faultQuery.pageSize"
        layout="total, prev, pager, next, jumper" :total="faultTotal">
      </el-pagination>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      // 设备列表查询参数
      queryInfo: {
        currentPage: 1,
        pageSize: 10,
        online: ''   // ''-全部, '1'-在线, '0'-离线
      },
      deviceList: [],
      total: 0,
      // 顶部统计卡片数据
      statistics: {
        total: 0,
        onlineCount: 0,
        offlineCount: 0
      },
      // 故障历史弹窗相关
      faultDialogVisible: false,
      faultList: [],
      faultTotal: 0,
      faultQuery: {
        currentPage: 1,
        pageSize: 10,
        deviceCode: ''
      }
    }
  },
  methods: {
    /**
     * 合并接口：一次请求获取统计+列表。
     * 初始加载和筛选变化时调用，减少 HTTP 请求次数。
     */
    async loadPage() {
      const { data: res } = await this.$http.get('/deviceMonitor/overview', {
        params: this.queryInfo
      })
      if (res.code !== 200) {
        return this.$message.error('获取数据失败')
      }
      // 更新统计数据
      if (res.data.statistics) {
        this.statistics = res.data.statistics
      }
      // 更新设备列表
      if (res.data.deviceList) {
        this.deviceList = res.data.deviceList.records
        this.total = res.data.deviceList.total
      }
    },
    /** 刷新 */
    async refreshData() {
      this.queryInfo.currentPage = 1
      await this.loadPage()
    },
    /**
     * 翻页处理：关键优化！
     * 先发请求，等接口返回后再更新 currentPage，
     * 保证序号和数据同时变化，避免"第一页数据+第二页序号"的闪现。
     */
    async handleCurrentChange(newPage) {
      const { data: res } = await this.$http.get('/deviceMonitor/list', {
        params: {
          currentPage: newPage,
          pageSize: this.queryInfo.pageSize,
          online: this.queryInfo.online
        }
      })
      if (res.code !== 200) {
        return this.$message.error('获取设备列表失败')
      }
      // 数据到达后，再更新页码和列表
      this.queryInfo.currentPage = newPage
      this.deviceList = res.data.records
      this.total = res.data.total
    },
    /** 每页条数变化 */
    handleSizeChange(newSize) {
      this.queryInfo.pageSize = newSize
      this.loadPage()
    },
    /** 表格行号计算 */
    tableIndex(index) {
      return (this.queryInfo.currentPage - 1) * this.queryInfo.pageSize + index + 1
    },
    /** 点击行：查看该设备故障历史 */
    async showFaultHistory(row) {
      this.faultQuery.deviceCode = row.rtuCd
      this.faultQuery.currentPage = 1
      this.faultDialogVisible = true
      await this.getFaultList()
    },
    /** 查询故障历史 */
    async getFaultList() {
      const { data: res } = await this.$http.get('/deviceFaultHistory/list-by-device', {
        params: this.faultQuery
      })
      if (res.code === 200) {
        this.faultList = res.data.records
        this.faultTotal = res.data.total
      }
    },
    /** 故障弹窗翻页 */
    handleFaultPageChange(newPage) {
      this.faultQuery.currentPage = newPage
      this.getFaultList()
    },
    /** 跳转到历史故障页面 */
    goToFaultHistory() {
      this.$router.push('/home/devicefault')
    },
    faultDialogClosed() {
      this.faultList = []
      this.faultTotal = 0
    }
  },
  filters: {
    formatDate(val) {
      if (!val) return '-'
      return val
    }
  },
  created() {
    this.loadPage()
    // 每30秒自动刷新设备状态
    this._timer = setInterval(() => {
      this.loadPage()
    }, 30000)
  },
  beforeDestroy() {
    // 页面销毁时清除定时器，防止内存泄漏
    if (this._timer) {
      clearInterval(this._timer)
    }
  }
}
</script>

<style lang="less" scoped>
.div1 {
  height: 100%;
  width: 100%;
}
.div-bread {
  height: 25px;
  width: 100%;
}
.div-body {
  height: calc(100% - 25px);
  width: 100%;
  display: flex;
}
.div-body-main {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;  /* 改为纵向 flex 布局，子项自动分配空间 */
}
.stat-cards {
  flex-shrink: 0;  /* 不压缩 */
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 10px 0;
}
.stat-card {
  width: 200px;
  height: 70px;
  background: #ecf5ff;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 1px solid #b3d8ff;
}
.stat-online {
  background: #f0f9eb;
  border-color: #c2e7b0;
}
.stat-offline {
  background: #fef0f0;
  border-color: #fbc4c4;
}
.stat-num {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}
.div-body-main-header {
  flex-shrink: 0;
  height: 50px;
  width: 100%;
  display: flex;
  background-color: rgb(253, 242, 228);
}
.div-body-main-body {
  flex: 1;         /* 自动填满剩余空间 */
  width: 100%;
  overflow: auto;  /* 内容过多时滚动 */
}
#div-body-main-footer {
  flex-shrink: 0;
  height: 35px;
  width: 100%;
  text-align: center;
  background-color: rgb(245, 237, 230);
}
</style>
