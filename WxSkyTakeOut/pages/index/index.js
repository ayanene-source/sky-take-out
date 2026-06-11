// index.js
Page({
  data:{
    msg:'wzf'
  },
  //获取微信用户头像和名称
  getUserInfo(){
    wx.getUserProfile({
      desc: 'desc',
      success:(res)=>{
        console.log(res.userInfo)
        this.setData({
          nickName:res.userInfo.nickName,
          url:res.userInfo.avatarUrl
        })
      }
    })
  },
  //登录获取
  Userlogin(){
   wx.login({
     success: (res) => {
       console.log(res.code)
     }
   })
  }
})
