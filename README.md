# gmall
# 电商项目
一个基本的电商购物平台，核心是基于SpringBoot和Dubbo搭建的，主要包括的功能模块有:后台管理系统，前台用户登录系统，商品详情系统，购物车系统，订单系统，单点登录系统，支付系统。
## 实现的功能
|模块|功能|
|:---:|:---:|
|后台管理模块|商品信息添加，商品信息管理|
|用户登录系统|用户账号密码登录|
|商品详情系统|商品详情展示，商品属性切换|
|购物车系统|商品加入购物车服务|
|单点登录模块|用户在下订单时必须登录，拦截用户并跳转到登录页面|
|订单系统|用户进行下订单功能|
|支付模块|选择使用支付宝或微信支付方式|
## 技术架构

- SpringBoot
- MySQL
- Mybatis
- Dubbo:高性能的rpc框架
- Zookeeper:注册中心
- Redis:缓存数据库
- Nginx:反向代理
- FastDFS:图片存储
- ActiveMQ:消息队列
- JWT:token生成工具
## 系统架构
整个系统是面向SOA的
![架构图](http://cdn.qingtianblog.com/pic/2020/08/26/架构.png
)
