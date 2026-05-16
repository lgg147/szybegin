<template>
    <div class="wrapper">
        <!-- 左侧 -->
        <div class="left">
            <!-- 控制栏 -->
            <div class="toolbar">
                <!-- <div class="toolbar2"> -->
                <div class="row">
                    <label>选择分割方式:</label>
                    <el-select v-model="wndNum" @change="changeWndNum">
                        <el-option label="1×1" value="1" />
                        <el-option label="2×2" value="2" />
                        <el-option label="3×3" value="3" />
                        <el-option label="4×4" value="4" />
                    </el-select>
                </div>
                <div class="row">
                    <label>选择地点:</label>
                    <el-select v-model="selectedChannel" @change="onChannelChange">
                        <el-option v-for="c in channels" :key="c.id" :label="'点位' + c.id" :value="c.id" />
                    </el-select>
                </div>
                <!-- 选择码流类型 -->
                <div class="row">
                    <label>清晰度:</label>
                    <el-select v-model="streamType">
                        <el-option label="标清" value="1" />
                        <el-option label="高清" value="2" />
                    </el-select>
                </div>
                <div class="row">
                    <a class="download-btn" href="http://111.47.99.130:8081/shipin/HCWebSDKPlugin.exe" target="_blank">
                        下载视频插件
                    </a>
                </div>
                <!-- </div> -->
            </div>

            <!-- 视频插件容器 -->
            <div id="divPlugin" class="plugin"></div>
            <!-- 不显示的内容 -->
            <div style="display: none;height: 2px;">
                <!-- 选择地点 -->
                <div style="display: none;">
                    <label>选择地点:</label>
                    <el-select v-model="selectedChannel" @change="onChannelChange">
                        <el-option v-for="c in channels" :key="c.id" :label="c.name" :value="c.id" />
                    </el-select>
                </div>
                <fieldset class="operate">
                    <legend>操作信息</legend>
                    <div id="opinfo" class="opinfo">
                        <div v-for="info in opInfo" :key="info">{{ info }}</div>
                    </div>
                </fieldset>
                <fieldset class="callback">
                    <legend>事件回调信息</legend>
                    <div id="cbinfo" class="cbinfo">
                        <div v-for="info in cbInfo" :key="info">{{ info }}</div>
                    </div>
                </fieldset>
                <fieldset class="ipchannel">
                    <legend>数字通道</legend>
                    <table width="100%" cellpadding="0" cellspacing="0" border="0">
                        <tr>
                            <td>
                                <input type="button" class="btn" value="获取数字通道列表" @click="clickGetDigitalChannelInfo" />
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <div class="digitaltdiv">
                                    <table id="digitalchannellist" class="digitalchannellist" cellpadding="0"
                                        cellspacing="0" border="0">
                                        <tr v-for="channel in digitalChannels" :key="channel.id">
                                            <td>{{ channel.id }}</td>
                                            <td>{{ channel.ipAddress }}</td>
                                            <td>{{ channel.srcInputPort }}</td>
                                            <td>{{ channel.managePortNo }}</td>
                                            <td>{{ channel.online }}</td>
                                            <td>{{ channel.proxyProtocol }}</td>
                                        </tr>
                                    </table>
                                </div>
                            </td>
                        </tr>
                    </table>
                </fieldset>
            </div>
        </div>
    </div>
</template>

<script>
import { watch } from 'less';

export default {
    data() {
        return {
            loginIp: "192.168.32.124",
            port: "80",
            username: "admin",
            password: "Dse@147258",
            devicePort: "",
            rtspPort: "",
            wndNum: "1",
            selectedDevice: "",
            selectedChannel: "",
            devices: [],
            channels: [],
            digitalChannels: [],
            streamType: "2",
            opInfo: [],
            cbInfo: [],
            currentWindowIndex: 0, // 当前窗口索引
            currentChannelIndex: 0, // 当前通道索引
            isAutoLoading: false, // 是否正在自动加载
        };
    },
    methods: {
        showOPInfo(info, status, xmlDoc) {
            const timestamp = this.dateFormat(new Date(), "yyyy-MM-dd hh:mm:ss");
            let message = `${timestamp} ${info}`;
            if (status && status !== 200) {
                const statusString = xmlDoc ? xmlDoc.querySelector("statusString").textContent : "";
                const subStatusCode = xmlDoc ? xmlDoc.querySelector("subStatusCode").textContent : "";
                message += ` (${status}, ${subStatusCode})`;
            }
            this.opInfo.unshift(message);
        },
        showCBInfo(info) {
            const timestamp = this.dateFormat(new Date(), "yyyy-MM-dd hh:mm:ss");
            this.cbInfo.unshift(`${timestamp} ${info}`);
        },
        dateFormat(date, fmt) {
            const o = {
                "M+": date.getMonth() + 1,
                "d+": date.getDate(),
                "h+": date.getHours(),
                "m+": date.getMinutes(),
                "s+": date.getSeconds(),
                "q+": Math.floor((date.getMonth() + 3) / 3),
                "S": date.getMilliseconds(),
            };
            if (/(y+)/.test(fmt)) {
                fmt = fmt.replace(RegExp.$1, (date.getFullYear() + "").substr(4 - RegExp.$1.length));
            }
            for (const k in o) {
                if (new RegExp(`(${k})`).test(fmt)) {
                    fmt = fmt.replace(RegExp.$1, RegExp.$1.length === 1 ? o[k] : ("00" + o[k]).substr(("" + o[k]).length));
                }
            }
            return fmt;
        },
        clickLogout() {
            if (!this.selectedDevice) return;
            WebVideoCtrl.I_Logout(this.selectedDevice).then(() => {
                this.showOPInfo(`${this.selectedDevice} 退出成功！`);
                this.selectedDevice = "";
            }).catch(() => {
                this.showOPInfo(`${this.selectedDevice} 退出失败！`);
            });
        },
        clickGetDeviceInfo() {
            const szDeviceIdentify = this.selectedDevice;
            if (!szDeviceIdentify) return;

            WebVideoCtrl.I_GetDeviceInfo(szDeviceIdentify, {
                success: (xmlDoc) => {
                    // 将 xmlDoc 转换为可操作的 DOM 对象
                    let xmlDocParsed;
                    if (typeof xmlDoc === "string") {
                        xmlDocParsed = new DOMParser().parseFromString(xmlDoc, "application/xml");
                    } else {
                        xmlDocParsed = xmlDoc;
                    }

                    // 解析设备信息
                    const deviceInfo = [
                        `设备名称：${xmlDocParsed.querySelector("deviceName").textContent}`,
                        `设备ID：${xmlDocParsed.querySelector("deviceID").textContent}`,
                        `型号：${xmlDocParsed.querySelector("model").textContent}`,
                        `设备序列号：${xmlDocParsed.querySelector("serialNumber").textContent}`,
                        `MAC地址：${xmlDocParsed.querySelector("macAddress").textContent}`,
                        `主控版本：${xmlDocParsed.querySelector("firmwareVersion").textContent} ${xmlDocParsed.querySelector("firmwareReleasedDate").textContent}`,
                        `编码版本：${xmlDocParsed.querySelector("encoderVersion").textContent} ${xmlDocParsed.querySelector("encoderReleasedDate").textContent}`,
                    ];

                    this.showOPInfo(`${szDeviceIdentify} 获取设备信息成功！`);
                    alert(deviceInfo.join("\n"));
                },
                error: (oError) => {
                    this.showOPInfo(`${szDeviceIdentify} 获取设备信息失败！`, oError.errorCode, oError);
                }
            });
        },
        // 窗口分割
        changeWndNum() {
            const szDeviceIdentify = this.selectedDevice;
            if (!szDeviceIdentify) {
                this.showOPInfo("未选择设备！");
                return;
            }

            const wndType = this.wndNum;
            // 普通窗口数量
            const wndNum = parseInt(wndType, 10);
            WebVideoCtrl.I_ChangeWndNum(wndNum).then(() => {
                this.showOPInfo("窗口分割成功！");
            }).catch((oError) => {
                this.showOPInfo("窗口分割失败！", oError.errorCode, oError);
            });

        },
        onDeviceChange() {
            this.getChannelInfo();
            this.getDevicePort();
        },
        getChannelInfo() {
            const szDeviceIdentify = this.selectedDevice;
            if (!szDeviceIdentify) {
                this.showOPInfo("未选择设备！");
                return;
            }

            // 获取模拟通道信息
            WebVideoCtrl.I_GetAnalogChannelInfo(szDeviceIdentify, {
                success: (xmlDoc) => {
                    this.parseChannelInfo(xmlDoc, szDeviceIdentify);
                },
                error: (oError) => {
                    this.showOPInfo(`${szDeviceIdentify} 获取模拟通道信息失败！`, oError.errorCode, oError);
                }
            });

            // 获取数字通道信息
            WebVideoCtrl.I_GetDigitalChannelInfo(szDeviceIdentify, {
                success: (xmlDoc) => {
                    this.parseChannelInfo(xmlDoc, szDeviceIdentify);
                },
                error: (oError) => {
                    this.showOPInfo(`${szDeviceIdentify} 获取数字通道信息失败！`, oError.errorCode, oError);
                }
            });
        },
        // 解析通道信息
        parseChannelInfo(xmlDoc, szDeviceIdentify) {
            // 将 xmlDoc 转换为可操作的 DOM 对象
            let xmlDocParsed;
            if (typeof xmlDoc === "string") {
                xmlDocParsed = new DOMParser().parseFromString(xmlDoc, "application/xml");
            } else {
                xmlDocParsed = xmlDoc;
            }

            // 解析通道信息
            const channels = Array.from(xmlDocParsed.querySelectorAll("VideoInputChannel, InputProxyChannelStatus")).map((channel) => ({
                id: channel.querySelector("id").textContent,
                name: channel.querySelector("name").textContent || `Camera ${channel.querySelector("id").textContent}`
            }));

            this.channels = channels; // 更新通道列表
            if (channels.length > 0) {
                this.selectedChannel = channels[0].id; // 自动选择第一个通道
            }
            this.showOPInfo(`${szDeviceIdentify} 获取通道列表成功！`);
        },
        getDevicePort() {
            // 示例：获取设备端口逻辑
            this.devicePort = "8080";
        },
        onChannelChange() {
            // 先停止当前的预览
            this.clickStopRealPlay();
            // 延迟1秒后开始新的预览
            setTimeout(() => {
                this.clickStartRealPlay();
            }, 1000); // 延迟1000毫秒（1秒）
        },
        clickStopRealPlay() {
            if (!this.selectedDevice) return;
            WebVideoCtrl.I_Stop().then(() => {
                this.showOPInfo(`${this.selectedDevice} 停止预览成功！`);
            }).catch((oError) => {
                this.showOPInfo(`${this.selectedDevice} 停止预览失败！`, oError.errorCode, oError);
            });
        },
        clickGetDigitalChannelInfo() {
            if (!this.selectedDevice) return;
            WebVideoCtrl.I_GetDigitalChannelInfo(this.selectedDevice, {
                success: (xmlDoc) => {
                    this.digitalChannels = Array.from(xmlDoc.querySelectorAll("InputProxyChannelStatus")).map((channel) => ({
                        id: channel.querySelector("id").textContent,
                        ipAddress: channel.querySelector("ipAddress").textContent,
                        srcInputPort: channel.querySelector("srcInputPort").textContent,
                        managePortNo: channel.querySelector("managePortNo").textContent,
                        online: channel.querySelector("online").textContent === "true" ? "在线" : "离线",
                        proxyProtocol: channel.querySelector("proxyProtocol").textContent,
                    }));
                    this.showOPInfo(`${this.selectedDevice} 获取数字通道成功！`);
                },
                error: (oError) => {
                    this.showOPInfo(`${this.selectedDevice} 获取数字通道失败！`, oError.errorCode, oError);
                },
            });
        },
        destroyPlugin() {
            const confirmMessage = "是否确定要销毁插件？再次获取插件需要刷新页面。";
            const userConfirmed = confirm(confirmMessage);

            if (userConfirmed) {
                WebVideoCtrl.I_DestroyPlugin().then(() => {
                    // this.showOPInfo("销毁插件成功！");
                }).catch((oError) => {
                    this.showOPInfo("销毁插件成功，请刷新页面再次加载插件！", oError.errorCode, oError);
                });
            } else {
                this.showOPInfo("销毁插件操作已取消。");
            }
        },
        deepDestroyPlugin() {
            WebVideoCtrl.I_DestroyPlugin().then(() => {
                // this.showOPInfo("销毁插件成功！");
            }).catch((oError) => {
                this.showOPInfo("销毁插件成功，请刷新页面再次加载插件！", oError.errorCode, oError);
            });
        },
        InitPlugin() {
            console.log(222);
            WebVideoCtrl.I_InitPlugin({
                bWndFull: true,
                iWndowType: 3,
                cbSelWnd: (xmlDoc) => {
                    const wndIndex = parseInt(xmlDoc.querySelector("SelectWnd").textContent, 10);
                    this.showCBInfo(`当前选择的窗口编号：${wndIndex}`);
                },
                cbDoubleClickWnd: (iWndIndex, bFullScreen) => {
                    this.showCBInfo(`当前放大的窗口编号：${iWndIndex}`);
                },
                cbEvent: (iEventType, iParam1, iParam2) => {
                    if (iEventType === 2) {
                        this.showCBInfo(`窗口${iParam1}回放结束！`);
                    } else if (iEventType === -1) {
                        this.showCBInfo(`设备${iParam1}网络错误！`);
                    }
                },
                cbInitPluginComplete: () => {
                    WebVideoCtrl.I_InsertOBJECTPlugin("divPlugin").then(() => {
                        WebVideoCtrl.I_CheckPluginVersion().then((bFlag) => {
                            if (bFlag) {
                                alert("检测到新的插件版本，请升级！");
                            }
                        });
                    }).catch(() => {
                        alert("插件初始化失败，请检查！");
                    });
                },
            });
            console.log(333);
        },
        hidPlugin() {
            WebVideoCtrl.I_HidePlugin();
            this.showOPInfo("隐藏插件成功！");
        },
        showPlugin() {
            this.showOPInfo("展示插件成功！");
        },
        clickLogin() {
            const szIP = this.loginIp;
            const szPort = this.port;
            const szUsername = this.username;
            const szPassword = this.password;

            if (!szIP || !szPort) {
                this.showOPInfo("IP地址或端口号不能为空！");
                return;
            }
            const szDeviceIdentify = `${szIP}_${szPort}`;
            WebVideoCtrl.I_Login(szIP, 1, szPort, szUsername, szPassword, {
                timeout: 3000,
                success: (xmlDoc) => {
                    this.showOPInfo(`${szDeviceIdentify} 登录成功！`);
                    this.devices.push(szDeviceIdentify);
                    this.selectedDevice = szDeviceIdentify;

                    // 登录成功后获取通道列表
                    this.getChannelInfo();
                    this.getDevicePort(); // 如果需要获取设备端口，也可以在这里调用
                    // 自动获取通道信息
                    this.clickGetDigitalChannelInfo();

                    // // 更新下拉框的值为4成4
                    this.wndNum = "3";
                    // 延迟1秒后开始自动加载通道视频
                    setTimeout(() => {
                        this.startAutoLoad();
                    }, 1000); // 延迟1秒，确保窗口分割完成
                },
                error: (oError) => {
                    this.showOPInfo(`${szDeviceIdentify} 登录失败！`, oError.errorCode, oError);
                }
            });
        },
        startAutoLoad() {
            if (this.isAutoLoading) return; // 如果已经在自动加载，则不再执行
            console.log("我开始自动加载了")
            this.isAutoLoading = true; // 标记为正在自动加载
            this.currentWindowIndex = 0; // 重置窗口索引
            this.currentChannelIndex = 0; // 重置通道索引
            this.autoLoadNextChannel(); // 开始加载第一个通道
        },
        autoLoadNextChannel() {
            if (this.currentChannelIndex >= this.channels.length) {
                // 所有通道加载完毕
                this.isAutoLoading = false;
                this.showOPInfo("所有通道已加载完毕！");
                return;
            }

            const channel = this.channels[this.currentChannelIndex];
            const digitalChannel = this.digitalChannels.find((dc) => dc.id === channel.id);
            this.selectedChannel = channel.id; // 选择当前通道
            if (!digitalChannel || digitalChannel.online !== "在线") {
                // 如果通道不存在或设备离线，跳过当前通道
                this.showOPInfo(`通道 ${channel.id} 设备离线，跳过加载！`);
                this.loadNextChannel();
                return;
            }

            // 开始预览（自动加载到当前窗口）
            this.autoStartRealPlay();
        },
        clickStartRealPlay() {
            if (!this.selectedDevice || !this.selectedChannel) return;
            console.log(this.selectedChannel)
            // 开始预览（自动加载到当前窗口）
            WebVideoCtrl.I_StartRealPlay(this.selectedDevice, {
                iStreamType: parseInt(this.streamType, 10),
                iChannelID: parseInt(this.selectedChannel, 10),

                success: () => {
                    this.showOPInfo(`${this.selectedDevice} 通道 ${this.selectedChannel} 开始预览成功！`);
                },
                error: (oError) => {
                    this.showOPInfo(`${this.selectedDevice} 通道 ${this.selectedChannel} 开始预览失败！`, oError.errorCode, oError);
                },
            });
        },
        // 可以连续加载
        autoStartRealPlay() {
            if (!this.selectedDevice || !this.selectedChannel) return;

            const channel = this.channels[this.currentChannelIndex];
            const digitalChannel = this.digitalChannels.find((dc) => dc.id === channel.id);
            this.selectedChannel = channel.id; // 选择当前通道
            if (!digitalChannel || digitalChannel.online !== "在线") {
                // 如果通道不存在或设备离线，跳过当前通道
                this.showOPInfo(`通道 ${channel.id} 设备离线，跳过加载！`);
                this.loadNextChannel();
                return;
            }

            // 开始预览（自动加载到当前窗口）
            WebVideoCtrl.I_StartRealPlay(this.selectedDevice, {
                iStreamType: parseInt(this.streamType, 10),
                iChannelID: parseInt(this.selectedChannel, 10),
                iWndIndex: this.currentWindowIndex, // 指定加载到当前窗口
                success: () => {
                    this.showOPInfo(`${this.selectedDevice} 通道 ${this.selectedChannel} 开始预览成功！`);
                    this.loadNextChannel(); // 加载下一个通道
                },
                error: (oError) => {
                    this.showOPInfo(`${this.selectedDevice} 通道 ${this.selectedChannel} 开始预览失败！`, oError.errorCode, oError);
                    this.loadNextChannel(); // 加载下一个通道
                },
            });
        },
        loadNextChannel() {
            this.currentWindowIndex++; // 切换到下一个窗口
            this.currentChannelIndex++; // 切换到下一个通道

            if (this.currentChannelIndex < this.channels.length) {
                // 如果还有未加载的通道，继续加载
                const channel = this.channels[this.currentChannelIndex];
                this.selectedChannel = channel.id; // 选择当前通道
                this.autoStartRealPlay(); // 开始加载
            } else {
                // 所有通道加载完毕
                this.isAutoLoading = false;
                this.showOPInfo("所有通道已加载完毕！");
            }
        }
    },
    mounted() {
        this.InitPlugin();
        setTimeout(() => {
            this.clickLogin();
        }, 2000);
    },
    activated() {
        setTimeout(() => {
            this.InitPlugin();
        }, 2000);
        setTimeout(() => {
            this.clickLogin();
        }, 4000);
    },
    beforeDestroy() { // Vue 2 使用 beforeDestroy
        this.deepDestroyPlugin();
    },
};
</script>


<style scoped>
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.wrapper {
    display: flex;
    gap: 20px;
    padding: 12px 200px;
    /* height: 100vh; */
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    font-size: 14px;
    background: #f5f7fa;
}

.left {
    display: flex;
    flex-direction: column;
    width: 100%
}

.toolbar {
    display: flex;
    flex-direction: row;
    background: #E8F3FC;
    border: 1px solid #e4e7ed;
    border-radius: 20px;
    margin-bottom: 12px;
    gap: 12px;
    padding: 12px 21px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    height: 230px;
    justify-content: space-between;
    align-items: start;
}

/* .toolbar2 {
    display: flex;
    flex-direction: row;
    gap: 12px;
    padding: 12px 21px;
    background: #E8F3FC;
    height: 230px;
    justify-content: center;
    align-items: start;
} */

.row {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
}

label {
    font-weight: 600;
    color: #303133;
    font-size: 16px;
    line-height: 1;
}

/* select {
    padding: 4px 8px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    background: #fff;
    transition: border-color 0.2s;
    min-width: 200px;
    font-size: 18px;
}

select:hover {
    border-color: #c0c4cc;
}

select:focus {
    border-color: #409eff;
    outline: none;
} */

.plugin {
    background: #000;
    border-radius: 6px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
    min-height: 600px;
}

.right {
    width: 220px;
    display: flex;
    align-items: flex-start;
}

.download-btn {
    height: 40px;
    display: inline-block;
    padding: 8px 16px;
    background: #409eff;
    color: #fff;
    border-radius: 4px;
    text-decoration: none;
    font-size: 14px;
    transition: background-color 0.2s;
}

.download-btn:hover {
    background: #66b1ff;
}

/* @media (max-width: 900px) {
  .wrapper {
    flex-direction: column;
  }
  .right {
    width: 100%;
    justify-content: center;
  }
} */
</style>