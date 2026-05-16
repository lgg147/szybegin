<template>
    <div id="div1">
        <div id="bread">
            <el-breadcrumb separator-class="el-icon-arrow-right" style="padding-top:5px; padding-left:10px">
                <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
                <el-breadcrumb-item>系统管理</el-breadcrumb-item>
                <el-breadcrumb-item :to="{ path: '/home/shouyeweihu' }">首页维护</el-breadcrumb-item>
            </el-breadcrumb>
        </div>

        <div id="main">
            <div id="main-body">
                <!-- 上部分：日期选择器、监测点选择框、查询按钮 -->
                <div id="search-section">
                    <div id="upload-button-container">
                        <!-- 隐藏文件输入框 -->
                        <input type="file" @change="handleVideoUpload" ref="fileInput" style="display: none">
                        <!-- 上传视频按钮 -->
                        <el-button id="upload-button" size="medium" type="primary"
                            @click="triggerUpload">更新首页视频</el-button>
                        <!-- 更新文字按钮 -->
                        <el-button id="upload-button2" size="medium" type="primary"
                            @click="showEditDialog(1)">更新首页文字</el-button>
                    </div>
                </div>
                <!-- 文本编辑对话框 -->
                <el-dialog title="编辑文字" :visible.sync="dialogVisible" width="50%" @close="editDialogClosed" 
                :close-on-press-escape="false" :close-on-click-modal="false">
                    <el-form :model="editForm">
                        <el-form-item prop="text">
                            <el-input type="textarea" v-model="editForm.text" :rows="13"
                                @input="inputEvent"></el-input>
                            <div class="char-count">{{ characterCount }}/450</div>
                        </el-form-item>
                    </el-form>
                    <span slot="footer" class="dialog-footer">
                        <el-button @click="dialogVisible = false">取消</el-button>
                        <el-button type="primary" @click="updateText">更新</el-button>
                    </span>
                </el-dialog>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: '',
    data() {
        return {
            projectInfo: [],
            dialogVisible: false,
            editForm: { text: '' },
            characterCount: 0, // 当前输入的字数
            maxCharacters: 450 // 最大允许的字数
        }
    },
    methods: {
        inputEvent() {
            this.updateCharacterCount();
            if (this.characterCount > this.maxCharacters) {
                this.editForm.text = this.editForm.text.substring(0, this.maxCharacters);
            }
        },
        async getProjectInfo() {
            const { data: res } = await this.$http.get(
                '/icon/getByName'
            )
            if (res.code !== 200) {
                return this.$message.error('查询数据信息失败')
            }
            this.projectInfo = res.data;
            this.editForm.text = this.projectInfo.text; // 初始化编辑框内容
            this.updateCharacterCount(); // 更新字数计数
        },
        triggerUpload() {
            this.$refs.fileInput.click(); // 触发文件输入
        },
        handleVideoUpload(event) {
            const file = event.target.files[0];
            if (file) {
                this.uploadVideo(file);
            }
        },
        uploadVideo(file) {
            const formData = new FormData();
            formData.append('video', file);

            // 使用 this.$http 发送 POST 请求
            this.$http.post('/icon/upload/video', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            })
                .then(response => {
                    // 假设后端返回的新视频URL在response的data中
                    const newVideoUrl = response.data.data; // 确保后端返回的数据结构与此匹配
                    this.$message.success('视频更新成功！');
                })
                .catch(error => {
                    console.error('上传视频出错:', error);
                    // 给用户一些反馈，例如弹出一个消息框
                    this.$message.error('视频上传失败，请重试！');
                });
        },
        //获取编辑框
        async showEditDialog(id) {
            const { data: res } = await this.$http.get(
                '/icon/info/' + id
            )
            if (res.code !== 200) {
                return this.$message.error('查询数据信息失败')
            }
            this.editForm = res.data;
            this.dialogVisible = true;
        },
        //监听编辑对话框关闭重置事件
        editDialogClosed() {
            this.$refs.editFormRef.resetFields();
            this.editForm.text = '';
            this.characterCount = this.editForm.text.length;
        },
        updateCharacterCount() {
            this.characterCount = this.editForm.text.length; // 更新当前字数
        },
        updateText() {
            if (this.characterCount > this.maxCharacters) {
                this.$message.error('文字超过最大限制450字');
                return;
            }
            // 发送更新请求
            this.$http.post('/icon/update', this.editForm)
                .then(response => {
                    if (response.data.code === 200) {
                        this.$message.success('文字更新成功');
                        this.projectInfo = response.data; // 更新显示的文本
                        this.getProjectInfo();
                        this.dialogVisible = false;
                    } else {
                        this.$message.error('文字更新失败');
                    }
                })
                .catch(error => {
                    console.error('更新文字出错:', error);
                    this.$message.error('文字更新失败，请重试！');
                });
        },
    },
    created() {

    },
    mounted() { 
        this.getProjectInfo();
        this.characterCount = this.editForm.text.length
    }
}
</script>

<style lang="less" scoped>
#div1 {
    height: 100%;
    width: 100%;
}

#bread {
    height: 3%;
    width: 100%;
}

#main {
    height: 94.5%;
    width: 99%;
    display: flex;
    margin: 10px;
    border: 1px solid rgb(212, 212, 212);
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.04);
    background-color: rgb(255, 255, 255);
}


#main-body {
    display: flex;
    flex-direction: column;
    height: 100%;
    padding: 20px;
}

#search-section {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
}
</style>