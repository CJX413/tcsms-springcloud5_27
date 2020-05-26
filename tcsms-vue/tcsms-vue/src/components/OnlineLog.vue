<template>
  <el-table
    :data="tableData"
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
</template>

<script>
  export default {
    name: "OnlineLog",
    data() {
      return {
        tableData: [],
        geocoder: null,
      };
    },
    mounted() {
      this.initPage();
      this.geocoder = new BMap.Geocoder();
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
              this.tableData = data;
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
