<template>
  <div>
    <el-row>
      <el-table
        :data="onlineLog"
        style="width: 100%">
        <el-table-column
          prop="deviceId"
          label="设备名"
          width="180">
        </el-table-column>
        <el-table-column
          prop="deviceModel"
          label="设备型号"
          width="180">
        </el-table-column>
        <el-table-column
          prop="startTime"
          label="上线时间">
        </el-table-column>
        <el-table-column
          prop="coordinate"
          :formatter="coordinateFormatter"
          label="经纬度">
        </el-table-column>
        <el-table-column
          prop="address"
          label="所在地点">
        </el-table-column>
      </el-table>
    </el-row>
    <el-row>
      <el-col :span="2">
        <el-date-picker
          :value-format="'yyyy-MM-dd'"
          v-model="date"
          size="mini"
          type="date"
          placeholder="选择日期">
        </el-date-picker>
      </el-col>
      <el-col :span="1" :offset="3">
        <el-button type="primary" size="mini" @click="sumbit">确定</el-button>
      </el-col>
    </el-row>
    <el-row>
      <v-charts :options="option" style="width: 800px;height: 400px"></v-charts>
    </el-row>
  </div>
</template>

<script>
  import VCharts from "vue-echarts";
  import "echarts/lib/chart/line"; // 线图
  import "echarts/lib/chart/bar"; // 柱图
  import 'echarts/lib/component/markLine'
  import 'echarts/lib/component/legend' // 图例
  import 'echarts/lib/component/tooltip' //提示框
  export default {
    name: "OnlineLog",
    components: {VCharts},
    data() {
      return {
        workingData: {},
        geocoder: null,
        date: '',
        option: {
          title: {
            textAlign: 'center',
            x: 'center',
            y: 'top',
            text: '设备上线日志分析表',
          },
          tooltip: {
            trigger: 'axis',
          },
          legend: {
            data: []
          },
          grid: {
            top: '10%',
            left: '10%',
            right: '10%',
            bottom: '10%',
            containLabel: true
          },
          toolbox: {
            feature: {
              saveAsImage: {}
            }
          },
          xAxis: {
            type: 'value',
            min: 0,
            axisLabel: {
              formatter: (value, index) => {
                let date = new Date(value) //时间戳为10位需*1000，时间戳为13位的话不需乘1000
                let Y = date.getFullYear() + '年'
                let M = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1) + '月'
                let D = date.getDate() + '日'
                let h = '\n' + date.getHours() + '点'
                let m = date.getMinutes() + '分'
                let s = date.getSeconds() + '秒'
                return Y + M + D + h + m + s
              },
            }
          },
          yAxis: {
            type: 'category',
          },
          series: [
            {
              name: '辅助',
              type: 'bar',
              stack: '总量',
              itemStyle: {
                barBorderColor: 'rgba(0,0,0,0)',
                color: 'rgba(0,0,0,0)'
              },
              emphasis: {
                itemStyle: {
                  barBorderColor: 'rgba(0,0,0,0)',
                  color: 'rgba(0,0,0,0)'
                }
              },
              data: []
            },
            {
              name: '时间',
              type: 'bar',
              stack: '总量',
              barWidth: 20,
              label: {},
              data: []
            }
          ],
        },
      };
    },
    mounted() {
      this.initPage();
    },
    computed: {
      onlineLog() {
        return this.$store.state.onlineLog;
      }
    },
    methods: {
      initPage() {
        this.geocoder = new BMap.Geocoder();
        this.axios.post('/onlineLog', {})
          .then((response) => {
            if (response.data.success === true) {
              let data = response.data.result;
              for (let i = 0; i < data.length; i++) {
                let result = this.getAddress(data[i].coordinate);
                result.then(res => {
                  data[i].address = res;
                })
              }
              this.$store.state.onlineLog = data;
            }
          });
        let date = this.$moment(new Date()).format('YYYY-MM-DD');
        this.date = date;
        this.axios.post('/onlineLogBar', {
          date: date,
        }).then((response) => {
          if (response.data.success === true) {
            let data = response.data.result;
            this.workingData = data;
            console.log(data);
            let that = this;
            this.option.series[1].label = {
              show: true,
              position: 'top',
              formatter: function (params) {
                console.log('++++++++++++++++++++++++++')
                console.log(params)
                return that.formatter(that.workingData.startTime[params.dataIndex])
                  + '--' + that.formatter(that.workingData.startTime[params.dataIndex] + that.workingData.endTime[params.dataIndex]);
              },
            };
            this.option.xAxis.min = data.startTime[0];
            this.option.series[0].data = data.startTime;
            this.option.series[1].data = data.endTime;
          } else {
            this.utils.alertErrorMessage('获取设备上线日志数据失败！', response.data.message);
          }
        });
      },
      coordinateFormatter(row, column) {
        return row.coordinate.lng + ',' + row.coordinate.lat;
      },
      async getAddress(coordinate) {
        let point = new BMap.Point(coordinate.lng, coordinate.lat);
        return await new Promise((resolve, reject) => {
          this.geocoder.getLocation(point, function (rs) {
            resolve(rs.address);
          });
        });
      },
      formatter(value) {
        let date = new Date(value) //时间戳为10位需*1000，时间戳为13位的话不需乘1000
        let h = date.getHours() + '点'
        let m = date.getMinutes() + '分'
        let s = date.getSeconds() + '秒'
        return h + m + s
      },
      sumbit() {
        if (this.date !== '') {
          this.axios.post('/onlineLogBar', {
            date: this.date,
          }).then((response) => {
            if (response.data.success === true) {
              let data = response.data.result;
              this.workingData = data;
              console.log(data);
              let that = this;
              this.option.series[1].label = {
                show: true,
                position: 'top',
                formatter: function (params) {
                  console.log('++++++++++++++++++++++++++')
                  console.log(params)
                  return that.formatter(that.workingData.startTime[params.dataIndex])
                    + '--' + that.formatter(that.workingData.startTime[params.dataIndex] + that.workingData.endTime[params.dataIndex]);
                },
              };
              this.option.xAxis.min = data.startTime[0];
              this.option.series[0].data = data.startTime;
              this.option.series[1].data = data.endTime;
            } else {
              this.utils.alertErrorMessage('获取设备上线日志数据失败！', response.data.message);
            }
          });
        } else {
          this.$message.error('日期不能为空！');
        }
      }
    }
  }
</script>

<style scoped>

</style>
