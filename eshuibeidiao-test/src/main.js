import Vue from 'vue';
import App from './App';
import qs from 'qs';
import router from './router';
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import axios from 'axios'
import JsonExcel from 'vue-json-excel'
import ExportJsonExcel from "js-export-excel";
import echarts from "echarts"
import WebVideoCtrl from '../static/js/webVideoCtrl'
import $ from 'jquery';
// import VueAxios from 'vue-axios'

import './assets/font_8nkoq6t8imk/iconfont.css'
import './assets/font_lf6omvgdibc/iconfont.css'
//导入全局样式表
import './assets/css/global.css'



Vue.prototype.$echarts = echarts


//使用路由和UI
Vue.use(router);
Vue.use(ElementUI);
// Vue.use(VueAxios, axios);

Vue.component('downloadExcel', JsonExcel)
//配置请求的根路径
axios.defaults.baseURL = 'http://111.47.99.130:8081'
//配置请求头
axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded'
axios.interceptors.request.use(config => {
  config.headers.Authorization = window.sessionStorage.getItem('token')
  return config
})
Vue.prototype.$http = axios
Vue.prototype.$qs = qs

new Vue({
  el: '#app',
  router,
  render: h => h(App)
})

export const baseURL = 'http://111.47.99.130:8081'
