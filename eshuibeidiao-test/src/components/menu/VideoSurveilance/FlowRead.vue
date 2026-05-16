<template>
  <div id="div1">
    <div id="bread">
      <el-breadcrumb separator-class="el-icon-arrow-right" style="padding-top:5px; padding-left:10px">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>实时监测</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/flow' }">流量监测</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div id="div-header">
      <span style="align-self:center;font-size:15px; margin-left:20px">监测点：</span>
      <el-select v-model="mpCd" clearable placeholder="请选择" style="align-self:center;" @change="handleConditonChange">
        <el-option v-for="item in reservoirsList" :key="item.value" :label="item.label" :value="item.value">
        </el-option>
      </el-select>

      <span style="margin-left:10px ;align-self:center; font-size:14px">监测时间：</span>
      <el-date-picker v-model="datepickers" type="datetimerange" range-separator="-" start-placeholder="开始日期"
        end-placeholder="结束日期" align="right" format="yyyy-MM-dd HH:mm" value-format="yyyy-MM-dd HH:mm:ss"
        style="margin-left: 10px;align-self:center;" @change="handleConditonChange" @clear="handleConditonChange">
      </el-date-picker>

      <!-- <el-button type="primary" round icon="iconfont icon-icon-test"
        style="margin-left:auto; align-self:center;margin-right:10px" @click="importDialogVisible = true">导入</el-button> -->

      <el-button type="primary" round icon="iconfont icon-icon-test"
        style="margin-left:auto; align-self:center;margin-right:10px" @click="exportExcel">导出</el-button>
    </div>

    <div id="div-main">
      <div id="div-main1">
        <div id="chart1" class="div-index"></div>
      </div>
      <div id="div-main2">
        <el-table :data="FLList" border stripe height="calc(100% - 20px)" style="width: 100%"
          :header-cell-style="{ background: '#cfe2f3', color: '#606266' }">
          <el-table-column type="index" label="序号" width="60px" align="center" :index="table_index">
          </el-table-column>
          <el-table-column prop="tm" label="监测时间" align="center">
          </el-table-column>
          <el-table-column prop="mpCd" label="站码" align="center">
          </el-table-column>
          <el-table-column prop="mpQ" label="瞬时流量(m³/s)" align="center">
          </el-table-column>
          <el-table-column prop="accW" label="累计流量(m³)" align="center">
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div id="div-footer">
      <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
        :current-page="queryinfo.currentPage" :page-sizes="[10, 20, 30, 40]" :page-size="queryinfo.pageSize"
        layout="total, sizes, prev, pager, next, jumper" :total="total">
      </el-pagination>
    </div>

    <!-- 导入框 -->
    <el-dialog :visible.sync="importDialogVisible" width="800px" :modal="false" :close-on-press-escape="false"
      :close-on-click-modal="false">
      <template slot="title">
        <div style="border-bottom: 1px solid #ebebeb;font-size:20px;padding-bottom:10px">
          导入Excel
        </div>
      </template>
      <el-upload class="upload-demo" drag ref="upload" accept=".xlsx,.xls" name="file" action="/flow/import-excel"
        :file-list="fileList" :auto-upload="false" :on-change="fileChange" :on-remove="handleRemove"
        :before-upload="beforeUpload" :http-request="httpRequest" multiple>
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip" slot="tip">
          只能上传xls/xlsx文件
        </div>
      </el-upload>
      <span slot="footer">
        <el-button type="info" icon="el-icon-download" style="margin-right:450px"
          @click="exportFormwork">下载模板</el-button>
        <el-button type="success" @click="submitUpload">上 传</el-button>
        <el-button @click="importDialogVisible = false">取 消</el-button>
      </span>
    </el-dialog>

    <!-- 导出表格 -->
    <el-table id="table" :data="ALLFLList" border stripe height="100%" style="width: 100%;display:none;"
      :header-cell-style="{ background: '#cfe2f3', color: '#606266' }">
      <el-table-column type="index" label="序号" width="50px" align="center" :index="table_index">
      </el-table-column>
      <el-table-column prop="tm" label="监测时间" align="center" width="120px">
      </el-table-column>
      <el-table-column prop="mpCd" label="站码" align="center" width="120px">
      </el-table-column>
      <el-table-column prop="mpQ" label="瞬时流量(m³/s)" align="center" width="120px">
      </el-table-column>
      <el-table-column prop="accW" label="累计流量(m³)" align="center" width="120px">
      </el-table-column>
    </el-table>

    <!-- 导出数据模板 -->
    <el-table id="tableFormwork" :data="ExamplePI" border stripe height="100%" style="width: 100%;display:none;"
      :header-cell-style="{ background: '#cfe2f3', color: '#606266' }">
      <el-table-column type="index" label="序号" width="80px" align="center" :index="table_index">
      </el-table-column>
      <el-table-column prop="tm" label="监测时间" align="center">
      </el-table-column>
      <el-table-column prop="mpCd" label="站码" align="center">
      </el-table-column>
      <el-table-column prop="mpQ" label="瞬时流量(m³/s)" align="center">
      </el-table-column>
      <el-table-column prop="accW" label="累计流量(m³)" align="center">
      </el-table-column>

    </el-table>
  </div>
</template>

<script>
import echarts from 'echarts'
import FileSaver from 'file-saver'
import XLSX from 'xlsx'

export default {
  name: '',
  data() {
    return {
      //获取表格的参数对象
      queryinfo: {
        //当前页数
        currentPage: 1,
        //当前每页显示条数
        pageSize: 10
      },
      mpCd: '',
      datepickers: ['', ''],
      //统计数据
      dateTimeList: [],
      valueList: [],
      //水库名称列表
      reservoirs: [],
      reservoirsList: [],
      //数据列表
      FLList: [],
      ALLFLList: [],
      total: 0,
      //导入导出数据
      importDialogVisible: false,
      fileList: [],
      ExamplePI: [
        {
          mpCd: "水库1",
          mpQ: '10.24',
          accW: "100",
          tm: '2023-02-24 10:43:17'
        }
      ],
    }
  },
  methods: {
    handleConditonChange () {
      this.selectFlowStatistics();
      this.selectAllFlows();
    },
    //查询所有监测站点名称，提供下拉框选项
    async selectReservior() {
      const { data: res } = await this.$http.get('/dict/LVs', {
        params: {
          name: '流量监测点'
        }
      })
      if (res.code !== 200) {
        return this.$message.error('获取数据列表失败')
      }
      this.reservoirs = res.data
      if (this.reservoirs && this.reservoirs.length > 0) {
        this.mpCd = this.reservoirs[0].value; // 假设reservoirs[0]是一个对象或字符串等可以直接赋值的值  
      }
      this.reservoirsList = this.reservoirs.map(reservoir => ({
        value: reservoir.value, // 从每个 reservoir 对象中获取 value
        label: reservoir.label  // 从每个 reservoir 对象中获取 label
      }));
      this.selectFlowStatistics()
    },
    //获取数据列表
    async selectFlowStatistics() {
      if (!this.datepickers || this.datepickers.length === 0) {
        this.initDate()
      }
      const { data: res } = await this.$http.get('/flow/statistics', {
        params: {
          mpCd: this.mpCd,
          startTime: this.datepickers[0],
          endTime: this.datepickers[1],
        }
      })
      if (res.code !== 200) {
        return this.$message.error('获取数据列表失败')
      }
      this.valueList = res.data.valueList
      this.dateTimeList = res.data.dateTimeList
      this.setDateList()
      this.setChart1()
      this.selectFlowList()
    },
    //查询监测记录
    async selectFlowList() {
      const { data: res } = await this.$http.get(
        '/flow/list',
        {
          params: {
            currentPage: this.queryinfo.currentPage,
            pageSize: this.queryinfo.pageSize,
            mpCd: this.mpCd,
            startTime: this.datepickers[0],
            endTime: this.datepickers[1],
          }
        }
      )
      if (res.code !== 200) {
        return this.$message.error('搜索数据错误')
      }
      this.FLList = res.data.records
      this.total = res.data.total
    },
    //获取不分页数据列表
    async selectAllFlows() {
      const { data: res } = await this.$http.get('/flow/export-excel',
        {
          params: {
            mpCd: this.mpCd,
            startTime: this.datepickers[0],
            endTime: this.datepickers[1],
          }
        }
      )
      if (res.code !== 200) {
        return this.$message.error('获取数据列表失败')
      }
      this.ALLFLList = res.data
    },
    //监听pageSize
    handleSizeChange(newSize) {
      this.queryinfo.pageSize = newSize
      this.selectFlowList()
    },
    //监听currentPage
    handleCurrentChange(newPage) {
      this.queryinfo.currentPage = newPage
      this.selectFlowList()
    },
    //序号连续
    table_index(index) {
      return (
        (this.queryinfo.currentPage - 1) * this.queryinfo.pageSize + index + 1
      )
    },
    setDateList() {
      // 1. 空保护
      if (!this.dateTimeList || !this.dateTimeList.length) {
        this.dateTimeList = [];
        return;
      }

      // 2. 毫秒戳 → Date → yyyy-MM-dd HH:mm
      const fmt = d => {
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, '0');
        const dd = String(d.getDate()).padStart(2, '0');
        const h = String(d.getHours()).padStart(2, '0');
        const min = String(d.getMinutes()).padStart(2, '0');
        return `${y}-${m}-${dd} ${h}:${min}`;
      };

      this.dateTimeList = this.dateTimeList.map(ts => fmt(new Date(Number(ts))));
    },
    //得到横坐标的日期
    // setDateList() {
    //   const first = new Date(this.dateTimeList[0]);
    //   const last = new Date(this.dateTimeList[this.dateTimeList.length - 1]);

    //   /* 2. 按 5 分钟间隔生成刻度，但保留真实分钟 */
    //   this.dateTimeList = this.generateSlot(first, last, 5 * 60 * 1000);
    // },
    // /* 工具：从 start 到 end，每隔 step 毫秒打一个刻度，格式 yyyy-MM-dd HH:mm */
    // generateSlot(start, end, step) {
    //   const list = [];
    //   const fmt = d => {
    //     const y = d.getFullYear();
    //     const m = String(d.getMonth() + 1).padStart(2, '0');
    //     const dd = String(d.getDate()).padStart(2, '0');
    //     const h = String(d.getHours()).padStart(2, '0');
    //     const min = String(d.getMinutes()).padStart(2, '0');
    //     return `${y}-${m}-${dd} ${h}:${min}`;
    //   };
    //   let cur = new Date(start);
    //   while (cur <= end) {
    //     list.push(fmt(cur));
    //     cur = new Date(cur.getTime() + step);
    //   }
    //   return list;
    // },
    //初始化起止时间为当前时间48之前-当前时间
    initDate() {
      const now = new Date();
      const twoDaysAgo = new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000);

      // 定义一个函数来格式化日期和时间  
      function formatDateTime(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
      }
      this.datepickers = [formatDateTime(twoDaysAgo), formatDateTime(now)];
    },
    //画图
    setChart1() {
      // 基于准备好的dom，初始化echarts实例
      var myChart = echarts.init(document.getElementById('chart1'))
      // 指定图表的配置项和数据
      var option = {
        title: {
          text: '流量统计'
        },
        legend: { // 添加图例  
          data: ['监测值'] // 图例的数据列表，应与 series 中的 name 对应  
        },
        tooltip: {
          trigger: 'axis', // 触发类型，可选为：'item'、'axis'  
          formatter: function (params) { // 自定义提示框内容  
            let item = params[0]; // 获取第一个（也是唯一的一个）数据点
            // 构建 tooltip 的内容
            let content = `监测时间: ${item.axisValue}<br/>`;
            // 添加系列名称和值
            content += `${item.seriesName}: ${item.value}`;
            return content; // 返回构建好的 tooltip 内容
          }
        },
        xAxis: {
          type: 'category',
          data: this.dateTimeList,
          name: '时间',
          position: 'bottom' // 确保x轴在底部
        },
        yAxis: {
          type: 'value',
          name: '流量(m³/s)'
        },
        series: [
          {
            name: '监测值',
            data: this.valueList,
            type: 'line',
            smooth: true
          }
        ]
      };
      // 使用刚指定的配置项和数据显示图表。
      myChart.setOption(option)
    },
    //导出模板
    exportFormwork() {
      var xlsxParam = { raw: true }
      var wb = XLSX.utils.table_to_book(
        document.querySelector('#tableFormwork'),
        xlsxParam
      )
      var wbout = XLSX.write(wb, {
        bookType: 'xlsx',
        bookSST: true,
        type: 'array'
      })
      try {
        FileSaver.saveAs(
          new Blob([wbout], { type: 'application/octet-stream' }),
          '流量监测数据模板.xlsx'
        )
      } catch (e) {
        if (typeof console !== 'undefined') {
          console.log(e, wbout)
        }
      }
      return wbout
    },
    //导入Excel
    submitUpload() {
      //关闭表单
      this.importDialogVisible = false
      this.$refs.upload.submit()
      setTimeout(() => {
        this.initDate()
        this.selectReservior()
        this.selectAllFlows()
      }, 1000)
    },
    //导出Excel
    exportExcel() {
      var xlsxParam = { raw: true }
      var wb = XLSX.utils.table_to_book(
        document.querySelector('#table'),
        xlsxParam
      )
      var wbout = XLSX.write(wb, {
        bookType: 'xlsx',
        bookSST: true,
        type: 'array'
      })
      try {
        FileSaver.saveAs(
          new Blob([wbout], { type: 'application/octet-stream' }),
          '流量监测数据纪录报告.xlsx'
        )
      } catch (e) {
        if (typeof console !== 'undefined') {
          console.log(e, wbout)
        }
      }
      return wbout
    },
    fileChange(file, fileList) {
      this.fileList = fileList
    },
    handleRemove(file, fileList) {
      this.fileList = fileList
    },
    async httpRequest(param) {
      let fileObj = param.file
      let formData = new FormData()
      formData.append('file', fileObj)
      let url = '/flow/import-excel'
      let config = {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }
      const { data: res } = await this.$http.post(url, formData, config)
      if (res.code === 200) {
        this.$message.success('导入成功')
      } else {
        this.$message.error(res.message)
      }
    },
    //上传文件之前进行文件类型判断
    beforeUpload(file) {
      // 允许上传的文件格式列表
      let acceptList = ['xlsx', 'xls']
      // 根据文件名获取文件的后缀名
      let fileType = file.name
        .split('.')
        .pop()
        .toLowerCase()
      // 判断文件格式是否符合要求
      if (acceptList.indexOf(fileType) === -1) {
        this.$message.error('只能上传 xlsx/xls 格式的文件 !')
        return false
      }
    }
  },
  created() {
    this.initDate()
    this.selectReservior()
    this.selectAllFlows()
  },
  mounted() {
  }
}
</script>

<style lang="less" scoped>
#div1 {
  height: 100%;
  width: 100%;
}

#bread {
  height: 25px;
  width: 100%;
}

#div-header {
  height: 60px;
  width: 100%;
  margin-top: 0px;
  display: flex;
  background-color: rgb(253, 242, 228);
}

#div-main {
  height: calc(100% - 120px);
  width: 100%;
}

#div-main1 {
  background-color: rgb(255, 255, 255);
  height: 300px;
  width: 100%;
  display: flex;
}

#div-main2 {
  background-color: rgb(255, 255, 255);
  height: calc(100% - 300px);
  width: 100%;
  display: flex;
}

#div-footer {
  height: 35px;
  width: 100%;
  float: left;
  text-align: center;
  background-color: rgb(245, 237, 230);
}

.div-index {
  height: 280px;
  width: 100%;
  margin: 10px 20px 10px 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.04);
}

#bread {
  height: 25px;
  width: 100%;
}

.upload-demo {
  margin: 10px 200px;
}
</style>