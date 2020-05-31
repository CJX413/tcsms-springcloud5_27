import Vue from 'vue';
import Vuex from 'vuex';
import utils from '../utils'

Vue.use(Vuex);

const state = {
    userInfo: {
      username: '',
      name: '',
      workerId: '',
      phoneNumber: '',
      sex: '',
    },
    allOperationLog: [],
    allOperationLogDate: {
    },
    operationLog: {
      deviceModel: '',
      deviceId: '',
      operator: '',
      workerId: '',
      time: '',
      longitude: '',
      latitude: '',
      radius: '',
      angle: '',
      height: '',
      torque: '',
      weight: '',
      windVelocity: '',
      magnification: '',
    },
    operationLogDate: {
      deviceModel: '',
      deviceId: '',
      operator: '',
      workerId: '',
      time: '',
      longitude: 0,
      latitude: 0,
      radius: 0,
      angle: 0,
      height: 0,
      torque: 0,
      weight: 0,
      windVelocity: '',
      magnification: '',
    },
    monitorStatus: {
      switch: false,
      status: [
        // {
        //   name: '',
        //   living: false,
        //   running: false,
        // }
      ]
    },
    onlineLog: [],
    tableData: [],//警报表的数据源
    warningMessage: [],
    websock: null,
  }
;

//同步方法
const mutations = {
  CLEAR_OPERATION() {
    state.operationLog = {
      deviceModel: '',
      deviceId: '',
      operator: '',
      workerId: '',
      time: '',
      longitude: 0,
      latitude: 0,
      radius: 0,
      angle: 0,
      height: 0,
      torque: 0,
      weight: 0,
      windVelocity: 0,
      magnification: 0,
    };
  },
  CLEAR_OPERATIONDATE() {
    state.operationLogDate = {
      deviceModel: '',
      deviceId: '',
      operator: '',
      workerId: '',
      time: 0,
      longitude: 0,
      latitude: 0,
      radius: 0,
      angle: 0,
      height: 0,
      torque: 0,
      weight: 0,
      windVelocity: 0,
      magnification: 0,
    };
  }
  ,
  CONNET_WEBSOCKET() {
    if (state.websock === null) {
      const wsuri = 'ws://localhost:8080/webSocket/' + localStorage.getItem('username');
      state.websock = new WebSocket(wsuri);
      state.websock.onopen = onopen;
      state.websock.onmessage = onmessage;
      state.websock.onerror = onerror;
      state.websock.onclose = onclose;
    } else {
      if (state.websock.readyState === 3) {
        const wsuri = 'ws://localhost:8080/webSocket/' + localStorage.getItem('username');
        state.websock = new WebSocket(wsuri);
        state.websock.onopen = onopen();
        state.websock.onmessage = onmessage();
        state.websock.onerror = onerror();
        state.websock.onclose = onclose();
      }
    }
  }
  ,
  ADD_WARNING(items) {
    let username = localStorage.getItem('username');
    let value = localStorage.getItem('warning_' + username);
    if (value === null || value === '') {
      console.info('添加到localStorage');
      state.tableData.push(formatTableData(items, state.tableData.length));
      localStorage.setItem('warning_' + username, JSON.stringify(state.tableData));
    } else {
      console.info('添加到localStorage');
      let value = localStorage.getItem('warning_' + username);
      state.tableData = JSON.parse(value);
      state.tableData.push(formatTableData(items, state.tableData.length));
      localStorage.setItem('warning_' + username, JSON.stringify(state.tableData));
    }
  }
  ,
  CLEARN_WARNING() {
    let username = localStorage.getItem('username');
    localStorage.setItem('warning_' + username, '');
    state.tableData = [];
  }
};

//异步方法
const actions = {};

const getters = {
  getTableData() {
    if (state.tableData.length === 0) {
      let username = localStorage.getItem('username');
      let value = localStorage.getItem('warning_' + username)
      if (value === null || value === '') {
        return state.tableData;
      }
      state.tableData = JSON.parse(value);
      return state.tableData;
    }
    return state.tableData;
  },
};

function formatTableData(items, order) {
  return {
    order: order,
    code: items.code,
    message: items.message,
    time: items.time,
    read: false,
    data: items.data,
  };
}

function onopen() {
  //连接建立之后执行send方法发送数据
  console.log('websocket打开');
}

function onmessage(e) {
  console.log('收到信息--------------');
  let receiveData = JSON.parse(e.data);
  console.log(receiveData)
  if (receiveData.message === 'warning') {
    console.log(receiveData);
    let innerData = receiveData.data;
    state.warningMessage.push(innerData.message);
    utils.alertMessageBox(innerData.message);
    mutations.ADD_WARNING(innerData);
  }
  else if (receiveData.message === 'operationLog') {
    if (receiveData.data.error === undefined) {
      state.operationLog = receiveData.data;
    } else {
      utils.alertErrorMessage('发送该设备当前的运行日志失败！', receiveData.data.error);
    }
  }
  else if (receiveData.message === 'operationLogDate') {
    if (receiveData.data.error === undefined) {
      state.operationLogDate = receiveData.data;
    } else {
      console.log('发送该设备的历史运行日志失败！');
      utils.alertErrorMessage('发送该设备的历史运行日志失败！', receiveData.data.error);
    }
  }
  else if (receiveData.message === 'monitorStatus') {
    state.monitorStatus = receiveData.data;
  }
  else if (receiveData.message === 'allOperationLog') {
    if (receiveData.data.error === undefined) {
      state.allOperationLog = receiveData.data;
    } else {
      utils.alertErrorMessage('发送所有设备当前的运行日志失败！', receiveData.data.error);
    }
  }
  else if (receiveData.message === 'allOperationLogDate') {
    if (receiveData.data.error === undefined) {
      state.allOperationLogDate = receiveData.data;
    } else {
      utils.alertErrorMessage('发送所有设备当前的运行日志失败！', receiveData.data.error);
    }
  }
  else if (receiveData.message === 'onlineLog') {
    console.log(receiveData.data);
    utils.alertMessage(receiveData.data.message);
    state.onlineLog = receiveData.data.data;
  }
}

function onerror() {
  mutations.CONNET_WEBSOCKET();
}

function onclose(e) {
  mutations.CONNET_WEBSOCKET();
  alert("和服务器的连接关闭!");
  console.log('断开连接', e);
}

// 该部分为了获取websocket的相关方法。会发现此处跟mutations 里的写法是类似的，但是，想使用return，需要将相关数据写在getters里面。

const store = new Vuex.Store({
  state,
  mutations,
  actions,
  getters,
});
export default store;
