<template>
  <div id="div1">
    <!-- 地图底图 -->
    <div id="map" class="map"></div>

    <!-- 点击坐标弹框 -->
    <div id="popup" class="ol-popup">
      <el-dialog title="坐标信息" v-model="zuobiaoMes" :visible.sync="zuobiaoDialogVisible" width="30%">
        <div class="zuobiaoMes">
          <label>经度：</label>
          <span>{{ zuobiaoMes[0] }}</span>
          <br />
          <label>纬度：</label>
          <span>{{ zuobiaoMes[1] }}</span>
        </div>
      </el-dialog>
    </div>

    <!-- 工程搜索框 -->
    <div class="project-select">
      <el-select v-model="project" clearable placeholder="请选择" style="align-self:center;" @change="handleButtonClick">
        <el-option v-for="item in projectsList" :key="item.value" :label="item.label" :value="item.value">
        </el-option>
      </el-select>
    </div>
    <el-dialog width="1500px" :visible.sync="dialogVisible" :before-close="handleClose">
      <div class="top-head">
        <span id="projectName" style="color: #259Dff">{{ projectName }}</span>
        <el-button style="margin-left: 20px; color:#259Dff" @click="locateProjectPosition"
          icon="el-icon-location-outline"></el-button>
      </div>
      <div style="width: 100%; height: calc(100%-40px); overflow: auto; box-sizing:">
        <div id="RS" style="width: 100%; height: 100%; box-sizing:">
          <div id="RSMSG" style="width: 100%; height: calc(100%-30px); overflow-y: auto; padding: 0 30px;">
            <div class="top-title">
              <span style="color: #000000; font-size: 14px;">基本信息</span>
            </div>
            <div v-if="baseinfoRS == true" class="table-content">
              <div class="msg-tip-content">
                <span style="font-size: 14px;">总库容(万m³):</span>
                <span style="font-size: 14px;" v-if="RSList.length > 0">{{
                  RSList[0].totalStorageCapacity }}</span>
              </div>
              <div class="msg-tip-content">
                <span style="font-size: 14px;">调节库容(万m³):</span>
                <span style="font-size: 14px;" v-if="RSList.length > 0">{{
                  RSList[0].regulatingStorageCapacity }}</span>
              </div>
              <div class="msg-tip-content">
                <span style="font-size: 14px;">死库容(万m³):</span>
                <span style="font-size: 14px;" v-if="RSList.length > 0">{{ RSList[0].deadStorage }}</span>
              </div>
              <div class="msg-tip-content">
                <span style="font-size: 14px;">设计洪水位(m):</span>
                <span style="font-size: 14px;" v-if="RSList.length > 0">{{ RSList[0].designFloodLevel
                  }}</span>
              </div>
              <div class="msg-tip-content">
                <span style="font-size: 14px;">正常蓄水位(m):</span>
                <span style="font-size: 14px;" v-if="RSList.length > 0">{{ RSList[0].normalStorageLevel
                  }}</span>
              </div>
              <div class="msg-tip-content">
                <span style="font-size: 14px;">死水位(m):</span>
                <span style="font-size: 14px;" v-if="RSList.length > 0">{{ RSList[0].deadWaterLevel }}</span>
              </div>
            </div>
            <div v-if="baseinfoPS == true" class="table-content">
              <div class="msg-tip-content">
                <span style="font-size: 14px;">设计规模(m³/天):</span>
                <span style="font-size: 14px;" v-if="PSList.length > 0">{{ PSList[0].designScale }}</span>
              </div>
              <div class="msg-tip-content">
                <span style="font-size: 14px;">扬程(m):</span>
                <span style="font-size: 14px;" v-if="PSList.length > 0">{{ PSList[0].lift }}</span>
              </div>
              <div class="msg-tip-content">
                <span style="font-size: 14px;">装机容量(KW):</span>
                <span style="font-size: 14px;" v-if="PSList.length > 0">{{ PSList[0].installedCapacity
                  }}</span>
              </div>
              <div class="msg-tip-content">
                <span style="font-size: 14px;">运行方式:</span>
                <span style="font-size: 14px;" v-if="PSList.length > 0">{{ PSList[0].operationMode }}</span>
              </div>
              <div class="msg-tip-content">
                <span style="font-size: 14px;">机组数量:</span>
                <span style="font-size: 14px;" v-if="PSList.length > 0">{{ PSList[0].number }}</span>
              </div>
              <!-- <div class="msg-tip-content">
                <span style="font-size: 14px;">泵站状态:</span>
                <span style="font-size: 14px;" v-if="PSList.length > 0">{{ PSList[0].company }}</span>
              </div> -->
            </div>
            <div v-if="baseinfoWW == true" class="table-content">
              <div class="msg-tip-content">
                <span style="font-size: 14px;">设计规模(m³/天):</span>
                <span style="font-size: 14px;" v-if="WWList.length > 0">{{ WWList[0].designScale }}</span>
              </div>
              <div class="msg-tip-content">
                <span style="font-size: 14px;">供水范围:</span>
                <span style="font-size: 14px;" v-if="WWList.length > 0">{{ WWList[0].waterSupplyRange }}</span>
              </div>
              <div class="msg-tip-content">
                <span style="font-size: 14px;">供水负荷率(%):</span>
                <span style="font-size: 14px;" v-if="WWList.length > 0">{{ WWList[0].waterSupplyLoadRate }}</span>
              </div>
              <div class="msg-tip-content">
                <span style="font-size: 14px;">供水人口(万人):</span>
                <span style="font-size: 14px;" v-if="WWList.length > 0">{{ WWList[0].population }}</span>
              </div>
              <div class="msg-tip-content">
                <span style="font-size: 14px;">负责人:</span>
                <span style="font-size: 14px;" v-if="WWList.length > 0">{{ WWList[0].responsiblePerson }}</span>
              </div>
              <div class="msg-tip-content">
                <span style="font-size: 14px;">负责人电话:</span>
                <span style="font-size: 14px;" v-if="WWList.length > 0">{{ WWList[0].phone }}</span>
              </div>
            </div>
            <div v-if="baseinfoFB == true" class="table-content">
              <div class="msg-tip-content">
                <span style="font-size: 14px;">浮船容量(m³/h): </span>
                <span style="font-size: 14px;" v-if="FBList.length > 0">{{ FBList[0].capacity }}</span>
              </div>
              <div class="msg-tip-content">
                <span style="font-size: 14px;">浮船能耗(kw/h): </span>
                <span style="font-size: 14px;" v-if="FBList.length > 0">{{ FBList[0].powerConsumption }}</span>
              </div>
            </div>
            <div v-if="baseinfoTown == true" class="table-content">
              <div class="msg-tip-content">
                <span style="font-size: 14px;">村庄名称: </span>
                <span style="font-size: 14px;" v-if="townList.length > 0">{{ townList[0].name }}</span>
              </div>
              <div class="msg-tip-content">
                <span style="font-size: 14px;">现居人口（人）: </span>
                <span style="font-size: 14px;" v-if="townList.length > 0">{{ townList[0].population }}</span>
              </div>
            </div>
            <div v-if="baseinfoFlow == true" class="table-content">
              <div class="msg-tip-content">
                <span style="font-size: 14px;">监测点名称: </span>
                <span style="font-size: 14px;" v-if="flowItemList.length > 0">{{ flowItemList[0].name }}</span>
              </div>
              <div class="msg-tip-content">
                <span style="font-size: 14px;">监测点编码: </span>
                <span style="font-size: 14px;" v-if="flowItemList.length > 0">{{ flowItemList[0].code }}</span>
              </div>
            </div>
            <div class="top-title" v-if="baseinfoTown == false && baseinfoFB == false && baseinfoPS == false">
              <span style="color: #000000; font-size: 14px;">监测信息</span>
            </div>
            <div class="small-title" v-if="WLList && WLList.length > 0 && baseinfoRS == true">
              <span style="color: #259Dff; font-size: 14px;">水位</span>
            </div>
            <el-table v-if="WLList && WLList.length > 0 && baseinfoRS == true" :data="WLList" border stripe
              style="width: 100%" :header-cell-style="{ background: '#cfe2f3', color: '#606266' }">
              <el-table-column prop="position" label="监测点" align="center">
              </el-table-column>
              <el-table-column prop="code" label="站码" align="center">
              </el-table-column>
              <el-table-column prop="value" label="水位(m)" align="center">
              </el-table-column>
              <el-table-column prop="monitorTime" label="监测时间" align="center">
              </el-table-column>
            </el-table>
            <div class="small-title" v-if="FLList && FLList.length > 0 && baseinfoFlow == true">
              <span style="color: #259Dff; font-size: 14px;">流量</span>
            </div>
            <el-table v-if="FLList && FLList.length > 0 && baseinfoFlow == true" :data="FLList" border stripe
              style="width: 100%" :header-cell-style="{ background: '#cfe2f3', color: '#606266' }">
              <el-table-column prop="mpCd" label="RTU编码" align="center">
              </el-table-column>
              <el-table-column prop="mpQ" label="瞬时流量(m³/s)" align="center">
              </el-table-column>
              <el-table-column prop="accW" label="累计流量(m³)" align="center">
              </el-table-column>
              <el-table-column prop="tm" label="监测时间" align="center">
              </el-table-column>
            </el-table>
            <div class="small-title" v-if="WQList && WQList.length > 0 && baseinfoWW == true">
              <span style="color: #259Dff; font-size: 14px;">水质</span>
            </div>
            <el-table v-if="WQList && WQList.length > 0 && baseinfoWW == true" :data="WQList" border stripe
              style="width: 100%" :header-cell-style="{ background: '#cfe2f3', color: '#606266' }">
              <el-table-column prop="position" label="监测点" align="center">
              </el-table-column>
              <el-table-column prop="code" label="站码" align="center">
              </el-table-column>
              <el-table-column prop="swt" label="水温(℃)" align="center">
              </el-table-column>
              <el-table-column prop="zd" label="浊度(NTU)" align="center">
              </el-table-column>
              <el-table-column prop="ph" label="PH" align="center">
              </el-table-column>
              <el-table-column prop="ddl" label="电导率(uS/cm)" align="center">
              </el-table-column>
              <el-table-column prop="rjy" label="溶解氧(mg/L)" align="center">
              </el-table-column>
              <el-table-column prop="ad" label="氨氮(mg/L)" align="center">
              </el-table-column>
              <el-table-column prop="cod" label="化学需氧量(mg/L)" align="center">
              </el-table-column>
              <el-table-column prop="yl" label="余氯(mg/L)" align="center">
              </el-table-column>
              <el-table-column prop="monitorTime" label="监测时间" width="160" align="center">
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog width="1000px" :visible.sync="dialogPwiVisible" :before-close="handleClose">
      <div style="margin-bottom: 10px;">
        <el-table :data="pwiOne" border stripe style="width: 100%"
          :header-cell-style="{ background: '#cfe2f3', color: '#606266' }">
          <el-table-column prop="content" label="预警内容" align="center">
          </el-table-column>
          <el-table-column prop="type" label="预警类型" align="center">
          </el-table-column>
          <el-table-column prop="position" label="预警地点" align="center">
          </el-table-column>
          <el-table-column prop="level" label="预警等级" align="center">
          </el-table-column>
          <el-table-column prop="startTime" label="发生时间" align="center">
          </el-table-column>
          <el-table-column label="位置" width="100px" align="center">
            <template slot-scope="scope">
              <el-button type="text" @click="goToLocation(scope.row.longitude, scope.row.latitude)">
                跳转
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

    </el-dialog>

    <!-- 图例 -->
    <div class="legend">
      <!-- 工程站点标题 -->
      <div class="legend-title">工程站点</div>
      <!-- 工程站点图标和文本排列 -->
      <div class="legend-row">
        <div class="legend-item">
          <div class="legend-icon icon-reservoir"></div>
          <div class="legend-text">两河口水库</div>
        </div>
        <div class="legend-item">
          <div class="legend-icon icon-flow-item"></div>
          <div class="legend-text">流量站</div>
        </div>
      </div>
      <div class="legend-row">
        <div class="legend-item">
          <div class="legend-icon icon-water-plant"></div>
          <div class="legend-text">水厂</div>
        </div>
        <div class="legend-item">
          <div class="legend-icon icon-impoundment"></div>
          <div class="legend-text">浮船</div>
        </div>
      </div>
      <div class="legend-row">
        <div class="legend-item">
          <div class="legend-icon icon-pump-station"></div>
          <div class="legend-text">加压泵站</div>
        </div>
        <div class="legend-item">
          <div class="legend-icon icon-pump-on"></div>
          <div class="legend-text">泵打开</div>
        </div>
      </div>
      <div class="legend-row">
        <div class="legend-item">
          <div class="legend-icon icon-pump-off"></div>
          <div class="legend-text">泵关闭</div>
        </div>
      </div>
      <!-- 预警站点标题 -->
      <div class="legend-title">预警站点</div>
      <!-- 预警站点图标和文本排列 -->
      <div class="legend-row">
        <div class="legend-item">
          <div class="legend-icon icon-general-warning"></div>
          <div class="legend-text">一般预警</div>
        </div>
        <div class="legend-item">
          <div class="legend-icon icon-serious-warning"></div>
          <div class="legend-text">严重预警</div>
        </div>
      </div>

      <div class="legend-title">供水管线</div>
      <div class="legend-row">
        <div class="legend-item">
          <div class="legend-icon icon-main-pipe"></div>
          <div class="legend-text">供水干管</div>
        </div>
        <div class="legend-item">
          <div class="legend-icon icon-branch-pipe"></div>
          <div class="legend-text">供水支管</div>
        </div>
      </div>
    </div>

    <!-- 预警信息 -->
    <el-popover style="border-radius: 10px;" align="center" placement="bottom" width="1300" trigger="click">
      <div id="div-header1">
        <span style="align-self:center; font-size:14px">发生时间：</span>
        <el-date-picker v-model="datepickers" type="datetimerange" range-separator="-" start-placeholder="开始日期"
          end-placeholder="结束日期" align="right" value-format="yyyy-MM-dd HH:mm:ss"
          style="align-self:center;width: 400px;" @change="getPwiInfo" @clear="getPwiInfo">
        </el-date-picker>
        <span style="align-self:center; margin-left:20px; font-size:14px;">预警地点：</span>
        <el-select v-model="position" clearable placeholder="请选择" style="align-self:center;width: 150px;"
          @change="getPwiInfo" @clear="getPwiInfo">
          <el-option v-for="item in positionList" :key="item.value" :label="item.label" :value="item.value">
          </el-option>
        </el-select>
        <span style="align-self:center; margin-left:20px; font-size:14px;">预警类型：</span>
        <el-select v-model="type" clearable placeholder="请选择" style="align-self:center;width: 100px;"
          @change="getPwiInfo" @clear="getPwiInfo">
          <el-option v-for="item in typeList" :key="item.value" :label="item.label" :value="item.value">
          </el-option>
        </el-select>
        <span style="align-self:center; margin-left:20px; font-size:14px;">预警等级：</span>
        <el-select v-model="level" clearable placeholder="请选择" style="align-self:center;width: 110px;"
          @change="getPwiInfo" @clear="getPwiInfo">
          <el-option v-for="item in levelList" :key="item.value" :label="item.label" :value="item.value">
          </el-option>
        </el-select>
        <div style="margin-left: auto; margin-right: 10px; align-self: center;">
          <div class="item">
            <span class="value" style="background-color: #ffa647;">{{ warningCounts[0] }}</span>
            <span style="align-self:center; margin-left:5px;font-size:14px">一般</span>
          </div>
          <div class="item" style="margin-left: 15px;">
            <span class="value" style="background-color: #ff4949;">{{ warningCounts[1] }}</span>
            <span style="align-self:center; margin-left:5px;font-size:14px">严重</span>
          </div>
        </div>
      </div>
      <div style="max-height: 390px; overflow-y: auto; margin-bottom: 10px;">
        <!-- 表格内容 -->
        <el-table :data="PWIList" style="width: 100%" :header-cell-style="{ background: '#cfe2f3', color: '#606266' }">
          <el-table-column type="index" label="序号" width="80px" align="center" :index="table_index">
          </el-table-column>
          <el-table-column prop="position" label="预警地点" align="center">
          </el-table-column>
          <el-table-column prop="type" label="预警类型" align="center">
          </el-table-column>
          <el-table-column prop="level" label="预警等级" align="center">
          </el-table-column>
          <el-table-column prop="content" label="预警内容" align="center">
          </el-table-column>
          <el-table-column prop="startTime" label="发生时间" align="center">
          </el-table-column>
          <el-table-column label="位置" width="100px" align="center">
            <template slot-scope="scope">
              <el-button type="text" @click="goToLocation(scope.row.longitude, scope.row.latitude)">
                跳转
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <el-button class="warning-button" slot="reference">预警信息</el-button>
    </el-popover>

    <!-- 工程站点 -->
    <el-popover style="border-radius: 10px;" align="center" placement="bottom" width="100" trigger="click">
      <!-- 勾选显示站点 -->
      <div id="div-checkbox">
        <label>
          <input type="checkbox" v-model="showReservoirs" @change="updateMapMarkers">
          <img src="../../icon/水库.png" alt="水库图标" class="checkbox-icon">
          两河口水库
        </label>
        <label>
          <input type="checkbox" v-model="showPumpStations" @change="updateMapMarkers">
          <img src="../../icon/泵站.png" alt="泵站图标" class="checkbox-icon">
          泵站
        </label>
        <label>
          <input type="checkbox" v-model="showWaterWorks" @change="updateMapMarkers">
          <img src="../../icon/水厂.png" alt="水厂图标" class="checkbox-icon">
          水厂
        </label>
        <label>
          <input type="checkbox" v-model="showFloatingBoats" @change="updateMapMarkers">
          <img src="../../icon/浮船.png" alt="浮船图标" class="checkbox-icon">
          浮船
        </label>
        <label>
          <input type="checkbox" v-model="showFlowItems" @change="updateMapMarkers">
          <img src="../../icon/流量站点.png" alt="流量站点图标" class="checkbox-icon">
          流量站点
        </label>
        <!-- <label>
          <input type="checkbox" v-model="showTowns" @change="updateMapMarkers">
          <img src="../../icon/村庄.png" alt="村庄图标" class="checkbox-icon">
          村庄
        </label> -->
      </div>
      <el-button class="project-button" slot="reference">工程站点</el-button>
    </el-popover>

  </div>


</template>

<script>
// 导入高德地图相关
import AMapLoader from '@amap/amap-jsapi-loader';
export default {
  // 使用高德地图api重写onemap页面
  name: '',
  data() {
    return {
      // 基础地图
      markers: [], // 存储所有标记的数组
      map: null,
      toolbar: null,
      scale: null,
      scageolocationle: null,
      maptype: null,
      zuobiaoDialogVisible: false, //控制点击坐标弹框显示隐藏
      zuobiaoMes: [], //坐标信息
      iconData: [], //存放图标
      curAMap: null,//备份AMap对象
      //预警信息数据
      queryinfo: {
        //当前页数
        currentPage: 1,
        //当前每页显示条数
        pageSize: 5
      },
      status: '未解除',
      level: '',
      levels: [],
      levelList: [],
      type: '',
      types: [],
      typeList: [],
      position: '',
      positions: [],
      positionList: [],
      datepickers: ['', ''],
      warningCounts: [],
      PWIList: [],
      total: 0,
      //复选框
      checkList: ['泵站', '管道', '水库', '巡检', '地下水', '蓄水池', '单独流量站', '单独压力测站'],
      checkListValue: [],
      //工程站点
      dialogVisible: false,
      baseinfoRS: false,
      baseinfoPS: false,
      baseinfoWD: false,
      baseinfoWW: false,
      baseinfoFB: false,
      baseinfoTown: false,
      baseinfoFlow: false,
      project: '',
      projectsList: [],
      projectName: '',
      projectNameList: [],
      projectNameListSelect: [],
      code: '',
      longitude: '',
      latitude: '',
      longitudeList: [],
      latitudeList: [],
      showReservoirs: true, // 控制是否显示水库站点
      showPumpStations: true, // 控制是否显示泵站站点
      showWaterWorks: true, // 控制是否显示水厂站点
      showFloatingBoats: true, // 控制是否显示浮船站点
      showFlowItems: true, // 流量
      showTowns: true, // 村庄
      RSList: [],
      reservoirList: [],
      PSList: [],
      pumpStationList: [],
      WWList: [],
      waterWorkList: [],
      FBList: [],
      floatingBoatList: [],
      WDList: [],
      pwiNameList: [],
      pwiTypeList: [],
      pwiLevelList: [],
      pwiLongitudeList: [],
      pwiLatitudeList: [],
      pwiMonitorTimeList: [],
      pwiName: '',
      pwiStartTime: '',
      pwiOne: [],
      dialogPwiVisible: false,
      WLList: [],
      FLList: [],
      WQList: [],
      townList: [],
      townNameList: [],
      townLongitudeList: [],
      townLatitudeList: [],
      flowItemList: [],
      flowItemNameList: [],
      flowItemCodeList: [],
      flowItemLongitudeList: [],
      flowItemLatitudeList: [],
      pumpList: [],
      pumpNameList: [],
      pumpLongitudeList: [],
      pumpLatitudeList: [],
      pumpStatusList: [],
      lineList: [],
      refreshInterval: null,
    }
  },
  methods: {
    // 地图初始化
    initAMap() {
      AMapLoader.load({
        key: "b29bbc8bcb421044d560756fb3d2c828", // 申请好的Web端开发者Key，首次调用 load 时必填
        version: "2.0", // 指定要加载的 JSAPI 的版本，缺省时默认为 1.4.15
        plugins: ['AMap.ToolBar', 'AMap.Scale', 'AMap.Geolocation', 'AMap.MapType'], // 需要使用的的插件列表，如比例尺'AMap.Scale'等
      })
        .then((AMap) => {
          // 备份AMap对象
          this.curAMap = AMap;
          this.map = new AMap.Map("map", {
            // 设置地图容器id
            viewMode: "3D", // 是否为3D地图模式
            zoom: 14.5, // 初始化地图级别
            center: [113.496541, 31.883941], // 初始化地图中心点位置
          });

          // 增加地图控件
          //在回调函数中实例化插件
          this.toolbar = new AMap.ToolBar({ position: 'LT' }); //创建工具条插件实例
          this.map.addControl(this.toolbar); //添加工具条插件到页面
          this.scale = new AMap.Scale();
          this.map.addControl(this.scale);  //比例尺
          this.geolocation = new AMap.Geolocation();
          this.map.addControl(this.geolocation); //获取当前位置
          this.maptype = new AMap.MapType();
          this.map.addControl(this.maptype); //图层切换
          // 增加工程站点 (水库，水厂，泵站，浮船)
          for (let i = 0; i < this.projectNameList.length; i++) {
            if (i < this.longitudeList.length && i < this.latitudeList.length) {
              const name = this.projectNameList[i];
              const longitude = this.longitudeList[i];
              const latitude = this.latitudeList[i];
              this.addProjectIcon({ name, longitude, latitude });
            }
          }
          // 增加泵站点
          for (let i = 0; i < this.pumpNameList.length; i++) {
            if (i < this.pumpLongitudeList.length && i < this.pumpLatitudeList.length) {
              const name = this.pumpNameList[i];
              const longitude = this.pumpLongitudeList[i];
              const latitude = this.pumpLatitudeList[i];
              const status = this.pumpStatusList[i];
              this.addPumpIcon({ name, longitude, latitude, status });
            }
          }
          // 增加预警站点
          for (let i = 0; i < this.pwiNameList.length; i++) {
            if (i < this.pwiLongitudeList.length && i < this.pwiLatitudeList.length) {
              const name = this.pwiNameList[i];
              const level = this.pwiLevelList[i];
              const longitude = this.pwiLongitudeList[i];
              const latitude = this.pwiLatitudeList[i];
              const type = this.pwiTypeList[i];
              const startTime = this.pwiMonitorTimeList[i];
              this.addPrewarnIcon({ name, level, longitude, latitude, type, startTime });
            }
          }
          // 增加村子站点
          for (let i = 0; i < this.townNameList.length; i++) {
            if (i < this.townLongitudeList.length && i < this.townLatitudeList.length) {
              const name = this.townNameList[i];
              const longitude = this.townLongitudeList[i];
              const latitude = this.townLatitudeList[i];
              // this.addTownIcon({ name, longitude, latitude });
            }
          }
          // 增加流量站点
          for (let i = 0; i < this.flowItemList.length; i++) {
            if (i < this.flowItemList.length && i < this.flowItemList.length) {
              const name = this.flowItemNameList[i];
              const code = this.flowItemCodeList[i];
              const longitude = this.flowItemLongitudeList[i];
              const latitude = this.flowItemLatitudeList[i];
              this.addFlowIcon({ name, code, longitude, latitude });
            }
          }
          // 获取该geolocation控件的dom元素
          const geoControl = document.querySelector('.amap-geolocation');
          if (geoControl) {
            geoControl.addEventListener('click', this.handelgeolocationClick);
          }
          // 添加地图点击事件
          this.map.on('click', this.mapClick);
          this.lineList.forEach(line => {
            if (line.points && line.points.length) {
              let points = JSON.parse(line.points);
              let path = points.map(point => [point.longitude, point.latitude]);

              // Determine color and line style based on 'type'
              let innerLineColor;
              let dashArray;
              let lineWidth;

              if (line.type === "供水干管") {
                innerLineColor = "#0D98BA";
                dashArray = [10, 5];
                lineWidth = 8;
              } else if (line.type === "供水支管") {
                innerLineColor = "#80DAEB";
                dashArray = [5, 10];
                lineWidth = 6;
              }

              var outerPolyline = new this.curAMap.Polyline({
                path: path,
                strokeColor: "#FFFFFF",
                strokeWeight: 10,
                strokeOpacity: 1,
                strokeStyle: "solid",
                lineJoin: 'round',
                lineCap: 'round',
                zIndex: 50
              });

              var innerPolyline = new this.curAMap.Polyline({
                path: path,
                strokeColor: innerLineColor,
                strokeWeight: lineWidth,
                strokeOpacity: 1,
                strokeStyle: "solid",
                lineJoin: 'round',
                lineCap: 'round',
                strokeDasharray: dashArray,
                zIndex: 100
              });

              // Add the lines to the map
              outerPolyline.setMap(this.map);
              innerPolyline.setMap(this.map);
            }
          });
        })
        .catch((e) => {
          console.log(e);
        });
    },
    // 添加工程站点
    addProjectIcon(plant) {
      if (this.map) {
        const iconSize = new this.curAMap.Size(36, 36);
        let iconImage;
        let type; // 站点类型

        // 根据plant的名字动态设置图标图片和类型
        if (plant.name.includes('水库') && this.showReservoirs) {
          iconImage = require('../../icon/水库.png');
          type = 'reservoir'; // 水库类型
        } else if (plant.name.includes('泵站') && this.showPumpStations) {
          iconImage = require('../../icon/泵站.png');
          type = 'pumpStation'; // 泵站类型
        } else if (plant.name.includes('水厂') && this.showWaterWorks) {
          iconImage = require('../../icon/水厂.png');
          type = 'waterWork'; // 水厂类型
        } else if (plant.name.includes('浮船') && this.showFloatingBoats) {
          iconImage = require('../../icon/浮船.png');
          type = 'floatingBoat'; // 浮船类型
        } else {
          return;
        }

        const marker = new this.curAMap.Marker({
          position: [plant.longitude, plant.latitude],
          icon: new this.curAMap.Icon({
            image: iconImage,
            size: iconSize,
          }),
          offset: new this.curAMap.Pixel(-iconSize.width / 2, -iconSize.height / 2),
          clickable: true,
          extData: {
            type: type
          }, // 将类型添加到marker的属性中
        });
        // 设置触摸时显示的文本提示
        marker.setTitle(plant.name);
        marker.on('click', (e) => {
          // 此处可以添加其他点击事件处理代码
          this.project = plant.name;
          this.handleButtonClick();
        });
        marker.setMap(this.map);
        this.markers.push(marker);
      } else {
        console.error('Map instance is undefined');
      }
    },
    // 添加泵站点
    addPumpIcon(plant) {
      if (this.map) {
        const iconSize = new this.curAMap.Size(36, 36);
        let iconImage;
        let type; // 站点类型

        if (plant.name.includes('加压泵站') && this.showPumpStations){
          if (plant.status.includes('打开')) {
            iconImage = require('../../icon/泵打开.png');
            type = 'pumpStation'; // 泵站类型
          }
          else if (plant.status.includes('关闭')){
            iconImage = require('../../icon/泵关闭.png');
            type = 'pumpStation'; // 泵站类型
          }
          else {
            iconImage = require('../../icon/泵未知.png');
            type = 'pumpStation'; // 泵站类型
          }
        } else {
          return;
        }

        const marker = new this.curAMap.Marker({
          position: [plant.longitude, plant.latitude],
          icon: new this.curAMap.Icon({
            image: iconImage,
            size: iconSize,
          }),
          offset: new this.curAMap.Pixel(-iconSize.width / 2, -iconSize.height / 2),
          clickable: false,
          extData: {
            type: type
          }, // 将类型添加到marker的属性中
        });
        marker.setTitle(plant.name);
        // marker.on('click', (e) => {
        //   // 此处可以添加其他点击事件处理代码
        //   this.project = plant.name;
        //   this.handleButtonClick();
        // });
        marker.setMap(this.map);
        this.markers.push(marker);
      } else {
        console.error('Map instance is undefined');
      }
    },
    // 切换标记显示状态
    toggleMarkerVisible(marker, visible) {
      if (visible) {
        marker.setMap(this.map); // 将标记添加到地图上
      } else {
        marker.setMap(null); // 从地图上移除标记
      }
    },
    // 添加预警站点
    addPrewarnIcon(plant) {
      if (this.map) {
        const iconSize = new AMap.Size(48, 48); // 图标大小
        // 根据plant的名字动态设置图标图片
        let iconImage;
        // 根据plant的名字动态设置图标图片和类型
        if (plant.level.includes('一般预警')) {
          iconImage = require('../../icon/一般预警.png');
        } else if (plant.level.includes('严重预警')) {
          iconImage = require('../../icon/严重预警.png');
        } else {
          return; // 退出函数，不添加标记
        }
        const marker = new AMap.Marker({
          position: [plant.longitude, plant.latitude],
          icon: new AMap.Icon({
            image: iconImage,
            size: iconSize,
          }),
          offset: new AMap.Pixel(-iconSize.width / 2, -iconSize.height / 2),
          clickable: true, // 确保标记可点击
        });
        marker.setTitle(plant.name + plant.type + '预警');
        marker.on('click', (e) => {
          this.pwiName = plant.name;
          this.pwiStartTime = plant.startTime;
          this.handlePwiButtonClick();
        })
        marker.setMap(this.map);
      } else {
        console.error('Map instance is undefined');
      }
    },
    //添加村庄站点
    addTownIcon(plant) {
      if (this.map) {
        const iconSize = new AMap.Size(36, 36); // 图标大小
        // 根据plant的名字动态设置图标图片
        let iconImage;
        let type;
        if (this.showTowns) {
          iconImage = require('../../icon/村庄.png');
          type = 'town';
        }
        else {
          return;
        }
        const marker = new AMap.Marker({
          position: [plant.longitude, plant.latitude],
          icon: new AMap.Icon({
            image: iconImage,
            size: iconSize,
          }),
          offset: new AMap.Pixel(-iconSize.width / 2, -iconSize.height / 2),
          clickable: true, // 确保标记可点击
          extData: {
            type: type
          },
        });
        marker.setTitle(plant.name);
        marker.on('click', (e) => {
          this.project = plant.name;
          this.handleButtonClick();
        })
        marker.setMap(this.map);
        this.markers.push(marker);
      } else {
        console.error('Map instance is undefined');
      }
    },
    //添加流量站点
    addFlowIcon(plant) {
      if (this.map) {
        const iconSize = new AMap.Size(36, 36); // 图标大小
        // 根据plant的名字动态设置图标图片
        let iconImage = require('../../icon/流量站点.png');
        let type = 'flowItem';
        const marker = new AMap.Marker({
          position: [plant.longitude, plant.latitude],
          icon: new AMap.Icon({
            image: iconImage,
            size: iconSize,
          }),
          offset: new AMap.Pixel(-iconSize.width / 2, -iconSize.height / 2),
          clickable: true, // 确保标记可点击
          extData: {
            type: type
          },
        });
        marker.setTitle(plant.name);
        marker.on('click', (e) => {
          this.project = plant.name;
          this.code = plant.code;
          this.handleButtonClick();
        })
        marker.setMap(this.map);
        this.markers.push(marker);
      } else {
        console.error('Map instance is undefined');
      }
    },
    // 更新标记
    updateMapMarkers() {
      this.markers.forEach((marker) => {
        const type = marker.getExtData().type;
        switch (type) {
          case 'reservoir':
            this.toggleMarkerVisible(marker, this.showReservoirs);
            break;
          case 'pumpStation':
            this.toggleMarkerVisible(marker, this.showPumpStations);
            break;
          case 'waterWork':
            this.toggleMarkerVisible(marker, this.showWaterWorks);
            break;
          case 'floatingBoat':
            this.toggleMarkerVisible(marker, this.showFloatingBoats);
            break;
          case 'flowItem':
            this.toggleMarkerVisible(marker, this.showFlowItems);
            break;
          case 'town':
            this.toggleMarkerVisible(marker, this.showTowns);
            break;
          default:
            // 如果标记类型未知，则保持其当前的显示状态
            break;
        }
      });
    },

    // 地图散点点击事件
    onMarkerClick(e) {
      console.log(e)
    },
    // 地图点击事件，弹框出具体坐标信息
    mapClick(event) {
      // this.zuobiaoDialogVisible = true
      this.zuobiaoMes = [event.lnglat.lng, event.lnglat.lat]
    },
    // 监听pageSize
    handleSizeChange(newSize) {
      this.queryinfo.pageSize = newSize
      this.selectWarningInfo()
    },
    // 监听currentPage
    handleCurrentChange(newPage) {
      this.queryinfo.currentPage = newPage
      this.selectWarningInfo()
    },
    // 序号连续
    table_index(index) {
      return (
        (this.queryinfo.currentPage - 1) * this.queryinfo.pageSize + index + 1
      )
    },
    // 增加geolocation点击事件
    handelgeolocationClick() {
      this.map.setZoomAndCenter(15.5, [113.3694850, 31.6460040])
    },
    // 切换成当前预警信息为中心的地图
    goToLocation(longitude, latitude) {
      if (this.map) {
        this.map.setCenter([longitude, latitude]);
        this.map.setZoom(15.5); // 也可以设置一个合适的缩放级别
      }
    },
    // 切换成当前工程坐标为中心的地图
    locateProjectPosition() {
      // 确保this.longitude和this.latitude已经被定义
      if (this.longitude && this.latitude) {
        this.map.setZoomAndCenter(15, [this.longitude, this.latitude]);
      } else {
        console.error('Longitude or latitude is undefined');
      }
    },
    // 复选框事件处理
    handleCheckedChange(e) {
      console.log(this.checkListValue)
    },
    // 获取预警信息
    async selectWarningInfo() {
      if (this.datepickers === null) {
        this.datepickers = ['', '']
      }
      if (this.status == null) {
        this.status = ''
      }
      if (this.level == null) {
        this.level = ''
      }
      if (this.type == null) {
        this.type = ''
      }
      if (this.position == null) {
        this.position = ''
      }
      const { data: res } = await this.$http.get(
        '/warning-information/listNoPage',
        {
          params: {
            status: this.status,
            level: this.level,
            type: this.type,
            position: this.position,
            startTime: this.datepickers[0],
            endTime: this.datepickers[1],
          }
        }
      )
      if (res.code !== 200) {
        return this.$message.error('搜索数据错误')
      }
      this.PWIList = res.data
      this.total = res.data.total
    },
    // 查看预警类型
    async selectTypes() {
      const { data: res } = await this.$http.get('/warning-information/getalltypes', {

      })
      if (res.code !== 200) {
        return this.$message.error('获取数据列表失败')
      }
      this.types = res.data
      this.typeList = this.types.map(name => ({
        value: name,
        label: name
      }))
    },
    // 查看预警等级
    async selectLevels() {
      const { data: res } = await this.$http.get('/dict/kinds', {
        params: {
          name: '预警等级'
        }
      })
      if (res.code !== 200) {
        return this.$message.error('获取数据列表失败')
      }
      this.levels = res.data
      this.levelList = this.levels.map(level => ({
        value: level,
        label: level
      }))
    },
    // 查看预警地点
    async selectPositions() {
      const { data: res } = await this.$http.get('/dict/kinds', {
        params: {
          name: '预警地点'
        }
      })
      if (res.code !== 200) {
        return this.$message.error('获取数据列表失败')
      }
      this.positions = res.data
      this.positionList = this.positions.map(position => ({
        value: position,
        label: position
      }))
    },
    // 获取不同预警类型数量
    async getWarningCounts() {
      const { data: res } = await this.$http.get('/warning-information/getlevels', {
        params: {
          status: this.status,
          startTime: this.datepickers[0],
          endTime: this.datepickers[1],
        }
      })
      if (res.code !== 200) {
        return this.$message.error('获取数据失败')
      }
      this.warningCounts = res.data;
    },
    // 初始化日期
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
      // 获取不同预警类型数量
      this.getWarningCounts()
    },
    // 处理搜索按钮
    async handleButtonClick() {
      await this.getProjectType()
      if (this.project === '') {
        this.dialogVisible = false;
      }
      else {
        this.dialogVisible = true;
      }
    },
    async handlePwiButtonClick() {
      await this.searchWarningInfo()
      this.dialogPwiVisible = true;
    },
    // 获取管道信息
    async getLineInfo() {
      const { data: res } = await this.$http.get(
        '/line/deal-with-list'
      );
      if (res.code === 200) {
        this.lineList = res.data;
      } else {
        this.$message.error('未能获取到管道信息');
      }
    },
    // 判断工程类型
    async getProjectType() {
      if (this.project.includes('水库')) {
        await this.searchRS();
        await this.selectWaterLevelLast();
      }
      else if (this.project.includes('泵站')) {
        await this.searchPS();
      }
      else if (this.project.includes('水厂')) {
        await this.searchWW();
        await this.selectWaterQualityLast();
      }
      else if (this.project.includes('浮船')) {
        await this.searchFB();
      }
      else if (this.project.includes('流量')) {
        await this.searchFlowItem();
        // 只有流量站要查询流量
        await this.selectFlowLast();
      }
      else {
        await this.searchTown();
      }
    },
    // 获取指定水库信息
    async searchRS() {
      const { data: res } = await this.$http.get(
        '/reservoir/name',
        {
          params: {
            name: this.project,
            currentPage: this.queryinfo.currentPage,
            pageSize: this.queryinfo.pageSize
          }
        }
      );
      if (res.code === 200 && res.data.records.length > 0) {
        const firstReservoir = res.data.records[0];
        this.RSList = res.data.records;
        this.projectName = firstReservoir.name + '工程档案';
        this.longitude = firstReservoir.longitude;
        this.latitude = firstReservoir.latitude;
        this.baseinfoRS = true;
        ['baseinfoPS', 'baseinfoWD', 'baseinfoWW', 'baseinfoFB', 'baseinfoTown', 'baseinfoFlow'].forEach(prop => {
          this[prop] = false;
        });
      } else {
        this.$message.error('输入的水库不存在或搜索数据错误');
      }
    },
    // 获取指定泵站信息
    async searchPS() {
      const { data: res } = await this.$http.get(
        '/pump-station/name',
        {
          params: {
            name: this.project,
            currentPage: this.queryinfo.currentPage,
            pageSize: this.queryinfo.pageSize
          }
        }
      )
      if (res.code === 200 && res.data.records.length > 0) {
        const firstPumpStation = res.data.records[0];
        this.PSList = res.data.records;
        this.projectName = firstPumpStation.name + '工程档案';
        this.longitude = firstPumpStation.longitude;
        this.latitude = firstPumpStation.latitude;
        // 将baseinfoPS设置为true，其他属性设置为false
        this.baseinfoPS = true;
        // 使用对象语法同时设置其他属性为false
        ['baseinfoRS', 'baseinfoWD', 'baseinfoWW', 'baseinfoFB', 'baseinfoTown', 'baseinfoFlow'].forEach(prop => {
          this[prop] = false;
        });
      } else {
        this.$message.error('输入的泵站不存在或搜索数据错误');
      }
    },
    // 获取指定水厂信息
    async searchWW() {
      const { data: res } = await this.$http.get(
        '/waterworks/name',
        {
          params: {
            name: this.project,
            currentPage: this.queryinfo.currentPage,
            pageSize: this.queryinfo.pageSize
          }
        }
      )
      if (res.code === 200 && res.data.records.length > 0) {
        this.WWList = res.data.records;
        const firstWaterWork = res.data.records[0];
        this.projectName = firstWaterWork.name + '工程档案';
        this.longitude = firstWaterWork.longitude;
        this.latitude = firstWaterWork.latitude;
        // 将baseinfoPS设置为true，其他属性设置为false
        this.baseinfoWW = true;
        // 使用对象语法同时设置其他属性为false
        ['baseinfoRS', 'baseinfoPS', 'baseinfoWD', 'baseinfoFB', 'baseinfoTown', 'baseinfoFlow'].forEach(prop => {
          this[prop] = false;
        });
      } else {
        this.$message.error('输入的水厂不存在或搜索数据错误');
      }
    },
    // 获取指定浮船信息
    async searchFB() {
      const { data: res } = await this.$http.get('/floatingboat/name', {
        params: {
          name: this.project,
          currentPage: this.queryinfo.currentPage,
          pageSize: this.queryinfo.pageSize
        }
      })
      if (res.code === 200 && res.data.records.length > 0) {
        this.FBList = res.data.records;
        const firstFloatingBoat = res.data.records[0];
        this.projectName = firstFloatingBoat.name + '工程档案';
        this.longitude = firstFloatingBoat.longitude;
        this.latitude = firstFloatingBoat.latitude;
        // 将baseinfoPS设置为true，其他属性设置为false
        this.baseinfoFB = true;
        // 使用对象语法同时设置其他属性为false
        ['baseinfoRS', 'baseinfoPS', 'baseinfoWD', 'baseinfoWW', 'baseinfoTown', 'baseinfoFlow'].forEach(prop => {
          this[prop] = false;
        });
      } else {
        this.$message.error('输入的浮船不存在或搜索数据错误');
      }
    },
    // 获取指定村庄信息
    async searchTown() {
      const { data: res } = await this.$http.get(
        '/town/list',
        {
          params: {
            name: this.project,
            currentPage: this.queryinfo.currentPage,
            pageSize: this.queryinfo.pageSize
          }
        }
      );
      if (res.code === 200 && res.data.records.length > 0) {
        const town = res.data.records[0];
        this.townList = res.data.records;
        this.projectName = town.name;
        this.longitude = town.longitude;
        this.latitude = town.latitude;
        this.baseinfoTown = true;
        ['baseinfoPS', 'baseinfoWD', 'baseinfoWW', 'baseinfoFB', 'baseinfoRS', 'baseinfoFlow'].forEach(prop => {
          this[prop] = false;
        });
      } else {
        this.$message.error('输入的村庄不存在或搜索数据错误');
      }
    },
    // 获取流量站点信息
    async searchFlowItem() {
      const { data: res } = await this.$http.get(
        '/measuring-station/list',
        {
          params: {
            name: this.project,
            currentPage: this.queryinfo.currentPage,
            pageSize: this.queryinfo.pageSize
          }
        }
      );
      if (res.code === 200 && res.data.records.length > 0) {
        const flowItem = res.data.records[0];
        this.flowItemList = res.data.records;
        this.projectName = flowItem.name + '工程档案';
        this.longitude = flowItem.longitude;
        this.latitude = flowItem.latitude;
        this.baseinfoFlow = true;
        ['baseinfoPS', 'baseinfoWD', 'baseinfoWW', 'baseinfoFB', 'baseinfoRS', 'baseinfoTown'].forEach(prop => {
          this[prop] = false;
        });
      } else {
        this.$message.error('输入的流量站点不存在或搜索数据错误');
      }
    },
    // 获取指定预警信息
    async searchWarningInfo() {
      const { data: res } = await this.$http.get('/warning-information/getByPositionTime', {
        params: {
          position: this.pwiName,
          startTime: this.pwiStartTime
        }
      })
      if (res.code !== 200) {
        return this.$message.error('获取数据列表失败')
      }
      this.pwiOne = res.data
    },
    // 获取所有水库信息
    async getReservoirList() {
      const { data: res } = await this.$http.get('/reservoir/list', {
        params: this.queryinfo
      })
      if (res.code !== 200) {
        return this.$message.error('获取数据列表失败')
      }
      this.reservoirList = res.data.records
    },
    // 获取所有泵站信息
    async getPumpStationList() {
      const { data: res } = await this.$http.get('/pump-station/list', {
        params: this.queryinfo
      })
      if (res.code !== 200) {
        return this.$message.error('获取数据列表失败')
      }
      this.pumpStationList = res.data.records
    },
    // 获取所有水厂信息
    async getWaterWorkList() {
      const { data: res } = await this.$http.get('/waterworks/list', {
        params: this.queryinfo
      })
      if (res.code !== 200) {
        return this.$message.error('获取数据列表失败')
      }
      this.waterWorkList = res.data.records
    },
    // 获取所有浮船信息
    async getFloatingBoatList() {
      const { data: res } = await this.$http.get('/floatingboat/list', {
        params: this.queryinfo
      })
      if (res.code !== 200) {
        return this.$message.error('获取数据列表失败')
      }
      this.floatingBoatList = res.data.records
    },
    // 获取所有流量监测站信息
    async getFlowItemList() {
      const { data: res } = await this.$http.get('/measuring-station/list', {
        params: {
          name: '流量',
          currentPage: this.queryinfo.currentPage,
          pageSize: this.queryinfo.pageSize
        }
      })
      if (res.code !== 200) {
        return this.$message.error('获取流量监测站失败')
      }
      this.flowItemList = res.data.records
    },
    // 获取乡镇信息
    async getTownList() {
      const { data: res } = await this.$http.get('/town/export-excel')

      if (res.code !== 200) {
        return this.$message.error('获取数据列表失败')
      }
      this.townList = res.data
    },
    // 获取最新的水位信息 
    async selectWaterLevelLast() {
      const { data: res } = await this.$http.get(
        '/water-level/last',
        {
          params: {
            position: this.project,
          }
        }
      )
      if (res.code !== 200) {
        return this.$message.error('搜索水位数据错误')
      }
      this.WLList = res.data
    },
    // 获取最新的流量信息, 获取流量是根据站点编码去获取
    async selectFlowLast() {
      const { data: res } = await this.$http.get(
        '/flow/last',
        {
          params: {
            position: this.project,
          }
        }
      )
      if (res.code !== 200) {
        return this.$message.error('搜索流量数据错误')
      }
      this.FLList = res.data
    },
    // 获取最新的水质信息
    async selectWaterQualityLast() {
      const { data: res } = await this.$http.get(
        '/water-quality/last',
        {
          params: {
            position: this.project,
          }
        }
      )
      if (res.code !== 200) {
        return this.$message.error('搜索水质数据错误')
      }
      this.WQList = res.data
    },
    // 获取所有泵信息
    async getPumpList() {
      const { data: res } = await this.$http.get('/pump/export-excel', {
        
      })
      if (res.code !== 200) {
        return this.$message.error('获取数据列表失败')
      }
      this.pumpList = res.data;
    },
    // 获取所有站点的经纬度
    async getLongLati() {
      if (this.datepickers[0] === '') {
        this.initDate();
      }
      await this.getReservoirList();
      this.reservoirList.forEach(reservoir => {
        this.projectNameList.push(reservoir.name);
        this.longitudeList.push(reservoir.longitude);
        this.latitudeList.push(reservoir.latitude);
      });
      await this.getPumpStationList();
      this.pumpStationList.forEach(pumpStation => {
        this.projectNameList.push(pumpStation.name);
        this.longitudeList.push(pumpStation.longitude);
        this.latitudeList.push(pumpStation.latitude);
      });
      await this.getWaterWorkList();
      this.waterWorkList.forEach(waterWorkList => {
        this.projectNameList.push(waterWorkList.name);
        this.longitudeList.push(waterWorkList.longitude);
        this.latitudeList.push(waterWorkList.latitude);
      });
      await this.getFloatingBoatList();
      this.floatingBoatList.forEach(floatingBoatList => {
        this.projectNameList.push(floatingBoatList.name);
        this.longitudeList.push(floatingBoatList.longitude);
        this.latitudeList.push(floatingBoatList.latitude);
      });
      await this.getTownList();
      this.townList.forEach(townList => {
        this.townNameList.push(townList.name);
        this.townLongitudeList.push(townList.longitude);
        this.townLatitudeList.push(townList.latitude);
      });
      await this.getFlowItemList();
      this.flowItemList.forEach(flowItemList => {
        this.flowItemNameList.push(flowItemList.name);
        this.flowItemCodeList.push(flowItemList.code);
        this.flowItemLongitudeList.push(flowItemList.longitude);
        this.flowItemLatitudeList.push(flowItemList.latitude);
      });
      await this.getPumpList();
      this.pumpList.forEach(pump => {
        this.pumpNameList.push(pump.name);
        this.pumpLongitudeList.push(pump.longitude);
        this.pumpLatitudeList.push(pump.latitude);
        this.pumpStatusList.push(pump.status);
      });
      await this.getPwiInfo();
    },
    // 预警信息
    async getPwiInfo() {
      this.getWarningCounts()
      this.PWIList = [];
      this.pwiNameList = [];
      this.pwiTypeList = [];
      this.pwiLevelList = [];
      this.pwiLongitudeList = [];
      this.pwiLatitudeList = [];
      this.pwiMonitorTimeList = [];
      await this.selectWarningInfo();
      this.PWIList.forEach(PWIList => {
        this.pwiNameList.push(PWIList.position);
        this.pwiTypeList.push(PWIList.type);
        this.pwiLevelList.push(PWIList.level);
        this.pwiLongitudeList.push(PWIList.longitude);
        this.pwiLatitudeList.push(PWIList.latitude);
        this.pwiMonitorTimeList.push(PWIList.startTime);
      });
      // 打开初始化地图
      this.initAMap();
    },
    //关闭对话框
    handleClose(done) {
      this.$confirm('确认关闭？')
        .then(_ => {
          done();
        })
        .catch(_ => { });
    },
    async getAllProjectsList() {
      const { data: res } = await this.$http.get(
        '/dict/kinds',
        {
          params: {
            name: '工程站点'
          }
        }
      )
      if (res.code !== 200) {
        return this.$message.error('搜索水质数据错误')
      }
      this.projectNameListSelect = res.data
      this.projectsList = this.projectNameListSelect.map(name => ({
        value: name,
        label: name
      }))
    }
  },
  // refreshPage() {
  //   console.log("我已经自动更新了");
  //   this.getAllProjectsList()
  //   // 获取管道数据
  //   this.getLineInfo();
  //   // 获取工程的经纬度
  //   this.getLongLati();
  //   // 获取监测对象
  //   this.selectTypes();
  //   // 获取预警等级
  //   this.selectLevels();
  //   // 获取监测地点
  //   this.selectPositions();
  // },
  computed() {

  },
  async mounted() {
    this.getAllProjectsList();
    // 获取管道数据
    this.getLineInfo();
    // 获取工程的经纬度
    await this.getLongLati();
    // 获取监测对象
    this.selectTypes();
    // 获取预警等级
    this.selectLevels();
    // 获取监测地点
    this.selectPositions();
  }
}
</script>

<style lang="less" scoped>
* {
  box-sizing: border-box;
}

#div1 {
  width: 100%;
  height: 100%;

}

.map {
  width: 100%;
  height: 100%;

}

#div-header1 {
  height: 50px;
  width: 100%;
  margin-top: 0px;
  display: flex;
}

.item {
  display: inline-block;
  background-color: #F2FAFF;
  width: 80px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  border-radius: 4px;
}

.value {
  border-radius: 50%;
  width: 24px;
  height: 24px;
  line-height: 24px;
  display: inline-block;
  text-align: center;
  color: #fff;
}

.warning-button {
  position: absolute;
  display: flex;
  margin: auto;
  left: 47%;
  top: 9%;
  padding: 20px;
  border-radius: 10px;
  background-color: rgb(255, 255, 255);
  opacity: 0.9;
  color: #ff4949;
}

.project-button {
  position: absolute;
  display: flex;
  margin: auto;
  left: 40%;
  top: 9%;
  padding: 20px;
  border-radius: 10px;
  background-color: rgb(255, 255, 255);
  opacity: 0.9;
  color: #316Af6;
}

.project-select {
  position: absolute;
  display: flex;
  margin: auto;
  left: 5%;
  top: 9%;
  border-radius: 20px;
  background-color: rgb(255, 255, 255);
  opacity: 0.9;
}

.legend {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  left: 2%;
  top: 15%;
  border-radius: 20px;
  background-color: rgb(255, 255, 255);
  opacity: 0.9;
  padding: 20px;
  width: 300px;
  font-family: Arial, sans-serif;
}

/* 图例标题样式 */
.legend-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
}

/* 图例项容器样式，确保每行放两个图标加文字的组合 */
.legend-row {
  display: flex;
  width: 100%;
  /* 调整为100%以占满容器宽度 */
  justify-content: space-between;
  /* 确保每行两端对齐 */
  margin-bottom: 10px;
}

/* 图标和文本的组合样式 */
.legend-item {
  display: flex;
  flex-basis: 50%;
  /* 每个项目占据行的一半宽度 */
  justify-content: flex-start;
  /* 图标和文本在项目内起始位置对齐 */
}

/* 图标样式 */
.legend-icon {
  width: 30px;
  height: 30px;
  margin-right: 10px;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  align-self: flex-start;
  /* 确保图标在交叉轴上对齐 */
}

/* 文本样式 */
.legend-text {
  font-size: 16px;
  align-self: center;
  /* 确保文本在交叉轴上对齐 */
}

/* 为最后一个图例项移除右边距 */
.legend-row:last-child .legend-item:last-child {
  margin-right: 0;
}

/* 为每种工程站点和预警站点添加特定的背景图片 */
.icon-reservoir {
  background-image: url('../../icon/水库.png');
}

.icon-pump-station {
  background-image: url('../../icon/泵站.png');
}

.icon-pump-on {
  background-image: url('../../icon/泵打开.png');
}

.icon-pump-off {
  background-image: url('../../icon/泵关闭.png');
}

.icon-water-plant {
  background-image: url('../../icon/水厂.png');
}

.icon-impoundment {
  background-image: url('../../icon/浮船.png');
}

.icon-flow-item {
  background-image: url('../../icon/流量站点.png');
}

.icon-town {
  background-image: url('../../icon/村庄.png');
}

.icon-general-warning {
  background-image: url('../../icon/一般预警.png');
}

.icon-serious-warning {
  background-image: url('../../icon/严重预警.png');
}

.icon-main-pipe {
  background-image: url('../../icon/供水干管.png');
}

.icon-branch-pipe {
  background-image: url('../../icon/供水支管.png');
}

#div-checkbox {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 10px;
  background-color: #ffffff;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

#div-checkbox label {
  display: flex;
  /* 使用Flexbox布局 */
  align-items: center;
  /* 垂直居中所有子元素 */
  font-size: 14px;
  margin-top: 3px;
  color: #316Af6;
  margin-bottom: 10px;
}

.checkbox-icon {
  width: 18px;
  height: 18px;
  margin-right: 5px;
  margin-left: 5px;
  vertical-align: middle;
}

/* 为复选框设置基本样式 */
#div-checkbox input[type="checkbox"] {
  appearance: none;
  -webkit-appearance: none;
  border: 2px solid #316Af6;
  width: 18px;
  height: 18px;
  vertical-align: middle;
  margin-right: 5px;
  transition: background-color 0.3s;
}

#div-checkbox input[type="checkbox"]:checked {
  background-color: #316Af6;
}

.model {
  position: absolute;
  background: #fff;
  border-radius: 4px;
  width: 1200px;
  height: 730px;
}

.top-head {
  width: 100%;
  height: 40px;
  position: relative;
  display: flex;
  justify-content: flex-start;
  align-items: center;
  border-bottom: 1px solid #ccc;
  background: #f5f7fa;
  padding: 0 10px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 500;
  text-align: left;
  color: #7a8399;
}

.top-title {
  width: 100%;
  height: 40px;
  line-height: 40px;
  margin-bottom: 10px;
  margin-top: 20px;
}

.small-title {
  width: 100%;
  height: 20px;
  line-height: 20px;
  margin-bottom: 5px;
  margin-top: 10px;
  padding-left: 10px;
  box-sizing: border-box;
}

.table-content {
  width: 100%;
  padding: 16px;
  display: flex;
  justify-content: flex-start;
  flex-wrap: wrap;
}

.msg-tip-content {
  width: 33.3%;
  line-height: 30px;
  padding: 0 10px;
  font-size: 14px;
}
</style>