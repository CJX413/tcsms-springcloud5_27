<template>
  <el-table
    :data="monitorStatus.status.filter(data => !search ||
     data.type.toLowerCase().includes(search.toLowerCase()) || data.deviceId.toLowerCase().includes(search.toLowerCase()))"
    style="width: 100%"
    height="550">
    <el-table-column
      :formatter="typeFormatter"
      prop="name"
      label="监控器类型">
    </el-table-column>
    <el-table-column
      :formatter="deviceFormatter"
      prop="name"
      label="监控设备">
    </el-table-column>
    <el-table-column
      prop="living"
      label="监控器是否存活">
      <template slot-scope="props">
        <el-switch
          v-model="props.row.living"
          disabled>
        </el-switch>
      </template>
    </el-table-column>
    <el-table-column
      prop="running"
      label="监控器是否正在运行">
      <template slot-scope="props">
        <el-switch
          v-model="props.row.running"
          disabled>
        </el-switch>
      </template>
    </el-table-column>
    <el-table-column
      align="left"
      width="210">
      <template slot="header" slot-scope="scope">
        <el-input
          v-model="search"
          size="mini"
          clearable
          placeholder="输入监控器类型、设备名搜索"/>
      </template>
    </el-table-column>
  </el-table>
</template>

<script>
  export default {
    name: "SystemStatus",
    data() {
      return {
        search: '',
      };
    },
    computed: {
      monitorStatus() {
        return this.$store.state.monitorStatus;
      },
    },
    mounted() {

    }
    ,
    methods: {
      typeFormatter(row, column) {
        return row.name.split('-')[0];
      },
      deviceFormatter(row, column) {
        let array = row.name.split('-');
        let device = '';
        for (let i = 1; i < array.length; i++) {
          device = device + array[i] + ' ';
        }
        return device;
      }
    }
  }
</script>

<style scoped>

</style>
