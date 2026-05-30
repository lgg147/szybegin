<template>
  <!-- 历史故障页面：展示所有设备的故障记录，管理人员可在此查看和处理故障 -->
  <div class="div1">
    <!-- 面包屑导航 -->
    <div class="div-bread">
      <el-breadcrumb separator-class="el-icon-arrow-right" style="padding-top: 5px; padding-left: 10px">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/devicemonitor' }">设备监控</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/devicefault' }">历史故障</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="div-body">
      <div class="div-body-main">
        <!-- 筛选区域：支持按故障类型和处理状态筛选 -->
        <div class="div-body-main-header">
          <div style="align-self: center; margin-left: 10px">
            <span style="align-self:center; margin-left:20px;font-size:14px">故障类型：</span>
            <el-select v-model="queryInfo.faultType" placeholder="故障类型" clearable @change="getFaultList" style="width: 140px; margin-right: 10px">
              <el-option label="全部" value=""></el-option>
              <el-option label="通讯离线" value="offline"></el-option>
              <el-option label="硬件采集数据异常" value="no_data"></el-option>
            </el-select>
            <span style="align-self:center; margin-left:20px;font-size:14px">设备状态：</span>            <el-select v-model="queryInfo.status" placeholder="处理状态" clearable @change="getFaultList" style="width: 140px">
              <el-option label="全部" value=""></el-option>
              <el-option label="未恢复" value="active"></el-option>
              <el-option label="已恢复" value="resolved"></el-option>
            </el-select>
          </div>
          <el-button type="primary" round style="margin-left: auto; align-self: center; margin-right: 10px"
            @click="goBack">返回</el-button>
          <el-button type="primary" round style="align-self: center; margin-right: 10px"
            @click="getFaultList">刷新</el-button>
        </div>

        <!-- 故障列表表格 -->
        <div class="div-body-main-body">
          <el-table :data="faultList" border stripe height="100%" style="width: 100%"
            :header-cell-style="{ background: '#cfe2f3', color: '#606266' }">
            <el-table-column type="index" label="序号" width="80px" align="center" :index="tableIndex">
            </el-table-column>
            <el-table-column prop="deviceName" label="设备名称" align="center" show-overflow-tooltip>
            </el-table-column>
            <el-table-column prop="deviceCode" label="设备编号" align="center" width="130px">
            </el-table-column>
            <!-- 故障类型标签：offline-通讯离线(红色) no_data-数据未采集(黄色) abnormal-数据异常(黄色) -->
            <el-table-column label="故障类型" align="center" width="110px">
              <template slot-scope="scope">
                <el-tag :type="scope.row.faultType === 'offline' ? 'danger' : 'warning'" size="small">
                  {{ scope.row.faultType === 'offline' ? '通讯离线' : '硬件采集数据异常' }}
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
            <!-- 处理状态：active-未恢复(红色) resolved-已恢复(绿色) -->
            <el-table-column label="处理状态" align="center" width="80px">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === 'active' ? 'danger' : 'success'" size="small">
                  {{ scope.row.status === 'active' ? '未恢复' : '已恢复' }}
                </el-tag>
              </template>
            </el-table-column>
            <!-- <el-table-column prop="handler" label="处理人" align="center" width="100px">
            </el-table-column> -->
            <!-- 操作列：已恢复的记录不可再点击处理 -->
            <!-- <el-table-column fixed="right" label="操作" align="center" width="120px">
              <template slot-scope="scope">
                <el-button size="mini" type="primary" :disabled="scope.row.status === 'resolved'"
                  @click="showHandleDialog(scope.row)">处理</el-button>
              </template>
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

    <!-- 处理故障对话框：填写处理人和处理方式后提交 -->
    <el-dialog title="处理故障" :visible.sync="handleDialogVisible" width="40%" @close="handleDialogClosed" center
      :close-on-press-escape="false">
      <el-form :model="handleForm" ref="handleFormRef" label-width="100px" size="small">
        <el-form-item label="设备名称">
          <span>{{ currentFault.deviceName }}</span>
        </el-form-item>
        <el-form-item label="故障类型">
          <el-tag :type="currentFault.faultType === 'offline' ? 'danger' : 'warning'" size="small">
            {{ currentFault.faultType === 'offline' ? '通讯离线' : '硬件采集数据异常' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="故障描述">
          <span>{{ currentFault.faultDescription }}</span>
        </el-form-item>
        <el-form-item label="处理人" prop="handler">
          <el-input v-model="handleForm.handler" placeholder="请输入处理人"></el-input>
        </el-form-item>
        <el-form-item label="处理方式" prop="handleMethod">
          <el-input type="textarea" v-model="handleForm.handleMethod" placeholder="请描述处理方式" :rows="3"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleFault">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      // 查询参数：支持故障类型和处理状态筛选
      queryInfo: {
        currentPage: 1,
        pageSize: 10,
        faultType: '',   // ''-全部, offline-通讯离线, no_data-数据未采集, abnormal-数据异常
        status: ''       // ''-全部, active-未恢复, resolved-已恢复
      },
      faultList: [],
      total: 0,
      // 处理故障弹窗相关
      handleDialogVisible: false,
      currentFault: {},
      handleForm: {
        handler: '',
        handleMethod: ''
      }
    }
  },
  methods: {
    /** 分页查询故障列表 */
    async getFaultList() {
      const { data: res } = await this.$http.get('/deviceFaultHistory/list', {
        params: this.queryInfo
      })
      if (res.code !== 200) {
        return this.$message.error('获取故障列表失败')
      }
      this.faultList = res.data.records
      this.total = res.data.total
    },
    /** 每页条数变化 */
    handleSizeChange(newSize) {
      this.queryInfo.pageSize = newSize
      this.getFaultList()
    },
    /** 当前页变化：先发请求，数据到了再更新页码，防止序号与数据不同步 */
    async handleCurrentChange(newPage) {
      const { data: res } = await this.$http.get('/deviceFaultHistory/list', {
        params: {
          currentPage: newPage,
          pageSize: this.queryInfo.pageSize,
          faultType: this.queryInfo.faultType,
          status: this.queryInfo.status
        }
      })
      if (res.code !== 200) {
        return this.$message.error('获取故障列表失败')
      }
      this.queryInfo.currentPage = newPage
      this.faultList = res.data.records
      this.total = res.data.total
    },
    /** 表格行号计算 */
    tableIndex(index) {
      return (this.queryInfo.currentPage - 1) * this.queryInfo.pageSize + index + 1
    },
    /** 打开处理故障弹窗 */
    /** 返回设备监控页面 */
    goBack() {
      this.$router.push('/home/devicemonitor')
    },
    showHandleDialog(row) {
      this.currentFault = row
      this.handleForm.handler = ''
      this.handleForm.handleMethod = ''
      this.handleDialogVisible = true
    },
    /** 提交故障处理：将状态更新为 resolved，记录处理人/处理方式/处理时间 */
    async handleFault() {
      if (!this.handleForm.handler) {
        return this.$message.warning('请输入处理人')
      }
      const { data: res } = await this.$http.post('/deviceFaultHistory/handle', {
        id: this.currentFault.id,
        handler: this.handleForm.handler,
        handleMethod: this.handleForm.handleMethod
      })
      if (res.code !== 200) {
        return this.$message.error('处理失败')
      }
      this.$message.success('处理成功')
      this.handleDialogVisible = false
      this.getFaultList()
    },
    /** 关闭弹窗时清空表单 */
    handleDialogClosed() {
      this.handleForm.handler = ''
      this.handleForm.handleMethod = ''
    }
  },
  filters: {
    /** 日期格式化：空值显示为 - */
    formatDate(val) {
      if (!val) return '-'
      return val
    }
  },
  created() {
    this.getFaultList()
    // 每30秒自动刷新故障列表
    this._timer = setInterval(() => {
      this.getFaultList()
    }, 30000)
  },
  beforeDestroy() {
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
  flex-direction: column;
}
.div-body-main-header {
  flex-shrink: 0;
  height: 50px;
  width: 100%;
  display: flex;
  background-color: rgb(253, 242, 228);
}
.div-body-main-body {
  flex: 1;
  width: 100%;
  overflow: auto;
}
#div-body-main-footer {
  flex-shrink: 0;
  height: 35px;
  width: 100%;
  text-align: center;
  background-color: rgb(245, 237, 230);
}
</style>
