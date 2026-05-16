<template>
  <div class="home-container">
    <div class="home-header">
      <!-- 导航栏 -->
      <el-menu :default-active="activePath" class="el-menu-demo" mode="horizontal" background-color="#545c64"
        text-color="#fff" active-text-color="#ffd04b" style="display:flex;height:60px;width:100%" router>
        <!-- 平台图标以及名称区域 -->
        <div style="display:flex;align-items:center;">
          <img src="../assets/img/sea2.jpg" style="width:50px;height:50px;padding:5px;padding-left:20px;" />
          <span style="margin-left:10px;display:flex;align-items:center;">鄂北水资源信息化平台</span>
        </div>

        <template v-if="windowwid" v-for="item in menulist">
          <!-- 无子菜单 -->
          <el-menu-item v-if="item.children.length === 0" :index="item.path" :key="item.id"
            @click="saveNavState(item.path)" style="margin-left:30px;">
            <span slot="title">{{ removeReadOnly(item.name) }}</span>
          </el-menu-item>
          <!-- 有子菜单 -->
          <el-submenu v-if="item.children.length !== 0" :index="item.id + ''" :key="item.id">
            <template slot="title">
              <span>{{ removeReadOnly(item.name) }}</span>
            </template>
            <el-menu-item v-for="subItem in item.children" :index="subItem.path" :key="subItem.id"
              @click="saveNavState(subItem.path)">
              <span>{{ removeReadOnly(subItem.name) }}</span>
            </el-menu-item>
          </el-submenu>
        </template>

        <el-submenu style="margin-left:auto;margin-right:10px;" index="">
          <template slot="title">{{ UserList.username }}</template>
          <el-menu-item @click="usersDialogVisible = true">个人信息</el-menu-item>
          <el-menu-item @click="changePasswordDialogVisible = true">修改密码</el-menu-item>
          <el-menu-item @click="logout">退出</el-menu-item>
        </el-submenu>
      </el-menu>
    </div>

    <div class="home-main-box">
      <router-view></router-view>
    </div>

    <!-- 个人信息对话框 -->
    <el-dialog :visible.sync="usersDialogVisible" width="900px" center>
      <!-- 标题样式 -->
      <template slot="title">
        <div class="userTitle">个人信息</div>
      </template>
      <!-- 内容区域 -->
      <el-descriptions class="margin-top" :column="2" border>
        <el-descriptions-item label-style="width:20%;" content-style="width:30%;">
          <template slot="label">
            <i class="el-icon-user"></i>
            用户名
          </template>
          <span>{{ UserList.username }}</span>
        </el-descriptions-item>
        <el-descriptions-item label-style="width:20%;" content-style="width:30%;">
          <template slot="label">
            <i class="el-icon-mobile-phone"></i>
            专业
          </template>
          <span>{{ UserList.major }}</span>
        </el-descriptions-item>
        <el-descriptions-item label-style="width:20%;" content-style="width:30%;">
          <template slot="label">
            <i class="el-icon-location-outline"></i>
            姓名
          </template>
          <span>{{ UserList.name }}</span>
        </el-descriptions-item>
        <el-descriptions-item label-style="width:20%;" content-style="width:30%;">
          <template slot="label">
            <i class="el-icon-tickets"></i>
            电子邮件
          </template>
          <span>{{ UserList.email }}</span>
        </el-descriptions-item>
        <el-descriptions-item label-style="width:20%;" content-style="width:30%;">
          <template slot="label">
            <i class="el-icon-office-building"></i>
            身份证号
          </template>
          <span>
            {{ UserList.idNumber }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label-style="width:20%;" content-style="width:30%;">
          <template slot="label">
            <i class="el-icon-location-outline"></i>
            家庭住址
          </template>
          <span>
            {{ UserList.address }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label-style="width:20%;" content-style="width:30%;">
          <template slot="label">
            <i class="el-icon-office-building"></i>
            性别
          </template>
          <span>
            {{ UserList.gender }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label-style="width:20%;" content-style="width:30%;">
          <template slot="label">
            <i class="el-icon-office-building"></i>
            开始工作时间
          </template>
          <span>
            {{ UserList.workingTime }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label-style="width:20%;" content-style="width:30%;">
          <template slot="label">
            <i class="el-icon-office-building"></i>
            技术职称
          </template>
          <span>
            {{ UserList.technicalTitle }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label-style="width:20%;" content-style="width:30%;">
          <template slot="label">
            <i class="el-icon-office-building"></i>
            出生年月
          </template>
          <span>
            {{ UserList.birthday }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label-style="width:20%;" content-style="width:30%;">
          <template slot="label">
            <i class="el-icon-office-building"></i>
            学历
          </template>
          <span>
            {{ UserList.academicQualifications }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label-style="width:20%;" content-style="width:30%;">
          <template slot="label">
            <i class="el-icon-office-building"></i>
            政治面貌
          </template>
          <span>
            {{ UserList.politicalAppearance }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label-style="width:20%;" content-style="width:30%;">
          <template slot="label">
            <i class="el-icon-office-building"></i>
            毕业院校
          </template>
          <span>
            {{ UserList.graduationInstitution }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label-style="width:20%;" content-style="width:30%;">
          <template slot="label">
            <i class="el-icon-tickets"></i>
            备注
          </template>
          <span style="width:100px">
            {{ UserList.note }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label-style="width:20%;" content-style="width:80%;">
          <template slot="label">
            <i class="el-icon-mobile-phone"></i>
            手机号码
          </template>
          <span>
            {{ UserList.phoneNumber }}
          </span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog title="修改密码" :visible.sync="changePasswordDialogVisible" width="400px"
      @close="changePasswordDialogClosed" center :close-on-press-escape="false">
      <el-form :model="passwordForm" :rules="passwordFormRules" ref="passwordFormRef" label-width="120px" size="small">
        <el-form-item label="原密码：" prop="currentPassword">
          <el-input v-model="passwordForm.currentPassword" style="width: 200px;" type="password"></el-input>
        </el-form-item>
        <el-form-item label="新密码：" prop="password">
          <el-input v-model="passwordForm.password" type="password" style="width: 200px;"></el-input>
        </el-form-item>
        <el-form-item label="确认新密码：" prop="newpassword">
          <el-input v-model="passwordForm.newpassword" type="password" style="width: 200px;"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="changePassword">确 定</el-button>
        <el-button @click="changePasswordDialogVisible = false">取 消</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'Home',
  data() {
    return {
      //窗口大小判断
      windowwid: '',
      //导航栏数据
      menulist: [],
      dialogFormVisible: false,
      //被激活的链接地址
      activePath: '',
      //控制个人信息对话框
      usersDialogVisible: false,
      //修改密码对话框
      changePasswordDialogVisible: false,
      //用户信息
      UserList: [],
      //修改密码数据
      passwordForm: {
        password: '',
        currentPassword: '',
        newpassword: '',
      },
      passwordFormRules: {
        password: [
          { required: true, message: '请输入新密码', trigger: 'blur' }
        ],
        currentPassword: [
          { required: true, message: '请输入原密码', trigger: 'blur' }
        ],
        newpassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { pattern: /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/, message: '密码必须包含大写字母、小写字母、数字和特殊符号，且至少8位', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    removeReadOnly(name) {
      // 首先替换掉 "只读用户"
      let cleanedName = name.replace(/只读用户/g, '');
      // 然后替换掉 "数据人员"
      cleanedName = cleanedName.replace(/数据人员/g, '');
      return cleanedName;
    },
    //获取所有菜单
    async getMenuList() {
      const { data: res } = await this.$http.get('/menu/nav')
      if (res.code != 200) return this.$message.error(res.message)
      this.menulist = res.data.nav
    },
    async getPersonInformation() {
      const { data: res } = await this.$http.get('/user/userInfo')
      if (res.code != 200) return this.$message.error('获取个人信息错误')
      this.UserList = res.data
    },
    //监听修改密码对话框关闭重置事件
    changePasswordDialogClosed() {
      this.$refs.passwordFormRef.resetFields()
    },
    //保存链接的激活状态
    saveNavState(activePath) {
      window.sessionStorage.setItem('activePath', activePath)
      this.activePath = activePath
    },
    //修改密码
    changePassword() {
      if (this.passwordForm.password != this.passwordForm.newpassword) {
        this.$message.error('两次密码输入不一致')
        return
      }
      this.$refs.passwordFormRef.validate(async valid => {
        if (!valid) return
        const { data: res } = await this.$http.post(
          '/user/updatePass',
          this.passwordForm
        )
        console.log(this.passwordForm)
        if (res.code == 200) {
          this.$message.success('修改密码成功')
          sessionStorage.removeItem('token')
          sessionStorage.removeItem('activePath')
          this.$router.push('/login')
          this.changePasswordDialogVisible = false
        }
        else {
          this.$message.error('原密码不对')
        }
      })
    },
    //退出
    logout() {
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('activePath')
      this.$router.push('/login')
    },
    //计算窗口宽度
    checkWindowWidth() {
      this.windowwid = window.innerWidth > 1350;
    }
  },
  created() {
    this.getMenuList();
    (this.activePath = window.sessionStorage.getItem('activePath'))
    this.getPersonInformation()
  },
  mounted() {
    //打开页面计算窗口宽度
    this.checkWindowWidth()
    //随时监听窗口大小变化
    window.addEventListener('resize', this.checkWindowWidth);
  },
  components: {}
}
</script>

<style lang="less" scoped>
.home-container {
  width: 100%;
  height: 100%;

}

.home-header {
  width: 100%;
  height: 60px;
  display: flex;
  color: #fff;
  font-size: 25px;
}

.home-main-box {
  width: 100%;
  height: calc(100% - 60px);
}

.router-link-active {
  text-decoration: none;
  color: white;
}

a {
  text-decoration: none;
  color: white;
}

.userTitle {
  font-size: 18px;
}

::v-deep .el-dialog__header {
  background-color: #304156;
  text-align: left;
}

.Item el-form-item__label {
  font-size: 18px !important;
}

.el-dialog {
  border-radius: 20px;
}
</style>
