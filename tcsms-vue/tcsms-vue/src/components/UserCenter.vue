<template xmlns:el-col="http://www.w3.org/1999/html">
  <el-row :gutter="20">
    <el-col :span="10">
      <el-card class="box-card" style="text-align: left">
        <div slot="header" class="clearfix">
          <span>用户信息</span>
          <el-button style="float: right" type="primary" size="small" @click="update">
            修改
          </el-button>
          <el-row style="padding-top: 20px">
            <el-input
              :placeholder="userInfoForm.username"
              prefix-icon="el-icon-user-solid"
              :disabled="true">
            </el-input>
          </el-row>
          <el-row style="padding-top: 20px">
            <el-col :span="19">
              <el-input
                maxlength="20"
                prefix-icon="el-icon-phone"
                v-model="userInfoForm.phoneNumber">
              </el-input>
            </el-col>
            <el-col :span="2" :offset="1">
              <el-button type="primary" size="small" style="padding-left: 5px;padding-right: 5px" :loading="send"
                         @click="getVerificationCode()">{{sendButtonView}}
              </el-button>
            </el-col>
          </el-row>
          <el-row style="padding-top: 20px">
            <el-col :span="8">
              <el-input v-model="verificationCode"
                        prefix-icon="el-icon-chat-dot-round"
                        oninput="value=value.replace(/[^\d]/g,'')"
                        maxLength="6"
                        autocomplete="off"></el-input>
            </el-col>
            <el-col :span="2" :offset="1">
              <el-button type="primary" size="small" @click="updatePhone" style="padding-left: 5px;padding-right: 5px">
                修改手机号
              </el-button>
            </el-col>
          </el-row>
          <el-row style="padding-top: 20px">
            <el-input
              prefix-icon="el-icon-user"
              placeholder="请输入真实姓名！"
              v-model="userInfoForm.name"
              oninput="value=value.replace( /[^A-Za-z0-9\u4e00-\u9fa5\/?\\?\*?\s]/g, '')"
              clearable>
            </el-input>
          </el-row>
          <el-row style="padding-top: 20px">
            <el-input
              prefix-icon="el-icon-s-check"
              placeholder="请输入工人ID！"
              maxlength="20"
              oninput="value=value.replace(/[^\a-\z\A-\Z0-9]/g, '')"
              v-model="userInfoForm.workerId"
              clearable>
            </el-input>
          </el-row>
          <el-row style="padding-top: 20px">
            <el-switch
              style="display: block"
              v-model="userInfoForm.sex"
              active-color="#ff4949"
              inactive-color="#409EFF"
              active-text="女"
              inactive-text="男"
              active-value="女"
              inactive-value="男">
            </el-switch>
          </el-row>
        </div>
      </el-card>
    </el-col>
    <el-col :span="12" style="text-align: left">
      <el-row>
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>申请权限</span>
            <el-button type="primary" size="small" @click="applyRole" style="float: right">申请</el-button>
          </div>
          <el-select v-model="authority" placeholder="请选择">
            <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-card>
      </el-row>
    </el-col>
  </el-row>
</template>

<script>
  export default {
    name: "UserCenter",
    data() {
      return {
        count: 60,
        send: false,
        sendButtonView: '获取验证码',
        verificationCode: '',
        authority: '',
        options: [{
          value: 'MONITOR',
          label: 'MONITOR'
        },
        ],
        userInfoForm: {
          username: '',
          name: '',
          workerId: '',
          phoneNumber: '',
          sex: '',
        },
      };
    },
    computed: {
      userInfo() {
        return this.$store.state.userInfo;
      }
    },
    mounted() {
      this.initPage();
    },
    methods: {
      initPage() {
        this.userInfoForm = JSON.parse(JSON.stringify(this.$store.state.userInfo));
      },
      getVerificationCode() {
        setInterval(() => {
          if (this.count < 1) {
            this.send = false;
            this.sendButtonView = '获取验证码';
          } else {
            this.send = true;
            this.sendButtonView = this.count-- + 's后重发';
          }
        }, 1000);
        this.sendVerificationCode();
      },
      sendVerificationCode() {
        if (this.userInfoForm.phoneNumber === '') {
          this.$message.error('手机号不能为空');
        } else {
          console.log(this.userInfoForm.phoneNumber);
          this.axios.post('/auth/updatePhoneVerificationCode', {
            phone: this.userInfoForm.phoneNumber,
          })
            .then((response) => {
              if (response.data.success === true) {
                this.$message({
                  message: '发送短信验证码成功！',
                  type: 'success'
                });
              } else {
                this.utils.alertErrorMessage('发送短信验证码失败！', response.data.message);
              }
            });
        }
      },
      updatePhone() {
        if (this.userInfoForm.phoneNumber === '' || this.verificationCode === '') {
          this.$message.error('手机号和验证码不能为空');
        } else {
          this.axios.post('/updatePhone', {
            phone: this.userInfoForm.phoneNumber,
            verificationCode: this.verificationCode,
          }).then((response) => {
            if (response.data.success === true) {
              this.$message({
                message: '修改绑定的手机号成功！',
                type: 'success'
              });
            } else {
              this.utils.alertErrorMessage('修改绑定的手机号失败！', response.data.message);
            }
          });
        }
      },
      applyRole() {
        this.$confirm('在申请权限之前请确保您的身份信息真实！', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.axios.post('/applyRole', {role: this.authority})
            .then((response) => {
              if (response.data.success === true) {
                this.$message({
                  message: '成功发出权限申请！',
                  type: 'success'
                });
              } else {
                this.utils.alertErrorMessage('发出申请失败！', response.data.message);
              }
            });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消申请'
          });
        });
      },
      update() {
        this.axios.post('/updateUserInfo', this.userInfoForm)
          .then((response) => {
            if (response.data.success === true) {
              this.$store.state.userInfo = response.data.result;
              this.$message({
                message: '修改个人信息成功！',
                type: 'success'
              });
            } else {
              this.utils.alertErrorMessage('修改个人信息失败！', response.data.message);
            }
          });
      },

    }
  }
</script>

<style scoped>
  .text {
    font-size: 14px;
  }

  .item {
    margin-bottom: 18px;
  }

  .clearfix:before,
  .clearfix:after {
    display: table;
    content: "";
  }

  .clearfix:after {
    clear: both
  }

  .box-card {
    width: 480px;
  }
</style>
