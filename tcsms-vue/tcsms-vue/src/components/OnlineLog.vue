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
      <v-charts :options="option" style="width: 600px;height: 400px"></v-charts>
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
        geocoder: null,
        option: null,
      };
    },
    mounted() {
      this.initPage();
      this.geocoder = new BMap.Geocoder();
      let data = [{
        name: "联调",
        start: "2020-02-17",
        end: "2020-02-23"
      },
        {
          name: "测试",
          start: "2020-02-24",
          end: "2020-03-10"
        },
        {
          name: "灰度",
          start: "2020-02-29",
          end: "2020-03-11"
        },
        {
          name: "发布",
          start: "2020-03-10",
          end: "2020-03-12"
        }
      ];
      let result = [];
      for (let i = data.length - 1; i >= 0; i--) {
        result.push(data[i]);
      }
      data = result;
      this.option = {
        title: {
          text: '深圳月最低生活费组成（单位:元）',
          subtext: 'From ExcelHome',
          sublink: 'http://e.weibo.com/1341556070/AjQH99che'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {            // 坐标轴指示器，坐标轴触发有效
            type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
          },
          formatter: function (params) {
            var tar = params[1];
            return tar.name + '<br/>' + tar.seriesName + ' : ' + tar.value;
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          splitLine: {show: false},
          data: ['总费用', '房租', '水电费', '交通费', '伙食费', '日用品数']
        },
        yAxis: {
          type: 'value'
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
            data: [0, 1700, 1400, 1200, 300, 0]
          },
          {
            name: '生活费',
            type: 'bar',
            stack: '总量',
            label: {
              show: true,
              position: 'inside'
            },
            data: [2900, 1200, 300, 200, 900, 300]
          }
        ]
      };

    },
    computed: {
      onlineLog() {
        return this.$store.state.onlineLog;
      }
    },
    methods: {
      initPage() {
        this.axios.post('/runningLog', {})
          .then((response) => {
            console.log(response.data);
            if (response.data.success === true) {
              console.log(response.data.result);
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
      },
      coordinateFormatter(row, column) {
        return row.coordinate.lng + ',' + row.coordinate.lat;
      },
      async getAddress(coordinate) {
        //let geocoder = new BMap.Geocoder();
        let point = new BMap.Point(coordinate.lng, coordinate.lat);
        return await new Promise((resolve, reject) => {
          this.geocoder.getLocation(point, function (rs) {
            resolve(rs.address);
            console.log(rs.address)
          });
        });
      }
    }
  }
</script>

<style scoped>

</style>
