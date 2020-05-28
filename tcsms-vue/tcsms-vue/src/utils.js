import {Message, MessageBox} from 'element-ui';

function restoreIllegalChar(string) {
  return string.replace(/\/n/g, '<br/>').replace(/\/t/g, '&nbsp;&nbsp;').replace(/\/r/g, '</br>');
}

function alertErrorMessage(description, error) {
  Message({
    dangerouslyUseHTMLString: true,
    type: 'error',
    message: '<strong>' + description + '<br/>报错信息：'
      + restoreIllegalChar(error) + '</strong>'
  });
}

function alertMessage(message) {
  Message({
    message: message,
    type: 'success'
  });
}

function alertMessageBox(message) {
  console.log('alertMessageBox');
  MessageBox.alert(message, '新消息', {
    confirmButtonText: '确定',
  });

}

// 将四个全局公共方法，组合成一个对象，并暴露出去
export default {
  restoreIllegalChar,
  alertErrorMessage,
  alertMessage,
  alertMessageBox,
}
