<template>
  <div>
    <el-row style="padding-bottom: 5px">
      <el-col :span="2" style="text-align: right">中心点：</el-col>
      <el-col :span="3">
        <el-input v-model="building.point.lat + ',' + building.point.lng" style="width: 160px" size="mini"></el-input>
      </el-col>
      <el-col :span="2" style="text-align: right">高度：</el-col>
      <el-col :span="1">
        <el-input v-model="building.height" style="width: 70px" size="mini"></el-input>
      </el-col>
      <el-col :span="2" style="text-align: right">半径：</el-col>
      <el-col :span="1">
        <el-input v-model="building.length" style="width: 70px" size="mini"></el-input>
      </el-col>
      <el-col :span="2" style="text-align: right">建筑ID：</el-col>
      <el-col :span="1">
        <el-input v-model="building.buildingId" style="width: 70px" size="mini"></el-input>
      </el-col>
      <el-col :span="2">
        <el-button type="primary" size="mini" @click="addBuilding">添加</el-button>
      </el-col>
    </el-row>
    <el-row>
      <baidu-map :center="center" :zoom="zoom"
                 @ready="handler" :scroll-wheel-zoom="true"
                 style="height: 500px">
        <!--缩放-->
        <bm-navigation anchor="BMAP_ANCHOR_TOP_LEFT"></bm-navigation>
        <!--定位-->
        <bm-geolocation anchor="BMAP_ANCHOR_BOTTOM_RIGHT" :showAddressBar="true"
                        :autoLocation="true"></bm-geolocation>
        <!--点-->
        <bml-marker-clusterer :average-center="true">
          <bm-marker v-for="device of deviceList" :key="device.deviceId"
                     :position="{lng: device.longitude, lat: device.latitude}">
            <bm-label :content="device.deviceId" :labelStyle="{color: 'red', fontSize : '15px'}"
                      :offset="{width: -20, height: 20}"/>
            <bm-circle :center="{lng: device.longitude, lat: device.latitude}" :radius="device.bigLength"
                       stroke-color="blue" :stroke-opacity="0.5" :stroke-weight="2"
                       :editing="false">
            </bm-circle>
          </bm-marker>

          <bm-marker v-for="building of buildingList" :key="building.buildingId"
                     :position="building.point">
            <bm-label :content="building.buildingId" :labelStyle="{color: 'red', fontSize : '15px'}"
                      :offset="{width: -20, height: 20}"/>
            <bm-circle :center="building.point" :radius="building.length"
                       stroke-color="blue" :stroke-opacity="0.5" :stroke-weight="2"
                       :editing="false">
            </bm-circle>
          </bm-marker>

          <bm-marker :position="building.point">
            <bm-label :content="building.buildingId+'/'+building.height+'m'" :labelStyle="{color: 'red', fontSize : '15px'}"
                      :offset="{width: -20, height: 20}"/>
            <bm-circle :center="building.point" :radius="building.length"
                       stroke-color="blue" :stroke-opacity="0.5" :stroke-weight="2"
                       :editing="false">
            </bm-circle>
          </bm-marker>
        </bml-marker-clusterer>
      </baidu-map>
    </el-row>
    <el-row>
      <el-col>
        <el-table
          :data="buildingList.filter(data => !search || data.buildingId.toLowerCase().includes(search.toLowerCase()))"
          style="width: 100%"
          height="550">
          <el-table-column
            label="建筑ID"
            width="70"
            prop="buildingId">
          </el-table-column>
          <el-table-column
            label="中心点(纬度,经度)"
            :formatter="formatPoint"
            prop="point">
          </el-table-column>
          <el-table-column
            label="半径(m)"
            prop="length">
          </el-table-column>
          <el-table-column
            label="高度(m)"
            prop="height">
          </el-table-column>
          <el-table-column
            align="left">
            <template slot="header" slot-scope="scope">
              <el-input
                v-model="search"
                size="mini"
                clearable
                placeholder="输入建筑ID"/>
            </template>
            <template slot-scope="props">
              <el-button
                size="mini"
                type="danger"
                @click="handleDelete(props.$index,props.row)">删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  import BmlMarkerClusterer from "vue-baidu-map/components/extra/MarkerClusterer";

  export default {
    name: "BuildingManage",
    components: {
      BmlMarkerClusterer,
    },
    data() {
      return {
        center: {
          lat: '',
          lng: '',
        },
        search: '',
        focus: false,
        deviceList: [
          {
            deviceId: null,
            isRegistered: null,
            deviceModel: null,
            longitude: null,
            latitude: null,
            rlt: null,
            bigHeight: null,
            smallHeight: null,
            bigLength: null,
            smallLength: null
          }
        ],
        building: {
          buildingId: '',
          height: '',
          length: '',
          point: {
            lat: '',
            lng: '',
          },
        },
        buildingList: [],
        zoom: 18,
      };
    },
    mounted() {
      this.initPage();
    },
    methods: {
      handler({BMap, map}) {
        let that = this;
        this.map = map;
        map.addEventListener('click', function (e) {
          that.building.point.lat = e.point.lat;
          that.building.point.lng = e.point.lng;
        })
      },
      initPage() {
        this.axios.post('/registeredDeviceInfo', {})
          .then((response) => {
            if (response.data.success === true) {
              this.deviceList = response.data.result;
            }
          });
        this.axios.post('/center', {})
          .then((response) => {
            if (response.data.success === true) {
              this.center = response.data.result;
              console.log(this.center)
            }
          });
        this.axios.post('/allBuilding', {})
          .then((response) => {
            console.log(response.data);
            if (response.data.success === true) {
              this.buildingList = response.data.result;
              console.log(this.buildingList)
            }
          });
      },
      addBuilding() {
        console.log('点击了')
        if (this.building.point.lat === '' || this.building.point.lng === ''
          || this.building.length === '' || this.building.height === '' || this.building.buildingId === '') {
          this.$message.error('内容不能为空！');
        } else {
          this.axios.post('/addBuilding', {
            buildingId: this.building.buildingId,
            height: parseFloat(this.building.height).toFixed(2),
            length: parseFloat(this.building.length).toFixed(2),
            longitude: this.building.point.lng,
            latitude: this.building.point.lat,
          }).then((response) => {
            if (response.data.success === true) {
              this.$message({
                message: '添加障碍建筑成功！',
                type: 'success'
              });
              let copy = JSON.parse(JSON.stringify(this.building));
              this.buildingList.push(copy);
              this.building = {
                buildingId: '',
                height: '',
                length: '',
                point: {
                  lat: '',
                  lng: ''
                },
              };
            } else {
              this.utils.alertErrorMessage('添加障碍建筑失败！', response.data.message);
            }
          });
        }
      },
      formatPoint(row, column) {
        let point = row[column.property]
        return point.lat + ',' + point.lng;
      },
      handleDelete(index, row) {
        this.$confirm('此操作将永久删除该设备, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
          center: true
        }).then(() => {
          this.axios.post('/deleteBuilding', {
            'buildingId': row.buildingId
          })
            .then((response) => {
              if (response.data.success === true) {
                this.buildingList.splice(index, 1);
                this.$message({
                  type: 'success',
                  message: '删除成功!'
                });
                this.restartMonitorSystem();
              } else {
                this.utils.alertErrorMessage('删除失败！', response.data.message);
              }
            });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          });
        });
      },
      restartMonitorSystem() {
        if (this.$store.state.monitorStatus.switch === true) {
          this.$confirm('进行此操作需要重启监控系统, 是否继续?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '稍后',
            type: 'warning'
          }).then(() => {
            this.axios.post('/restartSecuritySystem', {})
              .then((response) => {
                if (response.data.success === true) {
                  this.$message({
                    message: '重启系统成功！',
                    type: 'success'
                  });
                } else {
                  this.utils.alertErrorMessage('重启失败！', response.data.message)
                }
              });
          }).catch(() => {
          });
        }
      },
    },
  }
</script>

<style scoped>

</style>
