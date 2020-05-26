<template>
  <div>
    <el-row :gutter="5">
      <el-col :span="2"><span>center(中心)：</span></el-col>
      <el-col :span="2">
        <el-input v-model="center.lng" placeholder="lng" size="mini"></el-input>
      </el-col>
      <el-col :span="2">
        <el-input v-model="center.lat" placeholder="lat" size="mini"></el-input>
      </el-col>
      <el-col :span="1"><span>风速：</span></el-col>
      <el-col :span="1">
        <el-input v-model="windVelocity" placeholder="lng" size="mini"></el-input>
      </el-col>
      <el-col :span="4" :offset="8">
        <el-switch
          style="padding-top: 10px;padding-right: 40px"
          v-model="allSendSwitch"
          active-text="未连接也发送"
          inactive-text="未连接不发送"
          active-color="#13ce66"
          inactive-color="#ff4949">
        </el-switch>
      </el-col>
      <el-col :span="1">
        <el-button type="primary" size="small" @click="addDevice" plain>添加</el-button>
      </el-col>
      <el-col :span="1" style="padding-left: 10px">
        <el-switch
          active-icon-class="el-icon-switch-button"
          style="padding-top: 10px;padding-right: 40px"
          v-model="sendSwitch"
          active-color="#13ce66"
          inactive-color="#ff4949" @change="running">
        </el-switch>
      </el-col>
    </el-row>
    <el-divider></el-divider>
    <el-row :gutter="5">
      <el-col :span="2"><span>deviceId：</span></el-col>
      <el-col :span="2">
        <el-input v-model="device.deviceId" placeholder="" size="mini"></el-input>
      </el-col>
      <el-col :span="2"><span>deviceModel：</span></el-col>
      <el-col :span="2">
        <el-select
          size="mini"
          v-model="device.deviceModel"
          :multiple="false"
          filterable
          allow-create
          default-first-option
          placeholder="">
          <el-option
            v-for="item in deviceModelOption"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </el-col>
      <el-col :span="2"><span>坐标：</span></el-col>
      <el-col :span="2">
        <el-input v-model="device.point.lat + ',' + device.point.lng" placeholder="" size="mini"></el-input>
      </el-col>
      <el-col :span="2"><span>workerId：</span></el-col>
      <el-col :span="2">
        <el-select
          size="mini"
          v-model="device.workerId"
          :multiple="false"
          filterable
          allow-create
          default-first-option
          placeholder="">
          <el-option
            v-for="item in workerIdOption"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </el-col>
      <el-col :span="2"><span>operator：</span></el-col>
      <el-col :span="2">
        <el-input v-model="device.operator" placeholder="" size="mini" disabled></el-input>
      </el-col>
      <el-col :span="2"><span>magnification：</span></el-col>
      <el-col :span="2">
        <el-select
          size="mini"
          v-model="device.magnification"
          :multiple="false"
          filterable
          allow-create
          default-first-option
          placeholder="">
          <el-option
            v-for="item in magnificationOption"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </el-col>
    </el-row>
    <el-row :gutter="10">
      <el-col :span="2"><span>startRadius：</span></el-col>
      <el-col :span="2">
        <el-input v-model="device.startRadius" placeholder="" size="mini"></el-input>
      </el-col>
      <el-col :span="2"><span>startAngle：</span></el-col>
      <el-col :span="2">
        <el-input v-model="device.startAngle" placeholder="" size="mini"></el-input>
      </el-col>
      <el-col :span="2"><span>startHeight：</span></el-col>
      <el-col :span="2">
        <el-input v-model="device.startHeight" placeholder="" size="mini"></el-input>
      </el-col>
      <el-col :span="2"><span>radiusSpeed：</span></el-col>
      <el-col :span="2">
        <el-input v-model="device.radiusSpeed" placeholder="" size="mini"></el-input>
      </el-col>
      <el-col :span="2"><span>angleSpeed：</span></el-col>
      <el-col :span="2">
        <el-input v-model="device.angleSpeed" placeholder="" size="mini"></el-input>
      </el-col>
      <el-col :span="2"><span>heightSpeed：</span></el-col>
      <el-col :span="2">
        <el-input v-model="device.heightSpeed" placeholder="" size="mini"></el-input>
      </el-col>
    </el-row>
    <el-divider></el-divider>
    <el-row>
      <baidu-map class="map" :center="center" :zoom="zoom"
                 @ready="handler" :scroll-wheel-zoom="true"
                 style="height: 500px">
        <!--缩放-->
        <bm-navigation anchor="BMAP_ANCHOR_TOP_LEFT"></bm-navigation>
        <!--定位-->
        <bm-geolocation anchor="BMAP_ANCHOR_BOTTOM_RIGHT" :showAddressBar="true" :autoLocation="true"></bm-geolocation>
        <!--点-->
        <bml-marker-clusterer :average-center="true">
          <!--已经添加的设备的map-->
          <bm-marker v-for="device of deviceList" :key="device.deviceId"
                     :position="device.point">
            <bm-label :content="device.deviceId" :labelStyle="{color: 'red', fontSize : '15px'}"
                      :offset="{width: -20, height: 20}"/>
            <bm-circle :center="device.point" :radius="device.bigLength"
                       stroke-color="blue" :stroke-opacity="0.5" :stroke-weight="2"
                       :editing="false">
            </bm-circle>
            <bm-polyline :path="[device.point,device.path]" stroke-color="blue"
                         :stroke-opacity="0.5" :stroke-weight="2">
            </bm-polyline>
            <bm-marker :icon="pointImg" :position="device.radiusPoint"></bm-marker>
          </bm-marker>

          <!--添加设备的map-->
          <bm-marker :position="device.point">
            <bm-label :content="device.deviceId" :labelStyle="{color: 'red', fontSize : '15px'}"
                      :offset="{width: -20, height: 20}"/>
            <bm-circle :center="device.point" :radius="device.bigLength"
                       stroke-color="blue" :stroke-opacity="0.5" :stroke-weight="2"
                       :editing="false">
            </bm-circle>
          </bm-marker>
          <bm-polyline :path="[device.point,path]" stroke-color="blue"
                       :stroke-opacity="0.5" :stroke-weight="2">
            <bm-marker :icon="pointImg" :position="radiusPoint"></bm-marker>
          </bm-polyline>

          <!--建筑物的map-->
          <div v-for="building of buildingList">
            <bm-marker :position="building.pointOne">
              <bm-label :content="building.buildingId" :labelStyle="{color: 'red', fontSize : '15px'}"
                        :offset="{width: -20, height: 20}"/>
            </bm-marker>
            <bm-marker :position="building.pointTwo">
              <bm-label :content="building.buildingId" :labelStyle="{color: 'red', fontSize : '15px'}"
                        :offset="{width: -20, height: 20}"/>
            </bm-marker>
            <bm-marker :position="building.pointThree">
              <bm-label :content="building.buildingId" :labelStyle="{color: 'red', fontSize : '15px'}"
                        :offset="{width: -20, height: 20}"/>
            </bm-marker>
            <bm-marker :position="building.pointFour">
              <bm-label :content="building.buildingId" :labelStyle="{color: 'red', fontSize : '15px'}"
                        :offset="{width: -20, height: 20}"/>
            </bm-marker>
            <bm-polyline
              :path="[building.pointOne,building.pointTwo,building.pointThree,building.pointFour,building.pointOne]"
              stroke-color="blue"
              :stroke-opacity="0.5" :stroke-weight="2">
            </bm-polyline>
          </div>
        </bml-marker-clusterer>
      </baidu-map>
    </el-row>
    <el-table
      stripe
      :data="deviceList"
      style="width: 100%"
      height="550">
      <el-table-column
        label="deviceId"
        prop="deviceId">
      </el-table-column>
      <el-table-column
        label="deviceModel"
        prop="deviceModel">
      </el-table-column>
      <el-table-column
        label="workerId"
        prop="workerId">
      </el-table-column>
      <el-table-column
        label="operator"
        prop="operator">
      </el-table-column>
      <el-table-column
        label="radiusSpeed"
        prop="rlt">
        <template slot-scope="props">
          <el-input maxLength='7' size="mini" v-model="props.row.radiusSpeed"
                    placeholder="请输入内容" oninput="value=value.replace(/[^0-9.]/g,'')"></el-input>
        </template>
      </el-table-column>
      <el-table-column
        label="angleSpeed"
        prop="bigLength">
        <template slot-scope="props">
          <el-input maxLength='7' size="mini" v-model="props.row.angleSpeed"
                    placeholder="请输入内容" oninput="value=value.replace(/[^0-9.]/g,'')"></el-input>
        </template>
      </el-table-column>
      <el-table-column
        label="heightSpeed"
        prop="smallLength">
        <template slot-scope="props">
          <el-input maxLength='7' size="mini" v-model="props.row.heightSpeed"
                    placeholder="请输入内容" oninput="value=value.replace(/[^0-9.]/g,'')"></el-input>
        </template>
      </el-table-column>
      <el-table-column
        label="weight"
        prop="bigHeight">
        <template slot-scope="props">
          <el-input maxLength='7' size="mini" v-model="props.row.weight"
                    placeholder="请输入内容" oninput="value=value.replace(/[^0-9.]/g,'')"></el-input>
        </template>
      </el-table-column>
      <el-table-column
        label="magnification"
        prop="smallHeight">
        <template slot-scope="props">
          <el-select
            size="mini"
            v-model="props.row.magnification"
            :multiple="false"
            filterable
            allow-create
            default-first-option
            placeholder="">
            <el-option
              v-for="item in magnificationOption"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column
        label="connection"
        prop="connection">
        <template slot-scope="props">
          <el-switch
            v-model="props.row.connection"
            active-color="#13ce66"
            inactive-color="#ff4949"
            @change="connect($event,props.row)">
          </el-switch>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
  import DeviceCard from '../components/DeviceCard'
  import BmlMarkerClusterer from "vue-baidu-map/components/extra/MarkerClusterer";

  export default {
    name: "Emulator",
    components: {
      BmlMarkerClusterer,
      DeviceCard,
    },
    data() {
      return {
        allSendSwitch: false,
        windVelocity: 0,
        sendSwitch: false,
        interval: null,
        pointImg: {url: require('../../static/img/point.png'), size: {width: 12, height: 12}},
        center: {
          lng: 116.481231,
          lat: 39.920597,
        },
        buildingList: [],
        device: {
          deviceId: '',
          deviceModel: '',
          workerId: '',
          operator: '',
          magnification: '',
          startRadius: '',
          startAngle: '',
          startHeight: '',
          radiusSpeed: '',
          angleSpeed: '',
          heightSpeed: '',
          weight: 0,
          point: {
            lat: '',
            lng: '',
          },
          bigLength: 0,
        },
        deviceList: [],
        path: null,
        radiusPoint: null,
        workerIdOption: [{
          value: '1600300211',
          label: '1600300211'
        }, {
          value: '1600301332',
          label: '1600301332'
        }, {
          value: '1600300113',
          label: '1600300113'
        }],
        operatorOption: [{
          value: '陈嘉兴',
          label: '陈嘉兴'
        }, {
          value: '谢植赞',
          label: '谢植赞'
        }, {
          value: '陈涛',
          label: '陈涛'
        }],
        deviceModelOption: [
          {
            value: 'QTZ5010',
            label: 'QTZ5010/50m'
          }],
        magnificationOption: [
          {
            value: 2,
            label: 2
          },
          {
            value: 4,
            label: 4
          },
        ],
        zoom: 18,
      };
    },
    mounted() {   //初始化页面要在mounted方法中调用自己也得初始化方法
      this.initPage();
    },
    watch: {
      'device.deviceModel': {
        handler(newVal, oldVal) {
          switch (newVal) {
            case 'QTZ5010':
              this.device.bigLength = 50;
              break;
            default:
              break;
          }
        },
        deep: true,
        immediate: true
      },
      'device.workerId': {
        handler(newVal, oldVal) {
          switch (newVal) {
            case '1600300211':
              this.device.operator = '陈嘉兴';
              break;
            case '1600301332':
              this.device.operator = '谢植赞';
              break;
            case '1600300113':
              this.device.operator = '陈涛';
              break;
            default:
              break;
          }
        },
        deep: true,
        immediate: true
      },
      'device': {
        handler(newVal, oldVal) {
          let angle = parseFloat(newVal.startAngle).toFixed(1);
          let radius = parseFloat(newVal.startRadius).toFixed(1);
          this.path = this.pathLineCalLocation(angle, newVal.point.lng, newVal.point.lat, newVal.bigLength);
          this.radiusPoint = this.radiusPointCalLocation(angle, newVal.point.lng, newVal.point.lat, radius);
        },
        deep: true,
        immediate: true
      },
    },
    methods: {
      addDevice() {
        this.device.path = this.path;
        this.device.radiusPoint = this.radiusPoint;
        this.device.connection = false;
        this.device.startRadius = parseFloat(this.device.startRadius);
        this.device.startAngle = parseFloat(this.device.startAngle);
        this.device.startHeight = parseFloat(this.device.startHeight);

        this.device.radiusSpeed = parseFloat(this.device.radiusSpeed);
        this.device.angleSpeed = parseFloat(this.device.angleSpeed);
        this.device.heightSpeed = parseFloat(this.device.heightSpeed);

        this.device.magnification = parseInt(this.device.magnification);
        let copy = JSON.parse(JSON.stringify(this.device));
        console.log(copy);
        this.deviceList.push(copy);
        this.path = null;
        this.radiusPoint = null;
        this.device = {
          deviceId: '',
          deviceModel: '',
          workerId: '',
          operator: '',
          magnification: '',
          startRadius: '',
          startAngle: '',
          startHeight: '',
          radiusSpeed: '',
          angleSpeed: '',
          heightSpeed: '',
          weight: 0,
          point: {
            lat: '',
            lng: '',
          },
          bigLength: 0,
        };
      },
      handler({BMap, map}) {
        let that = this;
        map.addEventListener('click', function (e) {
          that.device.point = e.point;
        })
      },
      initPage() {
        this.axios.post('/allBuilding', {})
          .then((response) => {
            console.log(response.data);
            if (response.data.success === true) {
              this.buildingList = response.data.result;
            }
          });
      },
      pathLineCalLocation(angle, startLong, startLat, bigLength) {
        //将距离转换成经度的计算公式
        let L = bigLength / 6371e3;
        // 转换为radian，否则结果会不正确
        angle = this.toRadians(angle);
        startLong = this.toRadians(startLong);
        startLat = this.toRadians(startLat);
        let latLine = Math.asin(Math.sin(startLat) * Math.cos(L) + Math.cos(startLat) * Math.sin(L) * Math.cos(angle));
        let lonLine = startLong + Math.atan2(Math.sin(angle) * Math.sin(L) * Math.cos(startLat), Math.cos(L) - Math.sin(startLat) * Math.sin(latLine));
        // 转为正常的10进制经纬度
        lonLine = this.toDegrees(lonLine);
        latLine = this.toDegrees(latLine);

        return {
          lat: parseFloat(latLine.toFixed(6)),
          lng: parseFloat(lonLine.toFixed(6)),
        };
      },
      radiusPointCalLocation(angle, startLong, startLat, radius) {
        //将距离转换成经度的计算公式
        let P = radius / 6371e3;
        // 转换为radian，否则结果会不正确
        angle = this.toRadians(angle);
        startLong = this.toRadians(startLong);
        startLat = this.toRadians(startLat);


        let latPoint = Math.asin(Math.sin(startLat) * Math.cos(P) + Math.cos(startLat) * Math.sin(P) * Math.cos(angle));
        let lonPoint = startLong + Math.atan2(Math.sin(angle) * Math.sin(P) * Math.cos(startLat), Math.cos(P) - Math.sin(startLat) * Math.sin(latPoint));
        // 转为正常的10进制经纬度
        lonPoint = this.toDegrees(lonPoint);
        latPoint = this.toDegrees(latPoint);
        return {
          lng: parseFloat(lonPoint.toFixed(6)),
          lat: parseFloat(latPoint.toFixed(6)),
        };

      },
      toRadians(angdeg) {
        return angdeg / 180.0 * Math.PI;
      },
      toDegrees(angrad) {
        return angrad * 180.0 / Math.PI;
      },
      running($event) {
        if ($event) {
          this.connectAll();
          console.log('开始发送operationLog');
          this.interval = setInterval(this.sendOperationLog, 1000);
        } else {
          console.log('停止发送operationLog');
          this.disconnectAll();
          clearInterval(this.interval);
        }
      },
      connect($event, row) {
        if ($event) {
          console.log('连接')
          this.axios.post('http://localhost:8070/startRunning', {'deviceId': row.deviceId}, {
            headers: {
              'deviceId': row.deviceId,
            }
          })
            .then((response) => {
              if (response.success === true) {
                $event = true;
              } else {
                $event = false;
              }
            });
        } else {
          console.log('断开')
          this.axios.post('http://localhost:8070/stopRunning', {'deviceId': row.deviceId}, {
            headers: {
              'deviceId': row.deviceId,
            }
          })
            .then((response) => {
              if (response.success === true) {
                $event = false;
              } else {
                $event = true;
              }
            });
        }

      },
      connectAll() {
        console.log('连接全部');
        for (let i = 0; i < this.deviceList.length; i++) {
          this.axios.post('http://localhost:8070/startRunning', {deviceId: this.deviceList[i].deviceId}, {
            headers: {
              'deviceId': this.deviceList[i].deviceId,
            }
          })
            .then((response) => {
              if (response.data.success === true) {
                this.deviceList[i].connection = true;
              }
            });
        }
      },
      disconnectAll() {
        console.log('断开全部');
        for (let i = 0; i < this.deviceList.length; i++) {
          if (this.deviceList[i].connection) {
            this.axios.post('http://localhost:8070/stopRunning', {deviceId: this.deviceList[i].deviceId}, {
              headers: {
                'deviceId': this.deviceList[i].deviceId,
              }
            })
              .then((response) => {
                if (response.data.success === true) {
                  this.deviceList[i].connection = false;
                }
              });
          }
        }
      },
      sendOperationLog() {
        console.log('正在发送...')
        for (let i = 0; i < this.deviceList.length; i++) {
          if (this.deviceList[i].connection || this.allSendSwitch) {
            let operationLog = {
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
            };
            let radiusSpeed = this.deviceList[i].radiusSpeed === '' ? 0 : parseFloat(this.deviceList[i].radiusSpeed);
            let angleSpeed = this.deviceList[i].angleSpeed === '' ? 0 : parseFloat(this.deviceList[i].angleSpeed);
            let heightSpeed = this.deviceList[i].heightSpeed === '' ? 0 : parseFloat(this.deviceList[i].heightSpeed);
            let weight = this.deviceList[i].weight === '' ? 0 : parseFloat(this.deviceList[i].weight);
            let windVelocity = this.windVelocity === '' ? 0 : parseFloat(this.windVelocity);
            let time = this.$moment(new Date()).format('YYYY-MM-DD HH:mm:ss');
            this.deviceList[i].startRadius = Math.abs((this.deviceList[i].startRadius + radiusSpeed) % this.deviceList[i].bigLength);
            this.deviceList[i].startAngle = (this.deviceList[i].startAngle + angleSpeed) % 360;
            this.deviceList[i].startHeight = (this.deviceList[i].startHeight + heightSpeed) % 40;
            this.deviceList[i].path = this.pathLineCalLocation(this.deviceList[i].startAngle, this.deviceList[i].point.lng, this.deviceList[i].point.lat, this.deviceList[i].bigLength);
            this.deviceList[i].radiusPoint = this.radiusPointCalLocation(this.deviceList[i].startAngle, this.deviceList[i].point.lng, this.deviceList[i].point.lat, this.deviceList[i].startRadius);

            operationLog.deviceModel = this.deviceList[i].deviceModel;
            operationLog.deviceId = this.deviceList[i].deviceId;
            operationLog.operator = this.deviceList[i].operator;
            operationLog.workerId = this.deviceList[i].workerId;
            operationLog.time = time;
            operationLog.longitude = this.deviceList[i].point.lng;
            operationLog.latitude = this.deviceList[i].point.lat;
            operationLog.radius = this.deviceList[i].startRadius;
            operationLog.angle = this.deviceList[i].startAngle;
            operationLog.height = this.deviceList[i].startHeight;
            operationLog.torque = null;
            operationLog.weight = weight;
            operationLog.windVelocity = windVelocity;
            operationLog.magnification = this.deviceList[i].magnification;
            console.log(operationLog);
            this.axios.post('http://localhost:8070/operationLog', operationLog, {
              headers: {
                'deviceId': operationLog.deviceId,
              }
            }).then((response) => {
                console.log('发送成功')
              });
          }
        }
      },

    },

  }
</script>
<style scoped>

  .map {
    width: 100%;
    height: 400px;
  }
</style>
